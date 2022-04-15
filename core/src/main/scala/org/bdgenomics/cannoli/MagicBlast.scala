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
 * Magic-BLAST function arguments.
 */
class MagicBlastArgs extends Args4jBase {
  @Args4jOption(required = false, name = "-executable", usage = "Path to the Magic-BLAST executable. Defaults to magicblast.")
  var executable: String = "magicblast"

  @Args4jOption(required = false, name = "-image", usage = "Container image to use. Defaults to quay.io/biocontainers/magicblast:1.6.0--hf1761c0_1.")
  var image: String = "quay.io/biocontainers/magicblast:1.6.0--hf1761c0_1"

  @Args4jOption(required = false, name = "-sudo", usage = "Run via sudo.")
  var sudo: Boolean = false

  @Args4jOption(required = false, name = "-add_files", usage = "If true, use the SparkFiles mechanism to distribute files to executors.")
  var addFiles: Boolean = false

  @Args4jOption(required = false, name = "-use_docker", usage = "If true, uses Docker to launch Magic-BLAST.")
  var useDocker: Boolean = false

  @Args4jOption(required = false, name = "-use_singularity", usage = "If true, uses Singularity to launch Magic-BLAST.")
  var useSingularity: Boolean = false

  @Args4jOption(required = true, name = "-db", usage = "BLAST database name.")
  var db: String = null

  @Args4jOption(required = false, name = "-word_size", usage = "Word size for wordfinder algorithm (length of best perfect match). If specified, must be at least 12.")
  var wordSize: java.lang.Integer = null

  @Args4jOption(required = false, name = "-gapopen", usage = "Cost to open a gap.")
  var gapOpen: java.lang.Integer = null

  @Args4jOption(required = false, name = "-gapextend", usage = "Cost to extend a gap.")
  var gapExtend: java.lang.Integer = null

  @Args4jOption(required = false, name = "-penalty", usage = "Penalty for a nucleotide mismatch. If specified, must be less than or equal to zero.")
  var penalty: java.lang.Integer = null

  @Args4jOption(required = false, name = "-max_intron_length", usage = "Length of the largest intron allowed in a translated nucleotide sequence when linking multiple distinct alignments. Defaults to 500000.")
  var maxIntronLength: java.lang.Integer = null

  @Args4jOption(required = false, name = "-score", usage = "Cutoff score for accepting alignments. Can be expressed as a number or a function of read length: L,b,a for a * length + b. Defaults to 20.")
  var score: String = null

  @Args4jOption(required = false, name = "-max_edit_distance", usage = "Cutoff edit distance for accepting an alignment.")
  var maxEditDistance: java.lang.Integer = null

  @Args4jOption(required = false, name = "-perc_identity", usage = "Percent identity.")
  var percentIdentity: java.lang.Double = null

  @Args4jOption(required = false, name = "-transcriptome", usage = "If true, map reads to a transcriptome database.")
  var transcriptome: Boolean = false

  @Args4jOption(required = false, name = "-stringency", usage = "Stringency level for various checks; can be SILENT, LENIENT, or STRICT. Defaults to STRICT.")
  var stringency: String = "STRICT"
}

/**
 * Magic-BLAST wrapper as a function FragmentDataset &rarr; AlignmentDataset,
 * for use in cannoli-shell or notebooks.
 *
 * @param args Magic-BLAST function arguments.
 * @param sc Spark context.
 */
class MagicBlast(
    val args: MagicBlastArgs,
    sc: SparkContext) extends CannoliFn[FragmentDataset, AlignmentDataset](sc) {

  override def apply(fragments: FragmentDataset): AlignmentDataset = {

    // fail fast if db not found
    try {
      absolute(args.db)
    } catch {
      case e: FileNotFoundException => {
        error("BLAST database %s not found, touch an empty file at this path if it does not exist".format(args.db))
        throw e
      }
    }

    val builder = CommandBuilders.create(args.useDocker, args.useSingularity)
      .setExecutable(args.executable)
      .add("-db")
      .add(if (args.addFiles) "$0" else absolute(args.db))
      .add("-paired")
      .add("-infmt")
      .add("fastq")

    Option(args.wordSize).foreach(i => builder.add("-word_size").add(i.toString))
    Option(args.gapOpen).foreach(i => builder.add("-gapopen").add(i.toString))
    Option(args.gapExtend).foreach(i => builder.add("-gapextend").add(i.toString))
    Option(args.penalty).foreach(i => builder.add("-penalty").add(i.toString))
    Option(args.percentIdentity).foreach(d => builder.add("-perc_identity").add(d.toString))
    Option(args.maxIntronLength).foreach(i => builder.add("-max_intron_length").add(i.toString))
    Option(args.score).foreach(s => builder.add("-score").add(s))
    Option(args.maxEditDistance).foreach(i => builder.add("-max_edit_distance").add(i.toString))

    if (args.transcriptome) builder.add("-reftype").add("transcriptome")

    if (args.addFiles) {
      // add args.db for "$0"
      builder.addFile(args.db)
      // add BLAST database files via globbed db path
      builder.addFiles(files(args.db + "*"))
    }

    if (args.useDocker || args.useSingularity) {
      builder
        .setImage(args.image)
        .setSudo(args.sudo)
        .addMount(if (args.addFiles) "$root" else root(args.db))
    }

    info("Piping %s to magicblast with command: %s files: %s".format(
      fragments, builder.build(), builder.getFiles()))

    implicit val tFormatter = InterleavedFASTQInFormatter
    implicit val uFormatter = new AnySAMOutFormatter(ValidationStringency.valueOf(args.stringency))

    fragments.pipe[Alignment, AlignmentProduct, AlignmentDataset, InterleavedFASTQInFormatter](
      cmd = builder.build(),
      files = builder.getFiles()
    )
  }
}
