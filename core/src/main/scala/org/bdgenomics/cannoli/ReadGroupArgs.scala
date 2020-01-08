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

import org.bdgenomics.adam.models.{ ReadGroup, ReadGroupDictionary }
import org.bdgenomics.formats.avro.{ ReadGroup => ReadGroupMetadata }
import org.bdgenomics.utils.cli._
import org.kohsuke.args4j.{ Option => Args4jOption }
import scala.collection.JavaConversions._

/**
 * Abstract arguments that capture a read group.
 */
private[cannoli] class ReadGroupArgs extends Args4jBase {

  @Args4jOption(required = true, name = "-sample_id", usage = "Sample identifier for the read group. Required.")
  var sampleId: String = null

  @Args4jOption(required = false, name = "-flow_order", usage = "Read group flow order.")
  var flowOrder: String = null

  @Args4jOption(required = false, name = "-key_sequence", usage = "Read group key sequence.")
  var keySequence: String = null

  @Args4jOption(required = false, name = "-library", usage = "Read group library, defaults to -sample_id if not specified.")
  var library: String = null

  @Args4jOption(required = false, name = "-platform", usage = "Read group platform, defaults to ILLUMINA.")
  var platform: String = null

  @Args4jOption(required = false, name = "-platform_model", usage = "Read group platform model.")
  var platformModel: String = null

  @Args4jOption(required = false, name = "-platform_unit", usage = "Read group platform unit, defaults to 0.")
  var platformUnit: String = null

  @Args4jOption(required = false, name = "-predicted_median_insert_size", usage = "Read group predicted median insert size.")
  var predictedMedianInsertSize: java.lang.Integer = null

  @Args4jOption(required = false, name = "-read_group_id", usage = "Read group identifier, defaults to -sample_id if not specified.")
  var readGroupId: String = null

  @Args4jOption(required = false, name = "-run_date", usage = "Read group run date.")
  var runDate: java.lang.Long = null

  @Args4jOption(required = false, name = "-sequencing_center", usage = "Read group sequencing center.")
  var sequencingCenter: String = null

  /** Read group, to specify directly. */
  var readGroup: ReadGroup = null

  /**
   * Create a read group from the arguments in this class.
   *
   * @return a read group created from the arguments in this class.
   */
  def createReadGroup(): ReadGroup = {
    val builder = ReadGroupMetadata.newBuilder()
      .setId(Option(readGroupId).getOrElse(sampleId))
      .setLibrary(Option(library).getOrElse(sampleId))
      .setPlatform(Option(platform).getOrElse("ILLUMINA"))
      .setPlatformUnit(Option(platformUnit).getOrElse("0"))
      .setSampleId(sampleId)

    Option(flowOrder).foreach(builder.setFlowOrder)
    Option(runDate).foreach(builder.setRunDateEpoch)
    Option(predictedMedianInsertSize).foreach(builder.setPredictedMedianInsertSize)
    Option(platformModel).foreach(builder.setPlatformModel)
    Option(sequencingCenter).foreach(builder.setSequencingCenter)
    ReadGroup.fromAvro(builder.build())
  }
}
