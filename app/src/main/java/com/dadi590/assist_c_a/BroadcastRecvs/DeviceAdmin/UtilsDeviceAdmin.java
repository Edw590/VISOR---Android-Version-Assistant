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

package com.dadi590.assist_c_a.BroadcastRecvs.DeviceAdmin;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.os.Build;

import com.dadi590.assist_c_a.GlobalUtils.UtilsContext;

/**
 * <p>Device Administration related utilities.</p>
 */
public final class UtilsDeviceAdmin {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsDeviceAdmin() {
	}

	/**
	 * <p>Locks the device immediately.</p>
	 *
	 * @return true if the device was locked, false if {@link DevicePolicyManager#lockNow()} threw a SecurityException
	 * or the {@link DevicePolicyManager} does not exist on the system
	 */
	public static boolean lockDevice() {
		final DevicePolicyManager devicePolicyManager = (DevicePolicyManager) UtilsContext.
				getSystemService(Context.DEVICE_POLICY_SERVICE);
		if (null == devicePolicyManager) {
			return false;
		}

		try {
			devicePolicyManager.lockNow();
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				if (devicePolicyManager.getStorageEncryptionStatus() != DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE_PER_USER) {
					devicePolicyManager.lockNow(DevicePolicyManager.FLAG_EVICT_CREDENTIAL_ENCRYPTION_KEY);
					// From https://developer.android.com/reference/android/app/admin/DevicePolicyManager#lockNow(int):
					// "NOTE: In order to lock the parent profile and evict the encryption key of the managed profile,
					// lockNow() must be called twice: First, lockNow() should be called on the DevicePolicyManager
					// instance returned by getParentProfileInstance(android.content.ComponentName), then lockNow(int)
					// should be called on the DevicePolicyManager instance associated with the managed profile, with
					// the FLAG_EVICT_CREDENTIAL_ENCRYPTION_KEY flag. Calling the method twice in this order ensures
					// that all users are locked and does not stop the device admin on the managed profile from issuing
					// a second call to lock its own profile."
				}
			}

			return true;
		} catch (final SecurityException ignored) {
			// Could not lock the device, for example because the app is not a Device Administrator or (in case the app
			// is running in Android 11 or above) the app does not hold the LOCK_DEVICE permission.
		}

		return false;
	}
}
