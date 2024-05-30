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

package com.edw590.visor_c_a.Modules.PreferencesManager;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.edw590.visor_c_a.GlobalUtils.GL_CONSTS;
import com.edw590.visor_c_a.GlobalUtils.GPath;
import com.edw590.visor_c_a.GlobalUtils.UtilsApp;
import com.edw590.visor_c_a.GlobalUtils.UtilsFilesDirs;
import com.edw590.visor_c_a.GlobalUtils.UtilsShell;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import UtilsSWA.UtilsSWA;

/**
 * <p>Utilities related to the preferences file.</p>
 */
public final class StaticPreferences {



	// todo ALL SYNCHRONIZED HERE!!!!!!



	static final String PREFS_FILE_PATH = GL_CONSTS.VISOR_EXT_FOLDER_PATH + "visor_preferences.dat";
	static final String TEMP_PREFS_FILE_PATH = GL_CONSTS.VISOR_EXT_FOLDER_PATH + "visor_preferences_temp.dat";

	@Nullable private static String preferences = null;
	@Nullable private static String preferences_temp = null;

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private StaticPreferences() {
	}

	/**
	 * <p>Rewrite the preferences file with the contents of {@link #preferences}.</p>
	 *
	 * @return true if the operation completed successfully, false otherwise
	 */
	static synchronized boolean writePrefsFile(@NonNull final String to_write) {
		byte[] encrypted_bytes = {};
		try {
			encrypted_bytes = UtilsSWA.encryptBytesCRYPTOENDECRYPT(
					"test1".getBytes(Charset.defaultCharset()),
					"test2".getBytes(Charset.defaultCharset()),
					to_write.getBytes(GL_CONSTS.UTF7_NAME_LIB),
					null);
		} catch (final UnsupportedEncodingException ignored) {
			// Won't happen - UTF7 is always available
		}
		// Won't be null. It's not a big file and the data is always processed well (must always be).
		assert encrypted_bytes != null;

		return UtilsShell.noErr(UtilsFilesDirs.writeFile(new GPath(false, TEMP_PREFS_FILE_PATH), encrypted_bytes)) &&
				UtilsShell.noErr(UtilsFilesDirs.movePath(new GPath(false, TEMP_PREFS_FILE_PATH),
						new GPath(false, PREFS_FILE_PATH)));
	}

	/**
	 * <p>Read the preferences file to {@link #preferences}.</p>
	 *
	 * @return the preferences or null in case of error reading the file
	 */
	@Nullable
	static synchronized String readPrefsFile() {
		final byte[] file_bytes = UtilsFilesDirs.readFileBytes(new GPath(false, PREFS_FILE_PATH));
		if (null == file_bytes) {
			return null;
		}

		preferences = UtilsSWA.bytesToPrintableDATACONV(file_bytes, false);

		return preferences;
	}

	/**
	 * <p>Update the {@link #preferences} and broadcast a request to save them to file.</p>
	 *
	 * @param updated_prefs the updated preferences
	 */
	public static synchronized void updatePreferences(@NonNull final String updated_prefs) {
		preferences = updated_prefs;

		UtilsApp.sendInternalBroadcast(new Intent(CONSTS_BC_PreferencesManager.ACTION_REQUEST_SAVE_PREFS));
	}

	/**
	 * <p>Get the app preferences file contents.</p>
	 *
	 * @return {@link #preferences}
	 */
	@Nullable
	public static synchronized String getPreferences() {
		return preferences;
	}
}
