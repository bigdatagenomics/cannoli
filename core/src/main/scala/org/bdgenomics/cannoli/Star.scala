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
import org.bdgenomics.adam.ds.ADAMContext._
import org.bdgenomics.adam.ds.fragment.FragmentDataset
import org.bdgenomics.adam.ds.read.{ AlignmentDataset, AnySAMOutFormatter, SAMInFormatter }
import org.bdgenomics.adam.sql.{ Alignment => AlignmentProduct }
import org.bdgenomics.cannoli.builder.CommandBuilders
import org.bdgenomics.formats.avro.Alignment
import org.bdgenomics.utils.cli._
import org.kohsuke.args4j.{ Option => Args4jOption }
import scala.collection.JavaConversions._

/**
 * STAR-Mapper function arguments.
 */
class StarArgs extends Args4jBase {
  @Args4jOption(required = false, name = "-executable", usage = "Path to the STAR-Mapper executable. Defaults to STAR.")
  var executable: String = "STAR"

  @Args4jOption(required = false, name = "-image", usage = "Container image to use. Defaults to quay.io/biocontainers/star:2.7.5c--0.")
  var image: String = "quay.io/biocontainers/star:2.7.5c--0"

  @Args4jOption(required = false, name = "-sudo", usage = "Run via sudo.")
  var sudo: Boolean = false

  @Args4jOption(required = false, name = "-add_files", usage = "If true, use the SparkFiles mechanism to distribute files to executors.")
  var addFiles: Boolean = false

  @Args4jOption(required = false, name = "-use_docker", usage = "If true, uses Docker to launch STAR-Mapper.")
  var useDocker: Boolean = false

  @Args4jOption(required = false, name = "-use_singularity", usage = "If true, uses Singularity to launch STAR-Mapper.")
  var useSingularity: Boolean = false

  @Args4jOption(required = true, name = "-genome", usage = "Path to the directory where the STAR-Mapper genome files are stored.")
  var genomePath: String = null

  @Args4jOption(required = false, name = "-star_args", usage = "Additional arguments for STAR-Mapper, must be double-quoted, e.g. -star_args \"--outSAMmultNmax 4 --clip5pNbases 12\"")
  var starArgs: String = null

  @Args4jOption(required = false, name = "-seed", usage = "Random number generator seed. Defaults to 42.")
  var seed: Integer = 42
}

/**
 * STAR-Mapper wrapper as a function FragmentDataset &rarr; AlignmentDataset,
 * for use in cannoli-shell or notebooks.
 *
 * @param args STAR-Mapper function arguments.
 * @param sc Spark context.
 */
class Star(
    val args: StarArgs,
    sc: SparkContext) extends CannoliFn[FragmentDataset, AlignmentDataset](sc) {

  override def apply(fragments: FragmentDataset): AlignmentDataset = {

    val builder = CommandBuilders.create(args.useDocker, args.useSingularity)
      .setExecutable(args.executable)
      .add("--runMode")
      .add("alignReads")
      .add("--readFilesIn")
      .add("/dev/stdin")
      .add("--readFilesType")
      .add("SAM")
      .add("PE")
      .add("--outStd")
      .add("BAM_Unsorted")
      .add("--outSAMtype")
      .add("BAM")
      .add("Unsorted")
      .add("--outSAMunmapped")
      .add("Within")
      .add("--runRNGseed")
      .add(args.seed.toString)
      .add("--genomeDir")
      .add(if (args.addFiles) "$0" else absolute(args.genomePath))

    Option(args.starArgs).foreach(builder.add(_))

    if (args.addFiles) {
      // add args.genomePath for "$0"
      builder.addFile(args.genomePath)
      // add STAR genome files via globbed genome path
      builder.addFiles(files(args.genomePath + "**"))
    }

    if (args.useDocker || args.useSingularity) {
      builder
        .setImage(args.image)
        .setSudo(args.sudo)
        .addMount(if (args.addFiles) "$root" else root(args.genomePath))
    }

    info("Piping %s to STAR-Mapper with command: %s files: %s".format(
      fragments, builder.build(), builder.getFiles()))

    implicit val tFormatter = SAMInFormatter
    implicit val uFormatter = new AnySAMOutFormatter

    fragments
      .toAlignments
      .pipe[Alignment, AlignmentProduct, AlignmentDataset, SAMInFormatter](
        cmd = builder.build(),
        files = builder.getFiles()
      )
  }
}
