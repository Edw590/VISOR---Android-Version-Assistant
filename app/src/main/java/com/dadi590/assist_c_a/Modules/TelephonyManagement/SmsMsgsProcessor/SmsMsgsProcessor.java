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

package com.dadi590.assist_c_a.Modules.TelephonyManagement.SmsMsgsProcessor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.Telephony;
import android.telephony.SmsMessage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dadi590.assist_c_a.GlobalInterfaces.IModuleInst;
import com.dadi590.assist_c_a.GlobalUtils.UtilsGeneral;
import com.dadi590.assist_c_a.GlobalUtils.UtilsPermsAuths;
import com.dadi590.assist_c_a.Modules.Speech.Speech2;
import com.dadi590.assist_c_a.Modules.Speech.UtilsSpeech2BC;
import com.dadi590.assist_c_a.Modules.TelephonyManagement.TelephonyManagement;
import com.dadi590.assist_c_a.Modules.TelephonyManagement.UtilsTelephony;
import com.dadi590.assist_c_a.ModulesList;
import com.dadi590.assist_c_a.Modules.PreferencesManager.Registry.UtilsRegistry;
import com.dadi590.assist_c_a.Modules.PreferencesManager.Registry.ValuesRegistry;


/**
 * <p>Processes all SMS messages sent by and to the phone.</p>
 */
public final class SmsMsgsProcessor implements IModuleInst {

	private final int element_index = ModulesList.getElementIndex(this.getClass());
	private final HandlerThread main_handlerThread = new HandlerThread((String) ModulesList.getElementValue(element_index,
			ModulesList.ELEMENT_NAME));
	private final Handler main_handler;

	///////////////////////////////////////////////////////////////
	// IModuleInst stuff
	private boolean is_module_destroyed = false;
	@Override
	public boolean isFullyWorking() {
		if (is_module_destroyed) {
			return false;
		}

		return UtilsGeneral.isThreadWorking(main_handlerThread);
	}
	@Override
	public void destroy() {
		try {
			UtilsGeneral.getContext().unregisterReceiver(broadcastReceiver);
		} catch (final IllegalArgumentException ignored) {
		}
		UtilsGeneral.quitHandlerThread(main_handlerThread);

		is_module_destroyed = true;
	}
	@Override
	public int wrongIsSupported() {return 0;}
	/**.
	 * @return read all here {@link IModuleInst#wrongIsSupported()} */
	public static boolean isSupported() {
		final String[] min_required_permissions = {
				Manifest.permission.RECEIVE_SMS,
		};
		return TelephonyManagement.isSupported() && UtilsPermsAuths.checkSelfPermissions(min_required_permissions);
	}
	// IModuleInst stuff
	///////////////////////////////////////////////////////////////

	/**
	 * <p>Main class constructor.</p>>
	 */
	@SuppressLint("InlinedApi")
	public SmsMsgsProcessor() {
		main_handlerThread.start();
		main_handler = new Handler(main_handlerThread.getLooper());

		try {
			final IntentFilter intentFilter = new IntentFilter();

			// Ignore the warning below. The constant exists on API 15, so exists at least on >= API 15.
			intentFilter.addAction(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);

			UtilsGeneral.getContext().registerReceiver(broadcastReceiver, new IntentFilter(intentFilter), null,
					main_handler);
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
			@Nullable final String sender = sms_message.getOriginatingAddress();
			//String message = stringBuilder.toString();
			System.out.println("&&&&&&&&&&&&&&&&&");
			System.out.println(sender);
			//System.out.println(message);
			System.out.println("&&&&&&&&&&&&&&&&&");

			// Update the Values Storage
			UtilsRegistry.setValue(ValuesRegistry.Keys.LAST_SMS_MSG_TIME, System.currentTimeMillis());

			final String speak;
			if (UtilsTelephony.isPrivateNumber(sender)) {
				speak = "Sir, attention! New message from a private number!";
				UtilsRegistry.setValue(ValuesRegistry.Keys.LAST_SMS_MSG_NUMBER, "[Private number]");
			} else {
				speak = "Sir, new message from " + UtilsTelephony.getWhatToSayAboutNumber(sender) + ".";
				UtilsRegistry.setValue(ValuesRegistry.Keys.LAST_SMS_MSG_NUMBER, sender);
			}
			UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_MEDIUM, true, null);
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
