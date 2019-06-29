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
import org.bdgenomics.adam.rdd.fragment.{ FragmentDataset, InterleavedFASTQInFormatter }
import org.bdgenomics.adam.rdd.read.{ AlignmentRecordDataset, AnySAMOutFormatter }
import org.bdgenomics.adam.sql.{ AlignmentRecord => AlignmentRecordProduct }
import org.bdgenomics.cannoli.builder.CommandBuilders
import org.bdgenomics.formats.avro.AlignmentRecord
import org.bdgenomics.utils.cli._
import org.bdgenomics.utils.misc.Logging
import org.kohsuke.args4j.{ Option => Args4jOption }
import scala.collection.JavaConversions._

/**
 * GEM-Mapper function arguments.
 */
class GemArgs extends Args4jBase {
  @Args4jOption(required = false, name = "-executable", usage = "Path to the GEM-Mapper executable. Defaults to gem-mapper.")
  var executable: String = "gem-mapper"

  @Args4jOption(required = false, name = "-image", usage = "Container image to use. Defaults to quay.io/biocontainers/gem3-mapper:3.6.1--h98de208_7.")
  var image: String = "quay.io/biocontainers/gem3-mapper:3.6.1--h98de208_7"

  @Args4jOption(required = false, name = "-sudo", usage = "Run via sudo.")
  var sudo: Boolean = false

  @Args4jOption(required = false, name = "-add_files", usage = "If true, use the SparkFiles mechanism to distribute files to executors.")
  var addFiles: Boolean = false

  @Args4jOption(required = false, name = "-use_docker", usage = "If true, uses Docker to launch GEM-Mapper.")
  var useDocker: Boolean = false

  @Args4jOption(required = false, name = "-use_singularity", usage = "If true, uses Singularity to launch GEM-Mapper.")
  var useSingularity: Boolean = false

  @Args4jOption(required = true, name = "-index", usage = "GEM-Mapper index path, e.g. hsapiens_v37.gem.")
  var indexPath: String = null
}

/**
 * GEM-Mapper wrapper as a function FragmentDataset &rarr; AlignmentRecordDataset,
 * for use in cannoli-shell or notebooks.
 *
 * @param args GEM-Mapper function arguments.
 * @param sc Spark context.
 */
class Gem(
    val args: GemArgs,
    sc: SparkContext) extends CannoliFn[FragmentDataset, AlignmentRecordDataset](sc) with Logging {

  override def apply(fragments: FragmentDataset): AlignmentRecordDataset = {

    val builder = CommandBuilders.create(args.useDocker, args.useSingularity)
      .setExecutable(args.executable)
      .add("-I")
      .add(if (args.addFiles) "$0" else absolute(args.indexPath))
      .add("--paired-end-alignment")

    if (args.addFiles) {
      builder.addFile(args.indexPath)
    }

    if (args.useDocker || args.useSingularity) {
      builder
        .setImage(args.image)
        .setSudo(args.sudo)
        .addMount(if (args.addFiles) "$root" else root(args.indexPath))
    }

    log.info("Piping {} to GEM-Mapper with command: {} files: {}",
      fragments, builder.build(), builder.getFiles())

    implicit val tFormatter = InterleavedFASTQInFormatter
    implicit val uFormatter = new AnySAMOutFormatter

    fragments.pipe[AlignmentRecord, AlignmentRecordProduct, AlignmentRecordDataset, InterleavedFASTQInFormatter](
      cmd = builder.build(),
      files = builder.getFiles()
    )
  }
}
