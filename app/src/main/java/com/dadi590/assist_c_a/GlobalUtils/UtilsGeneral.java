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
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dadi590.assist_c_a.ApplicationClass;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.Random;

import CommandsDetection_APU.CommandsDetection_APU;
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
	 * like happens with Android Lollipop and below in which extract libraries is mandatory. From Marshmallow, it's
	 * possible to use libraries inside the APK, so this should not happen - but better prevent anyways.</p>
	 *
	 * @return the version of the APU if it's available, null otherwise
	 */
	// Ignore the error below saying the method never returns null. It can be null - just remove the library file from
	// due folder and see it happening... (if the app is run on below Marshmallow, where library files are extracted).
	@Nullable
	public static String platformUnifierVersion() {
		// "You see, Java's exception hierarchy is a bit unintuitive. You have two classes, Exception and Error, each of
		// which extends Throwable. Thus, if you want to catch absolutely everything you need to catch Throwable (not
		// recommended)."
		// In this case, the error is UnsatisfiedLinkError in case the library is not present (was not unpacked or
		// unsupported architecture like MIPS), which is part of the Error class, not Exception. But I want to catch ANY
		// error to know if its or not, so I chose Throwable.
		// EDIT: never mind. I'll stick to UnsatisfiedLinkError. Must only be changed if anything other than that
		// happens.
		try {
			// Leave this as it is unless you have a better idea. I need to check if the Commands Detection submodule is
			// present before returning the version (which also checks if the GlobalUtils are present or not - which
			// must always be).
			final String temp = CommandsDetection_APU.NONE;
			return GlobalUtils_APU.VERSION;
		} catch (final UnsatisfiedLinkError ignored) {
			return null;
		}
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
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
			return audioManager.isWiredHeadsetOn() || audioManager.isBluetoothScoOn()
					|| audioManager.isBluetoothA2dpOn();
		} else {
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
		}

		return false;
	}

	/**
	 * <p>Returns the Application Context.</p>
	 * <p>Main note: probably not good idea to use on Content Provider classes. Only on Activities, Services and
	 * Receivers. Read the doc of {@link ApplicationClass#applicationContext}.</p>
	 * <p>It will use the way that doesn't get a null value from the 2 below, in this order. It's also not supposed to
	 * return null ever, since I'm using 2 different ways, but it can. Though, I don't think it will. If it does, I'll
	 * change NonNull to Nullable or something. Until then, assume it's never null. Ways:</p>
	 * <p>- Calls {@link Context#getApplicationContext()} on {@link ActivityThread#currentApplication()}.</p>
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
			System.out.println("/UJHGRGT%&/UGFFFT%YUYG");
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
			final Method forName = Class.class.getMethod("forName", String.class);
			final Class<?> hidden_class = (Class<?>) forName.invoke(null, wanted_class);
			final Method getMethod = Class.class.getMethod("getMethod", String.class, Class[].class);
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

	public static final int FONTE_DISPONIVEL = 0;
	public static final int FONTE_INDISPONIVEL = 1;
	public static final int ERRO_NA_DETECAO = 2;
	// todo Falta a função "fonte_audio_grav_disp" aqui abaixo
}
