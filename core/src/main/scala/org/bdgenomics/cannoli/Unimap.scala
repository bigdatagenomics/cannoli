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
import org.bdgenomics.adam.ds.ADAMContext._
import org.bdgenomics.adam.ds.fragment.{ FragmentDataset, InterleavedFASTQInFormatter }
import org.bdgenomics.adam.ds.read.{ AlignmentDataset, AnySAMOutFormatter }
import org.bdgenomics.adam.models.ReadGroupDictionary
import org.bdgenomics.adam.sql.{ Alignment => AlignmentProduct }
import org.bdgenomics.cannoli.builder.CommandBuilders
import org.bdgenomics.formats.avro.Alignment
import org.bdgenomics.utils.cli._
import org.kohsuke.args4j.{ Option => Args4jOption }
import scala.collection.JavaConversions._

/**
 * Unimap function arguments.
 */
class UnimapArgs extends ReadGroupArgs {
  @Args4jOption(required = false, name = "-executable", usage = "Path to the Unimap executable. Defaults to unimap.")
  var executable: String = "unimap"

  @Args4jOption(required = false, name = "-image", usage = "Container image to use. Defaults to quay.io/biocontainers/unimap:0.1--he4a0461_4.")
  var image: String = "quay.io/biocontainers/unimap:0.1--he4a0461_4"

  @Args4jOption(required = false, name = "-sudo", usage = "Run via sudo.")
  var sudo: Boolean = false

  @Args4jOption(required = false, name = "-add_files", usage = "If true, use the SparkFiles mechanism to distribute files to executors.")
  var addFiles: Boolean = false

  @Args4jOption(required = false, name = "-use_docker", usage = "If true, uses Docker to launch Unimap.")
  var useDocker: Boolean = false

  @Args4jOption(required = false, name = "-use_singularity", usage = "If true, uses Singularity to launch Unimap.")
  var useSingularity: Boolean = false

  @Args4jOption(required = true, name = "-index", usage = "Reference in FASTA format, may be gzipped, or minimizer index (.mmi).")
  var indexPath: String = null

  @Args4jOption(required = false, name = "-preset", usage = "Unimap preset (-x), one of { ont, hifi, asm5, asm10, asm20, splice, splice:hq }. Defaults to asm5.")
  var preset: String = "asm5"

  @Args4jOption(required = false, name = "-seed", usage = "Integer seed for randomizing equally best hits. Unimap hashes seed and read name when choosing between equally best hits. Defaults to 42.")
  var seed: Integer = 42

  @Args4jOption(required = false, name = "-unimap_args", usage = "Additional arguments for Unimap, must be double-quoted, e.g. -unimap_args \"-N 42 --splice\"")
  var unimapArgs: String = null
}

/**
 * Unimap wrapper as a function FragmentDataset &rarr; AlignmentDataset,
 * for use in cannoli-shell or notebooks.
 *
 * @param args Unimap function arguments.
 * @param sc Spark context.
 */
class Unimap(
    val args: UnimapArgs,
    sc: SparkContext) extends CannoliFn[FragmentDataset, AlignmentDataset](sc) {

  override def apply(fragments: FragmentDataset): AlignmentDataset = {

    val readGroup = Option(args.readGroup).getOrElse(args.createReadGroup)

    val builder = CommandBuilders.create(args.useDocker, args.useSingularity)
      .setExecutable(args.executable)
      .add("-a")
      .add("-x")
      .add(args.preset)
      .add("--seed")
      .add(args.seed.toString)
      .add("-R")
      .add(readGroup.toSAMReadGroupRecord().getSAMString().replace("\t", "\\t"))

    Option(args.unimapArgs).foreach(builder.add(_))

    builder
      .add(if (args.addFiles) "$0" else absolute(args.indexPath))
      .add("-")

    if (args.addFiles) builder.addFile(args.indexPath)

    if (args.useDocker || args.useSingularity) {
      builder
        .setImage(args.image)
        .setSudo(args.sudo)
        .addMount(if (args.addFiles) "$root" else root(args.indexPath))
    }

    info("Piping %s to unimap with command: %s files: %s".format(
      fragments, builder.build(), builder.getFiles()))

    implicit val tFormatter = InterleavedFASTQInFormatter
    implicit val uFormatter = new AnySAMOutFormatter

    val alignments = fragments.pipe[Alignment, AlignmentProduct, AlignmentDataset, InterleavedFASTQInFormatter](
      cmd = builder.build(),
      files = builder.getFiles()
    )

    alignments.replaceReadGroups(ReadGroupDictionary(Seq(readGroup)))
  }
}
