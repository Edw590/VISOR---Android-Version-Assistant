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

package com.dadi590.assist_c_a.GlobalUtils.AndroidSystem;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.ServiceManager;
import android.telecom.TelecomManager;
import android.view.KeyEvent;

import androidx.annotation.NonNull;

import com.android.internal.telephony.ITelephony;
import com.dadi590.assist_c_a.GlobalUtils.UtilsGeneral;
import com.dadi590.assist_c_a.GlobalUtils.UtilsPermsAuths;
import com.dadi590.assist_c_a.GlobalUtils.UtilsReflection;
import com.dadi590.assist_c_a.Modules.Telephony.UtilsTelephony;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * <p>Telephony related utilities.</p>
 * <p>For example end or answer phone calls.</p>
 */
public final class UtilsAndroidTelephony {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsAndroidTelephony() {
	}

	// todo Check if the call was answered or ended based on the current call state (in case the broadcast way was not
	//  used, as that has some delay)


	// todo Use ADB commands here too. Example: "adb shell service call phone 5" for answerRingingCall(), but hopefully
	//  with root permissions and not needing the ANSWER_PHONE_CALLS one, or the MODIFY_PHONE_STATE, or whatever else.

	/**
	 * <p>Answer a ringing phone call.</p>
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link UtilsAndroid#NO_ERR} --> for the returning value: if the operation completed successfully</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @return one of the constants
	 */
	public static int answerPhoneCall() {
		final Context context = UtilsGeneral.getContext();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			if (UtilsPermsAuths.checkSelfPermission(Manifest.permission.ANSWER_PHONE_CALLS) ||
					UtilsPermsAuths.checkSelfPermission(Manifest.permission.MODIFY_PHONE_STATE)) {
				final TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
				try {
					telecomManager.acceptRingingCall();

					return UtilsAndroid.NO_ERR;
				} catch (final Exception ignored) {
				}
			}
		} else {
			if (UtilsPermsAuths.checkSelfPermission(Manifest.permission.MODIFY_PHONE_STATE)) {
				final ITelephony iTelephony = ITelephony.Stub.asInterface(ServiceManager.
						getService(Context.TELEPHONY_SERVICE));
				// There's also TelephonyManager.answerRingingCall(), but this one exists for many API levels, so why
				// not use it for all possible ones.

				// Deprecated and removed as of Android 10 (returns "void").
				final Method method = UtilsReflection.getMethod(ITelephony.class, "answerRingingCall");
				assert null != method; // Will never happen.
				try {
					method.invoke(iTelephony);

					return UtilsAndroid.NO_ERR;
				} catch (final IllegalAccessException ignored) {
				} catch (final InvocationTargetException ignored) {
				}
			}
		}

		// If none of the above are taken, key event.
		// Set a permission on the broadcast so that only applications with the CALL_PRIVILEGED permission can receive
		// it (which would mean the Phone app, for example). Without it, any media player could receive the broadcast
		// and start playing music or something.
		// NOTE: doesn't work on Oreo 8.1, but it's said on Stack Exchange that it used to work. So leave it and hope
		// it works... When it doesn't, well, what can we do... And since it doesn't work, another one below. Maybe at
		// least one of them works somewhere (not here...).
		UtilsGeneral.broadcastKeyEvent(KeyEvent.KEYCODE_HEADSETHOOK, Manifest.permission.CALL_PRIVILEGED);
		UtilsGeneral.broadcastKeyEvent(KeyEvent.KEYCODE_CALL, Manifest.permission.CALL_PRIVILEGED);

