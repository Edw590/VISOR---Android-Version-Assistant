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

import android.os.Build;

import androidx.annotation.NonNull;

import java.io.File;

/**
 * <p>Utilities related to native libraries.</p>
 */
public final class UtilsNativeLibs {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsNativeLibs() {
	}

	/**
	 * <p>Gets the full path to the native libraries directory of the app.</p>
	 *
	 * @return the full path to the directory
	 */
	@NonNull
	public static String getPrimaryNativeLibsPath() {
		return UtilsContext.getContext().getApplicationInfo().nativeLibraryDir;
	}

	public static final String POCKETSPHINX_LIB_NAME = "libpocketsphinx_jni.so";
	public static final String ACD_LIB_NAME = "libgojni.so";
	/**
	 * <p>Checks if a native library file, bundled from the APK, is available on the native libraries directories of the
	 * app.</p>
	 * <p>On Android Marshmallow and above this always returns true (extractNativeLibs="false" on the Manifest).</p>
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link #POCKETSPHINX_LIB_NAME} --> for {@code lib_name}: name of the PocketSphinx library file</p>
	 * <p>- {@link #ACD_LIB_NAME} --> for {@code lib_name}: name of the APU library file</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @param lib_name one of the constants, or the name of the library file (eg. "library.so")
	 *
	 * @return true if the file exists, false otherwise
	 */
	public static boolean isPrimaryNativeLibAvailable(@NonNull final String lib_name) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			// This returns always true ONLY because it assumes extractNativeLibs is set to false on the Manifest!

			return true;
		}

		final String native_libs_path = getPrimaryNativeLibsPath();

		final String[] available_libs = new File(native_libs_path).list();

		if (null != available_libs) {
			for (final String lib : available_libs) {
				if (lib_name.equals(lib)) {
					return true;
				}
			}
		}

		return false;
	}
}
