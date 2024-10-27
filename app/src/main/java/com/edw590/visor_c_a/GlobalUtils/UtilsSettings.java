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

import androidx.annotation.NonNull;

import java.nio.charset.Charset;

import UtilsSWA.UtilsSWA;

/**
 * <p>Utilities related to the settings files.</p>
 */
public final class UtilsSettings {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsSettings() {
	}

	/**
	 * <p>Gets the Device Settings in JSON format.</p>
	 *
	 * @return the Device Settings in JSON format
	 */
	@NonNull
	public static String readJsonDeviceSettings() {
		GPath device_settings_path = new GPath(true, GL_CONSTS.VISOR_EXT_FOLDER_PATH);
		device_settings_path.add2(true, UtilsSWA.DEVICE_SETTINGS_FILE);

		byte[] file_bytes = UtilsFilesDirs.readFileBytes(device_settings_path);

		return UtilsSWA.bytesToPrintableDATACONV(file_bytes, false);
	}

	/**
	 * <p>Gets the Generated Settings in JSON format.</p>
	 *
	 * @return the Generated Settings in JSON format
	 */
	@NonNull
	public static String readJsonGenSettings() {
		GPath gen_settings_path = new GPath(true, GL_CONSTS.VISOR_EXT_FOLDER_PATH);
		gen_settings_path.add2(true, UtilsSWA.GEN_SETTINGS_FILE_CLIENT);

		byte[] file_bytes = UtilsFilesDirs.readFileBytes(gen_settings_path);

		return UtilsSWA.bytesToPrintableDATACONV(file_bytes, false);
	}

	/**
	 * <p>Writes the Device Settings in JSON format.</p>
	 *
	 * @param json the Device Settings in JSON format
	 *
	 * @return true if the operation completed successfully, false otherwise
	 */
	public static boolean writeGenSettings(@NonNull final String json) {
		GPath gen_settings_path = new GPath(true, GL_CONSTS.VISOR_EXT_FOLDER_PATH);
		gen_settings_path.add2(true, UtilsSWA.GEN_SETTINGS_FILE_CLIENT);

		return UtilsFilesDirs.writeFile(gen_settings_path, json.getBytes(Charset.defaultCharset())) == 0;
	}

	/**
	 * <p>Gets the Device Settings in JSON format.</p>
	 *
	 * @return the Device Settings in JSON format
	 */
	@NonNull
	public static String readJsonUserSettings() {
		GPath user_settings_path = new GPath(true, GL_CONSTS.VISOR_EXT_FOLDER_PATH);
		user_settings_path.add2(true, UtilsSWA.USER_SETTINGS_FILE);

		byte[] file_bytes = UtilsFilesDirs.readFileBytes(user_settings_path);

		return UtilsSWA.bytesToPrintableDATACONV(file_bytes, false);
	}

	/**
	 * <p>Writes the Device Settings in JSON format.</p>
	 *
	 * @param json the Device Settings in JSON format
	 *
	 * @return true if the operation completed successfully, false otherwise
	 */
	public static boolean writeUserSettings(@NonNull final String json) {
		GPath user_settings_path = new GPath(true, GL_CONSTS.VISOR_EXT_FOLDER_PATH);
		user_settings_path.add2(true, UtilsSWA.USER_SETTINGS_FILE);

		return UtilsFilesDirs.writeFile(user_settings_path, json.getBytes(Charset.defaultCharset())) == 0;
	}
}
