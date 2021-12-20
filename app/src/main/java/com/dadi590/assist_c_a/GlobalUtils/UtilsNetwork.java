/*
 * Copyright 2021 DADi590
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

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Utilities related with network functions.</p>
 */
public final class UtilsNetwork {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsNetwork() {
	}

	/**
	 * <p>This function pings the specified IP address 50 times, eliminates outlier time values, and finally calculates
	 * the average of the resulting values (the round-trip time, RTT).</p>
	 *
	 * @param ip the IP address to ping
	 *
	 * @return the average RTT
	 */
	public static double getAveragePingRTT(@NonNull final String ip) {
		final int packets_num = 50; // 50 packets, each with 0.5 seconds delay, so 25 seconds of waiting time
		// 248 + 8 header = 256 bytes each packet
		final List<String> commands = new ArrayList<>(1);
		commands.add("ping -c " + packets_num + " -i 0.5 -n -s 248 -t 1 -v " + ip);
		final UtilsShell.CmdOutputObj cmdOutputObj = UtilsShell.executeShellCmd(commands, true);
		final String[] output_lines = UtilsGeneral.bytesToPrintableChars(cmdOutputObj.output_stream, false).split("\n");

		// Here it gets the time values (excluding the duplicated ones)
		final List<Double> time_values = new ArrayList<>(packets_num);
		for (final String line : output_lines) {
			if (!line.contains(" (DUP!)") && line.contains("time=") && line.contains(" ms")) {
				time_values.add(Double.parseDouble(line.split("time=")[1].split(" ")[0]));
			}
		}

		final double accuracy_parameter = 2.0; // Accuracy parameter to use with UtilsMath.isOutlier().

		// It will calculate the sum and sum of squares of the first 5 elements of the time_values list (5 seemed a good
		// number), and then will check if any of those first 5 elements is an outlier inside that 5-elements list, and
		// if any is an outlier, it will be removed from the time_values list.
		// If the time values are [2, 20, 18, 16, 34, 129, 21, 23], the average of the first values will still be near
		// the supposed value (between 16 and 20). If I put 4, it will come down to 14 (wrong). If it's less, even
		// worse will get. More than that, could catch the 129, which I don't see appearing in the first 5 elements on
		// some tests.
		final int first_n_elements = 5;
		double sum = 0.0;
		double sum_squares = 0.0;
		for (int i = 0; i < first_n_elements; ++i) {
			final double value = time_values.get(i);
			sum += value;
			sum_squares += value * value;
		}
		boolean any_outlier = false;
		for (int i = 0; i < first_n_elements; ++i) {
			if (UtilsMath.isOutlier(time_values.get(i), sum, sum_squares, 5, accuracy_parameter)) {
				any_outlier = true;
				time_values.remove(i);
				i--;
			}
		}

		int summed_elements = first_n_elements;
		if (any_outlier) {
			// Reset the variables since now it will calculate again the new sum and sum of squares (in case there was an
			// outlier and it was removed from the original list). This will be used as the starting parameters for the
			// outlier check.
			sum = 0.0;
			sum_squares = 0.0;
			summed_elements = 0;
			for (int i = 0; i < first_n_elements; ++i) {
				final double value = time_values.get(i);
				++summed_elements;
				sum += value;
				sum_squares += value * value;
			}
		}

		// With the remaining elements, the function will check if any of them is an outlier, comparing to the values
		// got from the first 5 elements (those 5 decide the fate of the list xD).
		for (int i = summed_elements; i < packets_num - summed_elements; ++i) {
			final double value = time_values.get(i);
			if (!UtilsMath.isOutlier(value, sum, sum_squares, summed_elements, accuracy_parameter)) {
				++summed_elements;
				sum += value;
				sum_squares += value * value;
			}
		}

		// The mean of the time values will be calculated using the first 5 elements (excluding any outliers) and the
		// rest of the list (excluding any outliers).
		return sum / (double) summed_elements;
	}
}
