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

package com.dadi590.assist_c_a.Modules.CmdsExecutor.CmdsList;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import ACD.ACD;

/**
 * <p>The list of all commands to send to the Advanced Commands Detection module.</p>
 */
public final class CmdsList {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private CmdsList() {
	}

	/**
	 * <p>The IDs of all commands sent to the ACD.</p>
	 */
	public static final class CmdIds {
		public static final String CMD_TOGGLE_FLASHLIGHT = "1";
		public static final String CMD_ASK_TIME = "2";
		public static final String CMD_ASK_DATE = "3";
		public static final String CMD_TOGGLE_WIFI = "4";
		public static final String CMD_TOGGLE_MOBILE_DATA = "5";
		public static final String CMD_TOGGLE_BLUETOOTH = "6";
		public static final String CMD_ANSWER_CALL = "7";
		public static final String CMD_END_CALL = "9";
		public static final String CMD_TOGGLE_SPEAKERS = "10";
		public static final String CMD_TOGGLE_AIRPLANE_MODE = "11";
		public static final String CMD_ASK_BATTERY_PERCENT = "12";
		public static final String CMD_SHUT_DOWN_DEVICE = "13";
		public static final String CMD_REBOOT_DEVICE = "14";
		public static final String CMD_TAKE_PHOTO = "15";
		public static final String CMD_RECORD_MEDIA = "16";
		public static final String CMD_SAY_AGAIN = "17";
		public static final String CMD_CALL_CONTACT = "18";
		public static final String CMD_TOGGLE_POWER_SAVER_MODE = "19";
		public static final String CMD_STOP_RECORD_MEDIA = "20";
		public static final String CMD_MEDIA_STOP = "21";
		public static final String CMD_MEDIA_PAUSE = "22";
		public static final String CMD_MEDIA_PLAY = "23";
		public static final String CMD_MEDIA_NEXT = "24";
		public static final String CMD_MEDIA_PREVIOUS = "25";
		public static final String CMD_CONFIRM = "26";
		public static final String CMD_REJECT = "27";

		/**
		 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
		 */
		private CmdIds() {
		}
	}

	/**
	 * <p>The return IDs of all commands (unless it's a simple one-variant command, like "shut down" - it's only shut
	 * down, not like reboot which can be a normal reboot, to recovery, etc).</p>
	 */
	public static final class CmdRetIds {
		// Do NOT set this to ACD.MAX_SUB_CMDS! It's like this on purpose!
		// ONLY update this AFTER you update ALL the return IDs to match the maximum number of sub-commands.
		// This is checked every time the app starts to see if it matches the ACD constant (sort of like a static
		// assertion) - see the ApplicationClass.
		public static final long LOCAL_MAX_SUB_CMDS = 100_000L; // DO NOT CHANGE WITHOUT READING THE ABOVE

		public static final String RET_ON = ".00001";
		public static final String RET_OFF = ".00002";

		public static final String RET_14_FAST = ".00001";
		public static final String RET_14_NORMAL = ".00002";
		public static final String RET_14_RECOVERY = ".00003";
		public static final String RET_14_SAFE_MODE = ".00004";
		public static final String RET_14_BOOTLOADER = ".00005";

		public static final String RET_15_REAR = ".00001";
		public static final String RET_15_FRONTAL = ".00002";

		public static final String RET_16_AUDIO = ".00001";
		public static final String RET_16_VIDEO_REAR = ".00002";
		public static final String RET_16_VIDEO_FRONTAL = ".00003";

		public static final String RET_20_ANY = ".00001";
		public static final String RET_20_AUDIO = ".00002";
		public static final String RET_20_VIDEO = ".00003";

		/**
		 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
		 */
		private CmdRetIds() {
		}
	}

	/**
	 * <p>Additional info about the commands.</p>
	 */
	public static final class CmdAddInfo {
		/** Signals that the referring command requires the assistant to do something. */
		public static final String CMDi_INF1_DO_SOMETHING = "0";

