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
import org.bdgenomics.utils.cli._
import org.kohsuke.args4j.{ Option => Args4jOption }
import scala.collection.JavaConversions._

/**
 * Unimap wrapper as a function AlignmentDataset &rarr; AlignmentDataset,
 * for use in cannoli-shell or notebooks.
 *
 * @param args Unimap function arguments.
 * @param sc Spark context.
 */
class SingleEndUnimap(
    val args: UnimapArgs,
    sc: SparkContext) extends CannoliFn[AlignmentDataset, AlignmentDataset](sc) {

  override def apply(reads: AlignmentDataset): AlignmentDataset = {

    val readGroup = Option(args.readGroup).getOrElse(args.createReadGroup)

    val builder = CommandBuilders.create(args.useDocker, args.useSingularity)
      .setExecutable(args.executable)
      .add("-a")
      .add("-x")
      .add(args.preset)
      .add("--seed")
      .add(args.seed.toString)
      .add("-R")
      .add(readGroup.toSAMReadGroupRecord().getSAMString().replace("\t", "\\t"))

    Option(args.unimapArgs).foreach(builder.add(_))

    builder
      .add(if (args.addFiles) "$0" else absolute(args.indexPath))
      .add("-")

    if (args.addFiles) builder.addFile(args.indexPath)

    if (args.useDocker || args.useSingularity) {
      builder
        .setImage(args.image)
        .setSudo(args.sudo)
        .addMount(if (args.addFiles) "$root" else root(args.indexPath))
    }

    info("Piping %s to unimap with command: %s files: %s".format(
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
