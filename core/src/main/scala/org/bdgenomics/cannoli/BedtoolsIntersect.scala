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

import org.apache.spark.SparkContext
import org.bdgenomics.adam.rdd.ADAMContext._
import org.bdgenomics.adam.rdd.feature.{
  FeatureRDD,
  BEDInFormatter,
  BEDOutFormatter
}
import org.bdgenomics.adam.sql.{ Feature => FeatureProduct }
import org.bdgenomics.cannoli.builder.CommandBuilders
import org.bdgenomics.formats.avro.Feature;
import org.bdgenomics.utils.cli._
import org.bdgenomics.utils.misc.Logging
import org.kohsuke.args4j.{ Option => Args4jOption }
import scala.collection.JavaConversions._

/**
 * Bedtools intersect function arguments.
 */
class BedtoolsIntersectArgs extends Args4jBase {
  @Args4jOption(required = false, name = "-a", usage = "Bedtools intersect -a option. One of {-a,-b} should be left unspecified to accept piped input.")
  var a: String = null

  @Args4jOption(required = false, name = "-b", usage = "Bedtools intersect -b option. One of {-a,-b} should be left unspecified to accept piped input.")
  var b: String = null

  @Args4jOption(required = false, name = "-sorted", usage = "Bedtools intersect -sorted option. Inputs must be sorted by chromosome and then by start position.")
  var sorted: Boolean = false

  @Args4jOption(required = false, name = "-executable", usage = "Path to the Bedtools executable. Defaults to bedtools.")
  var executable: String = "bedtools"

  @Args4jOption(required = false, name = "-image", usage = "Container image to use. Defaults to quay.io/biocontainers/bedtools:2.27.1--he941832_2.")
  var image: String = "quay.io/biocontainers/bedtools:2.27.1--he941832_2"

  @Args4jOption(required = false, name = "-sudo", usage = "Run via sudo.")
  var sudo: Boolean = false

  @Args4jOption(required = false, name = "-add_files", usage = "If true, use the SparkFiles mechanism to distribute files to executors.")
  var addFiles: Boolean = false

  @Args4jOption(required = false, name = "-use_docker", usage = "If true, uses Docker to launch Bedtools.")
  var useDocker: Boolean = false

  @Args4jOption(required = false, name = "-use_singularity", usage = "If true, uses Singularity to launch Bedtools.")
  var useSingularity: Boolean = false
}

/**
 * Bedtools intersect wrapper as a function FeatureRDD &rarr; FeatureRDD,
 * for use in cannoli-shell or notebooks.
 *
 * <code>
 * val args = new BedtoolsIntersectFnArgs()
 * args.b = "foo.bed"
 * args.useDocker = true
 * val features = ...
 * val pipedFeatures = new BedtoolsIntersectFn(args, sc).apply(features)
 * </code>
 *
 * @param args Bedtools intersect function arguments.
 * @param sc Spark context.
 */
class BedtoolsIntersect(
    val args: BedtoolsIntersectArgs,
    sc: SparkContext) extends CannoliFn[FeatureRDD, FeatureRDD](sc) with Logging {

  override def apply(features: FeatureRDD): FeatureRDD = {
    val optA = Option(args.a)
    val optB = Option(args.b)
    require(optA.size + optB.size == 1,
      "Strictly one of {-a,-b} should be left unspecified to accept piped input.")

    val file = List(optA, optB).flatten.get(0)

    val builder = CommandBuilders.create(args.useDocker, args.useSingularity)
      .setExecutable(args.executable)
      .add("intersect")
      .add("-a")
      .add(optA.fold("stdin")(f => if (args.addFiles) "$0" else absolute(f)))
      .add("-b")
      .add(optB.fold("stdin")(f => if (args.addFiles) "$0" else absolute(f)))

    if (args.sorted) builder.add("-sorted")
    if (args.addFiles) builder.addFile(file)

    if (args.useDocker || args.useSingularity) {
      builder
        .setImage(args.image)
        .setSudo(args.sudo)
        .addMount(if (args.addFiles) "$root" else root(file))
    }

    log.info("Piping {} to bedtools with command: {} files: {}",
      features, builder.build(), builder.getFiles())

    implicit val tFormatter = BEDInFormatter
    implicit val uFormatter = new BEDOutFormatter

    features.pipe[Feature, FeatureProduct, FeatureRDD, BEDInFormatter](
      cmd = builder.build(),
      files = builder.getFiles()
    )
  }
}
