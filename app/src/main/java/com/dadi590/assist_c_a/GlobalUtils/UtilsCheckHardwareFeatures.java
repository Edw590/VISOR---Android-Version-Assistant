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

package com.dadi590.assist_c_a.GlobalUtils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.telecom.PhoneAccount;
import android.telecom.TelecomManager;

import java.util.List;

/**
 * <p>Utilities to check device hardware features, like Camera, GPS, Telephony, etc.</p>
 */
public final class UtilsCheckHardwareFeatures {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsCheckHardwareFeatures() {
	}

	/**
	 * <p>Checks if the device has telephony support, more specifically for phone calls or SMS messages.</p>
	 *
	 * @param check_phone_calls true to check specifically for phone calls, false for SMS messages
	 *
	 * @return true if available, false otherwise
	 */
	public static boolean isTelephonySupported(final boolean check_phone_calls) {
		final Context context = UtilsGeneral.getContext();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
				UtilsPermsAuths.checkSelfPermission(Manifest.permission.MODIFY_PHONE_STATE)) {
			// TelecomManager.getAllPhoneAccounts() needs MODIFY_PHONE_STATE (by experience).
			final TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
			final List<PhoneAccount> phoneAccount_list = telecomManager.getAllPhoneAccounts();
			for (final PhoneAccount phoneAccount : phoneAccount_list) {
				if (0 == (phoneAccount.getCapabilities() & PhoneAccount.CAPABILITY_PLACE_EMERGENCY_CALLS)) {
					// If it can place emergency calls, it can make at least that type of call. So start the
					// module, as any call type counts.
					// Though, as CAPABILITY_PLACE_EMERGENCY_CALLS's documentation says, phone accounts BY
					// DEFAULT can place emergency calls ("by default").
					return true;
				}
			}
		}

		final PackageManager packageManager = context.getPackageManager();
		if (!packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY) &&
				!packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY_CDMA)) {
			return false;
		}

		// If the device has a telephony radio, check if it can specifically send SMS messages or make a phone
		// call by reacting to an Android scheme, for SMS messages defined in (at least - there are more places
		// where there is a definition, though always outside of the Android system code, I believe):
		// https://android.googlesource.com/platform/packages/apps/Messaging/+/master/src/com/android/messaging/util/UriUtil.java.
		final String scheme;
		final String intent_action;
		if (check_phone_calls) {
			intent_action = Intent.ACTION_CALL;
			scheme = "tel";
		} else {
			intent_action = Intent.ACTION_SENDTO;
			scheme = "smsto";
		}
		final Intent intent = new Intent(intent_action, Uri.fromParts(scheme, "+351000000000", null));

		return UtilsGeneral.isIntentActionAvailable(intent,
				Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PackageManager.MATCH_ALL : 0);
	}

	/**
	 * <p>Checks if the device has audio output support.</p>
	 *
	 * @return true if audio output is supported, false otherwise
	 */
	public static boolean isAudioOutputSupported() {
		final Context context = UtilsGeneral.getContext();

		final PackageManager packageManager = context.getPackageManager();
		final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

		Boolean has_audio_output_feature = null;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			has_audio_output_feature = packageManager.hasSystemFeature(PackageManager.FEATURE_AUDIO_OUTPUT);
		}

		// If the device is below Lollipop or the method returned false, check if anything else is connected to
		// the device that can output sound. If yes, cool. If not, then there's probably no way of playing sound
		// on the device.
		// Why would the FEATURE_AUDIO_OUTPUT return false with audio output available?
		// "I tested this feature on my MOTO 360 (no speaker), it don't has this feature, and Ticwatch (with
		// speaker) do have this feature. But when I connected a Bluetooth headset to the MOTO 360, it still
		// don't have this feature, this confused me." --> https://stackoverflow.com/a/32903108/8228163.
		final boolean any_audio_device_connected = audioManager.isBluetoothA2dpOn() ||
				audioManager.isBluetoothScoOn() || audioManager.isWiredHeadsetOn() ||
				audioManager.isSpeakerphoneOn();

		if (null == has_audio_output_feature) {
			// Assume there's always a speaker below Lollipop. I have a tablet with KitKat which does have
			// speakers and can have headphones, but with wired headphones connected or not, nothing works, so
			// whatever.
			return true;
		} else {
			if (has_audio_output_feature) {
				return true;
			} else {
				return any_audio_device_connected;
			}
		}
	}

	public static boolean isLocationSupported() {
		// todo This needs GPS checking, mobile data too, WiFi, and Bluetooth (just one needs to be true)
		return false; // Just for me to remember to come here when I find out the module is not working
	}

	/**
	 * <p>Checks if the device has camera support.</p>
	 *
	 * @return true if camera is supported, false otherwise
	 */
	@SuppressLint("UnsupportedChromeOsCameraSystemFeature")
	public static boolean isCameraSupported() {
		final Context context = UtilsGeneral.getContext();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
		} else {
			return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA) ||
					context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT);
		}
	}

	/**
	 * <p>Checks if the device has microphone support.</p>
	 *
	 * @return true if microphone is supported, false otherwise
	 */
	public static boolean isMicrophoneSupported() {
		return UtilsGeneral.getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE);
	}
}
