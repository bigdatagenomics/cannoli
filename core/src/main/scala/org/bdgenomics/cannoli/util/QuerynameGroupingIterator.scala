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

import org.bdgenomics.formats.avro.{ AlignmentRecord, Fragment }
import scala.annotation.tailrec
import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

private[util] class QuerynameGroupingIterator(
    iter: Iterator[AlignmentRecord]) extends Iterator[Fragment] {

  private val bufferedIter = iter.buffered

  def hasNext: Boolean = {
    bufferedIter.hasNext
  }

  def next: Fragment = {
    if (!bufferedIter.hasNext) {
      throw new IllegalStateException("Next called on empty iterator.")
    }

    val readsInFragment = ListBuffer.empty[AlignmentRecord]
    val readName = bufferedIter.head.getReadName
    val optInsertSize = Option(bufferedIter.head.getInferredInsertSize)
      .map(_.toInt)

    @tailrec def fillList() {
      if (bufferedIter.hasNext) {
        if (bufferedIter.head.getReadName == readName) {
          readsInFragment += bufferedIter.next
          fillList()
        }
      }
    }

    // pull reads from the iterator to fill up the fragment
    fillList()

    val fb = Fragment.newBuilder
      .setName(readName)
      .setAlignments(readsInFragment)

    // set the insert size if known
    optInsertSize.foreach(fs => fb.setInsertSize(fs))

    fb.build
  }
}
