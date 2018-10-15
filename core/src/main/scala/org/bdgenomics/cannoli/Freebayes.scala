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
import htsjdk.variant.vcf.VCFHeaderLine
import org.apache.spark.SparkContext
import org.apache.spark.util.CollectionAccumulator
import org.bdgenomics.adam.models.VariantContext
import org.bdgenomics.adam.rdd.ADAMContext._
import org.bdgenomics.adam.rdd.read.{ AlignmentRecordRDD, BAMInFormatter }
import org.bdgenomics.adam.rdd.variant.{ VariantContextRDD, VCFOutFormatter }
import org.bdgenomics.adam.sql.{ VariantContext => VariantContextProduct }
import org.bdgenomics.cannoli.builder.CommandBuilders
import org.bdgenomics.utils.cli._
import org.bdgenomics.utils.misc.Logging
import org.kohsuke.args4j.{ Option => Args4jOption }
import scala.collection.JavaConversions._

/**
 * Freebayes function arguments.
 */
class FreebayesArgs extends Args4jBase {
  @Args4jOption(required = false, name = "-executable", usage = "Path to the Freebayes executable. Defaults to freebayes.")
  var executable: String = "freebayes"

  @Args4jOption(required = false, name = "-image", usage = "Container image to use. Defaults to quay.io/biocontainers/freebayes:1.2.0--py27h82df9c4_3.")
  var image: String = "quay.io/biocontainers/freebayes:1.2.0--py27h82df9c4_3"

  @Args4jOption(required = false, name = "-sudo", usage = "Run via sudo.")
  var sudo: Boolean = false

  @Args4jOption(required = false, name = "-add_files", usage = "If true, use the SparkFiles mechanism to distribute files to executors.")
  var addFiles: Boolean = false

  @Args4jOption(required = false, name = "-use_docker", usage = "If true, uses Docker to launch Freebayes.")
  var useDocker: Boolean = false

  @Args4jOption(required = false, name = "-use_singularity", usage = "If true, uses Singularity to launch Freebayes.")
  var useSingularity: Boolean = false

  @Args4jOption(required = true, name = "-fasta_reference", usage = "Reference sequence for analysis. An index file (.fai) will be created if none exists.")
  var referencePath: String = null

  @Args4jOption(required = false, name = "-gvcf", usage = "Write gVCF output or equivalent genotypes which indicate coverage in uncalled regions.")
  var gvcf: Boolean = false

  @Args4jOption(required = false, name = "-gvcf_chunk", usage = "When writing gVCF output or equivalent genotypes emit a record for every N bases.")
  var gvcfChunk: Int = _
}

/**
 * Freebayes wrapper as a function AlignmentRecordRDD &rarr; VariantContextRDD,
 * for use in cannoli-shell or notebooks.
 *
 * @param args Freebayes function arguments.
 * @param stringency Validation stringency. Defaults to ValidationStringency.LENIENT.
 * @param sc Spark context.
 */
class Freebayes(
    val args: FreebayesArgs,
    val stringency: ValidationStringency = ValidationStringency.LENIENT,
    sc: SparkContext) extends CannoliFn[AlignmentRecordRDD, VariantContextRDD](sc) with Logging {

  override def apply(alignments: AlignmentRecordRDD): VariantContextRDD = {

    var builder = CommandBuilders.create(args.useDocker, args.useSingularity)
      .setExecutable(args.executable)
      .add("--fasta-reference")
      .add(if (args.addFiles) "$0" else absolute(args.referencePath))
      .add("--stdin")
      .add("--strict-vcf")

    if (args.gvcf) {
      builder.add("--gvcf")
      Option(args.gvcfChunk).foreach(i => builder.add("--gvcf-chunk").add(i.toString))
    }

    if (args.addFiles) {
      builder.addFile(args.referencePath)
      builder.addFile(args.referencePath + ".fai")
    }

    if (args.useDocker || args.useSingularity) {
      builder
        .setImage(args.image)
        .setSudo(args.sudo)
        .addMount(if (args.addFiles) "$root" else root(args.referencePath))
    }

    log.info("Piping {} to freebayes with command: {} files: {}",
      alignments, builder.build(), builder.getFiles())

    val accumulator: CollectionAccumulator[VCFHeaderLine] = sc.collectionAccumulator("headerLines")

    implicit val tFormatter = BAMInFormatter
    implicit val uFormatter = new VCFOutFormatter(sc.hadoopConfiguration, stringency, Some(accumulator))

    val variantContexts = alignments.pipe[VariantContext, VariantContextProduct, VariantContextRDD, BAMInFormatter](
      cmd = builder.build(),
      files = builder.getFiles()
    )

    val headerLines = accumulator.value.distinct
    variantContexts.replaceHeaderLines(headerLines)
  }
}
