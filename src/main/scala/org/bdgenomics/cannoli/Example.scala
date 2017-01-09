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
import org.apache.spark.SparkContext._
import org.apache.spark.rdd.RDD
import org.bdgenomics.adam.rdd.ADAMContext._
import org.bdgenomics.adam.rdd.ADAMSaveAnyArgs
import org.bdgenomics.adam.rdd.read.{
  AlignmentRecordRDD,
  AnySAMOutFormatter,
  SAMInFormatter
}
import org.bdgenomics.formats.avro.AlignmentRecord
import org.bdgenomics.utils.cli._
import org.bdgenomics.utils.misc.Logging
import org.kohsuke.args4j.{ Argument, Option => Args4jOption }

object Example extends BDGCommandCompanion {
  val commandName = "example"
  val commandDescription = "Example."

  def apply(cmdLine: Array[String]) = {
    new Example(Args4j[ExampleArgs](cmdLine))
  }
}

class ExampleArgs extends Args4jBase with ADAMSaveAnyArgs with ParquetArgs {
  @Argument(required = true, metaVar = "INPUT", usage = "Location to pipe from", index = 0)
  var inputPath: String = null
  @Argument(required = true, metaVar = "OUTPUT", usage = "Location to pipe to", index = 1)
  var outputPath: String = null
  @Args4jOption(required = false, name = "-single", usage = "Saves OUTPUT as single file")
  var asSingleFile: Boolean = false
  @Args4jOption(required = false, name = "-defer_merging", usage = "Defers merging single file output")
  var deferMerging: Boolean = false
  // must be defined due to ADAMSaveAnyArgs, but unused here
  var sortFastqOutput: Boolean = false
}

/**
 * Example.
 */
class Example(protected val args: ExampleArgs) extends BDGSparkCommand[ExampleArgs] with Logging {
  val companion = Example

  def run(sc: SparkContext) {
    var alignments: AlignmentRecordRDD = sc.loadAlignments(args.inputPath)

    implicit val tFormatter = SAMInFormatter
    implicit val uFormatter = new AnySAMOutFormatter
    val pipedAlignments: AlignmentRecordRDD = alignments.pipe("tee /dev/null")

    pipedAlignments.save(args)
  }
}
