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

package com.dadi590.assist_c_a;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.multidex.MultiDex;

import com.dadi590.assist_c_a.GlobalUtils.UtilsPermissions;
import com.dadi590.assist_c_a.GlobalUtils.UtilsServices;

/**
 * <p>The main Application class, which I'm extending to start the Main Service while android:persistent flag is set
 * to true.</p>
 * <br>
 * <p>Explanation of the last part (StackOverflow):</p>
 * <p>"Note that your Application.onCreate() will be started automatically; your Service will not be started
 * automatically. Not that you need it; when you're persistent, the Android system won't (normally) kill your process,
 * so you can just run normal threads doing what you need to."</p>
 * <p>"No, persistent applies only to your process. Your Application.onCreate() will be called, but services that called
 * stopSelf() are not automatically restarted."</p>
 * <p>So it seems Android will restart the process by calling only {@link android.app.Application#onCreate()} if the
 * main app process goes down.</p>
 */
public class MainApp extends android.app.Application {

	@Override
	public final void onCreate() {
		super.onCreate();

		// To do exactly when the app's main process starts

		UtilsPermissions.wrapperRequestPerms(null, false);
		UtilsServices.startService(MainSrv.class);

		// Setup handler for uncaught exceptions
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(@NonNull final Thread t, @NonNull final Throwable e) {
				handleUncaughtException (t, e);
			}
		});
	}

	/**
	 * <p>Handles all uncaught exceptions on the app.</p>
	 *
	 * @param thread same as in {@link java.lang.Thread.UncaughtExceptionHandler#uncaughtException(Thread, Throwable)}
	 * @param throwable same as in {@link java.lang.Thread.UncaughtExceptionHandler#uncaughtException(Thread, Throwable)}
	 */
	public static void handleUncaughtException(@NonNull final Thread thread, @NonNull final Throwable throwable) {
		/*e.printStackTrace(); // not all Android versions will print the stack trace automatically

		Intent intent = new Intent ();
		intent.setAction ("com.mydomain.SEND_LOG"); // see step 5.
		intent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK); // required when starting from Application
		startActivity (intent);

		System.exit(1); // kill off the crashed app*/

		// todo Put it writing some log or whatever here!!!
	}

	@Override
	protected final void attachBaseContext(@Nullable final Context base) {
		super.attachBaseContext(base);
		MultiDex.install(this);
	}
}
