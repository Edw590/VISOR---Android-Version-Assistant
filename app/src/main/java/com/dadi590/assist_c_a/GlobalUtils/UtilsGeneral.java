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

package com.dadi590.assist_c_a.GlobalUtils;

import android.app.ActivityManager;
import android.app.ActivityThread;
import android.app.AppGlobals;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.AudioDeviceInfo;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dadi590.assist_c_a.ApplicationClass;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import GlobalUtils_APU.GlobalUtils_APU;

/**
 * <p>Global app-related utilities.</p>
 */
public final class UtilsGeneral {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsGeneral() {
	}

	/**
	 * <p>Returns the version of the Assistant Platforms Unifier if it's available.</p>
	 * <p>Reason why it might not be available: if it's bundled as an external library and the file is not present -
	 * like happens with Android Lollipop and below in which library files are extracted. From Marshmallow onwards, it's
	 * possible to use libraries inside the APK, so this should not happen - UNLESS the app is installed as a system
	 * app, and in that case, it seems the libraries must be extracted anyway.</p>
	 *
	 * @return the version of the APU if it's available, null otherwise
	 */
	// Ignore the error below saying the method never returns null. It can be null - just remove the library file from
	// due folder on the device and see it happening... (on the conditions wrote on the function doc).
	@Nullable
	public static String getPlatformUnifierVersion() {
		// "You see, Java's exception hierarchy is a bit unintuitive. You have two classes, Exception and Error, each of
		// which extends Throwable. Thus, if you want to catch absolutely everything you need to catch Throwable (not
		// recommended)."
		// In this case, the error is UnsatisfiedLinkError in case the library is not present (was not unpacked or
		// unsupported architecture like MIPS), which is part of the Error class, not Exception. But I want to catch ANY
		// error to know if its or not, so I chose Throwable.
		// EDIT: never mind. I'll stick to UnsatisfiedLinkError. Must only be changed if anything other than that
		// happens, which I'm not expecting.
		try {
			// Leave only a check for the GlobalUtils. That one must always be exported. If it's not, error in the go
			// with good reasons. All the other modules, if they're not compiled, run-time error immediately. Can't
			// check each module... Just the main. All the others should be compiled too. If they're not, compilation
			// error and the module must be recompiled.
			return GlobalUtils_APU.VERSION;
		} catch (final UnsatisfiedLinkError ignored) {
		}

		return null;
	}

	/**
	 * <p>Generates a random string with the given length, containing only ASCII letters (upper case or lower case) and
	 * numbers.</p>
	 *
	 * @param length length of the generating string
	 *
	 * @return the generated string
	 */
	@NonNull
	public static String generateRandomString(final int length) {
		final String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		final String lower = upper.toLowerCase(Locale.ROOT);
		final String digits = "0123456789";
		final String alphanum = upper + lower + digits;

		final Random random = new SecureRandom();
		final char[] symbols = alphanum.toCharArray();
		final char[] buf;

		if (length < 1) throw new IllegalArgumentException("Length 0 string requested");
		buf = new char[length];

		for (int idx = 0; idx < length; ++idx) {
			buf[idx] = symbols[random.nextInt(symbols.length)];
		}
		return new String(buf);
	}

	/**
	 * <p>Check if an accessory with speakers (like earphones, headphones, headsets...) are connected.</p>
	 *
	 * @return true if an accessory with speakers is connected, false otherwise
	 */
	public static boolean areExtSpeakersOn() {
		final AudioManager audioManager = (AudioManager) UtilsGeneral.getContext()
				.getSystemService(Context.AUDIO_SERVICE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			final AudioDeviceInfo[] devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS);

			for (final AudioDeviceInfo device : devices) {
				if (device.getType() == AudioDeviceInfo.TYPE_WIRED_HEADSET
						|| device.getType() == AudioDeviceInfo.TYPE_WIRED_HEADPHONES
						|| device.getType() == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP
						|| device.getType() == AudioDeviceInfo.TYPE_BLUETOOTH_SCO
						//|| device.getType() == AudioDeviceInfo.TYPE_BLE_HEADSET - added in Android S
						// todo Remove the // when it's on the SDK
						|| device.getType() == AudioDeviceInfo.TYPE_USB_HEADSET) {
					return true;
				}
			}
		} else {
			return audioManager.isWiredHeadsetOn() || audioManager.isBluetoothScoOn()
					|| audioManager.isBluetoothA2dpOn();
		}

