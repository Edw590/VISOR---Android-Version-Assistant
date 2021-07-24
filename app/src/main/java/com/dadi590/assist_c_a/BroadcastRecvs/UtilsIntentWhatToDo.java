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

package com.dadi590.assist_c_a.BroadcastRecvs;

import android.content.Intent;
import android.media.AudioManager;
import android.telephony.TelephonyManager;

import androidx.annotation.Nullable;

import com.dadi590.assist_c_a.GlobalUtils.UtilsApp;
import com.dadi590.assist_c_a.MainSrv;
import com.dadi590.assist_c_a.Modules.Speech.Speech2;
import com.dadi590.assist_c_a.Modules.Telephony.SmsMsgsProcessor.SmsMsgsProcessor;

import java.util.LinkedHashMap;

/**
 * Class to contain what to do for each intent action received on the broadcast receivers.
 */
final class UtilsIntentWhatToDo {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsIntentWhatToDo() {
	}

	/**
	 * Method to call to decide what to do with the given intent.
	 *
	 * @param intent the intent to process
	 */
	static void intentWhatToDo(@Nullable final Intent intent) {
		if (/*context == null ||*/ intent == null || intent.getAction() == null) {
			return;
		}

		System.out.println("PPPPPPPPPPPPPPPPPP");
		System.out.println(intent.getAction());

		switch (intent.getAction()) {

			/////////////////////////////////////
			// Power
			/*case Intent.ACTION_USER_PRESENT:
			case Intent.ACTION_USER_UNLOCKED:
			case "com.htc.intent.action.QUICKBOOT_POWERON":
			case "android.intent.action.QUICKBOOT_POWERON":
			case Intent.ACTION_LOCKED_BOOT_COMPLETED:
			case Intent.ACTION_BOOT_COMPLETED: {
				// Try to start the Main Service at all times. Useful if it crashed and the user didn't notice.
				// todo Add infinite actions here, so it's checked every instant if the service is running or not,
				//  for example when switching user - it won't start until we unlock the phone... --> not cool
				// Useful in case it's not a system app. If it is, disable all that don't have to do with boot completed
				// or user present/unlocked, since the persistent flag will take care of restarting the service every
				// time, I think (maybe be sure that's what happens before disabling the actions if it's a system app).

				// todo Also try to find some way of right after remove Admin Mode + Force stop happens, restarting the
				// app through an intent instead of internal code (faster and easier, I think). If not because of being
				// locked and screen off, then get it to do something else before shutting down and detect that with an
				// intent action to restart the app promptly.
				UtilsPermissions.wrapperRequestPerms(null, false);
				UtilsServices.startService(MainSrv.class, true);

				break;

				Not used - all attempts to start the Main Service are in MainBroadcastRecv right in onReceive, so ANY
				detection tries to start the Main Service.
			}*/
			case CONSTS.ACTION_HTC_QCK_POFF:
			case CONSTS.ACTION_ANDR_QCK_POFF:
			case Intent.ACTION_SHUTDOWN: {
				UtilsApp.prepareShutdown();

				if (intent.getBooleanExtra(Intent.EXTRA_SHUTDOWN_USERSPACE_ONLY, false)) {
					final String speak = "Fast shut down detected.";
					MainSrv.getSpeech2().speak(speak, Speech2.NO_ADDITIONAL_COMMANDS, Speech2.PRIORITY_HIGH, null);
				} else {
					final String speak = "Shut down detected.";
					MainSrv.getSpeech2().speak(speak, Speech2.NO_ADDITIONAL_COMMANDS, Speech2.PRIORITY_HIGH, null);
				}
				// Note: must be very small speeches, since the phone will shut down fast.

				break;
			}
			case Intent.ACTION_REBOOT: {
				UtilsApp.prepareShutdown();

				// Might only work if it's a system app (not tested) - doesn't seem to work as normal app
				final String speak = "Reboot detected.";
				MainSrv.getSpeech2().speak(speak, Speech2.NO_ADDITIONAL_COMMANDS, Speech2.PRIORITY_USER_ACTION, null);
				// Note: must be a very small speech, since the phone will shut down fast.


				break;
			}

			/////////////////////////////////////
			// SMS and phone calls
			case CONSTS.ACTION_SMS_RECEIVED_ALL_API: {
				SmsMsgsProcessor.smsMsgsProcessor(intent);

				break;
			}
			case TelephonyManager.ACTION_PHONE_STATE_CHANGED: {
				if (intent.hasExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)) {
					// According to Android Developers website, having both READ_CALL_LOG and READ_PHONE_STATE
					// permissions (which I must, to receive the EXTRA_INCOMING_NUMBER), will make the app receive 2
					// broadcasts. One with and one without the EXTRA_INCOMING_NUMBER (the one without comes from the
					// READ_PHONE_STATE permission.
					// This below is to link the EXTRA_STATEs to the CALL_STATEs.
					final LinkedHashMap<String, Integer> map_EXTRA_STATE_CALL_STATE = new LinkedHashMap<>(0);
					map_EXTRA_STATE_CALL_STATE.put(TelephonyManager.EXTRA_STATE_RINGING, TelephonyManager.CALL_STATE_RINGING);
					map_EXTRA_STATE_CALL_STATE.put(TelephonyManager.EXTRA_STATE_IDLE, TelephonyManager.CALL_STATE_IDLE);
					map_EXTRA_STATE_CALL_STATE.put(TelephonyManager.EXTRA_STATE_OFFHOOK, TelephonyManager.CALL_STATE_OFFHOOK);

					final String state = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
					final String phoneNumber = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
					MainSrv.getPhoneCallsProcessor().phoneNumRecv(map_EXTRA_STATE_CALL_STATE.get(state),
							phoneNumber, false);
					// Not sure what to do with the possible NPE of map_EXTRA_STATE_CALL_STATE. Shouldn't happen, I
					// guess, unless the call states are updated to include a new one or something.
				}

				break;
			}
			case CONSTS.ACTION_PRECISE_CALL_STATE_CHANGED: {
				// See here for more: https://stackoverflow.com/questions/32821952/how-to-use-precisecallstate
				//MainSrv.getPhoneCallProcessor().phoneNumRecv(context, map_EXTRA_STATE_CALL_STATE.get(state), phoneNumber,
				//		true);

				// intent.getIntExtra(TelephonyManager.EXTRA_FOREGROUND_CALL_STATE, -2) --> why EXTRA_FOREGROUND_CALL_STATE?
				// Why not the BACKGROUND one, or the other one, which I don't remember?

				break;
			}

			/////////////////////////////////////
			// Battery
			case Intent.ACTION_BATTERY_CHANGED: {
				MainSrv.getBatteryProcessor().processBatteryLvlChg(intent);

				break;
			}
			case Intent.ACTION_POWER_CONNECTED: {
				MainSrv.getBatteryProcessor().processBatteryPwrChg(true);

				break;
			}
			case Intent.ACTION_POWER_DISCONNECTED: {
				MainSrv.getBatteryProcessor().processBatteryPwrChg(false);

				break;
			}

			/////////////////////////////////////
			// Volume changes
			case AudioManager.VOLUME_CHANGED_ACTION: {
				MainSrv.getSpeech2().setUserChangedVolumeTrue(intent.getIntExtra(AudioManager.EXTRA_VOLUME_STREAM_TYPE,
						Speech2.OPPOSITE_VOL_DND_OBJ_DEFAULT_VALUE));

				break;
			}
		}
	}
}
