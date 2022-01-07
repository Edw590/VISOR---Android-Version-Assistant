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

import com.dadi590.assist_c_a.R;

import java.io.File;

/**
 * <p>Global constants across the project.</p>
 */
public final class GL_CONSTS {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private GL_CONSTS() {
	}

	//Testa o nome novo na app antiga antes de fazeres commit!!!!!!

	// Assistant name and description
	/**
	 * <p>The name of the assistant as it's on {@link R.string#app_name_ACA}, but only what's the before the first space -
	 * real location is on the strings.xml file.</p>
	 */
	public static final String ASSISTANT_NAME = UtilsGeneral.getContext().getString(R.string.app_name_ACA).split(" ")[0];
	/**
	 * <p>The app description exactly as it's on {@link R.string#app_description_ACA} - real location is on the
	 * strings.xml file.</p>
	 */
	public static final String APP_DESCRIPTION = UtilsGeneral.getContext().getString(R.string.app_description_ACA);
	/**
	 * <p>The name of the assistant but with dots removed in case there are dots in {@link #ASSISTANT_NAME} (WO = W/O =
	 * without).</p>
	 * <p>Useful for speech, for example (TTS will spell the name if it has dots).</p>
	 */
	public static final String ASSISTANT_NAME_WO_DOTS = ASSISTANT_NAME.replaceAll("\\.", "");

	// Permission strings
	//public static final String ASSIST_C_A_RECV_PERM = "com.dadi590.assist_c_a.permission.INTERNAL_RECEIVERS";

	// Services
	public static final int NOTIF_ID_MAIN_SRV_FOREGROUND = 1;
	public static final int NOTIF_ID_PLS_SRV_FOREGROUND = 2; // PLS = Protected Lock Screen
	public static final String CH_ID_MAIN_SRV_FOREGROUND = "MainSrv:FOREGROUND";
	public static final String CH_ID_PLS_SRV_FOREGROUND = "ProtectedLockScrSrv:FOREGROUND";

	// Media
	public static final String MEDIA_FOLDER_REL_PATH = ASSISTANT_NAME + File.separator;

	// TTS
	public static final String PREFERRED_TTS_ENGINE = "ivona.tts";
	public static final String PREFERRED_TTS_VOICE = "en-GB-Brian";

	// UTF-7 --> to "put public" the private access of com.beetstra.jutf7.CharsetProvider.UTF7_NAME
	public static final String UTF7_NAME_LIB = "UTF-7";
}
