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

import org.apache.flink.shaded.guava18.com.google.common.collect.Maps;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * A abstract pending sample container, which collects samples and owns a
 * {@link BackPressureSample} promise.
 *
 * <p>Access pending sample in lock scope.
 */
public abstract class PendingBackPressureSample<T extends BackPressureSingleSample> {

	private final int sampleId;
	private final long startTime;
	private final Set<ExecutionAttemptID> pendingTasks;
	private final Map<ExecutionAttemptID, T> samplesByTask;
	private final CompletableFuture<BackPressureSample> sampleFuture;

	protected boolean isDiscarded;

	PendingBackPressureSample (
		int sampleId,
		ExecutionAttemptID[] tasksToCollect) {

		this.sampleId = sampleId;
		this.startTime = System.currentTimeMillis();
		this.pendingTasks = new HashSet<>(Arrays.asList(tasksToCollect));
		this.samplesByTask = Maps.newHashMapWithExpectedSize(tasksToCollect.length);
		this.sampleFuture = new CompletableFuture<>();
	}

	int getSampleId() {
		return sampleId;
	}

	long getStartTime() {
		return startTime;
	}

	boolean isDiscarded() {
		return isDiscarded;
	}

	boolean isComplete() {
		if (isDiscarded) {
			throw new IllegalStateException("Discarded");
		}

		return pendingTasks.isEmpty();
	}

	void discard(Throwable cause) {
		if (!isDiscarded) {
			pendingTasks.clear();
			samplesByTask.clear();

			sampleFuture.completeExceptionally(new RuntimeException("Discarded", cause));

			isDiscarded = true;
		}
	}

	void collectSamples(ExecutionAttemptID executionId, T samples) {
		if (isDiscarded) {
			throw new IllegalStateException("Discarded");
		}

		if (pendingTasks.remove(executionId)) {
			samplesByTask.put(executionId, samples);
		} else if (isComplete()) {
			throw new IllegalStateException("Completed");
		} else {
			throw new IllegalArgumentException("Unknown task " + executionId);
		}
	}

	void completePromiseAndDiscard() {
		if (isComplete()) {
			isDiscarded = true;

			long endTime = System.currentTimeMillis();

			BackPressureSample backPressureSample = createSample(
				sampleId,
				startTime,
				endTime,
				samplesByTask);

			sampleFuture.complete(backPressureSample);
		} else {
			throw new IllegalStateException("Not completed yet");
		}
	}

	@SuppressWarnings("unchecked")
	CompletableFuture<BackPressureSample> getBackPressureSampleFuture() {
		return sampleFuture;
	}

	protected abstract BackPressureSample createSample(
		int sampleId,
		long startTime,
		long endTime,
		Map<ExecutionAttemptID, T> samplesByTask
	);
}
