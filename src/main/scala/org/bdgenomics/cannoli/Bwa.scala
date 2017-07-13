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

import org.apache.hadoop.fs.{ FileSystem, Path }
import org.apache.spark.SparkContext
import org.bdgenomics.adam.models.{
  RecordGroup,
  RecordGroupDictionary
}
import org.bdgenomics.adam.rdd.ADAMContext._
import org.bdgenomics.adam.rdd.ADAMSaveAnyArgs
import org.bdgenomics.adam.rdd.fragment.{ FragmentRDD, InterleavedFASTQInFormatter }
import org.bdgenomics.adam.rdd.read.{ AlignmentRecordRDD, AnySAMOutFormatter }
import org.bdgenomics.cannoli.util.QuerynameGrouper
import org.bdgenomics.formats.avro.AlignmentRecord
import org.bdgenomics.utils.cli._
import org.bdgenomics.utils.misc.Logging
import org.kohsuke.args4j.{ Argument, Option => Args4jOption }

object Bwa extends BDGCommandCompanion {
  val commandName = "bwa"
  val commandDescription = "ADAM Pipe API wrapper for BWA."

  def apply(cmdLine: Array[String]) = {
    new Bwa(Args4j[BwaArgs](cmdLine))
  }
}

class BwaArgs extends Args4jBase with ADAMSaveAnyArgs with ParquetArgs {
  @Argument(required = true, metaVar = "INPUT", usage = "Location to pipe from, in interleaved FASTQ format.", index = 0)
  var inputPath: String = null

  @Argument(required = true, metaVar = "OUTPUT", usage = "Location to pipe to.", index = 1)
  var outputPath: String = null

  @Argument(required = true, metaVar = "SAMPLE", usage = "Sample ID.", index = 2)
  var sample: String = null

  @Args4jOption(required = true, name = "-index", usage = "Path to the bwa index to be searched, e.g. <ebwt> in bwa [options]* <ebwt> ...")
  var indexPath: String = null

  @Args4jOption(required = false, name = "-single", usage = "Saves OUTPUT as single file. Exclusive of -fragments.")
  var asSingleFile: Boolean = false

  @Args4jOption(required = false, name = "-fragments", usage = "Saves OUTPUT as Fragments in Parquet. Exclusive of -single.")
  var asFragments: Boolean = false

  @Args4jOption(required = false, name = "-defer_merging", usage = "Defers merging single file output.")
  var deferMerging: Boolean = false

  @Args4jOption(required = false, name = "-disable_fast_concat", usage = "Disables the parallel file concatenation engine.")
  var disableFastConcat: Boolean = false

  @Args4jOption(required = false, name = "-bwa_path", usage = "Path to the BWA executable. Defaults to bwa.")
  var bwaPath: String = "bwa"

  @Args4jOption(required = false, name = "-sequence_dictionary", usage = "Path to the sequence dictionary.")
  var sequenceDictionary: String = _

  @Args4jOption(required = false, name = "-docker_image", usage = "Docker image to use. Defaults to quay.io/ucsc_cgl/bwa:0.7.12--256539928ea162949d8a65ca5c79a72ef557ce7c.")
  var dockerImage: String = "quay.io/ucsc_cgl/bwa:0.7.12--256539928ea162949d8a65ca5c79a72ef557ce7c"

  @Args4jOption(required = false, name = "-use_docker", usage = "If true, uses Docker to launch BWA. If false, uses the BWA executable path.")
  var useDocker: Boolean = false

  @Args4jOption(required = false, name = "-docker_cmd", usage = "The docker command to run. Defaults to 'docker'.")
  var dockerCmd: String = "docker"

  @Args4jOption(required = false, name = "-force_load_ifastq", usage = "Forces loading using interleaved FASTQ.")
  var forceLoadIfastq: Boolean = false

  @Args4jOption(required = false, name = "-force_load_parquet", usage = "Forces loading using Parquet.")
  var forceLoadParquet: Boolean = false

  @Args4jOption(required = false, name = "-add_indices", usage = "Adds index files via SparkFiles mechanism.")
  var addIndices: Boolean = false

  // must be defined due to ADAMSaveAnyArgs, but unused here
  var sortFastqOutput: Boolean = false
}

