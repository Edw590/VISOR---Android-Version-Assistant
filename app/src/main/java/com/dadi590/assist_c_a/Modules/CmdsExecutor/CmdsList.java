/*
 * Copyright 2022 DADi590
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

package com.dadi590.assist_c_a.Modules.CmdsExecutor;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import ACD.ACD;

public class CmdsList {

	class CmdIds {
		static final String CMD_TOGGLE_FLASHLIGHT = "1";
		static final String CMD_ASK_TIME = "2";
		static final String CMD_ASK_DATE = "3";
		static final String CMD_TOGGLE_WIFI = "4";
		static final String CMD_TOGGLE_MOBILE_DATA = "5";
		static final String CMD_TOGGLE_BLUETOOTH = "6";
		static final String CMD_ANSWER_CALL = "7";
		static final String CMD_END_CALL = "9";
		static final String CMD_TOGGLE_SPEAKERS = "10";
		static final String CMD_TOGGLE_AIRPLANE_MODE = "11";
		static final String CMD_ASK_BATTERY_PERCENT = "12";
		static final String CMD_SHUT_DOWN_DEVICE = "13";
		static final String CMD_REBOOT_DEVICE = "14";
		static final String CMD_TAKE_PHOTO = "15";
		static final String CMD_RECORD_MEDIA = "16";
		static final String CMD_SAY_AGAIN = "17";
		static final String CMD_MAKE_CALL = "18";
		static final String CMD_TOGGLE_POWER_SAVER_MODE = "19";
		static final String CMD_STOP_RECORD_MEDIA = "20";
		static final String CMD_MEDIA_STOP = "21";
		static final String CMD_MEDIA_PAUSE = "22";
		static final String CMD_MEDIA_PLAY = "23";
		static final String CMD_MEDIA_NEXT = "24";
		static final String CMD_MEDIA_PREVIOUS = "25";
	}
	class CmdRetIds {
		static final String RET_ON = ".01";
		static final String RET_OFF = ".02";

		static final String RET_14_FAST = ".01";
		static final String RET_14_NORMAL = ".02";
		static final String RET_14_RECOVERY = ".03";
		static final String RET_14_SAFE_MODE = ".04";
		static final String RET_14_BOOTLOADER = ".05";

		static final String RET_15_REAR = ".01";
		static final String RET_15_FRONTAL = ".02";

		static final String RET_16_AUDIO = ".01";
		static final String RET_16_VIDEO_REAR = ".02";
		static final String RET_16_VIDEO_FRONTAL = ".03";

		static final String RET_20_ANY = ".01";
		static final String RET_20_AUDIO = ".02";
		static final String RET_20_VIDEO = ".03";
	}

	class CmdAddInfo {
		// CMDi_INF1_DO_SOMETHING signals that the referring command requires the assistant to do something.
		static final String CMDi_INF1_DO_SOMETHING = "0";

		// CMDi_INF1_ONLY_SPEAK signals that the referring command only requires the assistant to say something (like
		// asking what time is it).
		static final String CMDi_INF1_ONLY_SPEAK = "1";
	}

	static final Map<?, ?> CMDi_INFO = new HashMap<String, String>() {
			private static final long serialVersionUID = -8864195772334229619L;

			@NonNull
			@Override
			public HashMap<String, String> clone() throws AssertionError {
				throw new AssertionError();
			}

			{
				put(CmdIds.CMD_TOGGLE_FLASHLIGHT,         CmdAddInfo.CMDi_INF1_DO_SOMETHING); // 1
				put(CmdIds.CMD_ASK_TIME,                  CmdAddInfo.CMDi_INF1_ONLY_SPEAK);   // 2
				put(CmdIds.CMD_ASK_DATE,                  CmdAddInfo.CMDi_INF1_ONLY_SPEAK);   // 3
				put(CmdIds.CMD_TOGGLE_WIFI,               CmdAddInfo.CMDi_INF1_DO_SOMETHING); // 4
				put(CmdIds.CMD_TOGGLE_MOBILE_DATA,        CmdAddInfo.CMDi_INF1_DO_SOMETHING); // 5
				put(CmdIds.CMD_TOGGLE_BLUETOOTH,          CmdAddInfo.CMDi_INF1_DO_SOMETHING); // 6
				put(CmdIds.CMD_ANSWER_CALL,               CmdAddInfo.CMDi_INF1_DO_SOMETHING); // 7
				put(CmdIds.CMD_END_CALL,                  CmdAddInfo.CMDi_INF1_DO_SOMETHING); // 9
				put(CmdIds.CMD_TOGGLE_SPEAKERS,           CmdAddInfo.CMDi_INF1_DO_SOMETHING); // 10
				put(CmdIds.CMD_TOGGLE_AIRPLANE_MODE,      CmdAddInfo.CMDi_INF1_DO_SOMETHING); // 11
				put(CmdIds.CMD_ASK_BATTERY_PERCENT,       CmdAddInfo.CMDi_INF1_ONLY_SPEAK);   // 12
				put(CmdIds.CMD_SHUT_DOWN_DEVICE,          CmdAddInfo.CMDi_INF1_DO_SOMETHING); // 13
				put(CmdIds.CMD_REBOOT_DEVICE,             CmdAddInfo.CMDi_INF1_DO_SOMETHING); // 14
				put(CmdIds.CMD_TAKE_PHOTO,                CmdAddInfo.CMDi_INF1_DO_SOMETHING); // 15
				put(CmdIds.CMD_RECORD_MEDIA,              CmdAddInfo.CMDi_INF1_DO_SOMETHING); // 16
				put(CmdIds.CMD_SAY_AGAIN,                 CmdAddInfo.CMDi_INF1_ONLY_SPEAK);   // 17
				put(CmdIds.CMD_MAKE_CALL,                 CmdAddInfo.CMDi_INF1_DO_SOMETHING); // 18
				put(CmdIds.CMD_TOGGLE_POWER_SAVER_MODE,   CmdAddInfo.CMDi_INF1_DO_SOMETHING); // 19
				put(CmdIds.CMD_STOP_RECORD_MEDIA,         CmdAddInfo.CMDi_INF1_DO_SOMETHING); // 20
				put(CmdIds.CMD_MEDIA_STOP,                CmdAddInfo.CMDi_INF1_DO_SOMETHING); // 21
				put(CmdIds.CMD_MEDIA_PAUSE,               CmdAddInfo.CMDi_INF1_DO_SOMETHING); // 22
				put(CmdIds.CMD_MEDIA_PLAY,                CmdAddInfo.CMDi_INF1_DO_SOMETHING); // 23
				put(CmdIds.CMD_MEDIA_NEXT,                CmdAddInfo.CMDi_INF1_DO_SOMETHING); // 24
				put(CmdIds.CMD_MEDIA_PREVIOUS,            CmdAddInfo.CMDi_INF1_DO_SOMETHING); // 25
			}
	};

	private static final String[][] CMDS_LIST = {
			{CmdIds.CMD_TOGGLE_FLASHLIGHT, ACD.CMDi_TYPE_TURN_ONFF, "", "", "flashlight/lantern"},                                                          // 1
			{CmdIds.CMD_ASK_TIME, ACD.CMDi_TYPE_ASK, "", "", "time"},                                                                                       // 2
			{CmdIds.CMD_ASK_DATE, ACD.CMDi_TYPE_ASK, "", "", "date"},                                                                                       // 3
			{CmdIds.CMD_TOGGLE_WIFI, ACD.CMDi_TYPE_TURN_ONFF, "", "", "wifi"},                                                                              // 4
			{CmdIds.CMD_TOGGLE_MOBILE_DATA, ACD.CMDi_TYPE_TURN_ONFF, "", "", "mobile data"},                                                                // 5
			{CmdIds.CMD_TOGGLE_BLUETOOTH, ACD.CMDi_TYPE_TURN_ONFF, "", "", "bluetooth"},                                                                    // 6
			{CmdIds.CMD_ANSWER_CALL, ACD.CMDi_TYPE_ANSWER, "", "", "call"},                                                                                 // 7
			{CmdIds.CMD_END_CALL, ACD.CMDi_TYPE_STOP, "", "", "call"},                                                                                      // 9
			{CmdIds.CMD_TOGGLE_SPEAKERS, ACD.CMDi_TYPE_TURN_ONFF, "", "", "speaker/speakers"},                                                              // 10
			{CmdIds.CMD_TOGGLE_AIRPLANE_MODE, ACD.CMDi_TYPE_TURN_ONFF, "", "", "airplane mode"},                                                            // 11
			{CmdIds.CMD_ASK_BATTERY_PERCENT, ACD.CMDi_TYPE_ASK, "", "", "battery percentage/status/level"},                                                 // 12
			{CmdIds.CMD_SHUT_DOWN_DEVICE, ACD.CMDi_TYPE_SHUT_DOWN, "", "", "device/phone"},                                                                 // 13
			{CmdIds.CMD_REBOOT_DEVICE, ACD.CMDi_TYPE_REBOOT+"+"+ACD.CMDi_TYPE_MANUAL, "fast", "fast|"+ACD.ANY_MAIN_WORD, "reboot/restart device/phone|device/phone|device/phone recovery|device/phone safe mode|device/phone bootloader"},  // 14
			{CmdIds.CMD_TAKE_PHOTO, ACD.CMDi_TYPE_MANUAL, "take", "", "picture/photo|frontal picture/photo"},                                               // 15
			{CmdIds.CMD_RECORD_MEDIA, ACD.CMDi_TYPE_RECORD, "", "", "audio/sound|video/camera|frontal video/camera"},                                       // 16
			{CmdIds.CMD_SAY_AGAIN, ACD.CMDi_TYPE_REPEAT_SPEECH, "", "", "again", "say", "said"},                                                            // 17
			{CmdIds.CMD_MAKE_CALL, ACD.CMDi_TYPE_MANUAL, "make place", "", "call"},                                                                         // 18
			{CmdIds.CMD_TOGGLE_POWER_SAVER_MODE, ACD.CMDi_TYPE_TURN_ONFF, "", "", "power/battery saver"},                                                   // 19
			{CmdIds.CMD_STOP_RECORD_MEDIA, ACD.CMDi_TYPE_STOP, "", "", "recording/record|recording/record audio/sound|recording/record video/camera"},      // 20
			{CmdIds.CMD_MEDIA_STOP, ACD.CMDi_TYPE_MANUAL, "stop", "", "media/song/songs/music/musics/video/videos"},                                        // 21
			{CmdIds.CMD_MEDIA_PAUSE, ACD.CMDi_TYPE_MANUAL, "pause", "", "media/song/songs/music/musics/video/videos"},                                      // 22
			{CmdIds.CMD_MEDIA_PLAY, ACD.CMDi_TYPE_MANUAL, "play continue resume", "", "media/song/songs/music/musics/video/videos"},                        // 23
			{CmdIds.CMD_MEDIA_NEXT, ACD.CMDi_TYPE_MANUAL, "next", "", "media/song/songs/music/musics/video/videos"},                                        // 24
			{CmdIds.CMD_MEDIA_PREVIOUS, ACD.CMDi_TYPE_MANUAL, "previous", "", "media/song/songs/music/musics/video/videos"},                                // 25
	};
	static final int CMDS_LIST_len = CMDS_LIST.length;

	public static final String[] CMDS_LIST_description = {
			"Turn on/off flashlight/lantern",                                        // 1
			"(Ask for the time)",                                                    // 2
			"(Ask for the date)",                                                    // 3
			"Turn on/off Wi-Fi",                                                     // 4
			"Turn on/off mobile data",                                               // 5
			"Turn on/off bluetooth",                                                 // 6
			"Answer call",                                                           // 7
			"End call",                                                              // 9
			"Turn on/off speaker/speakers",                                          // 10
			"Turn on/off airplane mode",                                             // 11
			"(Ask for the battery percentage/status/level)",                         // 12
			"Shut down the device/phone",                                            // 13
			"[Fast] reboot the device/phone [into recovery/safe mode/bootloader]",   // 14
			"Take a [frontal] picture/photo",                                        // 15
			"Record audio/sound OR [frontal] camera/video",                          // 16
			"(Ask to repeat what was just said)",                                    // 17
			"Make a call",                                                           // 18
			"Turn on/off the power/battery saver mode",                              // 19
			"Stop recording [audio/sound OR video/camera]",                          // 20
			"Stop media/song/music/video",                                           // 21
			"Pause media/song/music/video",                                          // 22
			"Play media/song/music/video",                                           // 23
			"Next media/song/music/video",                                           // 24
			"Previous media/song/music/video",                                       // 25
	};

	/**
	 * <p>Encodes {@link #CMDS_LIST} into a string ready to be sent to
	 * {@link ACD#prepareCmdsArray(String)}.</p>
	 *
	 * @return the string
	 */
	@NonNull
	public static String prepareCommandsString() {
		final String[] commands_almost_str = new String[CMDS_LIST.length];
		for (int i = 0; i < CMDS_LIST_len; ++i) {
			commands_almost_str[i] = String.join("||", CMDS_LIST[i]);
		}

		return String.join("\\", commands_almost_str);
	}
}
