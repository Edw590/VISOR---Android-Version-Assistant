/*
 * Copyright 2021-2024 Edw590
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

package com.edw590.visor_c_a.GlobalUtils;

import android.Manifest;
import android.os.Build;

/**
 * <p>Constants for all assistant-required permissions.</p>
 */
public final class PERMS_CONSTS {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private PERMS_CONSTS() {
	}

	private static final String[][] danger_perms_list = {
			{Manifest.permission.CAMERA, String.valueOf(Build.VERSION_CODES.BASE)},
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
			{Manifest.permission.BLUETOOTH_CONNECT, String.valueOf(Build.VERSION_CODES.S)},
			{Manifest.permission.BLUETOOTH_SCAN, String.valueOf(Build.VERSION_CODES.S)},
	};
	private static final String[][] dev_perms_list = {
			// Only from Jelly Bean 4.2 this permission began being of development type, and hence can only be granted
			// through ADB commands from that point onwards.
			{Manifest.permission.WRITE_SECURE_SETTINGS, String.valueOf(Build.VERSION_CODES.JELLY_BEAN)},
	};

	// NOTICE: keep the dangerous permissions list on the index 0 of the array. On the permissions checker function,
	// only the permissions from the list on index 0 here are counted as not granted and warned to the user. With all
	// other indexes no permissions are counted (the user can't do anything about them - development permissions can be
	// granted with root permissions allowed to the app, and that's it - without root, no way to grant the permissions).
	public static final String[][][] list_of_perms_lists = {danger_perms_list, dev_perms_list};
}
