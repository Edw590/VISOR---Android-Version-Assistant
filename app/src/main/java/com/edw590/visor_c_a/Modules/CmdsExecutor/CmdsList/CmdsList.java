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

package com.edw590.visor_c_a.Modules.CmdsExecutor.CmdsList;

import androidx.annotation.NonNull;

import java.util.LinkedHashMap;

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
		public static final String CMD_POWER_SHUT_DOWN = "13";
		public static final String CMD_POWER_REBOOT = "14";
		public static final String CMD_TAKE_PHOTO = "15";
		public static final String CMD_RECORD_MEDIA = "16";
		public static final String CMD_SAY_AGAIN = "17";
		public static final String CMD_CALL_CONTACT = "18";
		public static final String CMD_TOGGLE_POWER_SAVER_MODE = "19";
		public static final String CMD_STOP_RECORD_MEDIA = "20";
		public static final String CMD_CONTROL_MEDIA = "21";
		public static final String CMD_STOP_LISTENING = "24";
		public static final String CMD_START_LISTENING = "25";
		public static final String CMD_TELL_WEATHER = "26";
		public static final String CMD_TELL_NEWS = "27";
		public static final String CMD_GONNA_SLEEP = "28";
		public static final String CMD_ASK_EVENTS = "31";

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
		public static final long LOCAL_MAX_SUB_CMDS = 100_000; // DO NOT CHANGE WITHOUT READING THE ABOVE

		public static final String RET_ON = ".00001";
		public static final String RET_OFF = ".00002";

		public static final String RET_14_FAST = ".00001";
		public static final String RET_14_NORMAL = ".00002";
		public static final String RET_14_RECOVERY = ".00003";
		public static final String RET_14_SAFE_MODE = ".00004";
		public static final String RET_14_BOOTLOADER = ".00005";

		public static final String RET_15_REAR = ".00001";
		public static final String RET_15_FRONTAL = ".00002";

		public static final String RET_16_AUDIO_1 = ".00001";
		public static final String RET_16_AUDIO_2 = ".00004";
		public static final String RET_16_VIDEO_1 = ".00002";
		public static final String RET_16_VIDEO_2 = ".00005";
		public static final String RET_16_SCREEN_1 = ".00003";
		public static final String RET_16_SCREEN_2 = ".00006";

		public static final String RET_20_ANY = ".00001";
		public static final String RET_20_AUDIO = ".00002";
		public static final String RET_20_VIDEO = ".00003";
		public static final String RET_20_SCREEN = ".00004";

		public static final String RET_21_PLAY = ".00001";
		public static final String RET_21_PAUSE = ".00002";
		public static final String RET_21_STOP = ".00003";
		public static final String RET_21_NEXT = ".00004";
		public static final String RET_21_PREVIOUS = ".00005";

		public static final String RET_31_TODAY = ".00001";
		public static final String RET_31_TOMORROW = ".00002";
		public static final String RET_31_THIS_WEEK = ".00003";
		public static final String RET_31_NEXT_WEEK = ".00004";

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

		public static final LinkedHashMap<String, String> CMDi_INFO = new LinkedHashMap<String, String>() {
			private static final long serialVersionUID = -8864195772334229619L;

			@NonNull
			@Override
			public LinkedHashMap<String, String> clone() throws AssertionError {
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
				put(CmdIds.CMD_POWER_SHUT_DOWN,           CmdAddInfo.CMDi_INF1_DO_SOMETHING);     // 13
				put(CmdIds.CMD_POWER_REBOOT,              CmdAddInfo.CMDi_INF1_DO_SOMETHING);     // 14
				put(CmdIds.CMD_TAKE_PHOTO,                CmdAddInfo.CMDi_INF1_DO_SOMETHING);     // 15
				put(CmdIds.CMD_RECORD_MEDIA,              CmdAddInfo.CMDi_INF1_DO_SOMETHING);     // 16
				put(CmdIds.CMD_SAY_AGAIN,                 CmdAddInfo.CMDi_INF1_ONLY_SPEAK);       // 17
				put(CmdIds.CMD_CALL_CONTACT,              CmdAddInfo.CMDi_INF1_DO_SOMETHING);     // 18
				put(CmdIds.CMD_TOGGLE_POWER_SAVER_MODE,   CmdAddInfo.CMDi_INF1_DO_SOMETHING);     // 19
				put(CmdIds.CMD_STOP_RECORD_MEDIA,         CmdAddInfo.CMDi_INF1_DO_SOMETHING);     // 20
				put(CmdIds.CMD_CONTROL_MEDIA,             CmdAddInfo.CMDi_INF1_DO_SOMETHING);     // 21
				put(CmdIds.CMD_STOP_LISTENING,            CmdAddInfo.CMDi_INF1_DO_SOMETHING);     // 24
				put(CmdIds.CMD_START_LISTENING,           CmdAddInfo.CMDi_INF1_DO_SOMETHING);     // 25
				put(CmdIds.CMD_TELL_WEATHER,              CmdAddInfo.CMDi_INF1_ONLY_SPEAK);       // 26
				put(CmdIds.CMD_TELL_NEWS,                 CmdAddInfo.CMDi_INF1_ONLY_SPEAK);       // 27
				put(CmdIds.CMD_GONNA_SLEEP,               CmdAddInfo.CMDi_INF1_ONLY_SPEAK);       // 28
				put(CmdIds.CMD_ASK_EVENTS,                CmdAddInfo.CMDi_INF1_ONLY_SPEAK);       // 31
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
		public static final String[] CMD_CALL_CONTACT = {CmdsList.CmdIds.CMD_CALL_CONTACT, ACD.CMDi_TYPE_NONE, "call", "", ""};                             // 18

		/**
		 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
		 */
		private AutoCmds() {
		}
	}

	static final String[][] CMDS_LIST = {
			{CmdIds.CMD_TOGGLE_FLASHLIGHT, ACD.CMDi_TYPE_TURN_ONFF, "", "", "flashlight/lantern"},                                                          // 1
			{CmdIds.CMD_ASK_TIME, ACD.CMDi_TYPE_ASK, "", "", "time"},                                                                                       // 2
			{CmdIds.CMD_ASK_DATE, ACD.CMDi_TYPE_ASK, "", "", "date/day/month/year"},                                                                        // 3
			{CmdIds.CMD_TOGGLE_WIFI, ACD.CMDi_TYPE_TURN_ONFF, "", "", "wifi"},                                                                              // 4
			{CmdIds.CMD_TOGGLE_MOBILE_DATA, ACD.CMDi_TYPE_TURN_ONFF, "", "", "mobile data"},                                                                // 5
			{CmdIds.CMD_TOGGLE_BLUETOOTH, ACD.CMDi_TYPE_TURN_ONFF, "", "", "bluetooth"},                                                                    // 6
			{CmdIds.CMD_ANSWER_CALL, ACD.CMDi_TYPE_ANSWER, "", "", "call"},                                                                                 // 7
			{CmdIds.CMD_END_CALL, ACD.CMDi_TYPE_STOP, "", "", "call"},                                                                                      // 9
			{CmdIds.CMD_TOGGLE_SPEAKERS, ACD.CMDi_TYPE_TURN_ONFF, "", "", "speakerphone/speaker/speakers"},                                                 // 10
			{CmdIds.CMD_TOGGLE_AIRPLANE_MODE, ACD.CMDi_TYPE_TURN_ONFF, "", "", "airplane mode"},                                                            // 11
			{CmdIds.CMD_ASK_BATTERY_PERCENT, ACD.CMDi_TYPE_ASK, "", "", "battery percentage/status/level/levels"},                                          // 12
			{CmdIds.CMD_POWER_SHUT_DOWN, ACD.CMDi_TYPE_SHUT_DOWN, "", "", "device/phone"},                                                                  // 13
			{CmdIds.CMD_POWER_REBOOT, ACD.CMDi_TYPE_REBOOT, "fast", "fast|"+ACD.ANY_MAIN_WORD+" -fast", "reboot/restart device/phone|device/phone|device/phone recovery|device/phone safe mode|device/phone bootloader"}, // 14
			{CmdIds.CMD_TAKE_PHOTO, ACD.CMDi_TYPE_NONE, "take", "", "picture/photo|frontal picture/photo"},                                                 // 15
			{CmdIds.CMD_RECORD_MEDIA, ACD.CMDi_TYPE_START, "record", "record|record|record|"+ACD.ANY_MAIN_WORD+" -record", "audio/sound|video/camera|screen/display|recording audio/sound|recording video/camera|recording screen/display"}, // 16
			{CmdIds.CMD_SAY_AGAIN, ACD.CMDi_TYPE_REPEAT_SPEECH, "", "", "again|say|said"},                                                                  // 17
			// 18 is a dynamic command
			{CmdIds.CMD_TOGGLE_POWER_SAVER_MODE, ACD.CMDi_TYPE_TURN_ONFF, "", "", "power/battery saver"},                                                   // 19
			{CmdIds.CMD_STOP_RECORD_MEDIA, ACD.CMDi_TYPE_STOP, "", "", "recording/record|recording/record audio/sound|recording/record video/camera|recording/record screen/display"}, // 20
			{CmdIds.CMD_CONTROL_MEDIA, ACD.CMDi_TYPE_NONE, "play continue resume pause stop next previous", "play continue resume|pause|stop|next|previous", "media/song/songs/music/audio/musics/video/videos"}, // 21
			{CmdIds.CMD_STOP_LISTENING, ACD.CMDi_TYPE_STOP, "", "", "listening"},                                                                           // 24
			{CmdIds.CMD_START_LISTENING, ACD.CMDi_TYPE_START, "", "", "listening"},                                                                         // 25
			{CmdIds.CMD_TELL_WEATHER, ACD.CMDi_TYPE_ASK, "", "", "weather"},                                                                                // 26
			{CmdIds.CMD_TELL_NEWS, ACD.CMDi_TYPE_ASK, "", "", "news"},                                                                                      // 27
			{CmdIds.CMD_GONNA_SLEEP, ACD.CMDi_TYPE_WILL_GO, "", "", "sleep"},                                                                               // 28
			{CmdIds.CMD_ASK_EVENTS, ACD.CMDi_TYPE_ASK, "", "", "have today|have tomorrow|have this week|have next week"},                                   // 31
	};
	static final int CMDS_LIST_len = CMDS_LIST.length;

	public static final String[] CMDS_LIST_description = {
			"Turn on/off flashlight/lantern",                                                // 1
			"(Ask for the time)",                                                            // 2
			"(Ask for the date)",                                                            // 3
			"Turn on/off Wi-Fi",                                                             // 4
			"Turn on/off mobile data",                                                       // 5
			"Turn on/off bluetooth",                                                         // 6
			"Answer call",                                                                   // 7
			"End call",                                                                      // 9
			"Turn on/off speakerphone/speaker/speakers",                                     // 10
			"Turn on/off airplane mode",                                                     // 11
			"(Ask for the battery percentage/status/level)",                                 // 12
			"Shut down the device/phone",                                                    // 13
			"[Fast] reboot the device/phone [into recovery/safe mode/bootloader]",           // 14
			"Take a [frontal] picture/photo",                                                // 15
			"Record audio/sound",                                                            // 16
			"(Ask to repeat what was just said)",                                            // 17
			"Call (a contact name)",                                                         // 18
			"Turn on/off the power/battery saver mode",                                      // 19
			"Stop recording [audio/sound]",                                                  // 20
			"Play/pause/stop/next/previous media (or song, video, etc)",                     // 21
			"Stop listening (hotword recognizer - useful if VISOR is not a " +               // 24
					"system app, in which case he'll lock the microphone on himself)",
			"Start listening (if stopped, to start again - or hold the Power button " +      // 25
					"(or Home if VISOR is the device's assistant))",
			"(Ask for the weather)",                                                         // 26
			"(Ask for the news)",                                                            // 27
			"(Say that the user is going to sleep)",                                         // 28
			"(Ask what you have for today/tomorrow/this week/next week - Google Calendar events and tasks (tasks " +
					"are only retrieved if you ask for today or tomorrow). After asking and if you asked with the server " +
					"connected (meaning will be the LLM answering), you can then talk normally about your events and tasks " +
					"with VISOR)",                                                           // 31
	};

	@NonNull
	public static DialogMan.Intent[] getIntentList() {
		DialogMan.Intent intent1 = new DialogMan.Intent();
		intent1.setAcd_cmd_id(CmdIds.CMD_TOGGLE_FLASHLIGHT);
		intent1.setTask_name("Toggle the flashlight");

		DialogMan.Intent intent2 = new DialogMan.Intent();
		intent2.setAcd_cmd_id(CmdIds.CMD_ASK_TIME);
		intent2.setTask_name("Ask for the time");

		DialogMan.Intent intent3 = new DialogMan.Intent();
		intent3.setAcd_cmd_id(CmdIds.CMD_ASK_DATE);
		intent3.setTask_name("Ask for the date");

		DialogMan.Intent intent4 = new DialogMan.Intent();
		intent4.setAcd_cmd_id(CmdIds.CMD_TOGGLE_WIFI);
		intent4.setTask_name("Toggle the Wi-Fi");

		DialogMan.Intent intent5 = new DialogMan.Intent();
		intent5.setAcd_cmd_id(CmdIds.CMD_TOGGLE_MOBILE_DATA);
		intent5.setTask_name("Toggle the Mobile Data");

		DialogMan.Intent intent6 = new DialogMan.Intent();
		intent6.setAcd_cmd_id(CmdIds.CMD_TOGGLE_BLUETOOTH);
		intent6.setTask_name("Toggle the Bluetooth");

		DialogMan.Intent intent7 = new DialogMan.Intent();
		intent7.setAcd_cmd_id(CmdIds.CMD_ANSWER_CALL);
		intent7.setTask_name("Answer the call");

		DialogMan.Intent intent9 = new DialogMan.Intent();
		intent9.setAcd_cmd_id(CmdIds.CMD_END_CALL);
		intent9.setTask_name("End the call");

		DialogMan.Intent intent10 = new DialogMan.Intent();
		intent10.setAcd_cmd_id(CmdIds.CMD_TOGGLE_SPEAKERS);
		intent10.setTask_name("Toggle the speakerphone");

		DialogMan.Intent intent11 = new DialogMan.Intent();
		intent11.setAcd_cmd_id(CmdIds.CMD_TOGGLE_AIRPLANE_MODE);
		intent11.setTask_name("Toggle the airplane mode");

		DialogMan.Intent intent12 = new DialogMan.Intent();
		intent12.setAcd_cmd_id(CmdIds.CMD_ASK_BATTERY_PERCENT);
		intent12.setTask_name("Ask for the battery percentage");

		DialogMan.Intent intent13 = new DialogMan.Intent();
		intent13.setAcd_cmd_id(CmdIds.CMD_POWER_SHUT_DOWN);
		intent13.setTask_name("Shut down the device");

		DialogMan.Intent intent14 = new DialogMan.Intent();
		intent14.setAcd_cmd_id(CmdIds.CMD_POWER_REBOOT);
		intent14.setTask_name("Reboot the device");

		DialogMan.Intent intent15 = new DialogMan.Intent();
		intent15.setAcd_cmd_id(CmdIds.CMD_TAKE_PHOTO);
		intent15.setTask_name("Take a photo");

		DialogMan.Intent intent16 = new DialogMan.Intent();
		intent16.setAcd_cmd_id(CmdIds.CMD_RECORD_MEDIA);
		intent16.setTask_name("Record media");

		DialogMan.Intent intent17 = new DialogMan.Intent();
		intent17.setAcd_cmd_id(CmdIds.CMD_SAY_AGAIN);
		intent17.setTask_name("Say again");

		DialogMan.Intent intent18 = new DialogMan.Intent();
		intent18.setAcd_cmd_id(CmdIds.CMD_CALL_CONTACT);
		intent18.setTask_name("Call a contact");

		DialogMan.Intent intent19 = new DialogMan.Intent();
		intent19.setAcd_cmd_id(CmdIds.CMD_TOGGLE_POWER_SAVER_MODE);
		intent19.setTask_name("Toggle the power saver mode");

		DialogMan.Intent intent20 = new DialogMan.Intent();
		intent20.setAcd_cmd_id(CmdIds.CMD_STOP_RECORD_MEDIA);
		intent20.setTask_name("Stop recording media");

		DialogMan.Intent intent21 = new DialogMan.Intent();
		intent21.setAcd_cmd_id(CmdIds.CMD_CONTROL_MEDIA);
		intent21.setTask_name("Control media playback");

		DialogMan.Intent intent24 = new DialogMan.Intent();
		intent24.setAcd_cmd_id(CmdIds.CMD_STOP_LISTENING);
		intent24.setTask_name("Stop listening");

		DialogMan.Intent intent25 = new DialogMan.Intent();
		intent25.setAcd_cmd_id(CmdIds.CMD_START_LISTENING);
		intent25.setTask_name("Start listening");

		DialogMan.Intent intent26 = new DialogMan.Intent();
		intent26.setAcd_cmd_id(CmdIds.CMD_TELL_WEATHER);
		intent26.setTask_name("Tell the weather");

		DialogMan.Intent intent27 = new DialogMan.Intent();
		intent27.setAcd_cmd_id(CmdIds.CMD_TELL_NEWS);
		intent27.setTask_name("Tell the news");

		DialogMan.Intent intent28 = new DialogMan.Intent();
		intent28.setAcd_cmd_id(CmdIds.CMD_GONNA_SLEEP);
		intent28.setTask_name("Gonna sleep");

		DialogMan.Intent intent31 = new DialogMan.Intent();
		intent31.setAcd_cmd_id(CmdIds.CMD_ASK_EVENTS);
		intent31.setTask_name("Ask for events");

		return new DialogMan.Intent[] {
				intent1, intent2, intent3, intent4, intent5, intent6, intent7, intent9, intent10, intent11, intent12,
				intent13, intent14, intent15, intent16, intent17, intent18, intent19, intent20, intent21, intent24,
				intent25, intent26, intent27, intent28, intent31
		};
	}
}
