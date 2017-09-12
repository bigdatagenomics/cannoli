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
import org.apache.spark.SparkContext
import org.bdgenomics.adam.rdd.ADAMContext._
import org.bdgenomics.adam.rdd.ADAMSaveAnyArgs
import org.bdgenomics.adam.rdd.variant.{
  VariantContextRDD,
  VCFInFormatter,
  VCFOutFormatter
}
import org.bdgenomics.utils.cli._
import org.bdgenomics.utils.misc.Logging
import org.kohsuke.args4j.{ Argument, Option => Args4jOption }

/**
 * SnpEff function arguments.
 */
class SnpEffFnArgs extends Args4jBase {
  @Args4jOption(required = false, name = "-database", usage = "SnpEff database name. Defaults to GRCh38.86.")
  var snpEffDatabase: String = "GRCh38.86"

  @Args4jOption(required = false, name = "-snpeff_path", usage = "Path to the SnpEff executable. Defaults to snpEff.")
  var snpEffPath: String = "snpEff"

  @Args4jOption(required = false, name = "-docker_image", usage = "Docker image to use. Defaults to quay.io/biocontainers/snpeff:4.3.1t--0.")
  var dockerImage: String = "quay.io/biocontainers/snpeff:4.3.1t--0"

  @Args4jOption(required = false, name = "-use_docker", usage = "If true, uses Docker to launch SnpEff. If false, uses the SnpEff executable path.")
  var useDocker: Boolean = false
}

/**
 * SnpEff wrapper as a function VariantContextRDD &rarr; VariantContextRDD,
 * for use in cannoli-shell or notebooks.
 *
 * @param args SnpEff function arguments.
 * @param files Files to make locally available to the commands being run.
 * @param environment A map containing environment variable/value pairs to set
 *   in the environment for the newly created process.
 * @param sc Spark context.
 */
class SnpEffFn(
    val args: SnpEffFnArgs,
    val files: Seq[String],
    val environment: Map[String, String],
    val sc: SparkContext) extends Function1[VariantContextRDD, VariantContextRDD] with Logging {

  /**
   * @param args SnpEff function arguments.
   * @param sc Spark context.
   */
  def this(args: SnpEffFnArgs, sc: SparkContext) = this(args, Seq.empty, Map.empty, sc)

  /**
   * @param args SnpEff function arguments.
   * @param files Files to make locally available to the commands being run.
   * @param sc Spark context.
   */
  def this(args: SnpEffFnArgs, files: Seq[String], sc: SparkContext) = this(args, files, Map.empty, sc)

  override def apply(variantContexts: VariantContextRDD): VariantContextRDD = {

    val snpEffCommand = if (args.useDocker) {
      Seq("docker",
        "run",
        "--rm",
        args.dockerImage,
        "snpEff",
        "-download",
        args.snpEffDatabase)
    } else {
      Seq(args.snpEffPath,
        "-download",
        args.snpEffDatabase)
    }

    log.info("Piping {} to snpEff with command: {} files: {} environment: {}",
      Array(variantContexts, snpEffCommand, files, environment))

    implicit val tFormatter = VCFInFormatter
    implicit val uFormatter = new VCFOutFormatter(sc.hadoopConfiguration)

    variantContexts.pipe(snpEffCommand, files, environment)
  }
}

object SnpEff extends BDGCommandCompanion {
  val commandName = "snpEff"
  val commandDescription = "ADAM Pipe API wrapper for SnpEff."

  def apply(cmdLine: Array[String]) = {
    new SnpEff(Args4j[SnpEffArgs](cmdLine))
  }
}

/**
 * SnpEff command line arguments.
 */
class SnpEffArgs extends SnpEffFnArgs with ADAMSaveAnyArgs with ParquetArgs {
  @Argument(required = true, metaVar = "INPUT", usage = "Location to pipe from, in VCF format.", index = 0)
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
 * SnpEff command line wrapper.
 */
class SnpEff(protected val args: SnpEffArgs) extends BDGSparkCommand[SnpEffArgs] with Logging {
  val companion = SnpEff
  val stringency: ValidationStringency = ValidationStringency.valueOf(args.stringency)

  def run(sc: SparkContext) {
    val variantContexts = sc.loadVcf(args.inputPath, stringency = stringency)
    val pipedVariantContexts = new SnpEffFn(args, sc).apply(variantContexts)
    pipedVariantContexts.saveAsVcf(args, stringency)
  }
}
