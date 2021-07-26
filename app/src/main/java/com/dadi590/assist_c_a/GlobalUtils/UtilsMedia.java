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

import com.dadi590.assist_c_a.Modules.Speech.Speech2;
import com.dadi590.assist_c_a.Modules.Speech.UtilsSpeech2BC;

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
	 * <p>- {@link #AUDIO} --> media type for audio recordings</p>
	 * <p>- {@link #PHOTO} --> media type for photographs</p>
	 * <p>- {@link #VIDEO} --> media type for video recordings</p>
	 * <p>- {@link #SCREENSHOT} --> media type for screenshots</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @param media_type one of the constants
	 *
	 * @return the {@link File} in case it was possible to generate a file, null otherwise
	 */
	@Nullable
	public static File getOutputMediaFile(final int media_type){
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			String folder = GL_CONSTS.MEDIA_FOLDER;
			switch (media_type) {
				case (AUDIO): {
					folder += "Audio recordings";
					break;
				}
				case (PHOTO): {
					folder += "Photos";
					break;
				}
				case (VIDEO): {
					folder += "Video recordings";
					break;
				}
				case (SCREENSHOT): {
					folder += "Screenshots";
					break;
				}
			}
			final File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), folder);
			// This location works best if you want the created images to be shared between applications and persist
			// after your app has been uninstalled.

			// Create the storage directory if it does not exist
			if (!mediaStorageDir.exists()) {
				mediaStorageDir.mkdirs();
				// In the beginning it's checked if the storage is available and that method returns true if and only if
				// the storage has read/write access - so the folder can be created. No need to check if it was or not.
			}

			// Create a media file name
			final String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)
					.format(System.currentTimeMillis());
			File mediaFile = null;
			switch (media_type) {
				case (AUDIO): {
					mediaFile = new File(mediaStorageDir.getPath() + File.separator + "AUD_" + timeStamp + ".aac");
					break;
				}
				case (PHOTO): {
					mediaFile = new File(mediaStorageDir.getPath() + File.separator + "PHO_" + timeStamp + ".jpg");
					break;
				}
				case (VIDEO): {
					mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
					break;
				}
				case (SCREENSHOT): {
					mediaFile = new File(mediaStorageDir.getPath() + File.separator + "SCR_" + timeStamp + ".jpg");
					break;
				}
			}

			return mediaFile;
		} else {
			final String speak = "There was a problem creating the media file in the external storage as " +
					"it is not mounted.";
			UtilsSpeech2BC.speak(speak, null, Speech2.PRIORITY_USER_ACTION, null);
		}

		return null;
	}
}
