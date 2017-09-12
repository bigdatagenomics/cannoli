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
import htsjdk.variant.vcf.VCFHeaderLine
import org.apache.spark.SparkContext
import org.apache.spark.util.CollectionAccumulator
import org.bdgenomics.adam.models.VariantContext
import org.bdgenomics.adam.rdd.ADAMContext._
import org.bdgenomics.adam.rdd.ADAMSaveAnyArgs
import org.bdgenomics.adam.rdd.read.{ AlignmentRecordRDD, BAMInFormatter }
import org.bdgenomics.adam.rdd.variant.{ VariantContextRDD, VCFOutFormatter }
import org.bdgenomics.utils.cli._
import org.bdgenomics.utils.misc.Logging
import org.kohsuke.args4j.{ Argument, Option => Args4jOption }
import scala.collection.JavaConversions._

/**
 * Freebayes function arguments.
 */
class FreebayesFnArgs extends Args4jBase {
  @Args4jOption(required = false, name = "-freebayes_path", usage = "Path to the Freebayes executable. Defaults to freebayes.")
  var freebayesPath: String = "freebayes"

  @Args4jOption(required = true, name = "-freebayes_reference", usage = "Reference sequence for analysis. An index file (.fai) will be created if none exists.")
  var referencePath: String = null

  @Args4jOption(required = false, name = "-docker_image", usage = "Docker image to use. Defaults to quay.io/biocontainers/freebayes:1.1.0.46--htslib1.6_2.")
  var dockerImage: String = "quay.io/biocontainers/freebayes:1.1.0.46--htslib1.6_2"

  @Args4jOption(required = false, name = "-use_docker", usage = "If true, uses Docker to launch Freebayes. If false, uses the Freebayes executable path.")
  var useDocker: Boolean = false
}

/**
 * Freebayes wrapper as a function AlignmentRecordRDD &rarr; VariantContextRDD,
 * for use in cannoli-shell or notebooks.
 *
 * @param args Freebayes function arguments.
 * @param files Files to make locally available to the commands being run.
 * @param environment A map containing environment variable/value pairs to set
 *   in the environment for the newly created process.
 * @param sc Spark context.
 */
class FreebayesFn(
    val args: FreebayesFnArgs,
    val files: Seq[String],
    val environment: Map[String, String],
    val sc: SparkContext) extends Function1[AlignmentRecordRDD, VariantContextRDD] with Logging {

  /**
   * @param args Freebayes function arguments.
   * @param sc Spark context.
   */
  def this(args: FreebayesFnArgs, sc: SparkContext) = this(args, Seq.empty, Map.empty, sc)

  /**
   * @param args Freebayes function arguments.
   * @param files Files to make locally available to the commands being run.
   * @param sc Spark context.
   */
  def this(args: FreebayesFnArgs, files: Seq[String], sc: SparkContext) = this(args, files, Map.empty, sc)

  override def apply(alignments: AlignmentRecordRDD): VariantContextRDD = {

    val freebayesCommand = if (args.useDocker) {
      Seq("docker",
        "run",
        "--rm",
        args.dockerImage,
        "freebayes",
        "--fasta-reference",
        args.referencePath,
        "--stdin")
    } else {
      Seq(args.freebayesPath,
        "--fasta-reference",
        args.referencePath,
        "--stdin")
    }

    log.info("Piping {} to freebayes with command: {} files: {} environment: {}",
      Array(alignments, freebayesCommand, files, environment))

    val accumulator: CollectionAccumulator[VCFHeaderLine] = sc.collectionAccumulator("headerLines")

    implicit val tFormatter = BAMInFormatter
    implicit val uFormatter = new VCFOutFormatter(sc.hadoopConfiguration, Some(accumulator))

    val variantContexts = alignments.pipe[VariantContext, VariantContextRDD, BAMInFormatter](freebayesCommand, files, environment)

    val headerLines = accumulator.value.distinct
    variantContexts.replaceHeaderLines(headerLines)
  }
}

object Freebayes extends BDGCommandCompanion {
  val commandName = "freebayes"
  val commandDescription = "ADAM Pipe API wrapper for Freebayes."

  def apply(cmdLine: Array[String]) = {
    new Freebayes(Args4j[FreebayesArgs](cmdLine))
  }
}

/**
 * Freebayes command line arguments.
 */
class FreebayesArgs extends FreebayesFnArgs with ADAMSaveAnyArgs with ParquetArgs {
  @Argument(required = true, metaVar = "INPUT", usage = "Location to pipe from.", index = 0)
  var inputPath: String = null

  @Argument(required = true, metaVar = "OUTPUT", usage = "Location to pipe to, in VCF format.", index = 1)
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
 * Freebayes command line wrapper.
 */
class Freebayes(protected val args: FreebayesArgs) extends BDGSparkCommand[FreebayesArgs] with Logging {
  val companion = Freebayes
  val stringency: ValidationStringency = ValidationStringency.valueOf(args.stringency)

  def run(sc: SparkContext) {
    val alignments = sc.loadAlignments(args.inputPath, stringency = stringency)
    val variantContexts = new FreebayesFn(args, sc).apply(alignments)
    variantContexts.saveAsVcf(args, stringency)
  }
}
