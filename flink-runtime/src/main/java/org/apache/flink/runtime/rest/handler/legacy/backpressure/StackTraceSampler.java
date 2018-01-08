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

import org.apache.flink.api.common.time.Time;
import org.apache.flink.runtime.executiongraph.Execution;
import org.apache.flink.runtime.executiongraph.ExecutionAttemptID;
import org.apache.flink.runtime.messages.backpressure.BackPressureSampleResponse;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * A coordinator for triggering and collecting stack traces of running tasks.
 */
public class StackTraceSampler extends BackPressureSampler {

	/** Maximum stack trace depth for samples. */
	static final int MAX_STACK_TRACE_DEPTH = 3;

	/* TODO: hide maxStackTraceDepth in upper abstract */
	private final int maxStackTraceDepth = MAX_STACK_TRACE_DEPTH;

	/**
	 * Creates a new coordinator for the job.
	 *
	 * @param executor to use to execute the futures
	 * @param sampleTimeout Time out after the expected sampling duration.
	 *                      This is added to the expected duration of a
	 *                      sample, which is determined by the number of
	 *                      samples and the delay between each sample.
	 */
	public StackTraceSampler(Executor executor, long sampleTimeout) {
		super(executor, sampleTimeout);
	}

	@Override
	protected CompletableFuture<? extends BackPressureSampleResponse> triggerExecutorSampling(
		Execution execution,
		int sampleId,
		int numSamples,
		Time delayBetweenSamples,
		Time timeout) {
		return execution.requestStackTraceSample(
			sampleId,
			numSamples,
			delayBetweenSamples,
			maxStackTraceDepth,
			timeout
		);
	}

	@Override
	protected PendingBackPressureSample createPendingSample(
		int sampleId,
		ExecutionAttemptID[] tasksToCollect) {
		return new PendingStackTraceSample(sampleId, tasksToCollect);
	}

	private class PendingStackTraceSample extends PendingBackPressureSample<StackTraceSingleSample> {

		PendingStackTraceSample(
			int sampleId,
			ExecutionAttemptID[] tasksToCollect) {
			super(sampleId, tasksToCollect);
		}

		@Override
		protected BackPressureSample createSample(
			int sampleId,
			long startTime,
			long endTime,
			Map<ExecutionAttemptID, StackTraceSingleSample> samplesByTask) {
			return new StackTraceSample(sampleId, startTime, endTime, samplesByTask);
		}
	}
}
