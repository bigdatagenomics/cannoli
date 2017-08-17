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
import org.bdgenomics.adam.rdd.read.{ AlignmentRecordRDD, BAMInFormatter, SAMInFormatter, AnySAMInFormatter }
import org.apache.spark.SparkContext
import org.bdgenomics.adam.rdd.ADAMContext._
import org.bdgenomics.adam.rdd.ADAMSaveAnyArgs
import org.bdgenomics.adam.rdd.feature.{ BEDInFormatter, BEDOutFormatter, FeatureRDD }
import org.bdgenomics.utils.cli._
import org.bdgenomics.utils.misc.Logging
import org.kohsuke.args4j.{ Argument, Option => Args4jOption }
import org.apache.spark.rdd.RDD
import org.apache.spark.HashPartitioner
import org.bdgenomics.adam.models._
import org.bdgenomics.formats.avro.Feature
import org.bdgenomics.formats.avro.{ Feature, AlignmentRecord }
import org.bdgenomics.adam.rdd.read.{ AlignmentRecordRDD, AnySAMOutFormatter }
import org.bdgenomics.adam.rdd.read.{ AlignmentRecordRDD, BAMInFormatter }

object MACS2 extends BDGCommandCompanion {
  val commandName = "macs2"
  val commandDescription = "ADAM Pipe API wrapper for MACS2."

  def apply(cmdLine: Array[String]) = {
    new MACS2(Args4j[MACS2Args](cmdLine))
  }
}

class MACS2Args extends Args4jBase with ADAMSaveAnyArgs with ParquetArgs {
  @Argument(required = true, metaVar = "function", usage = "MACS2 function to perform. Only callpeak is currently supported.", index = 0)
  var function: String = null

  @Argument(required = true, metaVar = "INPUT", usage = "Location to pipe from.", index = 1)
  var inputPath: String = null

  @Argument(required = true, metaVar = "OUTPUT", usage = "Location to pipe to.", index = 2)
  var outputPath: String = null

  @Args4jOption(required = true, name = "-macs2_output", usage = "Folder in which MACS2 output is saved.")
  var macs2OutputPath: String = null

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
 * MACS2.
 */
class MACS2(protected val args: MACS2Args) extends BDGSparkCommand[MACS2Args] with Logging {
  val companion = MACS2
  val stringency = ValidationStringency.valueOf(args.stringency)

  def run(sc: SparkContext) {
    val MACS2Command = "/home/eecs/gunjan/cannoli/run-macs2.sh " + args.macs2OutputPath
    val inputFiles = args.inputPath.split(",")
    val input = sc.parallelize(inputFiles, inputFiles.length)
    val outputFiles = input.pipe(MACS2Command)

    var output: FeatureRDD = null
    for (file <- outputFiles.collect()) {
      val features = sc.loadFeatures(file)
      output = if (output != null) {
        features.union(output)
      } else {
        features
      }
    }

    output.save(args.outputPath,
      asSingleFile = args.asSingleFile,
      disableFastConcat = args.disableFastConcat)
  }
}
