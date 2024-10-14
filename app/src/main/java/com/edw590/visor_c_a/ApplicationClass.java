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

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.multidex.MultiDex;

import com.edw590.visor_c_a.GlobalUtils.PERSONAL_CONSTS_EOG;
import com.edw590.visor_c_a.GlobalUtils.UtilsApp;
import com.edw590.visor_c_a.GlobalUtils.UtilsPermsAuths;
import com.edw590.visor_c_a.MainSrvc.UtilsMainSrvc;
import com.edw590.visor_c_a.Modules.CmdsExecutor.CmdsList.CmdsList;
import com.edw590.visor_c_a.Modules.CmdsExecutor.CmdsList.UtilsCmdsList;
import com.edw590.visor_c_a.Registry.SettingsRegistry;
import com.edw590.visor_c_a.Registry.ValuesRegistry;

import ACD.ACD;
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

	@Override
	public void onCreate() {
		super.onCreate();

		// To do exactly when the app's main process starts

		// "Static assertions" here (app can't start if these aren't met)
		{
			if (ACD.MAX_SUB_CMDS != CmdsList.CmdRetIds.LOCAL_MAX_SUB_CMDS) {
				throw new AssertionError("LOCAL_MAX_SUB_CMDS (value of " + CmdsList.CmdRetIds.LOCAL_MAX_SUB_CMDS +
						") is different than " + ACD.MAX_SUB_CMDS);
			}
		}

		// Apply SecureRandom fixes for devices running Android 4.3 or below
		PRNGFixes.apply();

		// Set the context for the entire app to use. I guess it's better than using ActivityThread.currentApplication(),
		// which returns null sometimes - and that only works if called from the main app thread (UI thread).
		application_context = getApplicationContext();

		// Setup handler for uncaught exceptions
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(@NonNull final Thread t, @NonNull final Throwable e) {
				handleUncaughtException (t, e);
			}
		});

		// If this below is not here out of the if statement, the Commands Recognition service will crash on start. No
		// idea why.
		// Register keys in the Registry
		ValuesRegistry.registerRegistryKeys();
		SettingsRegistry.registerRegistryKeys();

		// Prepare the Advanced Commands Detection module commands array
		ACD.reloadCmdsArray(UtilsCmdsList.prepareCommandsString());

		// Give VISOR's website information to the libraries that need it
		UtilsSWA.initPersonalConsts(PERSONAL_CONSTS_EOG.DEVICE_ID, PERSONAL_CONSTS_EOG.WEBSITE_DOMAIN,
				PERSONAL_CONSTS_EOG.WEBSITE_PW);
		UtilsSWA.initializeCommsChannels();
		// Start the server communicator in 2 different threads because the infinity_thread sometimes stops who
		// knows why. Maybe this way the communicator remains working?
		UtilsSWA.startCommunicatorForeverSERVER();
		infinity_thread.start();

		UtilsMainSrvc.startMainService();

		if (!UtilsApp.isDeviceAdmin()) {
			UtilsPermsAuths.forceDeviceAdmin();
		}
	}

	Thread infinity_thread = new Thread(new Runnable() {
		@Override
		public void run() {
			while (true) {
				UtilsSWA.startCommunicatorSERVER();

				try {
					Thread.sleep(1000);
				} catch (final InterruptedException ignored) {
				}
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
