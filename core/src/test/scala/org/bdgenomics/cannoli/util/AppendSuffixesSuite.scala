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

import org.scalatest.FunSuite
import org.bdgenomics.formats.avro.{ AlignmentRecord, Fragment }
import scala.collection.JavaConversions._

class AppendSuffixesSuite extends FunSuite {

  test("add a suffix to a read that has no suffix") {
    val read = AlignmentRecord.newBuilder
      .setReadName("myRead")
      .setReadInFragment(0)
      .build
    val suffixedRead = AppendSuffixes.addSuffixToRead(read)
    assert(suffixedRead.getReadName === "myRead/1")
  }

  test("don't add a suffix to a read that has a suffix") {
    val read = AlignmentRecord.newBuilder
      .setReadName("myRead/2")
      .setReadInFragment(1)
      .build
    val suffixedRead = AppendSuffixes.addSuffixToRead(read)
    assert(suffixedRead.getReadName === "myRead/2")
  }

  test("read number must exist") {
    val read = AlignmentRecord.newBuilder
      .setReadName("myRead")
      .setReadInFragment(null)
      .build
    intercept[IllegalArgumentException] {
      AppendSuffixes.addSuffixToRead(read)
    }
  }

  test("read number must be positive") {
    val read = AlignmentRecord.newBuilder
      .setReadName("myRead")
      .setReadInFragment(-1)
      .build
    intercept[IllegalArgumentException] {
      AppendSuffixes.addSuffixToRead(read)
    }
  }

  test("add suffixes to fragment") {
    val fragment = Fragment.newBuilder
      .setAlignments(Seq(AlignmentRecord.newBuilder
        .setReadName("myRead/1")
        .setReadInFragment(0)
        .build, AlignmentRecord.newBuilder
        .setReadName("myRead")
        .setReadInFragment(1)
        .build)).build

    val suffixedFragment = AppendSuffixes.addSuffixesToFragment(fragment)

    val readNames = suffixedFragment.getAlignments
      .map(_.getReadName)
      .toSet
    assert(readNames("myRead/1"))
    assert(readNames("myRead/2"))
  }
}
