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

import android.os.Environment;

import androidx.annotation.Nullable;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * <p>Global media-related utilities.</p>
 */
public final class UtilsMedia {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsMedia() {
	}

	public static final int AUDIO = 0;
	public static final int PHOTO = 1;
	public static final int VIDEO = 2;
	public static final int SCREENSHOT = 3;
	/**
	 * <p>Returns a {@link File} for the specified media type.</p>
	 * <br>
	 * <p>The format is as follows: [type]_YYYY-MM-DD_HH-mm-ss. The "type" can be "AUD" (audio recordings), "PHO"
	 * (photographs), "VID" (video recordings), or "SCR" (screenshots).</p>
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link #AUDIO} --> for {@code media_type}: audio recordings</p>
	 * <p>- {@link #PHOTO} --> for {@code media_type}: photographs</p>
	 * <p>- {@link #VIDEO} --> for {@code media_type}: video recordings</p>
	 * <p>- {@link #SCREENSHOT} --> for {@code media_type}: screenshots</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @param media_type one of the constants
	 *
	 * @return the {@link File} in case it was possible to generate it, null otherwise (the external storage may not be
	 * mounted or it was not possible to create the necessary sub-directories)
	 */
	@Nullable
	public static File getOutputMediaFile(final int media_type){
		if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			return null;
		}
		String folder = Environment.getExternalStorageDirectory() + File.separator + GL_CONSTS.MEDIA_FOLDER_REL_PATH;
		switch (media_type) {
			case (AUDIO): {
				folder += "Audio recordings" + File.separator;
				break;
			}
			case (PHOTO): {
				folder += "Photos" + File.separator;
				break;
			}
			case (VIDEO): {
				folder += "Video recordings" + File.separator;
				break;
			}
			case (SCREENSHOT): {
				folder += "Screenshots" + File.separator;
				break;
			}
		}

		// Create the storage directory if it does not exist
		// In the beginning it's checked if the storage is available and that method returns true if and only if
		// the storage has read/write access - so the folder can be created. No need to check for nullability.
		final File media_folder = UtilsFilesDirs.createDirectory(folder);
		if (media_folder == null) {
			return null;
		}

		// Create a media file name
		final String time_stamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)
				.format(System.currentTimeMillis());
		File mediaFile = null;
		switch (media_type) {
			case (AUDIO): {
				mediaFile = new File(media_folder.getPath() + File.separator + "AUD_" + time_stamp + ".aac");
				break;
			}
			case (PHOTO): {
				mediaFile = new File(media_folder.getPath() + File.separator + "PHO_" + time_stamp + ".jpg");
				break;
			}
			case (VIDEO): {
				mediaFile = new File(media_folder.getPath() + File.separator + "VID_" + time_stamp + ".mp4");
				break;
			}
			case (SCREENSHOT): {
				mediaFile = new File(media_folder.getPath() + File.separator + "SCR_" + time_stamp + ".jpg");
				break;
			}
		}

		return mediaFile;
	}
}
