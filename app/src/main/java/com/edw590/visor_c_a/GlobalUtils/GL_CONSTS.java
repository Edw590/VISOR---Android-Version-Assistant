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

package com.edw590.visor_c_a.GlobalUtils;

import android.os.Environment;

/**
 * <p>Global constants across the project.</p>
 */
public final class GL_CONSTS {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private GL_CONSTS() {
	}

	// Permission strings
	//public static final String VISOR_C_A_RECV_PERM = "com.edw590.visor_c_a.permission.INTERNAL_RECEIVERS";

	// Notifications
	public static final int NOTIF_ID_MAIN_SRV_FOREGROUND = 1;
	public static final int NOTIF_ID_PLS_SRV_FOREGROUND = 2;
	public static final int NOTIF_ID_SPEECHES = 3;
	public static final int NOTIF_ID_COMMANDS_RECOG_FOREGROUND = 4;
	public static final String CH_ID_MAIN_SRV_FOREGROUND = "MainSrv:FOREGROUND";
	public static final String CH_ID_PLS_SRV_FOREGROUND = "ProtectedLockScrSrv:FOREGROUND";
	public static final String CH_ID_SPEECHES = "Speech2:Speeches";
	public static final String CH_ID_COMMANDS_RECOG_FOREGROUND = "CommandsRecognition:FOREGROUND";

	// Media
	/** The complete path to the VISOR folder on the external storage. */
	public static final String VISOR_EXT_FOLDER_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() +
			"/VISOR/";

	// TTS
	public static final String PREFERRED_TTS_ENGINE = "ivona.tts";
	public static final String PREFERRED_TTS_VOICE = "en-GB-Brian";

	// UTF-7 --> to "put public" the private access of com.beetstra.jutf7.CharsetProvider.UTF7_NAME
	public static final String UTF7_NAME_LIB = "UTF-7";
}
