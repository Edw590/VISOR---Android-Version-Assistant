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

import androidx.annotation.NonNull;
import android.app.AppGlobals;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Process;
import android.os.RemoteException;
import android.os.UserHandle;

/**
 * <p>Original class: {@link PackageManager}.</p>
 */
public final class EPackageManager {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private EPackageManager() {
	}

	/**.
	 * @return an instance of {@link IPackageManager}
	 */
	@androidx.annotation.NonNull
	private static IPackageManager getIPackageManager() {
		return AppGlobals.getPackageManager();
	}

	/**
	 * <p>See {@link PackageManager#getPackageInfo(String, int)} ()}.</p>
	 * <br>
	 * <p>Changes:</p>
	 * <p>- Method implemented with the same technique as in {@link EAudioManager}, including an implementation to
	 * throw the exception on the signature (hopefully the correct implementation).
	 * <p>- Method made static as it doesn't use Context</p>
	 *
	 * @param packageName .
	 * @param flags .
	 *
	 * @return .
	 *
	 * @throws PackageManager.NameNotFoundException .
	 */
	@androidx.annotation.NonNull
	public static PackageInfo getPackageInfo(@NonNull final String packageName,
											 @PackageManager.PackageInfoFlags final int flags)
			throws PackageManager.NameNotFoundException {
		final IPackageManager iPackageManager = getIPackageManager();
		try {
			// Hopefully this is the user ID the method wants.
			final PackageInfo ret_value = iPackageManager.getPackageInfo(packageName, flags, UserHandle.myUserId());
			if (ret_value == null) {
				// I tested with a random string and the result was null. So I'm guessing this is the correct
				// implementation for when the package doesn't exist (check if the result is null or not and throw the
				// exception).
				throw new PackageManager.NameNotFoundException(packageName);
			}
			return iPackageManager.getPackageInfo(packageName, flags, UserHandle.myUserId());
		} catch (final RemoteException e) {
			throw e.rethrowFromSystemServer();
		}
	}
}
