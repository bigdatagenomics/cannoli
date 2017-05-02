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

object Gsnap extends BDGCommandCompanion {
  val commandName = "gsnap"
  val commandDescription = "ADAM Pipe API wrapper for GSNAP."

  def apply(cmdLine: Array[String]) = {
    new Gsnap(Args4j[GsnapArgs](cmdLine))
  }
}

class GsnapArgs extends Args4jBase with ADAMSaveAnyArgs with ParquetArgs {
  @Argument(required = true, metaVar = "INPUT", usage = "Location to pipe from, in interleaved FASTQ format.", index = 0)
  var inputPath: String = null

  @Argument(required = true, metaVar = "OUTPUT", usage = "Location to pipe to.", index = 1)
  var outputPath: String = null

  @Args4jOption(required = true, name = "-genome_path", usage = "Genome database path.  gsnap -D, --dir argument.")
  var genomePath: String = null

  @Args4jOption(required = true, name = "-genome_name", usage = "Genome database name.  gsnap -d, --db argument.")
  var genomeName: String = null

  @Args4jOption(required = false, name = "-single", usage = "Saves OUTPUT as single file.")
  var asSingleFile: Boolean = false

  @Args4jOption(required = false, name = "-defer_merging", usage = "Defers merging single file output.")
  var deferMerging: Boolean = false

  @Args4jOption(required = false, name = "-stringency", usage = "Stringency level for various checks; can be SILENT, LENIENT, or STRICT. Defaults to STRICT.")
  var stringency: String = "STRICT"

  // must be defined due to ADAMSaveAnyArgs, but unused here
  var sortFastqOutput: Boolean = false
}

/**
 * Gsnap.
 */
class Gsnap(protected val args: GsnapArgs) extends BDGSparkCommand[GsnapArgs] with Logging {
  val companion = Gsnap
  val stringency = ValidationStringency.valueOf(args.stringency)

  def run(sc: SparkContext) {
    val input: FragmentRDD = sc.loadFragments(args.inputPath)

    implicit val tFormatter = InterleavedFASTQInFormatter
    implicit val uFormatter = new AnySAMOutFormatter

    val gsnapCommand = "gsnap --dir= " + args.genomePath + " --db=" + args.genomeName + " --format=sam"
    val output: AlignmentRecordRDD = input.pipe[AlignmentRecord, AlignmentRecordRDD, InterleavedFASTQInFormatter](gsnapCommand)
      .transform(_.cache())

    output.save(args)
  }
}
