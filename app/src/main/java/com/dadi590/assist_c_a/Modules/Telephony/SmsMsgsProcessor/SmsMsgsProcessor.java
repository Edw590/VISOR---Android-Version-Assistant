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

package com.dadi590.assist_c_a.Modules.Telephony.SmsMsgsProcessor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Telephony;
import android.telephony.SmsMessage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dadi590.assist_c_a.GlobalInterfaces.IModuleInst;
import com.dadi590.assist_c_a.GlobalUtils.UtilsCheckHardwareFeatures;
import com.dadi590.assist_c_a.GlobalUtils.UtilsGeneral;
import com.dadi590.assist_c_a.GlobalUtils.UtilsPermsAuths;
import com.dadi590.assist_c_a.Modules.Speech.Speech2;
import com.dadi590.assist_c_a.Modules.Speech.UtilsSpeech2BC;
import com.dadi590.assist_c_a.Modules.Telephony.UtilsTelephony;
import com.dadi590.assist_c_a.ValuesStorage.CONSTS_ValueStorage;
import com.dadi590.assist_c_a.ValuesStorage.ValuesStorage;


/**
 * <p>Processes all SMS messages sent by and to the phone.</p>
 */
public final class SmsMsgsProcessor implements IModuleInst {

	///////////////////////////////////////////////////////////////
	// IModuleInst stuff
	@Override
	public boolean isFullyWorking() {
		// Always fully-working. The module is currently a "static module" (it's not instantiated).
		return true;
	}
	@Override
	public void destroy() {
		// Nothing to destroy. The module is currently a "static module" (it's not instantiated).
	}
	@Override
	public final int wrongIsSupported() {return 0;}
	/**.
	 * @return read all here {@link IModuleInst#wrongIsSupported()} */
	public static boolean isSupported() {
		final String[][] min_required_permissions = {{
				Manifest.permission.RECEIVE_SMS,
		}};
		return UtilsPermsAuths.checkSelfPermissions(min_required_permissions)[0]
				&& UtilsCheckHardwareFeatures.isTelephonySupported(false);
	}
	// IModuleInst stuff
	///////////////////////////////////////////////////////////////

	/**
	 * <p>Main class constructor.</p>>
	 */
	@SuppressLint("InlinedApi")
	public SmsMsgsProcessor() {
		try {
			final IntentFilter intentFilter = new IntentFilter();

			// Ignore the warning below. The constants exists on API 15, so exists >= API 15.
			intentFilter.addAction(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);

			UtilsGeneral.getContext().registerReceiver(broadcastReceiver, new IntentFilter(intentFilter));
		} catch (final IllegalArgumentException ignored) {
		}
	}

	/**
	 * <p>Process new SMS messages.</p>
	 *
	 * @param sms_messages a list of SMS messages (or only one) as returned by SMS received intent actions
	 */
	static void processSmsMsgs(@NonNull final SmsMessage[] sms_messages) {
		for (final SmsMessage sms_message : sms_messages) {
			final String sender = sms_message.getOriginatingAddress();
			//String message = stringBuilder.toString();
			System.out.println("&&&&&&&&&&&&&&&&&");
			System.out.println(sender);
			//System.out.println(message);
			System.out.println("&&&&&&&&&&&&&&&&&");

			// Update the Values Storage
			ValuesStorage.updateValue(CONSTS_ValueStorage.last_sms_msg_time, Long.toString(System.currentTimeMillis()));
			ValuesStorage.updateValue(CONSTS_ValueStorage.last_sms_msg_number, sender);

			if (UtilsTelephony.isPrivateNumber(sender)) {
				final String speak = "Sir, attention! New message from a private number!";
				UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_MEDIUM, null);
			} else {
				final String number_name = UtilsTelephony.getWhatToSayAboutNumber(sender);
				final String speak = "Sir, new message from " + number_name + ".";
				UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_MEDIUM, null);
			}
		}
	}

	public final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		@SuppressLint("NewApi")
		public void onReceive(@Nullable final Context context, @Nullable final Intent intent) {
			if (intent == null || intent.getAction() == null) {
				return;
			}

			System.out.println("PPPPPPPPPPPPPPPPPP-SmsMsgsProcessor - " + intent.getAction());

			switch (intent.getAction()) {
				////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
				////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
				////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////

				case (Telephony.Sms.Intents.SMS_RECEIVED_ACTION): {
					// Ignore the warning below. The Telephony class was in @hide before being released to the public.
					final SmsMessage[] sms_messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);

					processSmsMsgs(sms_messages);

					break;
				}

				////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
				////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
				////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
			}
		}
	};
}
