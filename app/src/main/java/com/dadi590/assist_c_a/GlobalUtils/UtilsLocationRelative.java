/*
 * Copyright 2022 DADi590
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

	public static final int ABSTR_DISTANCE_1 = 0;
	public static final int ABSTR_DISTANCE_5 = 1;
	public static final int ABSTR_DISTANCE_10 = 2;
	public static final int ABSTR_DISTANCE_50 = 3;
	public static final int ABSTR_DISTANCE_100 = 4;
	public static final int ABSTR_DISTANCE_INFINITY = 5;
	/**
	 * <p>Returns an abstract distance between the 2 devices using the RSSI of their detection.</p>
	 * <p>These distances are solely to standardize values on the app.</p>
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link #ABSTR_DISTANCE_1} --> for the returning value: if the distance is <= 1 meter</p>
	 * <p>- {@link #ABSTR_DISTANCE_5} --> for the returning value: else if the distance is <= 5 meters</p>
	 * <p>- {@link #ABSTR_DISTANCE_10} --> for the returning value: else if the distance is <= 10 meters</p>
	 * <p>- {@link #ABSTR_DISTANCE_50} --> for the returning value: else if the distance is <= 50 meters</p>
	 * <p>- {@link #ABSTR_DISTANCE_100} --> for the returning value: else if the distance is <= 100 meters</p>
	 * <p>- {@link #ABSTR_DISTANCE_INFINITY} --> for the returning value: else if the distance is > 100 meters</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @param rssi same as in {@link #getRealDistanceRSSI(int, int)}
	 *
	 * @return one of the constants
	 */
	public static int getAbstrDistanceRSSI(final int rssi) {
		// For now I don't see use for the tx_power parameter, so I'm passing always the default one. Some other time
		// might implement BLE device detection, on which the value is useful since some devices have a value (not
		// BV9500 it seems).
		final double real_distance = getRealDistanceRSSI(rssi, DEFAULT_TX_POWER);
		if (real_distance <= 1.0) {
			return ABSTR_DISTANCE_1;
		} else if (real_distance <= 5.0) {
			return ABSTR_DISTANCE_5;
		} else if (real_distance <= 10.0) {
			return ABSTR_DISTANCE_10;
		} else if (real_distance <= 50.0) {
			return ABSTR_DISTANCE_50;
		} else if (real_distance <= 100.0) {
			return ABSTR_DISTANCE_100;
		} else {
			return ABSTR_DISTANCE_INFINITY;
		}
	}

	private static final int DEFAULT_TX_POWER = -60;
	/**
	 * <p>Gets the distance in meters between 2 devices from the transmission signal strength between the 2 devices
	 * (RSSI) and the transmission power value at 1 meter for the current device.</p>
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

		(The formula seems to be from Texas Instruments on the CC2420 chip.)

		So getting d from there, we have this below. Should be RSSI+A, but that term is supposed to be 0 if both values
		are equal, with with a +, that won't happen, so I saw people having +A on the main formula, and then it results
		in:
		d = 10^(-(RSSI-A)/10n)
		*/

		// "The txPower field is supposed to be the expected signal strength in dBm at 1 meter. Every Android phone
		// model has a slightly different BLE transmitter output power, typically measuring from -50 dBm to -70 dBm at
		// 1 meter." - so I'll go for -60 dBm as the default txPower (middle value).

		/*
		According to Wikipedia (https://en.wikipedia.org/wiki/Log-distance_path_loss_model, 2021-07-29):
		----------------
		                         Empirical coefficient values for indoor propagation

		Empirical measurements of coefficients gamma and sigma in dB have shown the following values for a number of
		indoor wave propagation cases.

		Building Type                  Frequency of Transmission    gamma   sigma [dB]
		Vacuum, infinite space                                       2.0    0
		Retail store                         914 MHz                 2.2    8.7
		Grocery store                        914 MHz                 1.8    5.2
		Office with hard partition           1.5 GHz                 3.0    7
		Office with soft partition           900 MHz                 2.4    9.6
		Office with soft partition           1.9 GHz                 2.6    14.1
		Textile or chemical                  1.3 GHz                 2.0    3.0
		Textile or chemical                    4 GHz                 2.1    7.0, 9.7
		Office                                60 GHz                 2.2    3.92
		Commercial                            60 GHz                 1.7    7.9
		----------------
		[Note: our n here is gamma on Wikipedia.]

		So using the Office with soft-partition at 1.9 GHz with 2.6, knowing on 900 MHz it was 2.4, and also that
		Bluetooth operates at 2.4-2.5 GHz, 2.7-2.8 seems a good choice of value at home (so 2.75 - average).
		But that's only with objects in the way. If there's a straight path, 2.1 doesn't seem a bad value (there's the
		phone or computer case). So if the distance is very small, 2.1 will be used (can't be much in the way if the
		distance is like 1 meter with 2.75 as value).
		Another possibility is the object being further away. In case, then other objects might be in its path. Then we
		rise the value, depending on the distance. For example, 5 meters or more could have stuff in the way already.
		10-20 meters and even more stuff (not counting open field here... - can't guess this kind of thing without GPS).
		Else, 2.75 will be used.


		From https://en.wikipedia.org/wiki/Path_loss:
		"In the study of wireless communications, path loss can be represented by the path loss exponent, whose value
		is normally in the range of 2 to 4 (where 2 is for propagation in free space, 4 is for relatively lossy
		environments and for the case of full specular reflection from the earth surface—the so-called flat earth
		model). In some environments, such as buildings, stadiums and other indoor environments, the path loss exponent
		can reach values in the range of 4 to 6. On the other hand, a tunnel may act as a waveguide, resulting in a path
		loss exponent less than 2."

		So, 5 meters or more, could be 2.75 + (4+6)/2 = 3.875 = 3.88. For more than 20 meters, why not 6.0. On more than
		50 meters, I'll use 8.0. A person said n could reach from 2 to 8 in some cases. So I'll use that 8 value here.
		*/

		// This will be used if 2.0 < resulting distance < 7.5.
		final double ret_value = StrictMath.pow(10.0, (double) -(rssi - tx_power) / (10.0 * 2.75));

		if (ret_value <= 2.0) {
			// 2 meters here because it's near enough to 1 meter - can't be 1 meter because the idea of the if statements
			// is to correct incorrect calculation, so it's to assume if it gives about 2 meters, it might mean it's less
			// than that actually (like with my phone and tablet, a meter away from each other and this returning more
			// than 1 meter.
			return StrictMath.pow(10.0, (double) - (rssi - tx_power) / (10.0 * 2.1));
		} else if (ret_value >= 50.0) {
			return StrictMath.pow(10.0, (double) - (rssi - tx_power) / (10.0 * 8.0));
		} else if (ret_value >= 20.0) {
			return StrictMath.pow(10.0, (double) - (rssi - tx_power) / (10.0 * 6.0));
		} else if (ret_value >= 7.5) { // 7.5 meters because it's near enough to 5 meters and still away from 20 meters
			return StrictMath.pow(10.0, (double) - (rssi - tx_power) / (10.0 * 3.88));
		} else {
			return ret_value;
		}
	}
}
