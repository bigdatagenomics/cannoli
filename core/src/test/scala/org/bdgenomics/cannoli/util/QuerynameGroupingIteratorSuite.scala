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
package org.bdgenomics.cannoli.util

import org.bdgenomics.formats.avro.AlignmentRecord
import org.scalatest.FunSuite
import scala.collection.JavaConversions._

class QuerynameGroupingIteratorSuite extends FunSuite {

  test("can't call next when empty") {
    val iter = new QuerynameGroupingIterator(Iterator.empty)
    assert(!iter.hasNext)
    intercept[IllegalStateException] {
      iter.next
    }
  }

  test("if no paired reads, return fragments of single reads") {
    val iter = new QuerynameGroupingIterator(Iterator("read1", "read2", "read3")
      .map(readName => AlignmentRecord.newBuilder
        .setReadName(readName)
        .build))
    val fragments = iter.toSeq
    assert(fragments.size === 3)
    val readNames = fragments.flatMap(_.getAlignments.map(r => r.getReadName))
    assert(readNames.size === 3)
    assert(readNames.count(_ == "read1") === 1)
    assert(readNames.count(_ == "read2") === 1)
    assert(readNames.count(_ == "read3") === 1)
  }

  test("return fragments on iterator with paired reads") {
    val iter = new QuerynameGroupingIterator(Iterator(AlignmentRecord.newBuilder
      .setReadName("read1")
      .build, AlignmentRecord.newBuilder
      .setReadName("read2")
      .setReadInFragment(0)
      .setInferredInsertSize(100L)
      .build, AlignmentRecord.newBuilder
      .setReadName("read2")
      .setReadInFragment(1)
      .setInferredInsertSize(100L)
      .build, AlignmentRecord.newBuilder
      .setReadName("read3")
      .setReadInFragment(0)
      .build, AlignmentRecord.newBuilder
      .setReadName("read3")
      .setReadInFragment(1)
      .build, AlignmentRecord.newBuilder
      .setReadName("read3")
      .setReadInFragment(2)
      .build))
    val fragments = iter.toSeq
    assert(fragments.size === 3)
    val readNames = fragments.flatMap(_.getAlignments.map(r => r.getReadName))
    assert(readNames.size === 6)
    assert(readNames.count(_ == "read1") === 1)
    assert(readNames.count(_ == "read2") === 2)
    assert(readNames.count(_ == "read3") === 3)
    val read2Opt = fragments.find(_.getName == "read2")
    assert(read2Opt.isDefined)
    read2Opt.foreach(r => assert(r.getInsertSize === 100))
  }
}
