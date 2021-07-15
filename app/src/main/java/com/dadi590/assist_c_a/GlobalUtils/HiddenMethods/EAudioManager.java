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

package com.dadi590.assist_c_a.GlobalUtils.HiddenMethods;

import android.content.Context;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.media.IAudioService;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;

import androidx.annotation.NonNull;

import androidx.annotation.RequiresApi;

/**
 * <p>Original class: {@link AudioManager}.</p>
 */
public final class EAudioManager {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private EAudioManager() {
	}

	private static final AudioManager audioManager = new AudioManager();
	// ATTENTION - the constructor used is internal and is "For test purposes only". If in some device ANY method throws
	// the an NPE because of this, put the method return inside a try statement, catch the NPE, ignore it, and on the
	// catch statement put the implementation of the original method (you must keep updating it though).

	/**.
	 * @return an instance of {@link IAudioService}
	 */
	@NonNull
	private static IAudioService getService() {
		final IBinder iBinder = ServiceManager.getService(Context.AUDIO_SERVICE);
		return IAudioService.Stub.asInterface(iBinder);
	}

	/**
	 * <p>See {@link AudioManager#isWiredHeadsetOn()}.</p>
	 * <br>
	 * <p>Changes:</p>
	 * <p>- Method made static as it doesn't use Context</p>
	 *
	 * @return .
	 */
	public static boolean isWiredHeadsetOn() {
		return audioManager.isWiredHeadsetOn();
	}

	/**
	 * <p>See {@link AudioManager#isBluetoothScoOn()}.</p>
	 * <br>
	 * <p>Changes:</p>
	 * <p>- Method made static as it doesn't use Context</p>
	 *
	 * @return .
	 */
	public static boolean isBluetoothScoOn() {
		return audioManager.isBluetoothScoOn();
	}

	/**
	 * <p>See {@link AudioManager#isBluetoothA2dpOn()}.</p>
	 * <br>
	 * <p>Changes:</p>
	 * <p>- Method made static as it doesn't use Context</p>
	 *
	 * @return .
	 */
	public static boolean isBluetoothA2dpOn() {
		return audioManager.isBluetoothA2dpOn();
	}

	/**
	 * <p>See {@link AudioManager#getDevices(int)}.</p>
	 * <br>
	 * <p>Changes:</p>
	 * <p>- Method made static as it doesn't use Context</p>
	 *
	 * @param flags .
	 *
	 * @return .
	 */
	@NonNull
	@RequiresApi(api = Build.VERSION_CODES.M)
	public static AudioDeviceInfo[] getDevices(final int flags) {
		return audioManager.getDevices(flags);
	}

	/**
	 * <p>See {@link AudioManager#getRingerMode()}.</p>
	 * <br>
	 * <p>Changes:</p>
	 * <p>- Method made static as it doesn't use Context</p>
	 *
	 * @return .
	 */
	public static int getRingerMode() {
		return audioManager.getRingerMode();
	}


	/**
	 * <p>See {@link AudioManager#setStreamVolume(int, int, int)}.</p>
	 * <br>
	 * <p>Changes:</p>
	 * <p>- Removed the need for the Context instance by replacing a method call by a constant</p>
	 *
	 * @param streamType .
	 * @param index .
	 * @param flags .
	 */
	public static void setStreamVolume(final int streamType, final int index, final int flags) {
		final IAudioService service = getService();
		try {
			service.setStreamVolume(streamType, index, flags, EContext.getOpPackageName());
		} catch (final RemoteException e) {
			throw e.rethrowFromSystemServer();
		}
	}


	/**
	 * <p>See {@link AudioManager#getStreamVolume(int)}.</p>
	 * <br>
	 * <p>Changes:</p>
	 * <p>- Method made static as it doesn't use Context</p>
	 *
	 * @param streamType .
	 *
	 * @return .
	 */
	public static int getStreamVolume(final int streamType) {
		final IAudioService service = getService();

		try {
			return service.getStreamVolume(streamType);
		} catch (final RemoteException e) {
			throw e.rethrowFromSystemServer();
		}
	}


	/**
	 * <p>See {@link AudioManager#getStreamMaxVolume(int)}.</p>
	 * <br>
	 * <p>Changes:</p>
	 * <p>- Method made static as it doesn't use Context</p>
	 *
	 * @param streamType .
	 *
	 * @return .
	 */
	public static int getStreamMaxVolume(final int streamType) {
		final IAudioService service = getService();

		try {
			return service.getStreamMaxVolume(streamType);
		} catch (final RemoteException e) {
			throw e.rethrowFromSystemServer();
		}
	}
}
