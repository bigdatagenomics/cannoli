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
 * Vt function arguments.
 */
class VtFnArgs extends Args4jBase {
  @Args4jOption(required = false, name = "-vt_path", usage = "Path to the vt executable. Defaults to vt.")
  var vtPath: String = "vt"

  @Args4jOption(required = true, name = "-vt_reference", usage = "Reference sequence for analysis.")
  var referencePath: String = null

  @Args4jOption(required = false, name = "-docker_image", usage = "Docker image to use. Defaults to heuermh/vt.")
  var dockerImage: String = "heuermh/vt"

  @Args4jOption(required = false, name = "-use_docker", usage = "If true, uses Docker to launch vt. If false, uses the vt executable path.")
  var useDocker: Boolean = false
}

/**
 * Vt wrapper as a function VariantContextRDD &rarr; VariantContextRDD,
 * for use in cannoli-shell or notebooks.
 *
 * @param args Vt function arguments.
 * @param files Files to make locally available to the commands being run.
 * @param environment A map containing environment variable/value pairs to set
 *   in the environment for the newly created process.
 * @param sc Spark context.
 */
class VtFn(
    val args: VtFnArgs,
    val files: Seq[String],
    val environment: Map[String, String],
    val sc: SparkContext) extends Function1[VariantContextRDD, VariantContextRDD] with Logging {

  /**
   * @param args Vt function arguments.
   * @param sc Spark context.
   */
  def this(args: VtFnArgs, sc: SparkContext) = this(args, Seq.empty, Map.empty, sc)

  /**
   * @param args Vt function arguments.
   * @param files Files to make locally available to the commands being run.
   * @param sc Spark context.
   */
  def this(args: VtFnArgs, files: Seq[String], sc: SparkContext) = this(args, files, Map.empty, sc)

  override def apply(variantContexts: VariantContextRDD): VariantContextRDD = {

    val vtCommand = if (args.useDocker) {
      Seq("docker",
        "run",
        "--interactive",
        "--rm",
        args.dockerImage,
        "vt",
        "normalize",
        "-",
        "-r",
        args.referencePath)
    } else {
      Seq(args.vtPath,
        "normalize",
        "-",
        "-r",
        args.referencePath)
    }

    log.info("Piping {} to vt with command: {} files: {} environment: {}",
      Array(variantContexts, vtCommand, files, environment))

    implicit val tFormatter = VCFInFormatter
    implicit val uFormatter = new VCFOutFormatter(sc.hadoopConfiguration)

    variantContexts.pipe(vtCommand, files, environment)
  }
}

object Vt extends BDGCommandCompanion {
  val commandName = "vt"
  val commandDescription = "ADAM Pipe API wrapper for vt normalize."

  def apply(cmdLine: Array[String]) = {
    new Vt(Args4j[VtArgs](cmdLine))
  }
}

/**
 * Vt command line arguments.
 */
class VtArgs extends VtFnArgs with ADAMSaveAnyArgs with ParquetArgs {
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
 * Vt command line wrapper.
 */
class Vt(protected val args: VtArgs) extends BDGSparkCommand[VtArgs] with Logging {
  val companion = Vt
  val stringency: ValidationStringency = ValidationStringency.valueOf(args.stringency)

  def run(sc: SparkContext) {
    val variantContexts = sc.loadVcf(args.inputPath, stringency = stringency)
    val pipedVariantContexts = new VtFn(args, sc).apply(variantContexts)
    pipedVariantContexts.saveAsVcf(args, stringency)
  }
}
