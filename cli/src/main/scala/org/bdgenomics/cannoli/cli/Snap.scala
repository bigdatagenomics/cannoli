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
package org.bdgenomics.cannoli.cli

import htsjdk.samtools.ValidationStringency
import org.apache.hadoop.fs.{ FileSystem, Path, PathFilter }
import org.apache.spark.SparkContext
import org.bdgenomics.adam.rdd.ADAMContext._
import org.bdgenomics.adam.rdd.ADAMSaveAnyArgs
import org.bdgenomics.adam.rdd.fragment.{ FragmentRDD, InterleavedFASTQInFormatter }
import org.bdgenomics.adam.rdd.read.{ AlignmentRecordRDD, AnySAMOutFormatter }
import org.bdgenomics.adam.sql.{ AlignmentRecord => AlignmentRecordProduct }
import org.bdgenomics.cannoli.builder.CommandBuilders
import org.bdgenomics.cannoli.util.AppendSuffixes
import org.bdgenomics.formats.avro.AlignmentRecord
import org.bdgenomics.utils.cli._
import org.bdgenomics.utils.misc.Logging
import org.kohsuke.args4j.{ Argument, Option => Args4jOption }
import scala.collection.JavaConversions._

/**
 * Snap function arguments.
 */
class SnapFnArgs extends Args4jBase {
  @Args4jOption(required = false, name = "-executable", usage = "Path to the Snap executable. Defaults to snap-aligner.")
  var executable: String = "snap-aligner"

  @Args4jOption(required = false, name = "-image", usage = "Container image to use. Defaults to quay.io/ucsc_cgl/snap:1.0beta.18--07475052cd34f17055991cec339898ff0e8bd07d")
  var image: String = "quay.io/ucsc_cgl/snap:1.0beta.18--07475052cd34f17055991cec339898ff0e8bd07d"

  @Args4jOption(required = false, name = "-sudo", usage = "Run via sudo.")
  var sudo: Boolean = false

  @Args4jOption(required = false, name = "-add_files", usage = "If true, use the SparkFiles mechanism to distribute files to executors.")
  var addFiles: Boolean = false

  @Args4jOption(required = false, name = "-use_docker", usage = "If true, uses Docker to launch Snap.")
  var useDocker: Boolean = false

  @Args4jOption(required = false, name = "-use_singularity", usage = "If true, uses Singularity to launch Snap.")
  var useSingularity: Boolean = false

  @Args4jOption(required = true, name = "-index", usage = "Index directory?")
  var indexPath: String = null
}

/**
 * Snap wrapper as a function FragmentRDD &rarr; AlignmentRecordRDD,
 * for use in cannoli-shell or notebooks.
 *
 * @param args Snap function arguments.
 * @param sc Spark context.
 */
class SnapFn(
    val args: SnapFnArgs,
    sc: SparkContext) extends CannoliFn[FragmentRDD, AlignmentRecordRDD](sc) with Logging {

  override def apply(fragments: FragmentRDD): AlignmentRecordRDD = {

    val builder = CommandBuilders.create(args.useDocker, args.useSingularity)
      .setExecutable(args.executable)
      .add("paired")
      .add(if (args.addFiles) "$0" else absolute(args.indexPath))
      .add("-t")
      .add("1")
      .add("-pairedInterleavedFastq")
      .add("-")
      .add("-o")
      .add("-bam")
      .add("-")
      .add("-map")
      .add("-pre")

    if (args.addFiles) builder.addFile(args.indexPath)

    if (args.useDocker || args.useSingularity) {
      builder
        .setImage(args.image)
        .setSudo(args.sudo)
        .addMount(if (args.addFiles) "$root" else root(args.indexPath))
    }

    log.info("Piping {} to snap with command: {} files: {}",
      fragments, builder.build(), builder.getFiles())

    implicit val tFormatter = InterleavedFASTQInFormatter
    implicit val uFormatter = new AnySAMOutFormatter

    fragments.pipe[AlignmentRecord, AlignmentRecordProduct, AlignmentRecordRDD, InterleavedFASTQInFormatter](
      cmd = builder.build(),
      files = builder.getFiles()
    )
  }
}

object Snap extends BDGCommandCompanion {
  val commandName = "snap"
  val commandDescription = "ADAM Pipe API wrapper for Snap."

  def apply(cmdLine: Array[String]) = {
    new Snap(Args4j[SnapArgs](cmdLine))
  }
}

/**
 * Snap command line arguments.
 */
class SnapArgs extends SnapFnArgs with ADAMSaveAnyArgs with ParquetArgs {
  @Argument(required = true, metaVar = "INPUT", usage = "Location to pipe fragments from (e.g. interleaved FASTQ format, .ifq). If extension is not detected, Parquet is assumed.", index = 0)
  var inputPath: String = null

  @Argument(required = true, metaVar = "OUTPUT", usage = "Location to pipe alignments to (e.g. .bam, .cram, .sam). If extension is not detected, Parquet is assumed.", index = 1)
  var outputPath: String = null

  @Args4jOption(required = false, name = "-single", usage = "Saves OUTPUT as single file.")
  var asSingleFile: Boolean = false

  @Args4jOption(required = false, name = "-defer_merging", usage = "Defers merging single file output.")
  var deferMerging: Boolean = false

  @Args4jOption(required = false, name = "-disable_fast_concat", usage = "Disables the parallel file concatenation engine.")
  var disableFastConcat: Boolean = false

  @Args4jOption(required = false, name = "-stringency", usage = "Stringency level for various checks; can be SILENT, LENIENT, or STRICT. Defaults to STRICT.")
  var stringency: String = "STRICT"

  // must be defined due to ADAMSaveAnyArgs, but unused here
  var sortFastqOutput: Boolean = false
}

/**
 * Snap command line wrapper.
 */
class Snap(protected val args: SnapArgs) extends BDGSparkCommand[SnapArgs] with Logging {
  val companion = Snap
  val stringency: ValidationStringency = ValidationStringency.valueOf(args.stringency)

  def run(sc: SparkContext) {
    val fragments = sc.loadFragments(args.inputPath, stringency = stringency)
      .transform(AppendSuffixes(_))

    val alignments = new SnapFn(args, sc).apply(fragments)
    alignments.save(args)
  }
}
