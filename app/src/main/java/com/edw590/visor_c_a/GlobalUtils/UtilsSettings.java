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

import SettingsSync.SettingsSync;
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
	 * <p>Reads and loads the User and Generated settings from disk.</p>
	 *
	 * @param user_settings true if the user settings should be loaded, false if the generated settings should be loaded
	 *
	 * @return true if the settings were read and loaded successfully, false otherwise
	 */
	public static boolean loadSettingsFile(final boolean user_settings) {
		String settings_file_str = user_settings ? UtilsSWA.USER_SETTINGS_FILE : UtilsSWA.GEN_SETTINGS_FILE_CLIENT;
		String backup_file_str = settings_file_str + ".bak";

		GPath settings_file = new GPath(true, GL_CONSTS.VISOR_EXT_FOLDER_PATH).add2(false, settings_file_str);
		GPath backup_file = new GPath(true, GL_CONSTS.VISOR_EXT_FOLDER_PATH).add2(false, backup_file_str);

		byte[] file_bytes = UtilsFilesDirs.readFileBytes(settings_file);
		try {
			if (user_settings) {
				SettingsSync.loadUserSettings(UtilsSWA.bytesToPrintableDATACONV(file_bytes, false));
			} else {
				SettingsSync.loadGenSettings(UtilsSWA.bytesToPrintableDATACONV(file_bytes, false));
			}
		} catch (final Exception e) {
			e.printStackTrace();

			file_bytes = UtilsFilesDirs.readFileBytes(backup_file);
			try {
				if (user_settings) {
					SettingsSync.loadUserSettings(UtilsSWA.bytesToPrintableDATACONV(file_bytes, false));
				} else {
					SettingsSync.loadGenSettings(UtilsSWA.bytesToPrintableDATACONV(file_bytes, false));
				}
			} catch (final Exception e2) {
				e2.printStackTrace();

				String user_generated = user_settings ? "user" : "generated";
				System.out.println("Failed to load " + user_generated + " settings. Using empty ones...");

				return false;
			}
		}

		return true;
	}

	/**
	 * <p>Writes the User and Generated settings to disk.</p>
	 *
	 * @param json the JSON string to write
	 * @param user_settings true if the user settings should be saved, false if the generated settings should be saved
	 */
	public static void writeSettingsFile(@NonNull final String json, final boolean user_settings) {
		String settings_file_str = user_settings ? UtilsSWA.USER_SETTINGS_FILE : UtilsSWA.GEN_SETTINGS_FILE_CLIENT;
		String backup_file_str = settings_file_str + ".bak";

		GPath settings_file = new GPath(true, GL_CONSTS.VISOR_EXT_FOLDER_PATH).add2(false, settings_file_str);
		GPath backup_file = new GPath(true, GL_CONSTS.VISOR_EXT_FOLDER_PATH).add2(false, backup_file_str);

		UtilsFilesDirs.writeFile(settings_file, json.getBytes(Charset.defaultCharset()));
		UtilsFilesDirs.writeFile(backup_file, json.getBytes(Charset.defaultCharset()));
	}
}
