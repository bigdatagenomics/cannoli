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

import htsjdk.samtools.ValidationStringency
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.rdd.RDD
import org.bdgenomics.adam.converters.DefaultHeaderLines
import org.bdgenomics.adam.models.VariantContext
import org.bdgenomics.adam.rdd.ADAMContext._
import org.bdgenomics.adam.rdd.ADAMSaveAnyArgs
import org.bdgenomics.adam.rdd.read.{ AlignmentRecordRDD, BAMInFormatter }
import org.bdgenomics.adam.rdd.variant.{ VariantContextRDD, VCFOutFormatter }
import org.bdgenomics.utils.cli._
import org.bdgenomics.utils.misc.Logging
import org.kohsuke.args4j.{ Argument, Option => Args4jOption }

object Freebayes extends BDGCommandCompanion {
  val commandName = "freebayes"
  val commandDescription = "ADAM Pipe API wrapper for Freebayes."

  def apply(cmdLine: Array[String]) = {
    new Freebayes(Args4j[FreebayesArgs](cmdLine))
  }
}

class FreebayesArgs extends Args4jBase with ADAMSaveAnyArgs with ParquetArgs {
  @Argument(required = true, metaVar = "INPUT", usage = "Location to pipe from", index = 0)
  var inputPath: String = null

  @Argument(required = true, metaVar = "OUTPUT", usage = "Location to pipe to", index = 1)
  var outputPath: String = null

  @Args4jOption(required = true, name = "-freebayes_reference", usage = "Reference sequence for analysis. An index file (.fai) will be created if none exists.")
  var referencePath: String = null

  @Args4jOption(required = false, name = "-single", usage = "Saves OUTPUT as single file")
  var asSingleFile: Boolean = false

  @Args4jOption(required = false, name = "-defer_merging", usage = "Defers merging single file output")
  var deferMerging: Boolean = false

  @Args4jOption(required = false, name = "-stringency", usage = "Stringency level for various checks; can be SILENT, LENIENT, or STRICT. Defaults to STRICT")
  var stringency: String = "STRICT"

  // must be defined due to ADAMSaveAnyArgs, but unused here
  var sortFastqOutput: Boolean = false
}

/**
 * Freebayes.
 */
class Freebayes(protected val args: FreebayesArgs) extends BDGSparkCommand[FreebayesArgs] with Logging {
  val companion = Freebayes
  val stringency = ValidationStringency.valueOf(args.stringency)

  def run(sc: SparkContext) {
    val input: AlignmentRecordRDD = sc.loadAlignments(args.inputPath, stringency = stringency)

    implicit val tFormatter = BAMInFormatter
    //implicit val uFormatter = new VCFOutFormatter(SupportedHeaderLines.allHeaderLines))
    implicit val uFormatter = new VCFOutFormatter(Seq.empty)

    val freebayesCommand = "freebayes --fasta-reference " + args.referencePath + " --stdin"
    val output: VariantContextRDD = input.pipe[VariantContext, VariantContextRDD, BAMInFormatter](freebayesCommand)
      .transform(_.cache())

    output.saveAsVcf(args.outputPath, args.asSingleFile, stringency)
  }
}