/**
 * Bwa.
 */
class Bwa(protected val args: BwaArgs) extends BDGSparkCommand[BwaArgs] with Logging {
  val companion = Bwa

  def run(sc: SparkContext) {
    require(!(args.asSingleFile && args.asFragments),
      "-single and -fragments are mutually exclusive.")
    require(!(args.forceLoadIfastq && args.forceLoadParquet),
      "-force_load_ifastq and -force_load_parquet are mutually exclusive.")
    val input: FragmentRDD = if (args.forceLoadIfastq) {
      sc.loadInterleavedFastqAsFragments(args.inputPath)
    } else if (args.forceLoadParquet) {
      sc.loadParquetFragments(args.inputPath)
    } else {
      sc.loadFragments(args.inputPath)
    }

    implicit val tFormatter = InterleavedFASTQInFormatter
    implicit val uFormatter = new AnySAMOutFormatter

    val sample = args.sample

    def getIndexPaths(fastaPath: String): Seq[String] = {
      val requiredExtensions = Seq("",
        ".amb",
        ".ann",
        ".bwt",
        ".pac",
        ".sa")
      val optionalExtensions = Seq(".alt")

      // oddly, the hadoop fs apis don't seem to have a way to do this?
      def canonicalizePath(fs: FileSystem, path: Path): String = {
        val fsUri = fs.getUri()
        new Path(fsUri.getScheme, fsUri.getAuthority,
          Path.getPathWithoutSchemeAndAuthority(path).toString).toString
      }

      def optionalPath(ext: String): Option[String] = {
        val path = new Path(fastaPath, ext)
        val fs = path.getFileSystem(sc.hadoopConfiguration)
        if (fs.exists(path)) {
          Some(canonicalizePath(fs, path))
        } else {
          None
        }
      }

      val pathsWithScheme = requiredExtensions.map(ext => {
        optionalPath(ext).getOrElse({
          throw new IllegalStateException(
            "Required index file %s%s does not exist.".format(fastaPath, ext))
        })
      })

      val optionalPathsWithScheme = optionalExtensions.flatMap(optionalPath)

      pathsWithScheme ++ optionalPathsWithScheme
    }

    val (filesToAdd, bwaCommand) = if (args.useDocker) {
      val (mountpoint, indexPath, filesToMount) = if (args.addIndices) {
        ("$root", "$0", getIndexPaths(args.indexPath))
      } else {
        (Path.getPathWithoutSchemeAndAuthority(new Path(args.indexPath).getParent()).toString,
          args.indexPath,
          Seq.empty)
      }

      (filesToMount, Seq(args.dockerCmd,
        "-v", "%s:%s".format(mountpoint, mountpoint),
        "run",
        args.dockerImage,
        "mem",
        "-t", "1",
        "-R", s"@RG\\tID:${sample}\\tLB:${sample}\\tPL:ILLUMINA\\tPU:0\\tSM:${sample}",
        "-p",
        indexPath,
        "-").mkString(" "))
    } else {
      val (indexPath, filesToMount) = if (args.addIndices) {
        ("$0", getIndexPaths(args.indexPath))
      } else {
        (args.indexPath, Seq.empty)
      }

      (filesToMount, Seq(args.bwaPath,
        "mem",
        "-t", "1",
        "-R", s"@RG\\tID:${sample}\\tLB:${sample}\\tPL:ILLUMINA\\tPU:0\\tSM:${sample}",
        "-p",
        args.indexPath,
        "-").mkString(" "))
    }

    val output: AlignmentRecordRDD = input.pipe[AlignmentRecord, AlignmentRecordRDD, InterleavedFASTQInFormatter](bwaCommand)
      .replaceRecordGroups(RecordGroupDictionary(Seq(RecordGroup(sample, sample))))

    val outputMaybeWithSequences = Option(args.sequenceDictionary).fold(output)(sdPath => {
      val sequences = sc.loadSequenceDictionary(sdPath)
      output.replaceSequences(sequences)
    })

    if (!args.asFragments) {
      outputMaybeWithSequences.save(args)
    } else {
      log.info("Converting to fragments.")
      QuerynameGrouper(outputMaybeWithSequences)
        .saveAsParquet(args)
    }
  }
}
