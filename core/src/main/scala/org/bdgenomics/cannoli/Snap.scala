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
import htsjdk.samtools.ValidationStringency
import org.apache.spark.SparkContext
import org.bdgenomics.adam.models.ReadGroupDictionary
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
 * Scalable Nucleotide Alignment Program (SNAP) function arguments.
 */
class SnapArgs extends ReadGroupArgs {
  @Args4jOption(required = false, name = "-executable", usage = "Path to the SNAP executable. Defaults to snap-aligner.")
  var executable: String = "snap-aligner"

  @Args4jOption(required = false, name = "-image", usage = "Container image to use. Defaults to heuermh/snap-aligner-dev.")
  var image: String = "heuermh/snap-aligner-dev"

  @Args4jOption(required = false, name = "-sudo", usage = "Run via sudo.")
  var sudo: Boolean = false

  @Args4jOption(required = false, name = "-add_files", usage = "If true, use the SparkFiles mechanism to distribute files to executors.")
  var addFiles: Boolean = false

  @Args4jOption(required = false, name = "-use_docker", usage = "If true, uses Docker to launch SNAP.")
  var useDocker: Boolean = false

  @Args4jOption(required = false, name = "-use_singularity", usage = "If true, uses Singularity to launch SNAP.")
  var useSingularity: Boolean = false

  @Args4jOption(required = true, name = "-index", usage = "Index directory for the reference genome.")
  var indexPath: String = null

  // todo: readSpacing, maxSeeds, maxDist, maxHits
  @Args4jOption(required = false, name = "-snap_args", usage = "Additional arguments for SNAP, must be double-quoted, e.g. -snap_args \"-H 32000 -s 2000\"")
  var snapArgs: String = null

  @Args4jOption(required = false, name = "-stringency", usage = "Stringency level for various checks; can be SILENT, LENIENT, or STRICT. Defaults to STRICT.")
  var stringency: String = "STRICT"
}

/**
 * Scalable Nucleotide Alignment Program (SNAP) wrapper as a function FragmentDataset &rarr; AlignmentDataset,
 * for use in cannoli-shell or notebooks.
 *
 * @param args SNAP function arguments.
 * @param sc Spark context.
 */
class Snap(
    val args: SnapArgs,
    sc: SparkContext) extends CannoliFn[FragmentDataset, AlignmentDataset](sc) {

  override def apply(fragments: FragmentDataset): AlignmentDataset = {

    val readGroup = Option(args.readGroup).getOrElse(args.createReadGroup)

    val builder = CommandBuilders.create(args.useDocker, args.useSingularity)
      .setExecutable(args.executable)
      .add("paired")
      .add(if (args.addFiles) "$0" else absolute(args.indexPath))
      .add("-pairedInterleavedFastq")
      .add("-")
      .add("-o")
      .add("-sam")
      .add("-")
      .add("-=")
      .add("-R")
      .add(readGroup.toSAMReadGroupRecord().getSAMString().replace("\t", "\\t"))

    Option(args.snapArgs).foreach(builder.add(_))

    if (args.addFiles) {
      // add args.indexPath for "$0"
      builder.addFile(args.indexPath)
      // add SNAP indexes within directory
      builder.addFile(args.indexPath + "/Genome")
      builder.addFile(args.indexPath + "/GenomeIndex")
      builder.addFile(args.indexPath + "/GenomeIndexHash")
      builder.addFile(args.indexPath + "/OverflowTable")
    }

    if (args.useDocker || args.useSingularity) {
      builder
        .setImage(args.image)
        .setSudo(args.sudo)
        .addMount(if (args.addFiles) "$root" else root(args.indexPath))
    }

    info("Piping %s to SNAP with command: %s files: %s".format(
      fragments, builder.build(), builder.getFiles()))

    // SNAP requires /1, /2 suffixes in interleaved FASTQ input
    sc.hadoopConfiguration.setBoolean(FragmentDataset.WRITE_SUFFIXES, true)

    implicit val tFormatter = InterleavedFASTQInFormatter
    implicit val uFormatter = new AnySAMOutFormatter(ValidationStringency.valueOf(args.stringency))

    val alignments = fragments.pipe[Alignment, AlignmentProduct, AlignmentDataset, InterleavedFASTQInFormatter](
      cmd = builder.build(),
      files = builder.getFiles()
    )

    alignments.replaceReadGroups(ReadGroupDictionary(Seq(readGroup)))
  }
}
