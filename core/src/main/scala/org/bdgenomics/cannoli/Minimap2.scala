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
 * Minimap2 function arguments.
 */
class Minimap2Args extends Args4jBase {
  @Args4jOption(required = false, name = "-executable", usage = "Path to the Minimap2 executable. Defaults to minimap2.")
  var executable: String = "minimap2"

  @Args4jOption(required = false, name = "-image", usage = "Container image to use. Defaults to quay.io/biocontainers/minimap2:2.14--ha92aebf_0.")
  var image: String = "quay.io/biocontainers/minimap2:2.14--ha92aebf_0"

  @Args4jOption(required = false, name = "-sudo", usage = "Run via sudo.")
  var sudo: Boolean = false

  @Args4jOption(required = false, name = "-add_files", usage = "If true, use the SparkFiles mechanism to distribute files to executors.")
  var addFiles: Boolean = false

  @Args4jOption(required = false, name = "-use_docker", usage = "If true, uses Docker to launch Minimap2.")
  var useDocker: Boolean = false

  @Args4jOption(required = false, name = "-use_singularity", usage = "If true, uses Singularity to launch Minimap2.")
  var useSingularity: Boolean = false

  @Args4jOption(required = true, name = "-index", usage = "Reference in FASTA format, may be gzipped, or minimizer index (.mmi).")
  var indexPath: String = null

  @Args4jOption(required = false, name = "-preset", usage = "Minimap2 preset (-x), one of { map-pb, map-ont, asm5, asm10, ava-pb, ava-ont, splice, sr }. Defaults to map-ont.")
  var preset: String = "map-ont"

  @Args4jOption(required = false, name = "-seed", usage = "Integer seed for randomizing equally best hits. Minimap2 hashes seed and read name when choosing between equally best hits. Defaults to 42.")
  var seed: Integer = 42
}

/**
 * Minimap2 wrapper as a function FragmentRDD &rarr; AlignmentRecordRDD,
 * for use in cannoli-shell or notebooks.
 *
 * @param args Minimap2 function arguments.
 * @param sc Spark context.
 */
class Minimap2(
    val args: Minimap2Args,
    sc: SparkContext) extends CannoliFn[FragmentRDD, AlignmentRecordRDD](sc) with Logging {

  override def apply(fragments: FragmentRDD): AlignmentRecordRDD = {

    val builder = CommandBuilders.create(args.useDocker, args.useSingularity)
      .setExecutable(args.executable)
      .add("-a")
      .add("-x")
      .add(args.preset)
      .add("--seed")
      .add(args.seed.toString)
      .add(if (args.addFiles) "$0" else absolute(args.indexPath))
      .add("-")

    if (args.addFiles) builder.addFile(args.indexPath)

    if (args.useDocker || args.useSingularity) {
      builder
        .setImage(args.image)
        .setSudo(args.sudo)
        .addMount(if (args.addFiles) "$root" else root(args.indexPath))
    }

    log.info("Piping {} to minimap2 with command: {} files: {}",
      fragments, builder.build(), builder.getFiles())

    implicit val tFormatter = InterleavedFASTQInFormatter
    implicit val uFormatter = new AnySAMOutFormatter

    fragments.pipe[AlignmentRecord, AlignmentRecordProduct, AlignmentRecordRDD, InterleavedFASTQInFormatter](
      cmd = builder.build(),
      files = builder.getFiles()
    )
  }
}
