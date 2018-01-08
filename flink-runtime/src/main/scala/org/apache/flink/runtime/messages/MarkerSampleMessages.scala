/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
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

package org.apache.flink.runtime.messages

import akka.actor.ActorRef
import org.apache.flink.api.common.time.Time
import org.apache.flink.runtime.executiongraph.ExecutionAttemptID

object MarkerSampleMessages {

  trait MarkerSampleMessages

  /**
    * Triggers the sampling of a running task (sent by the job manager to the
    * task managers).
    *
    * @param sampleId ID of this sample.
    * @param executionId ID of the task to sample.
    * @param numSamples Number of stack trace samples to collect.
    * @param delayBetweenSamples Delay between consecutive samples.
    */
  case class TriggerMarkerSample(
                                      sampleId: Int,
                                      executionId: ExecutionAttemptID,
                                      numSamples: Int,
                                      delayBetweenSamples: Time)
    extends MarkerSampleMessages with java.io.Serializable

  /**
    * Task manager internal sample message.
    *
    * @param sampleId ID of the this sample.
    * @param executionId ID of the task to sample.
    * @param delayBetweenSamples Delay between consecutive samples.
    * @param numRemainingSamples Number of remaining samples before this
    *                            sample is finished.
    * @param numSampled Number of past samples
    * @param numBlocked Number of blocked samples
    * @param sender Actor triggering this sample (receiver of result).
    */
  case class SampleTaskMarker(
                               sampleId: Int,
                               executionId: ExecutionAttemptID,
                               delayBetweenSamples: Time,
                               numRemainingSamples: Int,
                               numSampled: Int,
                               numBlocked: Int,
                               sender: ActorRef)
    extends MarkerSampleMessages

}
