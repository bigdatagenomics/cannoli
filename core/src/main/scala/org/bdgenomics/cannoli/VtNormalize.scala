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
 * Vt normalize function arguments.
 */
class VtNormalizeArgs extends Args4jBase {
  @Args4jOption(required = false, name = "-executable", usage = "Path to the vt executable. Defaults to vt.")
  var executable: String = "vt"

  @Args4jOption(required = false, name = "-image", usage = "Container image to use. Defaults to heuermh/vt.")
  var image: String = "heuermh/vt"

  @Args4jOption(required = false, name = "-sudo", usage = "Run via sudo.")
  var sudo: Boolean = false

  @Args4jOption(required = false, name = "-add_files", usage = "If true, use the SparkFiles mechanism to distribute files to executors.")
  var addFiles: Boolean = false

  @Args4jOption(required = false, name = "-use_docker", usage = "If true, uses Docker to launch vt.")
  var useDocker: Boolean = false

  @Args4jOption(required = false, name = "-use_singularity", usage = "If true, uses Singularity to launch vt.")
  var useSingularity: Boolean = false

  @Args4jOption(required = true, name = "-reference", usage = "Reference sequence for analysis.")
  var referencePath: String = null

  @Args4jOption(required = false, name = "-window", usage = "Window size for local sorting of variants. Defaults to 10000.")
  var window: Int = _
}

/**
 * Vt normalize wrapper as a function VariantContextRDD &rarr; VariantContextRDD,
 * for use in cannoli-shell or notebooks.
 *
 * @param args Vt normalize function arguments.
 * @param stringency Validation stringency. Defaults to ValidationStringency.LENIENT.
 * @param sc Spark context.
 */
class VtNormalize(
    val args: VtNormalizeArgs,
    val stringency: ValidationStringency = ValidationStringency.LENIENT,
    sc: SparkContext) extends CannoliFn[VariantContextRDD, VariantContextRDD](sc) with Logging {

  override def apply(variantContexts: VariantContextRDD): VariantContextRDD = {

    var builder = CommandBuilders.create(args.useDocker, args.useSingularity)
      .setExecutable(args.executable)
      .add("normalize")
      .add("-")
      .add("-r")
      .add(if (args.addFiles) "$0" else absolute(args.referencePath))

    Option(args.window).foreach(i => builder.add("-w").add(i.toString))

    if (args.addFiles) builder.addFile(args.referencePath)

    if (args.useDocker || args.useSingularity) {
      builder
        .setImage(args.image)
        .setSudo(args.sudo)
        .addMount(if (args.addFiles) "$root" else root(args.referencePath))
    }

    log.info("Piping {} to vt with command: {} files: {}",
      variantContexts, builder.build(), builder.getFiles())

    implicit val tFormatter = VCFInFormatter
    implicit val uFormatter = new VCFOutFormatter(sc.hadoopConfiguration, stringency)

    variantContexts.pipe[VariantContext, VariantContextProduct, VariantContextRDD, VCFInFormatter](
      cmd = builder.build(),
      files = builder.getFiles()
    )
  }
}
