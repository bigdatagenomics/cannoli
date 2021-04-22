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

import grizzled.slf4j.Logging
import htsjdk.samtools.ValidationStringency
import org.apache.spark.SparkContext
import org.bdgenomics.adam.ds.ADAMContext._
import org.bdgenomics.adam.ds.ADAMSaveAnyArgs
import org.bdgenomics.cannoli.{ SingleEndUnimap => SingleEndUnimapFn, UnimapArgs => UnimapFnArgs }
import org.bdgenomics.utils.cli._
import org.kohsuke.args4j.{ Argument, Option => Args4jOption }

object SingleEndUnimap extends BDGCommandCompanion {
  val commandName = "singleEndUnimap"
  val commandDescription = "Align unaligned single-end reads in an alignment dataset with Unimap."

  def apply(cmdLine: Array[String]) = {
    new SingleEndUnimap(Args4j[SingleEndUnimapArgs](cmdLine))
  }
}

/**
 * Single-end read Unimap command line arguments.
 */
class SingleEndUnimapArgs extends UnimapFnArgs with ADAMSaveAnyArgs with CramArgs with ParquetArgs {
  @Argument(required = true, metaVar = "INPUT", usage = "Location to pipe single-end reads from (e.g. FASTQ format, .fq). If extension is not detected, Parquet is assumed.", index = 0)
  var inputPath: String = null

  @Argument(required = true, metaVar = "OUTPUT", usage = "Location to pipe alignments to (e.g. .bam, .cram, .sam). If extension is not detected, Parquet is assumed.", index = 1)
  var outputPath: String = null

  @Args4jOption(required = false, name = "-single", usage = "Saves OUTPUT as single file.")
  var asSingleFile: Boolean = false

  @Args4jOption(required = false, name = "-defer_merging", usage = "Defers merging single file output.")
  var deferMerging: Boolean = false

  @Args4jOption(required = false, name = "-disable_fast_concat", usage = "Disables the parallel file concatenation engine.")
  var disableFastConcat: Boolean = false

  @Args4jOption(required = false, name = "-sequence_dictionary", usage = "Path to the sequence dictionary.")
  var sequenceDictionary: String = _

  @Args4jOption(required = false, name = "-stringency", usage = "Stringency level for various checks; can be SILENT, LENIENT, or STRICT. Defaults to STRICT.")
  var stringency: String = "STRICT"

  // must be defined due to ADAMSaveAnyArgs, but unused here
  var sortFastqOutput: Boolean = false
}

/**
 * Single-end read Unimap command line wrapper.
 */
class SingleEndUnimap(protected val args: SingleEndUnimapArgs) extends BDGSparkCommand[SingleEndUnimapArgs] with Logging {
  val companion = SingleEndUnimap
  val stringency: ValidationStringency = ValidationStringency.valueOf(args.stringency)

  def run(sc: SparkContext) {
    args.configureCramFormat(sc)
    val reads = sc.loadAlignments(args.inputPath, stringency = stringency)
    val alignments = new SingleEndUnimapFn(args, sc).apply(reads)
    val maybeWithReferences = Option(args.sequenceDictionary).fold(alignments)(sdPath => {
      val references = sc.loadSequenceDictionary(sdPath)
      alignments.replaceReferences(references)
    })
    maybeWithReferences.save(args)
  }
}
