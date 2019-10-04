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
import org.bdgenomics.adam.rdd.read.{ AlignmentRecordDataset, BAMInFormatter, AnySAMOutFormatter }
import org.bdgenomics.adam.sql.{ AlignmentRecord => AlignmentRecordProduct }
import org.bdgenomics.cannoli.builder.CommandBuilders
import org.bdgenomics.formats.avro.AlignmentRecord
import org.bdgenomics.utils.cli._
import org.kohsuke.args4j.{ Option => Args4jOption }
import scala.collection.JavaConversions._

/**
 * Sambamba markdup function arguments.
 */
class SambambaMarkdupArgs extends Args4jBase {
  @Args4jOption(required = false, name = "-executable", usage = "Path to the Sambamba executable. Defaults to sambamba.")
  var executable: String = "sambamba"

  @Args4jOption(required = false, name = "-image", usage = "Container image to use. Defaults to quay.io/biocontainers/sambamba:0.7.0--h89e63da_1.")
  var image: String = "quay.io/biocontainers/sambamba:0.7.0--h89e63da_1"

  @Args4jOption(required = false, name = "-remove_duplicates", usage = "Remove duplicates instead of marking them.")
  var removeDuplicates: Boolean = false

  @Args4jOption(required = false, name = "-sudo", usage = "Run via sudo.")
  var sudo: Boolean = false

  @Args4jOption(required = false, name = "-use_docker", usage = "If true, uses Docker to launch Sambamba markdup.")
  var useDocker: Boolean = false

  @Args4jOption(required = false, name = "-use_singularity", usage = "If true, uses Singularity to launch Sambamba markdup.")
  var useSingularity: Boolean = false
}

/**
 * Sambamba markdup wrapper as a function AlignmentRecordDataset &rarr; AlignmentRecordDataset,
 * for use in cannoli-shell or notebooks.
 *
 * @param args Sambamba markdup function arguments.
 * @param sc Spark context.
 */
class SambambaMarkdup(
    val args: SambambaMarkdupArgs,
    sc: SparkContext) extends CannoliFn[AlignmentRecordDataset, AlignmentRecordDataset](sc) {

  override def apply(alignments: AlignmentRecordDataset): AlignmentRecordDataset = {

    val builder = CommandBuilders.create(args.useDocker, args.useSingularity)
      .setExecutable(args.executable)
      .add("markdup")

    if (args.removeDuplicates)
      builder.add("-remove-duplicates")

    builder.add("/dev/stdin")
      .add("/dev/stdout")

    if (args.useDocker || args.useSingularity) {
      builder
        .setImage(args.image)
        .setSudo(args.sudo)
    }

    info("Piping %s to sambamba with command: %s files: %s".format(
      alignments, builder.build(), builder.getFiles()))

    implicit val tFormatter = BAMInFormatter
    implicit val uFormatter = new AnySAMOutFormatter

    alignments.pipe[AlignmentRecord, AlignmentRecordProduct, AlignmentRecordDataset, BAMInFormatter](
      cmd = builder.build(),
      files = builder.getFiles()
    )
  }
}
