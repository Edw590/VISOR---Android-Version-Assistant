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

import android.Manifest;
import android.os.Build;

/**
 * <p>Constants for all assistant-required permissions.</p>
 */
final class PERMS_CONSTS {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private PERMS_CONSTS() {
	}

	// Note: all permissions listed here exist in Manifest.permission. If an error appears, that means the app is not
	// being compiled with hidden/internal classes enabled - it must be.

	/*private static final String[][] norm_perms_list = {
			{Manifest.permission.INTERNET, String.valueOf(Build.VERSION_CODES.BASE)},
			{Manifest.permission.ACCESS_NETWORK_STATE, String.valueOf(Build.VERSION_CODES.BASE)},
			{Manifest.permission.RECEIVE_BOOT_COMPLETED, String.valueOf(Build.VERSION_CODES.BASE)},
			{Manifest.permission.CAMERA, String.valueOf(Build.VERSION_CODES.BASE)},
			{Manifest.permission.ACCESS_WIFI_STATE, String.valueOf(Build.VERSION_CODES.BASE)},
			{Manifest.permission.CHANGE_WIFI_STATE, String.valueOf(Build.VERSION_CODES.BASE)},
			{Manifest.permission.BLUETOOTH, String.valueOf(Build.VERSION_CODES.BASE)},
			{Manifest.permission.BLUETOOTH_ADMIN, String.valueOf(Build.VERSION_CODES.BASE)},
			{Manifest.permission.FOREGROUND_SERVICE, String.valueOf(Build.VERSION_CODES.P)},
			{Manifest.permission.MODIFY_AUDIO_SETTINGS, String.valueOf(Build.VERSION_CODES.BASE)},
	};*/
	static final String[][] danger_perms_list = {
			{Manifest.permission.RECEIVE_SMS, String.valueOf(Build.VERSION_CODES.BASE)},
			{Manifest.permission.RECORD_AUDIO, String.valueOf(Build.VERSION_CODES.BASE)},
			{Manifest.permission.WRITE_EXTERNAL_STORAGE, String.valueOf(Build.VERSION_CODES.DONUT)},
			{Manifest.permission.READ_PHONE_STATE, String.valueOf(Build.VERSION_CODES.BASE)},
			{Manifest.permission.READ_CALL_LOG, String.valueOf(Build.VERSION_CODES.JELLY_BEAN)},
			{Manifest.permission.READ_CONTACTS, String.valueOf(Build.VERSION_CODES.BASE)},
			{Manifest.permission.ACCESS_COARSE_LOCATION, String.valueOf(Build.VERSION_CODES.BASE)},
			{Manifest.permission.ACCESS_FINE_LOCATION, String.valueOf(Build.VERSION_CODES.BASE)},
			{Manifest.permission.ACCESS_BACKGROUND_LOCATION, String.valueOf(Build.VERSION_CODES.Q)},
			{Manifest.permission.ANSWER_PHONE_CALLS, String.valueOf(Build.VERSION_CODES.O)},
			{Manifest.permission.CALL_PHONE, String.valueOf(Build.VERSION_CODES.BASE)},
	};
	/*private static final String[][] sys_perms_list = {
			{Manifest.permission.REBOOT, String.valueOf(Build.VERSION_CODES.BASE)},
			{Manifest.permission.SHUTDOWN, String.valueOf(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)},
			{Manifest.permission.WRITE_SETTINGS, String.valueOf(Build.VERSION_CODES.BASE)},
			{Manifest.permission.WRITE_SECURE_SETTINGS, String.valueOf(Build.VERSION_CODES.CUPCAKE)},
			{Manifest.permission.CONNECTIVITY_INTERNAL, String.valueOf(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)},
			{Manifest.permission.MODIFY_PHONE_STATE, String.valueOf(Build.VERSION_CODES.BASE)},
			{Manifest.permission.CAPTURE_AUDIO_OUTPUT, String.valueOf(Build.VERSION_CODES.KITKAT)},
	};*/

	//static final String[][][] list_of_perms_lists = {danger_perms_list};
}
