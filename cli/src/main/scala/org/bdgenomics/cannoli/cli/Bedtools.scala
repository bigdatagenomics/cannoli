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

import org.apache.spark.SparkContext
import org.bdgenomics.adam.rdd.ADAMContext._
import org.bdgenomics.adam.rdd.ADAMSaveAnyArgs
import org.bdgenomics.adam.rdd.feature.{
  FeatureRDD,
  BEDInFormatter,
  BEDOutFormatter
}
import org.bdgenomics.utils.cli._
import org.bdgenomics.utils.misc.Logging
import org.kohsuke.args4j.{ Argument, Option => Args4jOption }

/**
 * Bedtools function arguments.
 */
class BedtoolsFnArgs extends Args4jBase {
  @Args4jOption(required = false, name = "-a", usage = "Bedtools intersect -a option. One of {-a,-b} should be left unspecified to accept piped input.")
  var a: String = null

  @Args4jOption(required = false, name = "-b", usage = "Bedtools intersect -b option. One of {-a,-b} should be left unspecified to accept piped input.")
  var b: String = null

  @Args4jOption(required = false, name = "-sorted", usage = "Bedtools intersect -sorted option. Inputs must be sorted by chromosome and then by start position.")
  var sorted: Boolean = false

  @Args4jOption(required = false, name = "-bedtools_path", usage = "Path to the Bedtools executable. Defaults to bedtools.")
  var bedtoolsPath: String = "bedtools"

  @Args4jOption(required = false, name = "-docker_image", usage = "Docker image to use. Defaults to quay.io/biocontainers/bedtools:2.27.1--0.")
  var dockerImage: String = "quay.io/biocontainers/bedtools:2.27.1--0"

  @Args4jOption(required = false, name = "-use_docker", usage = "If true, uses Docker to launch Bedtools. If false, uses the Bedtools executable path.")
  var useDocker: Boolean = false
}

/**
 * Bedtools wrapper as a function FeatureRDD &rarr; FeatureRDD,
 * for use in cannoli-shell or notebooks.
 *
 * <code>
 * val args = new BedtoolsFnArgs()
 * args.b = "foo.bed"
 * args.useDocker = true
 * val features = ...
 * val pipedFeatures = new BedtoolsFn(args).apply(features)
 * </code>
 *
 * @param args Bedtools function arguments.
 * @param files Files to make locally available to the commands being run.
 * @param environment A map containing environment variable/value pairs to set
 *   in the environment for the newly created process.
 */
class BedtoolsFn(
    val args: BedtoolsFnArgs,
    val files: Seq[String],
    val environment: Map[String, String]) extends Function1[FeatureRDD, FeatureRDD] with Logging {

  /**
   * @param args Bedtools function arguments.
   */
  def this(args: BedtoolsFnArgs) = this(args, Seq.empty, Map.empty)

  /**
   * @param args Bedtools function arguments.
   * @param files Files to make locally available to the commands being run.
   */
  def this(args: BedtoolsFnArgs, files: Seq[String]) = this(args, files, Map.empty)

  override def apply(features: FeatureRDD): FeatureRDD = {
    val optA = Option(args.a)
    val optB = Option(args.b)
    require(optA.size + optB.size == 1,
      "Strictly one of {-a,-b} should be left unspecified to accept piped input.")

    val bedtoolsCommand = if (args.useDocker) {
      Seq("docker",
        "run",
        "--rm",
        args.dockerImage,
        "bedtools",
        "intersect",
        "-a",
        optA.getOrElse("stdin"),
        "-b",
        optB.getOrElse("stdin"),
        if (args.sorted) "-sorted" else ""
      )
    } else {
      Seq(args.bedtoolsPath,
        "intersect",
        "-a",
        optA.getOrElse("stdin"),
        "-b",
        optB.getOrElse("stdin"),
        if (args.sorted) "-sorted" else ""
      )
    }

    log.info("Piping {} to bedtools with command: {} files: {} environment: {}",
      Array(features, bedtoolsCommand, files, environment))

    implicit val tFormatter = BEDInFormatter
    implicit val uFormatter = new BEDOutFormatter
    features.pipe(bedtoolsCommand, files, environment)
  }
}

object Bedtools extends BDGCommandCompanion {
  val commandName = "bedtools"
  val commandDescription = "ADAM Pipe API wrapper for Bedtools intersect."

  def apply(cmdLine: Array[String]) = {
    new Bedtools(Args4j[BedtoolsArgs](cmdLine))
  }
}

/**
 * Bedtools command line arguments.
 */
class BedtoolsArgs extends BedtoolsFnArgs with ADAMSaveAnyArgs with ParquetArgs {
  @Argument(required = true, metaVar = "INPUT", usage = "Location to pipe from.", index = 0)
  var inputPath: String = null

  @Argument(required = true, metaVar = "OUTPUT", usage = "Location to pipe to.", index = 1)
  var outputPath: String = null

  @Args4jOption(required = false, name = "-single", usage = "Saves OUTPUT as single file.")
  var asSingleFile: Boolean = false

  @Args4jOption(required = false, name = "-disable_fast_concat", usage = "Disables the parallel file concatenation engine.")
  var disableFastConcat: Boolean = false

  @Args4jOption(required = false, name = "-defer_merging", usage = "Defers merging single file output.")
  var deferMerging: Boolean = false

  // must be defined due to ADAMSaveAnyArgs, but unused here
  var sortFastqOutput: Boolean = false
}

/**
 * Bedtools command line wrapper.
 */
class Bedtools(protected val args: BedtoolsArgs) extends BDGSparkCommand[BedtoolsArgs] with Logging {
  val companion = Bedtools

  override def run(sc: SparkContext) {
    val features = sc.loadFeatures(args.inputPath)
    val pipedFeatures = new BedtoolsFn(args).apply(features)

    pipedFeatures.save(args.outputPath,
      asSingleFile = args.asSingleFile,
      disableFastConcat = args.disableFastConcat)
  }
}