		/** Signals that the referring command only requires the assistant to say something (like asking what time is it). */
		public static final String CMDi_INF1_ONLY_SPEAK = "1";

		/** Signals that the referring command is an assistance to another command (like saying "I confirm" (the
		 * previous command)). */
		public static final String CMDi_INF1_ASSIST_CMD = "2";

		public static final Map<String, String> CMDi_INFO = new HashMap<String, String>() {
			private static final long serialVersionUID = -8864195772334229619L;

			@NonNull
			@Override
			public HashMap<String, String> clone() throws AssertionError {
				throw new AssertionError();
			}

			{
				put(CmdIds.CMD_TOGGLE_FLASHLIGHT,         CmdAddInfo.CMDi_INF1_DO_SOMETHING);     // 1
				put(CmdIds.CMD_ASK_TIME,                  CmdAddInfo.CMDi_INF1_ONLY_SPEAK);       // 2
				put(CmdIds.CMD_ASK_DATE,                  CmdAddInfo.CMDi_INF1_ONLY_SPEAK);       // 3
				put(CmdIds.CMD_TOGGLE_WIFI,               CmdAddInfo.CMDi_INF1_DO_SOMETHING);     // 4
				put(CmdIds.CMD_TOGGLE_MOBILE_DATA,        CmdAddInfo.CMDi_INF1_DO_SOMETHING);     // 5
				put(CmdIds.CMD_TOGGLE_BLUETOOTH,          CmdAddInfo.CMDi_INF1_DO_SOMETHING);     // 6
				put(CmdIds.CMD_ANSWER_CALL,               CmdAddInfo.CMDi_INF1_DO_SOMETHING);     // 7
				put(CmdIds.CMD_END_CALL,                  CmdAddInfo.CMDi_INF1_DO_SOMETHING);     // 9
				put(CmdIds.CMD_TOGGLE_SPEAKERS,           CmdAddInfo.CMDi_INF1_DO_SOMETHING);     // 10
				put(CmdIds.CMD_TOGGLE_AIRPLANE_MODE,      CmdAddInfo.CMDi_INF1_DO_SOMETHING);     // 11
				put(CmdIds.CMD_ASK_BATTERY_PERCENT,       CmdAddInfo.CMDi_INF1_ONLY_SPEAK);       // 12
				put(CmdIds.CMD_SHUT_DOWN_DEVICE,          CmdAddInfo.CMDi_INF1_DO_SOMETHING);     // 13
				put(CmdIds.CMD_REBOOT_DEVICE,             CmdAddInfo.CMDi_INF1_DO_SOMETHING);     // 14
				put(CmdIds.CMD_TAKE_PHOTO,                CmdAddInfo.CMDi_INF1_DO_SOMETHING);     // 15
				put(CmdIds.CMD_RECORD_MEDIA,              CmdAddInfo.CMDi_INF1_DO_SOMETHING);     // 16
				put(CmdIds.CMD_SAY_AGAIN,                 CmdAddInfo.CMDi_INF1_ONLY_SPEAK);       // 17
				put(CmdIds.CMD_CALL_CONTACT,              CmdAddInfo.CMDi_INF1_DO_SOMETHING);     // 18
				put(CmdIds.CMD_TOGGLE_POWER_SAVER_MODE,   CmdAddInfo.CMDi_INF1_DO_SOMETHING);     // 19
				put(CmdIds.CMD_STOP_RECORD_MEDIA,         CmdAddInfo.CMDi_INF1_DO_SOMETHING);     // 20
				put(CmdIds.CMD_MEDIA_STOP,                CmdAddInfo.CMDi_INF1_DO_SOMETHING);     // 21
				put(CmdIds.CMD_MEDIA_PAUSE,               CmdAddInfo.CMDi_INF1_DO_SOMETHING);     // 22
				put(CmdIds.CMD_MEDIA_PLAY,                CmdAddInfo.CMDi_INF1_DO_SOMETHING);     // 23
				put(CmdIds.CMD_MEDIA_NEXT,                CmdAddInfo.CMDi_INF1_DO_SOMETHING);     // 24
				put(CmdIds.CMD_MEDIA_PREVIOUS,            CmdAddInfo.CMDi_INF1_DO_SOMETHING);     // 25
				put(CmdIds.CMD_CONFIRM,                   CmdAddInfo.CMDi_INF1_ASSIST_CMD);       // 26
				put(CmdIds.CMD_REJECT,                    CmdAddInfo.CMDi_INF1_ASSIST_CMD);     // 27
			}
		};

