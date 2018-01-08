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

package org.apache.flink.runtime.rest.handler.legacy.backpressure;

import org.apache.flink.runtime.executiongraph.ExecutionAttemptID;

import java.util.Map;

/**
 * A marker sample for one or more tasks.
 *
 * <p>The sampling is triggered in {@link MarkerSampler}.
 */
public class MarkerSample extends BackPressureSample<MarkerSingleSample> {

	public MarkerSample(
		int sampleId,
		long startTime,
		long endTime,
		Map<ExecutionAttemptID, MarkerSingleSample> samplesByTask) {
		super(sampleId, startTime, endTime, samplesByTask);
	}

	@Override
	public String toString() {
		return "MarkerSample{" +
			"sampleId=" + sampleId +
			", startTime=" + startTime +
			", endTime=" + endTime +
			'}';
	}

	/**
	 * Creates the back pressure stats from a stack trace sample.
	 *
	 * @param subtaskIndexMap Map from task ID to subtask index
	 *
	 * @return Back pressure stats
	 */
	@Override
	public OperatorBackPressureStats toStats(Map<ExecutionAttemptID, Integer> subtaskIndexMap) {

		// Ratio of blocked samples to total samples per sub task. Array
		// position corresponds to sub task index.
		double[] backPressureRatio = new double[samplesByTask.size()];

		for (Map.Entry<ExecutionAttemptID, MarkerSingleSample> entry : samplesByTask.entrySet()) {

			double ratio = entry.getValue().getBlockRatio();
			int subtaskIndex = subtaskIndexMap.get(entry.getKey());

			backPressureRatio[subtaskIndex] = ratio;
		}

		return new OperatorBackPressureStats(
			sampleId,
			endTime,
			backPressureRatio);
	}
}
