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

object Bedtools extends BDGCommandCompanion {
  val commandName = "bedtools"
  val commandDescription = "ADAM Pipe API wrapper for Bedtools intersect."

  def apply(cmdLine: Array[String]) = {
    new Bedtools(Args4j[BedtoolsArgs](cmdLine))
  }
}

class BedtoolsArgs extends Args4jBase with ADAMSaveAnyArgs with ParquetArgs {
  @Argument(required = true, metaVar = "INPUT", usage = "Location to pipe from.", index = 0)
  var inputPath: String = null

  @Argument(required = true, metaVar = "OUTPUT", usage = "Location to pipe to.", index = 1)
  var outputPath: String = null

  @Args4jOption(required = false, name = "-a", usage = "Bedtools intersect -a option. One of {-a,-b} should be left unspecified to accept piped input.")
  var a: String = null

  @Args4jOption(required = false, name = "-b", usage = "Bedtools intersect -b option. One of {-a,-b} should be left unspecified to accept piped input.")
  var b: String = null

  @Args4jOption(required = false, name = "-sorted", usage = "Bedtools intersect -sorted option. Inputs must be sorted by chromosome and then by start position.")
  var sorted: Boolean = false

  @Args4jOption(required = false, name = "-bedtools_path", usage = "Path to the Bedtools executable. Defaults to bedtools.")
  var bedtoolsPath: String = "bedtools"

  @Args4jOption(required = false, name = "-docker_image", usage = "Docker image to use. Defaults to heuermh/bedtools.")
  var dockerImage: String = "heuermh/bedtools"

  @Args4jOption(required = false, name = "-use_docker", usage = "If true, uses Docker to launch Bedtools. If false, uses the Bedtools executable path.")
  var useDocker: Boolean = false

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
 * Bedtools.
 */
class Bedtools(protected val args: BedtoolsArgs) extends BDGSparkCommand[BedtoolsArgs] with Logging {
  val companion = Bedtools

  def run(sc: SparkContext) {
    var features: FeatureRDD = sc.loadFeatures(args.inputPath)

    val optA = Option(args.a)
    val optB = Option(args.b)
    require(optA.size + optB.size == 1,
      "Strictly one of {-a,-b} should be left unspecified to accept piped input.")

    val bedtoolsCommand = if (args.useDocker) {
      Seq("docker",
        "run",
        args.dockerImage,
        "bedtools",
        "intersect",
        "-a",
        optA.getOrElse("stdin"),
        "-b",
        optB.getOrElse("stdin"),
        if (args.sorted) "-sorted" else ""
      ).mkString(" ")
    } else {
      Seq(args.bedtoolsPath,
        "intersect",
        "-a",
        optA.getOrElse("stdin"),
        "-b",
        optB.getOrElse("stdin"),
        if (args.sorted) "-sorted" else ""
      ).mkString(" ")
    }

    implicit val tFormatter = BEDInFormatter
    implicit val uFormatter = new BEDOutFormatter
    val pipedFeatures: FeatureRDD = features.pipe(bedtoolsCommand)

    pipedFeatures.save(args.outputPath,
      asSingleFile = args.asSingleFile,
      disableFastConcat = args.disableFastConcat)
  }
}
