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
import org.kohsuke.args4j.{ Option => Args4jOption }
import scala.collection.JavaConversions._

/**
 * Bwa mem function arguments.
 */
class BwaMemArgs extends ReadGroupArgs {
  @Args4jOption(required = true, name = "-index", usage = "Path to the BWA index to be searched, e.g. <idxbase> in bwa [options]* <idxbase>. Required.")
  var indexPath: String = null

  @Args4jOption(required = false, name = "-executable", usage = "Path to the BWA executable. Defaults to bwa.")
  var executable: String = "bwa"

  @Args4jOption(required = false, name = "-image", usage = "Container image to use. Defaults to quay.io/biocontainers/bwa:0.7.18--he4a0461_1.")
  var image: String = "quay.io/biocontainers/bwa:0.7.18--he4a0461_1"

  @Args4jOption(required = false, name = "-sudo", usage = "Run via sudo.")
  var sudo: Boolean = false

  @Args4jOption(required = false, name = "-add_files", usage = "If true, use the SparkFiles mechanism to distribute files to executors.")
  var addFiles: Boolean = false

  @Args4jOption(required = false, name = "-use_docker", usage = "If true, uses Docker to launch bwa mem.")
  var useDocker: Boolean = false

  @Args4jOption(required = false, name = "-use_singularity", usage = "If true, uses Singularity to launch bwa mem.")
  var useSingularity: Boolean = false
}

/**
 * Bwa mem wrapper as a function FragmentDataset &rarr; AlignmentDataset,
 * for use in cannoli-shell or notebooks.
 *
 * @param args Bwa mem function arguments.
 * @param sc Spark context.
 */
class BwaMem(
    val args: BwaMemArgs,
    sc: SparkContext) extends CannoliFn[FragmentDataset, AlignmentDataset](sc) {

  override def apply(fragments: FragmentDataset): AlignmentDataset = {

    val requiredExtensions = Seq("",
      ".amb",
      ".ann",
      ".bwt",
      ".pac",
      ".sa")
    val optionalExtensions = Seq(".alt")

    val readGroup = Option(args.readGroup).getOrElse(args.createReadGroup)

    val builder = CommandBuilders.create(args.useDocker, args.useSingularity)
      .setExecutable(args.executable)
      .add("mem")
      .add("-t")
      .add("1")
      .add("-R")
      .add(readGroup.toSAMReadGroupRecord().getSAMString().replace("\t", "\\t"))
      .add("-p")
      .add(if (args.addFiles) "$0" else args.indexPath)
      .add("-")

    if (args.addFiles) {
      indexPaths(args.indexPath, requiredExtensions, optionalExtensions).foreach(builder.addFile(_))
    }

    if (args.useDocker || args.useSingularity) {
      builder
        .setImage(args.image)
        .setSudo(args.sudo)
        .addMount(if (args.addFiles) "$root" else root(args.indexPath))
    }

    info("Piping %s to bwa mem with command: %s files: %s".format(
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
