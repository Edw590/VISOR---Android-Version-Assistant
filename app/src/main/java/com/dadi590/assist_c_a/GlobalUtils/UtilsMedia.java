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

import androidx.annotation.NonNull;

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
	 * @return the {@link File}
	 */
	@NonNull
	public static File getOutputMediaFile(final int media_type){
		String file_path = GL_CONSTS.VISOR_EXT_FOLDER_PATH;
		final String time_stamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)
				.format(System.currentTimeMillis());
		switch (media_type) {
			case (AUDIO): {
				file_path += "Audio recordings/AUD_" + time_stamp + ".aac";
				break;
			}
			case (PHOTO): {
				file_path += "Photos/PIC_" + time_stamp + ".jpg";
				break;
			}
			case (VIDEO): {
				file_path += "Video recordings/VID_" + time_stamp + ".mp4";
				break;
			}
			case (SCREENSHOT): {
				file_path += "Screenshots/SCR_" + time_stamp + ".jpg";
				break;
			}
		}

		return new File(file_path);
	}
}
