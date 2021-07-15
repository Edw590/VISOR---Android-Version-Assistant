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

import android.app.ActivityThread;
import android.content.Context;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dadi590.assist_c_a.MainSrv;
import com.dadi590.assist_c_a.Modules.Speech.Speech2;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.Random;

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
	 * <p>Deletes a directory (either file or folder).</p>
	 *
	 * @param dir the path to delete
	 *
	 * @return true if deletion was completely successful, including all files if a non-empty folder was selected for
	 * deletion; false otherwise
	 */
	public static boolean deletePath(@NonNull final File dir) {
		if (dir.isDirectory()) {
			final String[] children = dir.list();
			boolean success = true;
			if (children == null) {
				return false;
			} else {
				for (final String child : children) {
					success = success && deletePath(new File(dir, child));
				}
			}
			return success && dir.delete();
		} else if (dir.isFile()) {
			return dir.delete();
		} else {
			return false;
		}
	}

	/**
	 * <p>Checks if the External Functions are available or not.</p>
	 *
	 * @return true if the External Functions are available, false otherwise.
	 */
	public static boolean ext_funcs_available() {
		// "You see, Java's exception hierarchy is a bit unintuitive. You have two classes, Exception and Error, each of
		// which extends Throwable. Thus, if you want to catch absolutely everything you need to catch Throwable (not
		// recommended)."
		// In this case, the error for this case is UnsatisfiedLinkError, which is part of the Error class, not
		// Exception. But I want to catch ANY error to know if they're available or not, so I chose Throwable.
		try {
			//Funcoes_externas.chamar_tarefa("sgfhjvfgsbvysd");
		} catch (final Throwable ignored) {
			return false;
		}
		return true;
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

		final int buf_length = buf.length;
		for (int idx = 0; idx < buf_length; idx++) {
			buf[idx] = symbols[random.nextInt(symbols.length)];
		}
		return new String(buf);
	}

	/**
	 * <p>Checks and warns about root access availability for the app.</p>
	 *
	 * @param warn_root_available true to warn if root access is available, false to only warn when there's no access
	 */
	public static void checkWarnRootAccess(final boolean warn_root_available) {
		// todo See if you can delete this... It's not supposed for the app to execute any root commands. Only system
		//  hidden/internal methods.

		switch (UtilsRoot.rootCommandsAvailability()) {
			case (UtilsRoot.ROOT_AVAILABLE): {
				if (warn_root_available) {
					final String speak = "Root access available on the device.";
					MainSrv.getSpeech2().speak(speak, Speech2.NO_ADDITIONAL_COMMANDS, Speech2.PRIORITY_USER_ACTION, null);
				}

				break;
			}
			case (UtilsRoot.ROOT_DENIED): {
				final String speak = "WARNING! Root access was denied on this device! Some features may not " +
						"be available!";
				MainSrv.getSpeech2().speak(speak, Speech2.NO_ADDITIONAL_COMMANDS, Speech2.PRIORITY_HIGH, null);

				break;
			}
			case (UtilsRoot.ROOT_UNAVAILABLE): {
				final String speak = "Attention! The device is not rooted! Some features may not be available!";
				MainSrv.getSpeech2().speak(speak, Speech2.NO_ADDITIONAL_COMMANDS, Speech2.PRIORITY_MEDIUM, null);

				break;
			}
		}
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
	 * <p>Calls {@link Context#getApplicationContext()} on {@link ActivityThread#currentApplication()}.</p>
	 *
	 * @return .
	 */
	@NonNull
	public static Context getContext() {
		return ActivityThread.currentApplication().getApplicationContext();
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
	 * <p>Note: all bytes except null will be attempted to be printed, all based on the platform's default charset (on
	 * Android is always the UTF-8 charset).</p>
	 *
	 * @param byte_array the byte array
	 *
	 * @return a string containing printable characters representative of the provided bytes
	 */
	@NonNull
	public static String convertBytes2Printable(@NonNull final byte[] byte_array) {
		final byte[] new_array = new byte[byte_array.length];
		int new_length = 0;
		for (final byte _byte : byte_array) {
			// Allow all possible characters except the null character to be printed
			if ((int) _byte != 0) {
				new_array[new_length] = _byte;
				new_length++;
			}
		}
		// The default charset on Android is always UTF-8 as stated in the method documentation, so all is ok
		return new String(new_array, 0, new_length, Charset.defaultCharset());
	}

	public static final int FONTE_DISPONIVEL = 0;
	public static final int FONTE_INDISPONIVEL = 1;
	public static final int ERRO_NA_DETECAO = 2;
	// todo Falta a função "fonte_audio_grav_disp" aqui abaixo
}
