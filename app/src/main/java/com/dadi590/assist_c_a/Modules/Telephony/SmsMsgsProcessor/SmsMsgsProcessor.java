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

package com.dadi590.assist_c_a.Modules.Telephony.SmsMsgsProcessor;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;

import androidx.annotation.NonNull;

import com.dadi590.assist_c_a.MainSrv;
import com.dadi590.assist_c_a.Modules.Speech.Speech2;
import com.dadi590.assist_c_a.Modules.Telephony.UtilsTelephony;


/**
 * <p>Processes all SMS messages sent by and to the phone.</p>
 */
public final class SmsMsgsProcessor {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private SmsMsgsProcessor() {
	}

	/**
	 * <p>Main class function.</p>
	 *
	 * @param intent the intent
	 */
	public static void smsMsgsProcessor(@NonNull final Intent intent) {
		final Bundle bundle = intent.getExtras();
		if (bundle == null) {
			// Put some warning here. No extras in a message intent --> ???
			return;
		}

		final SmsMessage[] msgs;
		final String format = bundle.getString("format");

		// get sms objects
		final Object[] pdus = (Object[]) bundle.get("pdus");
		if (pdus == null) {
			// Pus some warning here. No message objects but the alert of message was sent --> ???
			return;
		}
		if (pdus.length == 0) {
			// Put some warning here. No messages but the message alert was sent --> ???
			return;
		}
		// large message might be broken into many
		msgs = new SmsMessage[pdus.length];
		final StringBuilder stringBuilder = new StringBuilder(25);
		// Check Android version and use appropriate createFromPdu.
		final int pdus_length = pdus.length;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			for (int i = 0; i < pdus_length; i++) {
				msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
				stringBuilder.append(msgs[i].getMessageBody());
			}
		} else {
			for (int i = 0; i < pdus_length; i++) {
				msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
				stringBuilder.append(msgs[i].getMessageBody());
			}
		}

		final String sender = msgs[0].getOriginatingAddress();
		//String message = stringBuilder.toString();
		System.out.println("&&&&&&&&&&&&&&&&&");
		System.out.println(sender);
		//System.out.println(message);
		System.out.println("&&&&&&&&&&&&&&&&&");

		if (UtilsTelephony.isPrivateNumber(sender)) {
			final String speak = "Sir, attention! New message from a private number!";
			MainSrv.getSpeech2().speak(speak, Speech2.NO_ADDITIONAL_COMMANDS, Speech2.PRIORITY_MEDIUM, null);
		} else {
			final String number_name = UtilsTelephony.getWhatToSayAboutNumber(sender);
			final String speak = "Sir, new message from " + number_name + ".";
			MainSrv.getSpeech2().speak(speak, Speech2.NO_ADDITIONAL_COMMANDS, Speech2.PRIORITY_MEDIUM, null);
		}
	}
}
