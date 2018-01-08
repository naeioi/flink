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

/**
 * A marker backpressure sample corresponding to a single ExecutionAttemptID.
 */
public class MarkerSingleSample extends BackPressureSingleSample {

	private int numSampled;

	private int numBlocked;

	public MarkerSingleSample(int numSampled, int numBlocked) {
		this.numSampled = numSampled;
		this.numBlocked = numBlocked;
	}

	public int getNumSampled() {
		return numSampled;
	}

	public int getNumBlocked() {
		return numBlocked;
	}

	public double getBlockRatio() {
		return (double) numBlocked / numSampled;
	}
}
