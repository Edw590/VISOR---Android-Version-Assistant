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
import android.telephony.TelephonyManager;

import androidx.annotation.Nullable;

import com.dadi590.assist_c_a.GlobalUtils.UtilsApp;
import com.dadi590.assist_c_a.MainSrv.MainSrv;
import com.dadi590.assist_c_a.Modules.Speech.Speech2;
import com.dadi590.assist_c_a.Modules.Speech.UtilsSpeech2BC;
import com.dadi590.assist_c_a.Modules.Telephony.SmsMsgsProcessor.SmsMsgsProcessor;

import java.util.LinkedHashMap;

/**
 * <p>Class to contain what to do for each intent action received on the main broadcast receivers.</p>
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

		System.out.println("PPPPPPPPPPPPPPPPPP-Main - " + intent.getAction());

		switch (intent.getAction()) {

			/////////////////////////////////////
			// Shut down and reboot
			case (CONSTS.ACTION_HTC_QCK_POFF):
			case (CONSTS.ACTION_ANDR_QCK_POFF):
			case (Intent.ACTION_SHUTDOWN): {
				UtilsApp.prepareShutdown();

				if (intent.getBooleanExtra(Intent.EXTRA_SHUTDOWN_USERSPACE_ONLY, false)) {
					final String speak = "Fast shut down detected.";
					UtilsSpeech2BC.speak(speak, null, Speech2.PRIORITY_HIGH, null);
				} else {
					final String speak = "Shut down detected.";
					UtilsSpeech2BC.speak(speak, null, Speech2.PRIORITY_HIGH, null);
				}
				// Note: must be very small speeches, since the phone will shut down fast.

				break;
			}
			case (Intent.ACTION_REBOOT): {
				UtilsApp.prepareShutdown();

				// No idea if this is supposed detected at all (might be stopped before it gets here by the system as
				// soon as it detects it or something).
				final String speak = "Reboot detected.";
				UtilsSpeech2BC.speak(speak, null, Speech2.PRIORITY_HIGH, null);
				// Note: must be a very small speech, since the phone will shut down fast.


				break;
			}

			/////////////////////////////////////
			// SMS and phone calls
			case (CONSTS.ACTION_SMS_RECEIVED_ALL_API): {
				SmsMsgsProcessor.smsMsgsProcessor(intent);

				break;
			}
			case (TelephonyManager.ACTION_PHONE_STATE_CHANGED): {
				if (intent.hasExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)) {
					// According to Android Developers website, having both READ_CALL_LOG and READ_PHONE_STATE
					// permissions (which I must, to receive the EXTRA_INCOMING_NUMBER), will make the app receive 2
					// broadcasts. One with and one without the EXTRA_INCOMING_NUMBER (the one without comes from the
					// READ_PHONE_STATE permission.
					// This below is to link the EXTRA_STATEs to the CALL_STATEs.
					final LinkedHashMap<String, Integer> map_EXTRA_STATE_CALL_STATE = new LinkedHashMap<>(3);
					map_EXTRA_STATE_CALL_STATE.put(TelephonyManager.EXTRA_STATE_RINGING, TelephonyManager.CALL_STATE_RINGING);
					map_EXTRA_STATE_CALL_STATE.put(TelephonyManager.EXTRA_STATE_IDLE, TelephonyManager.CALL_STATE_IDLE);
					map_EXTRA_STATE_CALL_STATE.put(TelephonyManager.EXTRA_STATE_OFFHOOK, TelephonyManager.CALL_STATE_OFFHOOK);

					final String state = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
					final String phoneNumber = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
					final Integer call_state = map_EXTRA_STATE_CALL_STATE.get(state);
					if (call_state != null) {
						MainSrv.getPhoneCallsProcessor().phoneNumRecv(call_state, phoneNumber, false);
					}
					// Not sure what to do with the possible NPE of map_EXTRA_STATE_CALL_STATE. Shouldn't happen, I
					// guess, unless the call states are updated to include a new one or something.
				}

				break;
			}
			case (CONSTS.ACTION_PRECISE_CALL_STATE_CHANGED): {
				// See here for more: https://stackoverflow.com/questions/32821952/how-to-use-precisecallstate
				//MainSrv.getPhoneCallProcessor().phoneNumRecv(context, map_EXTRA_STATE_CALL_STATE.get(state), phoneNumber,
				//		true);

				// intent.getIntExtra(TelephonyManager.EXTRA_FOREGROUND_CALL_STATE, -2) --> why EXTRA_FOREGROUND_CALL_STATE?
				// Why not the BACKGROUND one, or the other one, which I don't remember?

				break;
			}

			/////////////////////////////////////
			// Battery / Power
			case (Intent.ACTION_BATTERY_CHANGED): {
				MainSrv.getBatteryProcessor().processBatteryLvlChg(intent);

				break;
			}
			case (Intent.ACTION_POWER_CONNECTED): {
				MainSrv.getBatteryProcessor().processBatteryPwrChg(true);

				break;
			}
			case (Intent.ACTION_POWER_DISCONNECTED): {
				MainSrv.getBatteryProcessor().processBatteryPwrChg(false);

				break;
			}
		}
	}
}
