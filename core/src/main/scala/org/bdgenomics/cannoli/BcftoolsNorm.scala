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
import org.bdgenomics.adam.models.VariantContext
import org.bdgenomics.adam.rdd.ADAMContext._
import org.bdgenomics.adam.rdd.variant.{
  VariantContextRDD,
  VCFInFormatter,
  VCFOutFormatter
}
import org.bdgenomics.adam.sql.{ VariantContext => VariantContextProduct }
import org.bdgenomics.cannoli.builder.CommandBuilders
import org.bdgenomics.utils.cli._
import org.bdgenomics.utils.misc.Logging
import org.kohsuke.args4j.{ Option => Args4jOption }
import scala.collection.JavaConversions._

/**
 * Bcftools norm function arguments.
 */
class BcftoolsNormArgs extends Args4jBase {
  @Args4jOption(required = false, name = "-executable", usage = "Path to the BCFtools executable. Defaults to bcftools.")
  var executable: String = "bcftools"

  @Args4jOption(required = false, name = "-image", usage = "Container image to use. Defaults to quay.io/biocontainers/bcftools:1.9--h4da6232_0.")
  var image: String = "quay.io/biocontainers/bcftools:1.9--h4da6232_0"

  @Args4jOption(required = false, name = "-sudo", usage = "Run via sudo.")
  var sudo: Boolean = false

  @Args4jOption(required = false, name = "-add_files", usage = "If true, use the SparkFiles mechanism to distribute files to executors.")
  var addFiles: Boolean = false

  @Args4jOption(required = false, name = "-use_docker", usage = "If true, uses Docker to launch BCFtools.")
  var useDocker: Boolean = false

  @Args4jOption(required = false, name = "-use_singularity", usage = "If true, uses Singularity to launch BCFtools.")
  var useSingularity: Boolean = false

  @Args4jOption(required = true, name = "-reference", usage = "Reference sequence for analysis. An index file (.fai) will be created if none exists.")
  var referencePath: String = null
}

/**
 * Bcftools norm wrapper as a function VariantContextRDD &rarr; VariantContextRDD,
 * for use in cannoli-shell or notebooks.
 *
 * @param args Bcftools norm function arguments.
 * @param stringency Validation stringency. Defaults to ValidationStringency.LENIENT.
 * @param sc Spark context.
 */
class BcftoolsNorm(
    val args: BcftoolsNormArgs,
    val stringency: ValidationStringency = ValidationStringency.LENIENT,
    sc: SparkContext) extends CannoliFn[VariantContextRDD, VariantContextRDD](sc) with Logging {

  override def apply(variantContexts: VariantContextRDD): VariantContextRDD = {

    val builder = CommandBuilders.create(args.useDocker, args.useSingularity)
      .setExecutable(args.executable)
      .add("norm")
      .add("--fasta-ref")
      .add(if (args.addFiles) "$0" else absolute(args.referencePath))

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

    log.info("Piping {} to bcftools with command: {} files: {}",
      variantContexts, builder.build(), builder.getFiles())

    implicit val tFormatter = VCFInFormatter
    implicit val uFormatter = new VCFOutFormatter(sc.hadoopConfiguration, stringency)

    variantContexts.pipe[VariantContext, VariantContextProduct, VariantContextRDD, VCFInFormatter](
      cmd = builder.build(),
      files = builder.getFiles()
    )
  }
}
