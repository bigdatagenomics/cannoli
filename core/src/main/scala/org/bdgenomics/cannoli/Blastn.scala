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
import org.bdgenomics.adam.rdd.ADAMContext._
import org.bdgenomics.adam.rdd.read.{ AlignmentRecordDataset, AnySAMOutFormatter }
import org.bdgenomics.adam.rdd.sequence.{ FASTAInFormatter, SequenceDataset }
import org.bdgenomics.adam.sql.{ AlignmentRecord => AlignmentRecordProduct }
import org.bdgenomics.cannoli.builder.CommandBuilders
import org.bdgenomics.formats.avro.AlignmentRecord
import org.bdgenomics.utils.cli._
import org.kohsuke.args4j.{ Option => Args4jOption }
import scala.collection.JavaConversions._

/**
 * Blastn function arguments.
 */
class BlastnArgs extends Args4jBase {
  @Args4jOption(required = false, name = "-executable", usage = "Path to the blastn executable. Defaults to blastn.")
  var executable: String = "blastn"

  @Args4jOption(required = false, name = "-image", usage = "Container image to use. Defaults to quay.io/biocontainers/blast:2.9.0--pl526h979a64d_3.")
  var image: String = "quay.io/biocontainers/blast:2.9.0--pl526h979a64d_3"

  @Args4jOption(required = false, name = "-sudo", usage = "Run via sudo.")
  var sudo: Boolean = false

  @Args4jOption(required = false, name = "-add_files", usage = "If true, use the SparkFiles mechanism to distribute files to executors.")
  var addFiles: Boolean = false

  @Args4jOption(required = false, name = "-use_docker", usage = "If true, uses Docker to launch blastn.")
  var useDocker: Boolean = false

  @Args4jOption(required = false, name = "-use_singularity", usage = "If true, uses Singularity to launch blastn.")
  var useSingularity: Boolean = false

  @Args4jOption(required = true, name = "-db", usage = "BLAST database name.")
  var db: String = null

  @Args4jOption(required = false, name = "-evalue", usage = "Expectation value (E) threshold for saving hits.")
  var evalue: java.lang.Double = null

  @Args4jOption(required = false, name = "-word_size", usage = "Word size for wordfinder algorithm (length of best perfect match). If specified, must be at least 4.")
  var wordSize: java.lang.Integer = null

  @Args4jOption(required = false, name = "-gapopen", usage = "Cost to open a gap.")
  var gapOpen: java.lang.Integer = null

  @Args4jOption(required = false, name = "-gapextend", usage = "Cost to extend a gap.")
  var gapExtend: java.lang.Integer = null

  @Args4jOption(required = false, name = "-penalty", usage = "Penalty for a nucleotide mismatch. If specified, must be less than or equal to zero.")
  var penalty: java.lang.Integer = null

  @Args4jOption(required = false, name = "-reward", usage = "Reward for a nucleotide match. If specified, must be at least zero.")
  var reward: java.lang.Integer = null

  @Args4jOption(required = false, name = "-max_hsps", usage = "Maximum number of High-scoring Segment Pair (HSP)s per sequence.")
  var maxHsps: java.lang.Integer = null

  @Args4jOption(required = false, name = "-perc_identity", usage = "Percent identity.")
  var percentIdentity: java.lang.Double = null

  @Args4jOption(required = false, name = "-stringency", usage = "Stringency level for various checks; can be SILENT, LENIENT, or STRICT. Defaults to STRICT.")
  var stringency: String = "STRICT"
}

/**
 * Blastn wrapper as a function SequenceDataset &rarr; AlignmentRecordDataset,
 * for use in cannoli-shell or notebooks.
 *
 * @param args Blastn function arguments.
 * @param sc Spark context.
 */
class Blastn(
    val args: BlastnArgs,
    sc: SparkContext) extends CannoliFn[SequenceDataset, AlignmentRecordDataset](sc) {

  override def apply(sequences: SequenceDataset): AlignmentRecordDataset = {

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
      .add("-outfmt")
      .add("17 SR SQ")
      .add("-parse_deflines")

    Option(args.evalue).foreach(d => builder.add("-evalue").add(d.toString))
    Option(args.wordSize).foreach(i => builder.add("-word_size").add(i.toString))
    Option(args.gapOpen).foreach(i => builder.add("-gapopen").add(i.toString))
    Option(args.gapExtend).foreach(i => builder.add("-gapextend").add(i.toString))
    Option(args.penalty).foreach(i => builder.add("-penalty").add(i.toString))
    Option(args.reward).foreach(i => builder.add("-reward").add(i.toString))
    Option(args.maxHsps).foreach(i => builder.add("-max_hsps").add(i.toString))
    Option(args.percentIdentity).foreach(d => builder.add("-perc_identity").add(d.toString))

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

    info("Piping %s to blastn with command: %s files: %s".format(
      sequences, builder.build(), builder.getFiles()))

    implicit val tFormatter = FASTAInFormatter
    implicit val uFormatter = new AnySAMOutFormatter(ValidationStringency.valueOf(args.stringency))

    sequences.pipe[AlignmentRecord, AlignmentRecordProduct, AlignmentRecordDataset, FASTAInFormatter](
      cmd = builder.build(),
      files = builder.getFiles()
    )
  }
}
