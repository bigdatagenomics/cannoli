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

import java.io.FileNotFoundException
import org.apache.spark.SparkContext
import org.bdgenomics.adam.ds.ADAMContext._
import org.bdgenomics.adam.ds.fragment.{ FragmentDataset, InterleavedFASTQInFormatter }
import org.bdgenomics.adam.ds.read.{ AlignmentDataset, AnySAMOutFormatter }
import org.bdgenomics.adam.sql.{ Alignment => AlignmentProduct }
import org.bdgenomics.cannoli.builder.CommandBuilders
import org.bdgenomics.formats.avro.Alignment
import org.bdgenomics.utils.cli._
import org.kohsuke.args4j.{ Option => Args4jOption }
import scala.collection.JavaConversions._

/**
 * Bowtie 2 function arguments.
 */
class Bowtie2Args extends Args4jBase {
  @Args4jOption(required = false, name = "-executable", usage = "Path to the Bowtie 2 executable. Defaults to bowtie2.")
  var executable: String = "bowtie2"

  @Args4jOption(required = false, name = "-image", usage = "Container image to use. Defaults to quay.io/biocontainers/bowtie2:2.5.4--h7071971_4.")
  var image: String = "quay.io/biocontainers/bowtie2:2.5.4--h7071971_4"

  @Args4jOption(required = false, name = "-sudo", usage = "Run via sudo.")
  var sudo: Boolean = false

  @Args4jOption(required = false, name = "-add_files", usage = "If true, use the SparkFiles mechanism to distribute files to executors.")
  var addFiles: Boolean = false

  @Args4jOption(required = false, name = "-use_docker", usage = "If true, uses Docker to launch Bowtie 2.")
  var useDocker: Boolean = false

  @Args4jOption(required = false, name = "-use_singularity", usage = "If true, uses Singularity to launch Bowtie 2.")
  var useSingularity: Boolean = false

  @Args4jOption(required = true, name = "-index", usage = "Basename of the index for the reference genome, e.g. <bt2-idx> in bowtie2 [options]* -x <bt2-idx>.")
  var indexPath: String = null

  @Args4jOption(required = false, name = "-bowtie2_args", usage = "Additional arguments for Bowtie 2, must be double-quoted, e.g. -bowtie2_args \"-N 1 --end-to-end\"")
  var bowtie2Args: String = null
}

/**
 * Bowtie 2 wrapper as a function FragmentDataset &rarr; AlignmentDataset,
 * for use in cannoli-shell or notebooks.
 *
 * @param args Bowtie 2 function arguments.
 * @param sc Spark context.
 */
class Bowtie2(
    val args: Bowtie2Args,
    sc: SparkContext) extends CannoliFn[FragmentDataset, AlignmentDataset](sc) {

  override def apply(fragments: FragmentDataset): AlignmentDataset = {

    // fail fast if index basename not found
    try {
      absolute(args.indexPath)
    } catch {
      case e: FileNotFoundException => {
        error("Basename of the index %s not found, touch an empty file at this path if it does not exist".format(args.indexPath))
        throw e
      }
    }

    val builder = CommandBuilders.create(args.useDocker, args.useSingularity)
      .setExecutable(args.executable)
      .add("-x")
      .add(if (args.addFiles) "$0" else absolute(args.indexPath))
      .add("--interleaved")
      .add("-")

    Option(args.bowtie2Args).foreach(builder.add(_))

    if (args.addFiles) {
      // add args.indexPath for "$0"
      builder.addFile(args.indexPath)
      // add bowtie2 indexes via globbed index path
      builder.addFiles(files(args.indexPath + "*.bt2"))
    }

    if (args.useDocker || args.useSingularity) {
      builder
        .setImage(args.image)
        .setSudo(args.sudo)
        .addMount(if (args.addFiles) "$root" else root(args.indexPath))
    }

    info("Piping %s to bowtie2 with command: %s files: %s".format(
      fragments, builder.build(), builder.getFiles()))

    implicit val tFormatter = InterleavedFASTQInFormatter
    implicit val uFormatter = new AnySAMOutFormatter

    fragments.pipe[Alignment, AlignmentProduct, AlignmentDataset, InterleavedFASTQInFormatter](
      cmd = builder.build(),
      files = builder.getFiles()
    )
  }
}
