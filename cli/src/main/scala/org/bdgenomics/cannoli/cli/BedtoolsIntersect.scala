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

import org.apache.spark.SparkContext
import org.bdgenomics.adam.projections.{ FeatureField, Projection }
import org.bdgenomics.adam.rdd.ADAMContext._
import org.bdgenomics.adam.rdd.ADAMSaveAnyArgs
import org.bdgenomics.cannoli.{
  BedtoolsIntersect => BedtoolsIntersectFn,
  BedtoolsIntersectArgs => BedtoolsIntersectFnArgs
}
import org.bdgenomics.utils.cli._
import org.bdgenomics.utils.misc.Logging
import org.kohsuke.args4j.{ Argument, Option => Args4jOption }

object BedtoolsIntersect extends BDGCommandCompanion {
  val commandName = "bedtoolsIntersect"
  val commandDescription = "ADAM Pipe API wrapper for Bedtools intersect."

  def apply(cmdLine: Array[String]) = {
    new BedtoolsIntersect(Args4j[BedtoolsIntersectArgs](cmdLine))
  }
}

/**
 * Bedtools intersect command line arguments.
 */
class BedtoolsIntersectArgs extends BedtoolsIntersectFnArgs with ADAMSaveAnyArgs with ParquetArgs {
  @Argument(required = true, metaVar = "INPUT", usage = "Location to pipe features from (e.g., .bed, .gff/.gtf, .gff3, .interval_list, .narrowPeak). If extension is not detected, Parquet is assumed.", index = 0)
  var inputPath: String = null

  @Argument(required = true, metaVar = "OUTPUT", usage = "Location to pipe features to. If extension is not detected, Parquet is assumed.", index = 1)
  var outputPath: String = null

  @Args4jOption(required = false, name = "-limit_projection", usage = "If input is Parquet, limit to BED format-only fields by projection.")
  var limitProjection: Boolean = false

  @Args4jOption(required = false, name = "-partitions", usage = "Number of partitions to use when loading a text file.")
  var partitions: Int = _

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
 * Bedtools intersect command line wrapper.
 */
class BedtoolsIntersect(protected val args: BedtoolsIntersectArgs) extends BDGSparkCommand[BedtoolsIntersectArgs] with Logging {
  val companion = BedtoolsIntersect

  override def run(sc: SparkContext) {
    val projection = Projection(
      FeatureField.contigName,
      FeatureField.start,
      FeatureField.end,
      FeatureField.name,
      FeatureField.score,
      FeatureField.strand
    )

    val features = sc.loadFeatures(
      args.inputPath,
      optMinPartitions = Option(args.partitions),
      optProjection = if (args.limitProjection) Some(projection) else None
    )
    val pipedFeatures = new BedtoolsIntersectFn(args, sc).apply(features)

    pipedFeatures.save(args.outputPath,
      asSingleFile = args.asSingleFile,
      disableFastConcat = args.disableFastConcat)
  }
}
