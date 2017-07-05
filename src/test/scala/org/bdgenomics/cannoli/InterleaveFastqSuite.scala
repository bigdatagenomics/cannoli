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

import htsjdk.tribble.readers.{
  AsciiLineReader,
  AsciiLineReaderIterator
}
import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.compress.{
  BZip2Codec,
  CompressionCodec,
  GzipCodec
}
import org.bdgenomics.adam.rdd.ADAMContext._
import org.bdgenomics.utils.misc.SparkFunSuite

class InterleaveFastqSuite extends SparkFunSuite {

  sparkTest("interleave two paired FASTQ files") {
    val file1 = testFile("fastq_sample1_1.fq")
    val file2 = testFile("fastq_sample1_2.fq")
    val interleavedFile = testFile("interleaved_fastq_sample1.ifq")
    val outputFile = tmpFile("test.ifq")

    InterleaveFastq(Array(file1, file2, outputFile)).run(sc)

    checkFiles(outputFile, interleavedFile)
  }

  sparkTest("interleave two paired FASTQ files and save as BAM") {
    val file1 = testFile("fastq_sample1_1.fq")
    val file2 = testFile("fastq_sample1_2.fq")
    val outputFile = tmpFile("test.bam")

    InterleaveFastq(Array(file1, file2, outputFile, "-as_bam")).run(sc)

    val fragments = sc.loadFragments(outputFile).rdd.collect
    assert(fragments.length === 6)
    /*
     * see: https://github.com/bigdatagenomics/adam/issues/1530
     * assert(fragments.length === 3)
     * assert(fragments.forall(f => f.getAlignments.size == 2))
     */
  }

  def checkCodecAndLines(fileName: String,
                         codec: CompressionCodec,
                         records: Int) {
    val path = new Path(fileName)
    val fs = path.getFileSystem(sc.hadoopConfiguration)
    val is = fs.open(path)
    val cIs = codec.createInputStream(is)
    val iter = new AsciiLineReaderIterator(new AsciiLineReader(cIs))
    var linesRead = 0
    while (iter.hasNext) {
      iter.next()
      linesRead += 1
    }
    // four lines per fastq record
    assert((4 * records) === linesRead)
  }

  sparkTest("interleave gzipped and bzipped files") {
    val file1 = testFile("fastq_sample1_1.fq")
    val file2 = testFile("fastq_sample1_2.fq")
    val outputFile1 = tmpFile("test.ifq.gz")
    val outputFile2 = tmpFile("test.ifq.bz2")
    val outputFile3 = tmpFile("double.ifq")
    val doublyInterleavedFile = testFile("doubly_interleaved_fastq.ifq")

    // first, interleave to gzip
    InterleaveFastq(Array(file1, file2, outputFile1)).run(sc)
    val gzipCodec = new GzipCodec()
    gzipCodec.setConf(sc.hadoopConfiguration)
    checkCodecAndLines(outputFile1, gzipCodec, 6)

    // then interleave to bzip2
    InterleaveFastq(Array(file1, file2, outputFile2)).run(sc)
    val bzipCodec = new BZip2Codec()
    bzipCodec.setConf(sc.hadoopConfiguration)
    checkCodecAndLines(outputFile2, bzipCodec, 6)

    // then interleave the two zipped files
    InterleaveFastq(Array(outputFile1, outputFile2, outputFile3)).run(sc)

    checkFiles(outputFile3, doublyInterleavedFile)
  }
}
