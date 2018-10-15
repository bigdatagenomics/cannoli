/**
 * Licensed to Big Data Genomics (BDG) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The BDG licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bdgenomics.cannoli

import org.apache.hadoop.fs.{ FileSystem, Path }
import org.apache.spark.SparkContext
import org.bdgenomics.adam.rdd.ADAMContext._
import org.bdgenomics.adam.rdd.fragment.{ FragmentRDD, InterleavedFASTQInFormatter }
import org.bdgenomics.adam.rdd.read.{ AlignmentRecordRDD, AnySAMOutFormatter }
import org.bdgenomics.adam.sql.{ AlignmentRecord => AlignmentRecordProduct }
import org.bdgenomics.cannoli.builder.CommandBuilders
import org.bdgenomics.formats.avro.AlignmentRecord
import org.bdgenomics.utils.cli._
import org.bdgenomics.utils.misc.Logging
import org.kohsuke.args4j.{ Argument, Option => Args4jOption }
import scala.collection.JavaConversions._

/**
 * Bwa function arguments.
 */
class BwaArgs extends Args4jBase {
  @Argument(required = true, metaVar = "SAMPLE", usage = "Sample ID.", index = 2)
  var sample: String = null

  @Args4jOption(required = true, name = "-index", usage = "Path to the BWA index to be searched, e.g. <idxbase> in bwa [options]* <idxbase>.")
  var indexPath: String = null

  @Args4jOption(required = false, name = "-executable", usage = "Path to the BWA executable. Defaults to bwa.")
  var executable: String = "bwa"

  @Args4jOption(required = false, name = "-image", usage = "Container image to use. Defaults to quay.io/biocontainers/bwa:0.7.17--ha92aebf_3.")
  var image: String = "quay.io/biocontainers/bwa:0.7.17--ha92aebf_3"

  @Args4jOption(required = false, name = "-sudo", usage = "Run via sudo.")
  var sudo: Boolean = false

  @Args4jOption(required = false, name = "-add_files", usage = "If true, use the SparkFiles mechanism to distribute files to executors.")
  var addFiles: Boolean = false

  @Args4jOption(required = false, name = "-use_docker", usage = "If true, uses Docker to launch BWA.")
  var useDocker: Boolean = false

  @Args4jOption(required = false, name = "-use_singularity", usage = "If true, uses Singularity to launch BWA.")
  var useSingularity: Boolean = false
}

/**
 * Bwa wrapper as a function FragmentRDD &rarr; AlignmentRecordRDD,
 * for use in cannoli-shell or notebooks.
 *
 * @param args Bwa function arguments.
 * @param sc Spark context.
 */
class Bwa(
    val args: BwaArgs,
    sc: SparkContext) extends CannoliFn[FragmentRDD, AlignmentRecordRDD](sc) with Logging {

  override def apply(fragments: FragmentRDD): AlignmentRecordRDD = {
    val sample = args.sample

    def getIndexPaths(fastaPath: String): Seq[String] = {
      val requiredExtensions = Seq("",
        ".amb",
        ".ann",
        ".bwt",
        ".pac",
        ".sa")
      val optionalExtensions = Seq(".alt")

      // oddly, the hadoop fs apis don't seem to have a way to do this?
      def canonicalizePath(fs: FileSystem, path: Path): String = {
        val fsUri = fs.getUri()
        new Path(fsUri.getScheme, fsUri.getAuthority,
          Path.getPathWithoutSchemeAndAuthority(path).toString).toString
      }

      def optionalPath(ext: String): Option[String] = {
        val path = new Path(fastaPath + ext)
        val fs = path.getFileSystem(sc.hadoopConfiguration)
        if (fs.exists(path)) {
          Some(canonicalizePath(fs, path))
        } else {
          None
        }
      }

      val pathsWithScheme = requiredExtensions.map(ext => {
        optionalPath(ext).getOrElse({
          throw new IllegalStateException(
            "Required index file %s%s does not exist.".format(fastaPath, ext))
        })
      })

      val optionalPathsWithScheme = optionalExtensions.flatMap(optionalPath)

      pathsWithScheme ++ optionalPathsWithScheme
    }

    val builder = CommandBuilders.create(args.useDocker, args.useSingularity)
      .setExecutable(args.executable)
      .add("mem")
      .add("-t")
      .add("1")
      .add("-R")
      .add(s"@RG\\tID:${sample}\\tLB:${sample}\\tPL:ILLUMINA\\tPU:0\\tSM:${sample}")
      .add("-p")
      .add(if (args.addFiles) "$0" else args.indexPath)
      .add("-")

    if (args.addFiles) {
      getIndexPaths(args.indexPath).foreach(builder.addFile(_))
    }

    if (args.useDocker || args.useSingularity) {
      builder
        .setImage(args.image)
        .setSudo(args.sudo)
        .addMount(if (args.addFiles) "$root" else root(args.indexPath))
    }

    log.info("Piping {} to bwa with command: {} files: {}",
      fragments, builder.build(), builder.getFiles())

    implicit val tFormatter = InterleavedFASTQInFormatter
    implicit val uFormatter = new AnySAMOutFormatter

    fragments.pipe[AlignmentRecord, AlignmentRecordProduct, AlignmentRecordRDD, InterleavedFASTQInFormatter](
      cmd = builder.build(),
      files = builder.getFiles()
    )
  }
}