		return UtilsAndroid.NO_ERR;
	}

	/**
	 * <p>End the current phone call.</p>
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link UtilsAndroid#NO_ERR} --> for the returning value: if the operation completed successfully</p>
	 * <p>- {@link UtilsAndroid#GEN_ERR} --> for the returning value: if an error occurred and the operation did not
	 * succeed</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @return one of the constants
	 */
	public static int endPhoneCall() {
		final Context context = UtilsGeneral.getContext();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			if (UtilsPermsAuths.checkSelfPermission(Manifest.permission.ANSWER_PHONE_CALLS)) {
				final TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);

				try {
					return telecomManager.endCall() ? UtilsAndroid.NO_ERR : UtilsAndroid.GEN_ERR;
				} catch (final Exception ignored) {
				}
			}
		} else {
			if (UtilsPermsAuths.checkSelfPermission(Manifest.permission.CALL_PHONE)) {
				final ITelephony iTelephony = ITelephony.Stub.asInterface(ServiceManager.
						getService(Context.TELEPHONY_SERVICE));
				// There's also TelephonyManager.endCall(), but this one exists for many API levels, so why not use it
				// for all possible ones.

				// Deprecated and removed as of Android 10 (returns a boolean).
				final Method method = UtilsReflection.getMethod(ITelephony.class, "endCall");
				assert null != method; // Will never happen.
				try {
					final Boolean ret_method = (Boolean) method.invoke(iTelephony);
					assert ret_method != null; // Which will never be... (but the warning is gone now)

					return ret_method ? UtilsAndroid.NO_ERR : UtilsAndroid.GEN_ERR;
				} catch (final IllegalAccessException ignored) {
				} catch (final InvocationTargetException ignored) {
				}
			}
		}

		// If none of the above are taken, key event.
		// Set a permission on the broadcast so that only applications with the CALL_PRIVILEGED permission can receive
		// it (which would mean the Phone app, for example). Without it, any media player could receive the broadcast
		// and start playing music or something.
		// NOTE: doesn't work on Oreo 8.1, but it's said on Stack Exchange that it used to work. So leave it and hope
		// it works... When it doesn't, well, what can we do...
		UtilsGeneral.broadcastKeyEvent(KeyEvent.KEYCODE_ENDCALL, Manifest.permission.CALL_PRIVILEGED);

		return UtilsAndroid.NO_ERR;
	}

	/**
	 * <p>Sets the phone's speakerphone in a phone call.</p>
	 *
	 * @param enabled true to enable it, false to disable it
	 */
	public static void setCallSpeakerphoneEnabled(final boolean enabled) {
		final AudioManager audioManager = (AudioManager) UtilsGeneral.getContext().getSystemService(Context.AUDIO_SERVICE);
		audioManager.setMode(AudioManager.MODE_IN_CALL);
		audioManager.setSpeakerphoneOn(enabled);
	}

	/**
	 * <p>Places a phone call.</p>
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link UtilsAndroid#NO_ERR} --> for the returning value: if the operation completed successfully</p>
	 * <p>- {@link UtilsAndroid#NO_CALL_ANY} --> for the returning value: no permission to call numbers</p>
	 * <p>- {@link UtilsAndroid#NO_CALL_EMERGENCY} --> for the returning value: no permission to call emergency numbers</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @param phone_number the phone number to call
	 *
	 * @return one of the constants
	 */
	public static int makePhoneCall(@NonNull final String phone_number) {
		final Context context = UtilsGeneral.getContext();

		final Intent intent = new Intent("", Uri.fromParts("tel", phone_number, null));

		// On 2023-02-01, Android Developers says ACTION_DIAL can be used for emergency numbers (so at least until
		// Android 12 it's like this).
		final String action_dial_emergency = Intent.ACTION_DIAL;
		final boolean is_potentially_emergency_number = UtilsTelephony.isPotentialEmergencyNumber(phone_number);

		final String action;
		final int return_code;
		if (UtilsPermsAuths.checkSelfPermission(Manifest.permission.CALL_PRIVILEGED)) {
			// As written on the AOSP code for ACTION_CALL_PRIVILEGED:
			// "Perform a call to any number (emergency or not) specified by the data."
			action = Intent.ACTION_CALL_PRIVILEGED;
			return_code = UtilsAndroid.NO_ERR;
		} else if (UtilsPermsAuths.checkSelfPermission(Manifest.permission.CALL_PHONE)) {
			if (is_potentially_emergency_number) {
				action = action_dial_emergency;
				return_code = UtilsAndroid.NO_CALL_EMERGENCY;
			} else {
				action = Intent.ACTION_CALL;
				return_code = UtilsAndroid.NO_ERR;
			}
		} else {
			action = is_potentially_emergency_number ? action_dial_emergency : Intent.ACTION_DIAL;
			return_code = UtilsAndroid.NO_CALL_ANY;
		}
		intent.setAction(action);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);

		return return_code;
	}
}
