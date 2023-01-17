/*
 * Copyright 2023 DADi590
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.dadi590.assist_c_a.GlobalUtils;

/**
 * <p>Utilities related to math operations.</p>
 */
public final class UtilsMath {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsMath() {
	}

	/**
	 * <p>Checks if an element is an outlier of an array.</p>
	 * <p>The functions does so by checking if the elements is inside a range of mean +- X * standard deviation.</p>
	 *
	 * @param element the element to check
	 * @param sum the sum of all the elements
	 * @param sum_squares the sum of the squares of all the elements
	 * @param num_elements the number of elements
	 * @param accuracy_parameter the mentioned X value
	 *
	 * @return true if it's an outlier, false otherwise
	 */
	public static boolean isOutlier(final double element, final double sum, final double sum_squares,
								   final int num_elements, final double accuracy_parameter) {
		final double mean = sum / (double) num_elements;
		final double variance = sum_squares / (double) num_elements - mean*mean;
		final double standard_deviation = Math.sqrt(variance);
		System.out.println("------------------");
		System.out.println("Element - " + element);
		System.out.println("Mean - " + mean);
		System.out.println("Standard Deviation - " + standard_deviation);

		final boolean is_outlier = !(element >= mean - accuracy_parameter * standard_deviation
				&& element <= mean + accuracy_parameter * standard_deviation);
		System.out.println("Outlier - " + is_outlier);
		return is_outlier;
		// All values inside accuracy_parameter * standard_deviation, remain. All others leave the array.
	}
}
