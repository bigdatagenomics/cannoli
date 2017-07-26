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
import org.bdgenomics.adam.rdd.ADAMContext._
import org.bdgenomics.adam.rdd.ADAMSaveAnyArgs
import org.bdgenomics.adam.rdd.fragment.{ FragmentRDD, InterleavedFASTQInFormatter }
import org.bdgenomics.adam.rdd.read.{ AlignmentRecordRDD, AnySAMOutFormatter }
import org.bdgenomics.formats.avro.AlignmentRecord
import org.bdgenomics.utils.cli._
import org.bdgenomics.utils.misc.Logging
import org.kohsuke.args4j.{ Argument, Option => Args4jOption }

object SampleReads extends BDGCommandCompanion {
  val commandName = "sampleReads"
  val commandDescription = "Sample reads"

  def apply(cmdLine: Array[String]) = {
    new SampleReads(Args4j[SampleReadsArgs](cmdLine))
  }
}

class SampleReadsArgs extends Args4jBase with ADAMSaveAnyArgs with ParquetArgs {
  @Argument(required = true, metaVar = "INPUT", usage = "Location to read from, in interleaved FASTQ format.", index = 0)
  var inputPath: String = null

  @Argument(required = true, metaVar = "OUTPUT", usage = "Location to write Parquet fragments to.", index = 1)
  var outputPath: String = null

  @Args4jOption(required = true, name = "-fraction", usage = "Expected size of the sample as a fraction of the input size, [0.0, 1.0].")
  var fraction: Double = 0.5d

  @Args4jOption(required = false, name = "-seed", usage = "Random number seed. Defaults to current timestamp.")
  var seed: Long = System.currentTimeMillis

  @Args4jOption(required = false, name = "-stringency", usage = "Stringency level for various checks; can be SILENT, LENIENT, or STRICT. Defaults to STRICT.")
  var stringency: String = "STRICT"

  // must be defined due to ADAMSaveAnyArgs, but unused here
  var sortFastqOutput: Boolean = false
  var asSingleFile: Boolean = false
  var deferMerging: Boolean = false
  var disableFastConcat: Boolean = false
}

/**
 * Sample reads.
 */
class SampleReads(protected val args: SampleReadsArgs) extends BDGSparkCommand[SampleReadsArgs] with Logging {
  val companion = SampleReads
  val stringency = ValidationStringency.valueOf(args.stringency)

  def run(sc: SparkContext) {
    val fragments: FragmentRDD = sc.loadFragments(args.inputPath)

    log.info("Sampling fraction %f with seed %d".format(args.fraction, args.seed))
    fragments
      .transform(_.sample(withReplacement = false, args.fraction, args.seed))
      .save(args.outputPath)
  }
}
