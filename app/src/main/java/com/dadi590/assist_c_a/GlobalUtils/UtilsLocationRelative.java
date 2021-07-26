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

/**
 * <p>Class with methods to get relative distances between devices.</p>
 * <p>Unlike absolute locations which locate the device on Earth, these locate the device near some point and don't
 * return coordinates.</p>
 */
public final class UtilsLocationRelative {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsLocationRelative() {
	}

	public static final int DISTANCE_IMMEDIATE = 0;
	public static final int DISTANCE_NEARBY = 1;
	public static final int DISTANCE_FAR = 2;
	/**
	 * <p>Returns an abstract distance between the 2 devices.</p>
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link #DISTANCE_IMMEDIATE} --> for the returning value: if the distance is <= 1 meter</p>
	 * <p>- {@link #DISTANCE_NEARBY} --> for the returning value: if the distance, x, is 1 meter < x <= 5 meters</p>
	 * <p>- {@link #DISTANCE_FAR} --> for the returning value: if the distance is > 5 meters</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @param rssi same as in {@link #getRealDistanceRSSI(int, int)}
	 *
	 * @return one of the constants
	 */
	public static int getDistanceRSSI(final int rssi) {
		final double real_distance = getRealDistanceRSSI(rssi, DEFAULT_TX_POWER);
		if (real_distance <= 1.0) {
			return DISTANCE_IMMEDIATE;
		} else if (real_distance <= 5.0) {
			return DISTANCE_NEARBY;
		} else {
			return DISTANCE_FAR;
		}
	}

	private static final int DEFAULT_TX_POWER = -60;
	/**
	 * <p>Gets the distance in meters between 2 devices from the transmission signal strength between the 2 devices
	 * (RSSI) and the transmission power for 1 meter for the current device.</p>
	 *
	 * @param rssi the RSSI between both devices
	 * @param tx_power the expected transmission power of the current device for 1 meter distance from another device
	 *
	 * @return the distance between the 2 devices
	 */
	private static double getRealDistanceRSSI(final int rssi, final int tx_power) {
		/*
		Copied from: Dong, Q., & Dargie, W. (2012). Evaluation of the reliability of RSSI for indoor localization. 2012
		International Conference on Wireless Communications in Underground and Confined Areas, 1-6.
		Link for IEEE: https://ieeexplore.ieee.org/stamp/stamp.jsp?tp=&arnumber=6402492.

		"RSSI = -10*n*log10(d)-A (4)
		In Equation 4, RSSI is the radio signal strength indicator in dBm, n is the signal propagation constant or
		exponent [, also called Path Loss Exponent, which is dimensionless], d is the relative distance between the
		communicating nodes [in meters], and A is a reference received signal strength in dBm (the RSSI value measured
		when the separation distance between the receiver and the transmitter is one meter)."

		So getting d from there, we have this below. Should be RSSI+A, but that term is supposed to be 0 if both values
		are equal, with with a +, that won't happen, so I saw people having +A on the main formula, and then it results
		in:
		d = 10^(-(RSSI-A)/10n)
		*/

		// "The txPower field is supposed to be the expected signal strength in dBm at 1 meter. Every Android phone
		// model has a slightly different BLE transmitter output power, typically measuring from -50 dBm to -70 dBm at
		// 1 meter." - so I'll go for -60 dBm (middle value).

		final double n = 2.0;
		// todo Check if this is the best value to have here. This is in "free space".
		// If this method will ONLY be used to calculate small distances, like if a phone is near to another phone or
		// any other device (NEAR, not far), then I guess it's ok. If it's to be used for more thing, probably bad idea
		// to use 2 - read the paper.

		return StrictMath.pow(10.0, (double) -(rssi - tx_power) / (10.0 * n));
	}
}
