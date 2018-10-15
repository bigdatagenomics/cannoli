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

import org.apache.spark.SparkContext
import org.bdgenomics.adam.rdd.ADAMContext._
import org.bdgenomics.adam.rdd.fragment.{ FragmentRDD, InterleavedFASTQInFormatter }
import org.bdgenomics.adam.rdd.read.{ AlignmentRecordRDD, AnySAMOutFormatter }
import org.bdgenomics.adam.sql.{ AlignmentRecord => AlignmentRecordProduct }
import org.bdgenomics.cannoli.builder.CommandBuilders
import org.bdgenomics.formats.avro.AlignmentRecord
import org.bdgenomics.utils.cli._
import org.bdgenomics.utils.misc.Logging
import org.kohsuke.args4j.{ Option => Args4jOption }
import scala.collection.JavaConversions._

/**
 * Bowtie function arguments.
 */
class BowtieArgs extends Args4jBase {
  @Args4jOption(required = false, name = "-executable", usage = "Path to the Bowtie executable. Defaults to bowtie.")
  var executable: String = "bowtie"

  @Args4jOption(required = false, name = "-image", usage = "Container image to use. Defaults to quay.io/biocontainers/bowtie:1.2.2--py27h2d50403_1.")
  var image: String = "quay.io/biocontainers/bowtie:1.2.2--py27h2d50403_1"

  @Args4jOption(required = false, name = "-sudo", usage = "Run via sudo.")
  var sudo: Boolean = false

  @Args4jOption(required = false, name = "-add_files", usage = "If true, use the SparkFiles mechanism to distribute files to executors.")
  var addFiles: Boolean = false

  @Args4jOption(required = false, name = "-use_docker", usage = "If true, uses Docker to launch Bowtie.")
  var useDocker: Boolean = false

  @Args4jOption(required = false, name = "-use_singularity", usage = "If true, uses Singularity to launch Bowtie.")
  var useSingularity: Boolean = false

  @Args4jOption(required = true, name = "-index", usage = "Basename of the bowtie index to be searched, e.g. <ebwt> in bowtie [options]* <ebwt>.")
  var indexPath: String = null
}

/**
 * Bowtie wrapper as a function FragmentRDD &rarr; AlignmentRecordRDD,
 * for use in cannoli-shell or notebooks.
 *
 * @param args Bowtie function arguments.
 * @param sc Spark context.
 */
class Bowtie(
    val args: BowtieArgs,
    sc: SparkContext) extends CannoliFn[FragmentRDD, AlignmentRecordRDD](sc) with Logging {

  override def apply(fragments: FragmentRDD): AlignmentRecordRDD = {

    val builder = CommandBuilders.create(args.useDocker, args.useSingularity)
      .setExecutable(args.executable)
      .add("-S")
      .add(if (args.addFiles) "$0" else absolute(args.indexPath))
      .add("--interleaved")
      .add("-")

    if (args.addFiles) {
      // add args.indexPath for "$0"
      builder.addFile(args.indexPath)
      // add bowtie indexes via globbed index path
      builder.addFiles(files(args.indexPath + "*.ebwt"))
    }

    if (args.useDocker || args.useSingularity) {
      builder
        .setImage(args.image)
        .setSudo(args.sudo)
        .addMount(if (args.addFiles) "$root" else root(args.indexPath))
    }

    log.info("Piping {} to bowtie with command: {} files: {}",
      fragments, builder.build(), builder.getFiles())

    implicit val tFormatter = InterleavedFASTQInFormatter
    implicit val uFormatter = new AnySAMOutFormatter

    fragments.pipe[AlignmentRecord, AlignmentRecordProduct, AlignmentRecordRDD, InterleavedFASTQInFormatter](
      cmd = builder.build(),
      files = builder.getFiles()
    )
  }
}
