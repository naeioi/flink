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

import java.util.List;
import java.util.Map;

/**
 * A sample of stack traces for one or more tasks.
 *
 * <p>The sampling is triggered in {@link StackTraceSampler}.
 */
public class StackTraceSample extends BackPressureSample<StackTraceSingleSample> {

	/** Expected class name for back pressure indicating stack trace element. */
	static final String EXPECTED_CLASS_NAME = "org.apache.flink.runtime.io.network.buffer.LocalBufferPool";

	/** Expected method name for back pressure indicating stack trace element. */
	static final String EXPECTED_METHOD_NAME = "requestBufferBlocking";

	public StackTraceSample(
		int sampleId,
		long startTime,
		long endTime,
		Map<ExecutionAttemptID, StackTraceSingleSample> samplesByTask) {
		super(sampleId, startTime, endTime, samplesByTask);
	}

//	/**
//	 * Returns the a map of stack traces by execution ID.
//	 *
//	 * @return Map of stack traces by execution ID
//	 */
//	public Map<ExecutionAttemptID, List<StackTraceElement[]>> getStackTraces() {
//		return stackTracesByTask;
//	}

	@Override
	public String toString() {
		return "StackTraceSample{" +
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

		for (Map.Entry<ExecutionAttemptID, StackTraceSingleSample> entry : samplesByTask.entrySet()) {
			int backPressureSamples = 0;

			List<StackTraceElement[]> taskTraces = entry.getValue().get();

			for (StackTraceElement[] trace : taskTraces) {
				for (int i = trace.length - 1; i >= 0; i--) {
					StackTraceElement elem = trace[i];

					if (elem.getClassName().equals(EXPECTED_CLASS_NAME) &&
						elem.getMethodName().equals(EXPECTED_METHOD_NAME)) {

						backPressureSamples++;
						break; // Continue with next stack trace
					}
				}
			}

			int subtaskIndex = subtaskIndexMap.get(entry.getKey());

			int size = taskTraces.size();
			double ratio = (size > 0)
				? ((double) backPressureSamples) / size
				: 0;

			backPressureRatio[subtaskIndex] = ratio;
		}

		return new OperatorBackPressureStats(
			sampleId,
			endTime,
			backPressureRatio);
	}
}