		/**
		 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
		 */
		private CmdAddInfo() {
		}
	}

	/**
	 * <p>All automated/dynamic commands (means they need to be processed before being sent to the ACD).</p>
	 */
	public static final class AutoCmds {
		public static final String[] CMD_CALL_CONTACT = {CmdsList.CmdIds.CMD_CALL_CONTACT, ACD.CMDi_TYPE_MANUAL, "call", "", ""};                           // 18

		/**
		 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
		 */
		private AutoCmds() {
		}
	}

	static final String[][] CMDS_LIST = {
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
			{CmdIds.CMD_RECORD_MEDIA, ACD.CMDi_TYPE_RECORD, "", "", "audio/sound|video/camera"},                                                            // 16
			{CmdIds.CMD_SAY_AGAIN, ACD.CMDi_TYPE_REPEAT_SPEECH, "", "", "again|say|said"},                                                                  // 17
			{CmdIds.CMD_TOGGLE_POWER_SAVER_MODE, ACD.CMDi_TYPE_TURN_ONFF, "", "", "power/battery saver"},                                                   // 19
			{CmdIds.CMD_STOP_RECORD_MEDIA, ACD.CMDi_TYPE_STOP, "", "", "recording/record|recording/record audio/sound"},                                    // 20
			{CmdIds.CMD_MEDIA_STOP, ACD.CMDi_TYPE_MANUAL, "stop", "", "media/song/songs/music/musics/video/videos"},                                        // 21
			{CmdIds.CMD_MEDIA_PAUSE, ACD.CMDi_TYPE_MANUAL, "pause", "", "media/song/songs/music/musics/video/videos"},                                      // 22
			{CmdIds.CMD_MEDIA_PLAY, ACD.CMDi_TYPE_MANUAL, "play continue resume", "", "media/song/songs/music/musics/video/videos"},                        // 23
			{CmdIds.CMD_MEDIA_NEXT, ACD.CMDi_TYPE_MANUAL, "next", "", "media/song/songs/music/musics/video/videos"},                                        // 24
			{CmdIds.CMD_MEDIA_PREVIOUS, ACD.CMDi_TYPE_MANUAL, "previous", "", "media/song/songs/music/musics/video/videos"},                                // 25
			{CmdIds.CMD_CONFIRM, ACD.CMDi_TYPE_MANUAL, "i", "", "do/confirm/approve/certify"},                                                              // 26
			{CmdIds.CMD_REJECT, ACD.CMDi_TYPE_MANUAL, "i", "", "don't/reject/disapprove"},                                                                  // 27
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
			"Record audio/sound",                                                    // 16
			"(Ask to repeat what was just said)",                                    // 17
			"Call (a contact name)",                                                 // 18
			"Turn on/off the power/battery saver mode",                              // 19
			"Stop recording [audio/sound]",                                          // 20
			"Stop media/song/music/video",                                           // 21
			"Pause media/song/music/video",                                          // 22
			"Play media/song/music/video",                                           // 23
			"Next media/song/music/video",                                           // 24
			"Previous media/song/music/video",                                       // 25
			"I do/confirm/approve/certify (confirm action when VISOR requests it)",  // 26
			"I don't/reject/disapprove (reject action instead)",                     // 27
	};
}
