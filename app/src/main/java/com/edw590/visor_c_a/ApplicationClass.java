/*
 * Copyright 2021-2024 Edw590
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

package com.edw590.visor_c_a;

import android.Manifest;
import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.multidex.MultiDex;

import com.edw590.visor_c_a.GlobalUtils.UtilsApp;
import com.edw590.visor_c_a.GlobalUtils.UtilsPermsAuths;
import com.edw590.visor_c_a.GlobalUtils.UtilsSettings;
import com.edw590.visor_c_a.MainSrvc.UtilsMainSrvc;
import com.edw590.visor_c_a.Modules.CmdsExecutor.CmdsList.CmdsList;
import com.edw590.visor_c_a.Registry.RegistryKeys;

import org.opencv.android.OpenCVLoader;

import ACD.ACD;
import GPTComm.GPTComm;
import SettingsSync.SettingsSync;
import UtilsSWA.UtilsSWA;

/**
 * <p>The Application class of the app, which I'm extending to start the Main Service while android:persistent flag is
 * set to true.</p>
 * <br>
 * <p>Explanation of the last part (StackOverflow):</p>
 * <p>"Note that your Application.onCreate() will be started automatically; your Service will not be started
 * automatically. Not that you need it; when you're persistent, the Android system won't (normally) kill your process,
 * so you can just run normal threads doing what you need to."</p>
 * <p>"No, persistent applies only to your process. Your Application.onCreate() will be called, but services that called
 * stopSelf() are not automatically restarted."</p>
 * <p>So it seems Android will restart the main app process if it goes down, by calling only
 * {@link Application#onCreate()} if the main app.</p>
 */
public final class ApplicationClass extends Application {

	// Won't ever be null while the app is running because everything else will be called after
	// ApplicationClass.onCreate(), after which applicationContext is initialized. So it's fine to initialize it to null
	// here and say it's NonNull.
	// Except, it seems, with Content Providers, in which case the Application class may not have been initialized yet.
	// But with Activities, Services and Receivers it's alright. Though, if called from this class's Constructor, this
	// variable may not be ready yet. Not sure.
	// EDIT: I've just put it @Nullable. @NonNull is now only on UtilsGeneral.getContext(), as it already was. @Nullable
	// is here now too to indicate it can actually be null (only with Content Providers, as said above - but it can).
	/**
	 * <p>The main application's context.</p>
	 * <p>Do NOT use with Content Provider classes! This variable may not be ready in those cases and be null!</p>
	 * <p>Use in Activities, Services and Receivers and everything else.</p>
	 */
	@Nullable public static Context application_context = null;

	public static long init_nano_time = 0;
	public static long init_millis_time = 0;

	private boolean settings_files_checked = false;

	@Override
	public void onCreate() {
		super.onCreate();

		// To do exactly when the app's main process starts

		// Assertions here (app can't start if these aren't met)
		assert ACD.MAX_SUB_CMDS == CmdsList.CmdRetIds.LOCAL_MAX_SUB_CMDS;

		// Apply SecureRandom fixes for devices running Android 4.3 or below
		PRNGFixes.apply();

		// Set the context for the entire app to use. I guess it's better than using ActivityThread.currentApplication(),
		// which returns null sometimes - and that only works if called from the main app thread (UI thread).
		application_context = getApplicationContext();

		init_millis_time = System.currentTimeMillis();
		init_nano_time = System.nanoTime();

		// Setup handler for uncaught exceptions
		Thread.setDefaultUncaughtExceptionHandler(ApplicationClass::handleUncaughtException);

		/////////////////////////////////////////////////////////////

		if (UtilsPermsAuths.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
			if (!UtilsSettings.loadSettingsFile(false)) {
				System.out.println("Failed to load generated settings. Using empty ones...");
			}

			if (!UtilsSettings.loadSettingsFile(true)) {
				System.out.println("Failed to load user settings. Using empty ones...");
			}

			settings_files_checked = true;
		}

		// Load OpenCV
		OpenCVLoader.initDebug();

		infinity_thread.start();

		UtilsSWA.initializeCommsChannels();

		GPTComm.startReportingNoModelsOLLAMA();
		UtilsSWA.startCommunicatorSERVER();
		SettingsSync.syncUserSettings();

		// Register keys in the Registry
		RegistryKeys.registerValues();

		UtilsMainSrvc.startMainService();

		if (!UtilsApp.isDeviceAdmin()) {
			UtilsPermsAuths.forceDeviceAdmin();
		}
	}

	Thread infinity_thread = new Thread(() -> {
		while (!settings_files_checked) {
			// Keep trying to check if the files exist or not
			if (UtilsPermsAuths.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
				UtilsSettings.loadSettingsFile(false);
				UtilsSettings.loadSettingsFile(true);

				settings_files_checked = true;
			}

			try {
				Thread.sleep(1000);
			} catch (final InterruptedException ignored) {
				return;
			}
		}
		while (true) {
			// Write user and gen settings every 5 seconds

			UtilsSettings.writeSettingsFile(SettingsSync.getJsonUserSettings(), true);

			UtilsSettings.writeSettingsFile(SettingsSync.getJsonGenSettings(), false);

			try {
				Thread.sleep(5000);
			} catch (final InterruptedException ignored) {
				return;
			}
		}
	});

	@Override
	protected void attachBaseContext(final Context base) {
		super.attachBaseContext(base);
		MultiDex.install(this);
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

		System.out.println("1------------- CRITICAL APP ERROR -------------1");
		System.out.println("Thread: " + thread);
		System.out.println("Error:");
		throwable.printStackTrace();
		System.out.println("2------------- CRITICAL APP ERROR -------------2");

		// todo Put it writing some log or whatever here!!!
		// If you need Context for anything, use getApplicationContext, since this is used before the static one is set.
	}
}
