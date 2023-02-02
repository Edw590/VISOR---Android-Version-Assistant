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

package com.dadi590.assist_c_a.Modules.ProtectedLockScr;

import android.app.KeyguardManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.os.UserHandle;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.android.internal.widget.LockPatternUtils;
import com.dadi590.assist_c_a.GlobalInterfaces.IModuleInst;
import com.dadi590.assist_c_a.GlobalInterfaces.IModuleSrv;
import com.dadi590.assist_c_a.GlobalUtils.GL_CONSTS;
import com.dadi590.assist_c_a.GlobalUtils.ObjectClasses;
import com.dadi590.assist_c_a.GlobalUtils.UtilsGeneral;
import com.dadi590.assist_c_a.GlobalUtils.UtilsNotifications;
import com.dadi590.assist_c_a.MainSrvc.UtilsMainSrvc;

/**
 * <p>The Protected Lock Screen service, which is basically an insurance that the Protected Lock Screen activity is
 * always running.</p>
 * <p>And, since this is also always running, does other checks too to be 100% sure the user can't leave the
 * Protected Lock Screen.</p>
 */
public class ProtectedLockScrSrv extends Service implements IModuleSrv {

	final Intent intentPLS = UtilsProtectedLockScr.getPLSIntent();

	boolean locked = true;

	///////////////////////////////////////////////////////////////
	// IModuleSrv stuff
	@Override
	public final int wrongIsSupported() {return 0;}
	/**.
	 * @return read all here {@link IModuleInst#wrongIsSupported()} */
	public static boolean isSupported() {
		return true;
	}
	// IModuleSrv stuff
	///////////////////////////////////////////////////////////////

	@Override
	public final void onCreate() {
		super.onCreate();

		// Do this only once, when the service is created and while it's not destroyed

		final ObjectClasses.NotificationInfo notificationInfo = new ObjectClasses.NotificationInfo(
				GL_CONSTS.CH_ID_PLS_SRV_FOREGROUND,
				"Protected Lock Screen notification",
				"",
				NotificationCompat.PRIORITY_MAX,
				GL_CONSTS.ASSISTANT_NAME + "'s Protected Lock Screen running",
				"",
				null
		);
		startForeground(GL_CONSTS.NOTIF_ID_PLS_SRV_FOREGROUND, UtilsNotifications.getNotification(notificationInfo).
				setVisibility(NotificationCompat.VISIBILITY_PUBLIC).
				setOngoing(true).
				build());

		UtilsMainSrvc.startMainService();

		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_USER_PRESENT);
		try {
			UtilsGeneral.getContext().registerReceiver(localBroadcastReceiver, intentFilter);
		} catch (final IllegalArgumentException ignored) {
		}

		if (locked) { // Just a check to be more sure the activity doesn't come if it's not needed anymore.
			try {
				infinity_checker.start();
			} catch (final IllegalThreadStateException ignored) {
			}
		}
	}

	private final BroadcastReceiver localBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(@Nullable final Context context, @Nullable final Intent intent) {
			UtilsMainSrvc.startMainService();

			if (intent == null || intent.getAction() == null) {
				return;
			}

			boolean device_is_secured;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				try {
					final LockPatternUtils lockPatternUtils = new LockPatternUtils(UtilsGeneral.getContext());
					device_is_secured = lockPatternUtils.isLockScreenDisabled(UserHandle.myUserId());
				} catch (final Throwable ignored) { // ANY error....
					// This won't differentiate between None and Swipe though. But it's better than nothing, since the
					// method for Lollipop and older doesn't seem to be working when I call it on Oreo 8.1 for some reason.
					final KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
					device_is_secured = keyguardManager.isDeviceSecure();
				}
			} else {
				device_is_secured = UtilsProtectedLockScr.isLockScreenEnabled22Older();
			}

			// If the user unlocked the phone successfully, lock it again immediately.
			if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
				if (locked && device_is_secured) { // If device is not secured (Lock Screen mode as None), as soon as
					// the screen is On, the user will be present --> not supposed to lock the phone again... (or in
					// this case, turn the screen off.
					UtilsProtectedLockScr.lockAndShowPLS(intentPLS);
				}
			}
		}
	};

	/**
	 * <p>This will restart the {@link ProtectedLockScrAct} activity if there is some error and it is shut down if it
	 * cannot retrieve running tasks from all the apps - if it can (depends on permissions), it will also check if the
	 * activity is not the one in absolute foreground, and if it's not, restart it.</p>
	 * <p>This means that:</p>
	 * <p>- Below Lollipop and on Lollipop and above with the app installed as a system app, if you click on the Recents
	 * button and switch to some other app but don't close the PLS (just switch from app), it will detect the change and
	 * will know the PLS is no longer in the foreground.</p>
	 * <p>- On Lollipop and above without app installed as a system app, if you switch apps, the app won't detect that
	 * and will think it's still in foreground. Though, if there's an error and the PLS is closed, it will detect that,
	 * so it's not useless.</p>
	 */
	final Thread infinity_checker = new Thread(new Runnable() {
		@Override
		public void run() {
			while (locked) {
				UtilsProtectedLockScr.showPLS(intentPLS);

				UtilsMainSrvc.startMainService();

				try {
					Thread.sleep(1000L);
				} catch (final InterruptedException ignored) {
					Thread.currentThread().interrupt();

					return;
				}
			}
		}
	});

	@Override
	public final int onStartCommand(@Nullable final Intent intent, final int flags, final int startId) {
		// Do this below every time the service is started/resumed/whatever

		// Do NOT put ANYTHING here!!!
		// The Main Service keeps starting this service every some time to be sure it never stops - and doesn't check
		// if it's already running or not. Read the rest on Main Service's onStartCommand().

		return START_NOT_STICKY;
	}

	@Override
	@Nullable
	public final IBinder onBind(@Nullable final Intent intent) {
		return null;
	}
}
