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

import org.apache.spark.rdd.RDD
import org.bdgenomics.formats.avro.{ AlignmentRecord, Fragment }
import scala.collection.JavaConversions._

object AppendSuffixes {

  def apply(rdd: RDD[Fragment]): RDD[Fragment] = {
    rdd.map(addSuffixesToFragment)
  }

  def addSuffixesToFragment(fragment: Fragment): Fragment = {
    Fragment.newBuilder(fragment)
      .setAlignments(fragment.getAlignments.map(addSuffixToRead))
      .build
  }

  def addSuffixToRead(read: AlignmentRecord): AlignmentRecord = {
    require(read.getReadInFragment != null,
      "Cannot process read with null read in fragment number (%s)".format(read))
    require(read.getReadInFragment >= 0,
      "Read in fragment must be positive (%s)".format(read))

    val suffix = "/%d".format(read.getReadInFragment + 1)

    // don't modify the read if it already is suffixed properly
    if (read.getReadName.endsWith(suffix)) {
      println("no new name: %s%s".format(read.getReadName, suffix))
      read
    } else {
      println("new name: %s%s".format(read.getReadName, suffix))
      AlignmentRecord.newBuilder(read)
        .setReadName("%s%s".format(read.getReadName, suffix))
        .build
    }
  }
}
