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
import org.bdgenomics.adam.rdd.variant.{ VariantContextDataset, VCFInFormatter, VCFOutFormatter }
import org.bdgenomics.adam.sql.{ VariantContext => VariantContextProduct }
import org.bdgenomics.cannoli.builder.CommandBuilders
import org.bdgenomics.utils.cli._
import org.kohsuke.args4j.{ Option => Args4jOption }
import scala.collection.JavaConversions._

/**
 * Bcftools call function arguments.
 */
class BcftoolsCallArgs extends Args4jBase {
  @Args4jOption(required = false, name = "-executable", usage = "Path to the bcftools executable. Defaults to bcftools.")
  var executable: String = "bcftools"

  @Args4jOption(required = false, name = "-docker_image", usage = "Container image to use. Defaults to quay.io/biocontainers/bcftools:1.11--h7c999a4_0.")
  var image: String = "quay.io/biocontainers/bcftools:1.11--h7c999a4_0"

  @Args4jOption(required = false, name = "-sudo", usage = "Run via sudo.")
  var sudo: Boolean = false

  @Args4jOption(required = false, name = "-add_files", usage = "If true, use the SparkFiles mechanism to distribute files to executors.")
  var addFiles: Boolean = false

  @Args4jOption(required = false, name = "-use_docker", usage = "If true, uses Docker to launch bcftools.")
  var useDocker: Boolean = false

  @Args4jOption(required = false, name = "-use_singularity", usage = "If true, uses Singularity to launch bcftools.")
  var useSingularity: Boolean = false

  @Args4jOption(required = false, name = "-bcftools_args", usage = "Additional arguments for Bcftools, must be double-quoted, e.g. -bcftools_args \"--gcvf 5,15\"")
  var bcftoolsArgs: String = null
}

/**
 * Bcftools call wrapper as a function VariantContextDataset &rarr; VariantContextDataset,
 * for use in cannoli-shell or notebooks.
 *
 * @param args Bcftools call function arguments.
 * @param stringency Validation stringency. Defaults to ValidationStringency.LENIENT.
 * @param sc Spark context.
 */
class BcftoolsCall(
    val args: BcftoolsCallArgs,
    val stringency: ValidationStringency = ValidationStringency.LENIENT,
    sc: SparkContext) extends CannoliFn[VariantContextDataset, VariantContextDataset](sc) {

  override def apply(variants: VariantContextDataset): VariantContextDataset = {

    val builder = CommandBuilders.create(args.useDocker, args.useSingularity)
      .setExecutable(args.executable)
      .add("call")
      .add("--output-type")
      .add("v")

    Option(args.bcftoolsArgs).foreach(builder.add(_))

    if (args.useDocker || args.useSingularity) {
      builder
        .setImage(args.image)
        .setSudo(args.sudo)
    }

    info("Piping %s to bcftools with command: %s files: %s".format(
      variants, builder.build(), builder.getFiles()))

    implicit val tFormatter = VCFInFormatter
    implicit val uFormatter = new VCFOutFormatter(sc.hadoopConfiguration, stringency)

    variants.pipe[VariantContext, VariantContextProduct, VariantContextDataset, VCFInFormatter](
      cmd = builder.build(),
      files = builder.getFiles()
    )
  }
}
