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
import org.bdgenomics.adam.rdd.ADAMContext._
import org.bdgenomics.adam.rdd.ADAMSaveAnyArgs
import org.bdgenomics.adam.util.FileExtensions._
import org.bdgenomics.cannoli.{
  VtNormalize => VtNormalizeFn,
  VtNormalizeArgs => VtNormalizeFnArgs
}
import org.bdgenomics.utils.cli._
import org.kohsuke.args4j.{ Argument, Option => Args4jOption }

object VtNormalize extends BDGCommandCompanion {
  val commandName = "vtNormalize"
  val commandDescription = "ADAM Pipe API wrapper for vt normalize."

  def apply(cmdLine: Array[String]) = {
    new VtNormalize(Args4j[VtNormalizeArgs](cmdLine))
  }
}

/**
 * Vt normalize command line arguments.
 */
class VtNormalizeArgs extends VtNormalizeFnArgs with ADAMSaveAnyArgs with ParquetArgs {
  @Argument(required = true, metaVar = "INPUT", usage = "Location to pipe variant contexts from (e.g. .vcf, .vcf.gz, .vcf.bgz). If extension is not detected, Parquet is assumed.", index = 0)
  var inputPath: String = null

  @Argument(required = true, metaVar = "OUTPUT", usage = "Location to pipe variant contexts to (e.g. .vcf, .vcf.gz, .vcf.bgz). If extension is not detected, Parquet is assumed.", index = 1)
  var outputPath: String = null

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
 * Vt normalize command line wrapper.
 */
class VtNormalize(protected val args: VtNormalizeArgs) extends BDGSparkCommand[VtNormalizeArgs] with Logging {
  val companion = VtNormalize
  val stringency: ValidationStringency = ValidationStringency.valueOf(args.stringency)

  def run(sc: SparkContext) {
    val variantContexts = sc.loadVariantContexts(args.inputPath)
    val pipedVariantContexts = new VtNormalizeFn(args, stringency, sc).apply(variantContexts)

    if (isVcfExt(args.outputPath)) {
      pipedVariantContexts.saveAsVcf(
        args.outputPath,
        asSingleFile = args.asSingleFile,
        deferMerging = args.deferMerging,
        disableFastConcat = args.disableFastConcat,
        stringency
      )
    } else {
      pipedVariantContexts.saveAsParquet(args)
    }
  }
}
