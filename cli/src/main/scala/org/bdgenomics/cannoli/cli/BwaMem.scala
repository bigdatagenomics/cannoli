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
import org.bdgenomics.adam.ds.fragment.FragmentDataset
import org.bdgenomics.adam.ds.read.AlignmentDataset
import org.bdgenomics.cannoli.{ BwaMem => BwaMemFn, BwaMemArgs => BwaMemFnArgs }
import org.bdgenomics.cannoli.util.QuerynameGrouper
import org.bdgenomics.utils.cli._
import org.kohsuke.args4j.{ Argument, Option => Args4jOption }

object BwaMem extends BDGCommandCompanion {
  val commandName = "bwaMem"
  val commandDescription = "Align paired-end reads in a fragment dataset with bwa mem."

  def apply(cmdLine: Array[String]) = {
    new BwaMem(Args4j[BwaMemArgs](cmdLine))
  }
}

/**
 * Bwa mem command line arguments.
 */
class BwaMemArgs extends BwaMemFnArgs with ADAMSaveAnyArgs with CramArgs with ParquetArgs {
  @Argument(required = true, metaVar = "INPUT", usage = "Location to pipe fragments from (e.g. interleaved FASTQ format, .ifq). If extension is not detected, Parquet is assumed.", index = 0)
  var inputPath: String = null

  @Argument(required = true, metaVar = "OUTPUT", usage = "Location to pipe alignments to (e.g. .bam, .cram, .sam). If extension is not detected, Parquet is assumed.", index = 1)
  var outputPath: String = null

  @Args4jOption(required = false, name = "-single", usage = "Saves OUTPUT as single file. Exclusive of -fragments.")
  var asSingleFile: Boolean = false

  @Args4jOption(required = false, name = "-fragments", usage = "Saves OUTPUT as Fragments in Parquet. Exclusive of -single.")
  var asFragments: Boolean = false

  @Args4jOption(required = false, name = "-defer_merging", usage = "Defers merging single file output.")
  var deferMerging: Boolean = false

  @Args4jOption(required = false, name = "-disable_fast_concat", usage = "Disables the parallel file concatenation engine.")
  var disableFastConcat: Boolean = false

  @Args4jOption(required = false, name = "-force_load_ifastq", usage = "Forces loading using interleaved FASTQ.")
  var forceLoadIfastq: Boolean = false

  @Args4jOption(required = false, name = "-force_load_parquet", usage = "Forces loading using Parquet.")
  var forceLoadParquet: Boolean = false

  @Args4jOption(required = false, name = "-sequence_dictionary", usage = "Path to the sequence dictionary.")
  var sequenceDictionary: String = _

  @Args4jOption(required = false, name = "-stringency", usage = "Stringency level for various checks; can be SILENT, LENIENT, or STRICT. Defaults to STRICT.")
  var stringency: String = "STRICT"

  // must be defined due to ADAMSaveAnyArgs, but unused here
  var sortFastqOutput: Boolean = false
}

/**
 * Bwa mem command line wrapper.
 */
class BwaMem(protected val args: BwaMemArgs) extends BDGSparkCommand[BwaMemArgs] with Logging {
  val companion = BwaMem
  val stringency: ValidationStringency = ValidationStringency.valueOf(args.stringency)

  def run(sc: SparkContext) {
    require(!(args.asSingleFile && args.asFragments),
      "-single and -fragments are mutually exclusive.")
    require(!(args.forceLoadIfastq && args.forceLoadParquet),
      "-force_load_ifastq and -force_load_parquet are mutually exclusive.")

    args.configureCramFormat(sc)

    val input: FragmentDataset = if (args.forceLoadIfastq) {
      sc.loadInterleavedFastqAsFragments(args.inputPath)
    } else if (args.forceLoadParquet) {
      sc.loadParquetFragments(args.inputPath)
    } else {
      sc.loadFragments(args.inputPath, stringency = stringency)
    }

    val output: AlignmentDataset = new BwaMemFn(args, sc).apply(input)

    val outputMaybeWithReferences = Option(args.sequenceDictionary).fold(output)(sdPath => {
      val references = sc.loadSequenceDictionary(sdPath)
      output.replaceReferences(references)
    })

    if (!args.asFragments) {
      outputMaybeWithReferences.save(args)
    } else {
      info("Converting to fragments.")
      QuerynameGrouper(outputMaybeWithReferences)
        .saveAsParquet(args)
    }
  }
}
