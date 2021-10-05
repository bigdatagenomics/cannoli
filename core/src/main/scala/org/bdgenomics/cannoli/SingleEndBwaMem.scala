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
import org.bdgenomics.adam.ds.read.{
  AlignmentDataset,
  AnySAMOutFormatter,
  FASTQInFormatter
}
import org.bdgenomics.adam.models.ReadGroupDictionary
import org.bdgenomics.adam.sql.{ Alignment => AlignmentProduct }
import org.bdgenomics.cannoli.builder.CommandBuilders
import org.bdgenomics.formats.avro.Alignment
import scala.collection.JavaConversions._

/**
 * Bwa mem wrapper as a function AlignmentDataset &rarr; AlignmentDataset,
 * for use in cannoli-shell or notebooks.
 *
 * @param args Bwa mem function arguments.
 * @param sc Spark context.
 */
class SingleEndBwaMem(
    val args: BwaMemArgs,
    sc: SparkContext) extends CannoliFn[AlignmentDataset, AlignmentDataset](sc) {

  override def apply(reads: AlignmentDataset): AlignmentDataset = {

    val requiredExtensions = Seq("",
      ".amb",
      ".ann",
      ".bwt",
      ".pac",
      ".sa")
    val optionalExtensions = Seq(".alt")

    val readGroup = Option(args.readGroup).getOrElse(args.createReadGroup)

    val builder = CommandBuilders.create(args.useDocker, args.useSingularity)
      .setExecutable(args.executable)
      .add("mem")
      .add("-t")
      .add("1")
      .add("-R")
      .add(readGroup.toSAMReadGroupRecord().getSAMString().replace("\t", "\\t"))
      .add(if (args.addFiles) "$0" else args.indexPath)
      .add("-")

    if (args.addFiles) {
      indexPaths(args.indexPath, requiredExtensions, optionalExtensions).foreach(builder.addFile(_))
    }

    if (args.useDocker || args.useSingularity) {
      builder
        .setImage(args.image)
        .setSudo(args.sudo)
        .addMount(if (args.addFiles) "$root" else root(args.indexPath))
    }

    info("Piping %s to bwa mem with command: %s files: %s".format(
      reads, builder.build(), builder.getFiles()))

    implicit val tFormatter = FASTQInFormatter
    implicit val uFormatter = new AnySAMOutFormatter

    val alignments = reads.pipe[Alignment, AlignmentProduct, AlignmentDataset, FASTQInFormatter](
      cmd = builder.build(),
      files = builder.getFiles()
    )

    alignments.replaceReadGroups(ReadGroupDictionary(Seq(readGroup)))
  }
}
