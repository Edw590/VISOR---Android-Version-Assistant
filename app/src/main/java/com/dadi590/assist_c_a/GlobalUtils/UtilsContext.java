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

import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.ServiceManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dadi590.assist_c_a.ApplicationClass;

import java.util.Objects;

/**
 * <p>Utilties related to Context stuff.</p>
 */
public class UtilsContext {
	/**
	 * <p>Returns the Application Context.</p>
	 * <p>Main note: do NOT use on Content Provider classes. Only on Activities, Services and Receivers. Read the doc of
	 * {@link ApplicationClass#application_context}, which is the variable returned by this function.</p>
	 *
	 * @return same as in {@link Context#getApplicationContext()}
	 */
	@NonNull
	public static Context getContext() {
		// This shouldn't return null ever, if used from Activities, Service, and Receivers. If it ever does, change
		// @NonNull to @Nullable or think of/find something else/other method.
		return ApplicationClass.application_context;
	}

	/**
	 * <p>Same as {@code Application}{@link Context#getSystemService(String)}, but that includes @Nullable on it from AndroidX
	 * to provide warnings.</p>
	 *
	 * @param name same as in {@link Context#getSystemService(String)}
	 *
	 * @return same as in {@link Context#getSystemService(String)}
	 */
	@Nullable
	public static Object getSystemService(@NonNull final String name) {
		return getContext().getSystemService(name);
	}

	/**
	 * <p>Same as {@link ServiceManager#getService(String)}, but that includes @Nullable on it from AndroidX
	 * to provide warnings.</p>
	 *
	 * @param name same as in {@link ServiceManager#getService(String)}
	 *
	 * @return same as in {@link ServiceManager#getService(String)}
	 */
	@Nullable
	public static IBinder getService(@NonNull final String name) {
		return ServiceManager.getService(name);
	}

	/**
	 * <p>Same as {@link ServiceManager#getService(String)}, but that includes @Nullable on it from AndroidX
	 * to provide warnings.</p>
	 *
	 * @param name same as in {@link ServiceManager#getService(String)}
	 *
	 * @return same as in {@link ServiceManager#getService(String)}
	 */
	@NonNull
	public static NotificationManager getNotificationManager() {
		// Won't be null, because the Main Service won't start if there's no notification service on the device.
		return (NotificationManager) Objects.requireNonNull(getSystemService(Context.NOTIFICATION_SERVICE));
	}

	/**
	 * <p>Same as {@link Context#startActivity(Intent)}, but doesn't throw an exception.</p>
	 *
	 * @param intent the intent to start the activity
	 *
	 * @return true if the activity was started, false if it was not found
	 */
	public static boolean startActivity(@NonNull final Intent intent) {
		try {
			getContext().startActivity(intent);

			return true;
		} catch (final ActivityNotFoundException ignored) {
			return false;
		}
	}
}
