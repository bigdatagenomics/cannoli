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

import htsjdk.samtools.{
  SAMFileHeader,
  SAMFileWriter,
  SAMFileWriterFactory,
  SAMRecord
}
import htsjdk.tribble.readers.{
  AsciiLineReader,
  AsciiLineReaderIterator,
  LineIterator
}
import java.io.{
  InputStream,
  OutputStream
}
import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.compress.CompressionCodecFactory
import org.apache.spark.SparkContext
import org.bdgenomics.utils.cli._
import org.bdgenomics.utils.misc.Logging
import org.kohsuke.args4j.{ Argument, Option => Args4jOption }
import scala.annotation.tailrec

object InterleaveFastq extends BDGCommandCompanion {
  val commandName = "interleaveFastq"
  val commandDescription = "Interleaves two FASTQ files."

  def apply(cmdLine: Array[String]) = {
    new InterleaveFastq(Args4j[InterleaveFastqArgs](cmdLine))
  }
}

class InterleaveFastqArgs extends Args4jBase {
  @Argument(required = true, metaVar = "INPUT1", usage = "First of pair reads in (possibly compressed) FASTQ", index = 0)
  var input1Path: String = null

  @Argument(required = true, metaVar = "INPUT2", usage = "Second of pair reads in (possibly compressed) FASTQ", index = 1)
  var input2Path: String = null

  @Argument(required = true, metaVar = "OUTPUT", usage = "Location to write interleaved FASTQ.", index = 2)
  var outputPath: String = null

  @Args4jOption(required = false, name = "-as_bam", usage = "Saves the output as BAM.")
  var asBam: Boolean = false
}

/**
 * Interleaves two FASTQ files.
 */
class InterleaveFastq(protected val args: InterleaveFastqArgs) extends BDGSparkCommand[InterleaveFastqArgs] with Logging {
  val companion = InterleaveFastq

  def run(sc: SparkContext) {

    val path1 = new Path(args.input1Path)
    val path2 = new Path(args.input2Path)
    val pathOut = new Path(args.outputPath)

    // open streams for reading/writing
    // there's no guarantee that everything is on the same file system
    // TRUST NO ONE, especially not ourselves
    val fs1 = path1.getFileSystem(sc.hadoopConfiguration)
    val fs2 = path2.getFileSystem(sc.hadoopConfiguration)
    val fsOut = pathOut.getFileSystem(sc.hadoopConfiguration)
    val pair1Is: InputStream = fs1.open(path1)
    val pair2Is: InputStream = fs2.open(path2)
    val os: OutputStream = fsOut.create(pathOut)
    // note: these types are required because the later Option.fold on the
    // compression codec causes type inference to fail

    // get compression codecs
    val codecFactory = new CompressionCodecFactory(sc.hadoopConfiguration)
    val pair1Codec = codecFactory.getCodec(path1)
    val pair2Codec = codecFactory.getCodec(path2)
    val outputCodec = codecFactory.getCodec(pathOut)

    // if codecs were non-null, create new streams
    val cIs1 = Option(pair1Codec).fold(pair1Is)(codec => {
      codec.createInputStream(pair1Is)
    })
    val cIs2 = Option(pair2Codec).fold(pair2Is)(codec => {
      codec.createInputStream(pair2Is)
    })
    val cOs = Option(outputCodec).fold(os)(codec => {
      codec.createOutputStream(os)
    })

    // wrap input streams in line readers
    val pair1Reader = new AsciiLineReaderIterator(new AsciiLineReader(cIs1))
    val pair2Reader = new AsciiLineReaderIterator(new AsciiLineReader(cIs2))

    // interleave the streams
    if (args.asBam) {
      val header = new SAMFileHeader()
      header.setGroupOrder(SAMFileHeader.GroupOrder.query)
      val bamWriter = new SAMFileWriterFactory()
        .makeBAMWriter(header, true, os)
      interleaveStreamsAsBam(pair1Reader, pair2Reader,
        bamWriter,
        header,
        new SAMRecord(header))
    } else {
      interleaveStreamsAsFastq(pair1Reader, pair2Reader, cOs)
    }
  }

  @tailrec private def interleaveStreamsAsBam(pair1Reader: LineIterator,
                                              pair2Reader: LineIterator,
                                              writer: SAMFileWriter,
                                              header: SAMFileHeader,
                                              record: SAMRecord,
                                              lineInPair: Int = 0,
                                              read1: Boolean = true) {
    if (!pair2Reader.hasNext) {
      assert(lineInPair == 0,
        "File ended in the middle of a FASTQ record.")
      writer.close()
    } else {

      val (nextLineInPair, nextRead1, nextRecord) = if (lineInPair == 3) {
        (0, !read1, new SAMRecord(header))
      } else {
        (lineInPair + 1, read1, record)
      }

      val fastqLine = if (read1) {
        assert(pair1Reader.hasNext, "File 1 ended before file 2.")
        assert(pair2Reader.hasNext, "File 2 ended before file 1.")
        pair1Reader.next
      } else {
        pair2Reader.next
      }

      if (lineInPair == 0) {
        // @readname
        record.setReadName(fastqLine.drop(1))
      } else if (lineInPair == 1) {
        // read sequence
        record.setReadString(fastqLine)
      } else if (lineInPair == 3) {
        // read qualities
        record.setBaseQualityString(fastqLine)

        // set other metadata
        record.setReadPairedFlag(true)
        record.setFirstOfPairFlag(read1)
        record.setSecondOfPairFlag(!read1)
        record.setReadUnmappedFlag(true)
        record.setMateUnmappedFlag(true)

        // and write!
        writer.addAlignment(record)
      } else {
        assert(lineInPair == 2,
          "Invalid line number (%d) in read.".format(lineInPair))
      }

      interleaveStreamsAsBam(pair1Reader, pair2Reader,
        writer, header, nextRecord,
        nextLineInPair,
        nextRead1)
    }
  }

  // java.io.OutputStream takes the lower 8 bytes of an int to write a byte
  // odd decision, but we'll play along
  private val newline = '\n'.toInt

  @tailrec private def interleaveStreamsAsFastq(pair1Reader: LineIterator,
                                                pair2Reader: LineIterator,
                                                os: OutputStream,
                                                lineInPair: Int = 0,
                                                read1: Boolean = true) {
    if (!pair2Reader.hasNext) {
      assert(lineInPair == 0,
        "File ended in the middle of a FASTQ record.")
      os.flush()
      os.close()
    } else {

      val (nextLineInPair, nextRead1) = if (lineInPair == 3) {
        (0, !read1)
      } else {
        (lineInPair + 1, read1)
      }

      val fastqLine = if (read1) {
        assert(pair1Reader.hasNext, "File 1 ended before file 2.")
        assert(pair2Reader.hasNext, "File 2 ended before file 1.")
        pair1Reader.next
      } else {
        pair2Reader.next
      }

      // line iterator strips the newline
      os.write(fastqLine.getBytes())
      os.write(newline)

      interleaveStreamsAsFastq(pair1Reader,
        pair2Reader,
        os,
        nextLineInPair,
        nextRead1)
    }
  }
}