		return false;
	}

	/**
	 * <p>Returns the Application Context.</p>
	 * <p>Main note: probably not good idea to use on Content Provider classes. Only on Activities, Services and
	 * Receivers. Read the doc of {@link ApplicationClass#applicationContext}.</p>
	 * <br>
	 * <p>It will use the way that doesn't get a null value from the 2 below, in this order. It's also not supposed to
	 * return null ever, since I'm using 2 different ways, but it can. Though, I don't think it will. If it does, I'll
	 * change @NonNull to @Nullable or something. Until then, assume it's never null (except on Content Providers, as
	 * said above, which might be null). Ways:</p>
	 * <p>- Returns {@link Context#getApplicationContext()} on {@link ActivityThread#currentApplication()}.</p>
	 * <p>Warning from {@link AppGlobals#getInitialApplication()}, which calls the last mentioned method above:</p>
	 * <p>"Return the first Application object made in the process.</p>
	 * <p>NOTE: Only works on the main thread."</p>
	 * <p>- Returns the Application Context, got at app boot on {@link ApplicationClass}.</p>
	 *
	 * @return same as in {@link Context#getApplicationContext()}
	 */
	@NonNull
	public static Context getContext() {
		Context context = null;
		final Application application = ActivityThread.currentApplication();
		if (null != application) { // Was null in various runs of the app on API 15
			context = application.getApplicationContext();
		}
		if (null == context) {
			System.out.println("\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\");
			context = ApplicationClass.applicationContext;
		}
		return context;
	}

	/**
	 * <p>Gets {@link Class#getMethod(String, Class[])} and the wanted class ready for use with meta-reflection to
	 * bypass limitations with API 28 and above.</p>
	 * <br>
	 * <p>Sample code of meta-reflection in Kotlin from XDA Developers
	 * (https://www.xda-developers.com/android-development-bypass-hidden-api-restrictions/):
	 * <pre>
	 * val forName = Class::class.java.getMethod("forName", String::class.java)
	 * val getMethod = Class::class.java.getMethod("getMethod", String::class.java, arrayOf<Class<*>>()::class.java)
	 * val someHiddenClass = forName.invoke(null, "android.some.hidden.Class") as Class<*>
	 * val someHiddenMethod = getMethod.invoke(someHiddenClass, "someHiddenMethod", String::class.java)
	 *
	 * someHiddenMethod.invoke(null, "some important string")
	 * </pre>
	 *
	 * @param wanted_class the class to retrieve
	 *
	 * @return {@link Class#forName(String)} on index 0, {@link Class#getMethod(String, Class[])} on index 1; null in
	 * case some problem occurred.
	 */
	@Nullable
	public static ObjectClasses.GetMethodClassObj getMethodClass(@NonNull final String wanted_class) {
		try {
			final Method forName = Class.class.getDeclaredMethod("forName", String.class);
			final Class<?> hidden_class = (Class<?>) forName.invoke(null, wanted_class);
			final Method getMethod = Class.class.getDeclaredMethod("getDeclaredMethod", String.class, Class[].class);

			return new ObjectClasses.GetMethodClassObj(getMethod, hidden_class);
		} catch (final NoSuchMethodException ignored) {
		} catch (final IllegalAccessException ignored) {
		} catch (final InvocationTargetException ignored) {
		}

		return null;

		/*
		Sample code of meta-reflection in Kotlin from XDA
		(https://www.xda-developers.com/android-development-bypass-hidden-api-restrictions/):

		val forName = Class::class.java.getMethod("forName", String::class.java)
		val getMethod = Class::class.java.getMethod("getMethod", String::class.java, arrayOf<Class<*>>()::class.java)
		val someHiddenClass = forName.invoke(null, "android.some.hidden.Class") as Class<*>
		val someHiddenMethod = getMethod.invoke(someHiddenClass, "someHiddenMethod", String::class.java)

		someHiddenMethod.invoke(null, "some important string")
		*/
	}

	/**
	 * <p>Convert a byte array to printable characters in a string.</p>
	 * <p>Note: all bytes will be attempted to be printed, all based on the platform's default charset (on Android is
	 * always the UTF-8 charset).</p>
	 *
	 * @param byte_array the byte array
	 * @param utf7 true if the bytes were encoded using UTF-7, false if they were encoded using UTF-8
	 *
	 * @return a string containing printable characters representative of the provided bytes
	 */
	@NonNull
	public static String bytesToPrintableChars(@NonNull final byte[] byte_array, final boolean utf7) {
		if (utf7) {
			try {
				return new String(byte_array, GL_CONSTS.UTF7_NAME_LIB);
			} catch (final UnsupportedEncodingException ignored) {
				// Won't happen - UTF-7 is included in the project through com.beetstra.jutf7 library.
				return null;
			}
		} else {
			// The default charset on Android is always UTF-8 as stated in the method documentation, so all is ok
			return new String(byte_array, Charset.defaultCharset());
		}
	}

	/**
	 * <p>Converts a {@link byte} to an "unsigned" {@link int}.</p>
	 * <p>Primitives in Java are all signed. So {@code (byte) 200 = -56} - this gets it equal to 200, for an unsigned
	 * storage and representation. Useful if the byte is supposed to be used with indexes on arrays and stuff.</p>
	 *
	 * @param _byte the {@link byte} to be converted
	 *
	 * @return the converted byte to an "unsigned" {@link int}
	 */
	public static int byteToIntUnsigned(final byte _byte) {
		// 'Will print a negative int -56 because upcasting byte to int does so called "sign extension" which yields
		// those bits: 1111 1111 1111 1111 1111 1111 1100 1000 (-56)' - https://stackoverflow.com/a/4266841/8228163
		// This below will zero everything out except the first 8 bits (ANDed with 1) - so a positive number (the
		// unsigned byte) remains.
		return (int) _byte & 0xFF;
	}

	/**
	 * <p>Checks if the device is currently running on low memory.</p>
	 *
	 * @return true if running on low memory, false otherwise
	 */
	public static boolean isDeviceRunningOnLowMemory() {
		final ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
		final ActivityManager activityManager = (ActivityManager) UtilsGeneral.getContext()
				.getSystemService(Context.ACTIVITY_SERVICE);
		activityManager.getMemoryInfo(memoryInfo);

		return memoryInfo.lowMemory;
	}

	/**
	 * <p>Vibrate the device once for the amount of time given.</p>
	 *
	 * @param duration milliseconds to vibrate
	 */
	public static void vibrateDeviceOnce(final long duration) {
		final Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE));
		} else {
			vibrator.vibrate(duration);
		}
	}

	/**
	 * <p>Broadcast a key press (key down and key up).</p>
	 *
	 * @param key_code one of the {@link KeyEvent}#KEYCODE_-started constants
	 * @param permission the permission the receivers must hold to receive this broadcast, or null to don't require this
	 *                   restriction
	 */
	public static void broadcastKeyEvent(final int key_code, @Nullable final String permission) {
		final Context context = UtilsGeneral.getContext();
		final Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
		intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, key_code));
		context.sendBroadcast(intent, permission);
		final Intent intent1 = new Intent(Intent.ACTION_MEDIA_BUTTON);
		intent1.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, key_code));
		context.sendBroadcast(intent1, permission);
	}

	/**
	 * <p>Checks if an intent is recognized on the device (that includes any packages, not just system ones).</p>
	 * <p>For example the intent of sending a message is not supposed to be available on a device which cannot send
	 * SMS messages, and would be a way to check such thing.</p>
	 *
	 * @param intent the intent to check
	 * @param flags same as in {@link PackageManager#queryIntentActivities(Intent, int)}
	 *
	 * @return true if the intent is available, false otherwise
	 */
	public static boolean isIntentActionAvailable(@NonNull final Intent intent, final int flags) {
		final List<ResolveInfo> list = UtilsGeneral.getContext().getPackageManager().
				queryIntentActivities(intent, flags);

		return !list.isEmpty();
	}

	/**
	 * <p>Checks if a {@link android.media.MediaRecorder.AudioSource} is available for immediate use.</p>
	 *
	 * @param audio_source the audio source to check
	 *
	 * @return true if it's available for immediate use, false otherwise (doesn't exist on the device or is busy)
	 */
	public static boolean isAudioSourceAvailable(final int audio_source) {
		final int sample_rate = 44100;
		final int channel_config = AudioFormat.CHANNEL_IN_MONO;
		final int audio_format = AudioFormat.ENCODING_PCM_16BIT;

		final AudioRecord audioRecord;
		try {
			audioRecord = new AudioRecord(audio_source, sample_rate, channel_config, audio_format,
					AudioRecord.getMinBufferSize(sample_rate, channel_config, audio_format));
		} catch (final IllegalStateException ignored) {
			return false;
		}

		final boolean initialized = AudioRecord.STATE_INITIALIZED == audioRecord.getState();

		boolean success_recording = false;
		if (initialized) {
			try {
				audioRecord.startRecording();
				success_recording = audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING;
			} catch (final IllegalStateException ignored) {
			}
		}

		if (initialized) {
			// If it's initialized, stop it.
			audioRecord.stop();
		}
		audioRecord.release();

		return success_recording;
	}
}
