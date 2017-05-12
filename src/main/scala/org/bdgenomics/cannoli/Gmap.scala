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

object Gmap extends BDGCommandCompanion {
  val commandName = "gmap"
  val commandDescription = "ADAM Pipe API wrapper for GMAP."

  def apply(cmdLine: Array[String]) = {
    new Gmap(Args4j[GmapArgs](cmdLine))
  }
}

class GmapArgs extends Args4jBase with ADAMSaveAnyArgs with ParquetArgs {
  @Argument(required = true, metaVar = "INPUT", usage = "Location to pipe from, in interleaved FASTQ format.", index = 0)
  var inputPath: String = null

  @Argument(required = true, metaVar = "OUTPUT", usage = "Location to pipe to.", index = 1)
  var outputPath: String = null

  @Args4jOption(required = true, name = "-genome_path", usage = "Genome database path. gmap -D, --dir argument.")
  var genomePath: String = null

  @Args4jOption(required = true, name = "-genome_name", usage = "Genome database name. gmap -d, --db argument.")
  var genomeName: String = null

  @Args4jOption(required = false, name = "-single", usage = "Saves OUTPUT as single file.")
  var asSingleFile: Boolean = false

  @Args4jOption(required = false, name = "-defer_merging", usage = "Defers merging single file output.")
  var deferMerging: Boolean = false

  @Args4jOption(required = false, name = "-disable_fast_concat", usage = "Disables the parallel file concatenation engine.")
  var disableFastConcat: Boolean = false

  @Args4jOption(required = false, name = "-stringency", usage = "Stringency level for various checks; can be SILENT, LENIENT, or STRICT. Defaults to STRICT.")
  var stringency: String = "STRICT"

  @Args4jOption(required = false, name = "-gmap_path", usage = "Path to the GMAP executable. Defaults to gmap.")
  var gmapPath: String = "gmap"

  // must be defined due to ADAMSaveAnyArgs, but unused here
  var sortFastqOutput: Boolean = false
}

/**
 * Gmap.
 */
class Gmap(protected val args: GmapArgs) extends BDGSparkCommand[GmapArgs] with Logging {
  val companion = Gmap
  val stringency = ValidationStringency.valueOf(args.stringency)

  def run(sc: SparkContext) {
    val input: FragmentRDD = sc.loadFragments(args.inputPath)

    implicit val tFormatter = InterleavedFASTQInFormatter
    implicit val uFormatter = new AnySAMOutFormatter

    val gmapCommand = Seq(args.gmapPath,
      "--dir=" + args.genomePath,
      "--db=" + args.genomeName,
      "--format=sampe").mkString(" ")

    val output: AlignmentRecordRDD = input.pipe[AlignmentRecord, AlignmentRecordRDD, InterleavedFASTQInFormatter](gmapCommand)

    output.save(args)
  }
}
