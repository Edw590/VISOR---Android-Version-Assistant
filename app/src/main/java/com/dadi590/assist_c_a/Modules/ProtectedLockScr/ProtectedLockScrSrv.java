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

package com.dadi590.assist_c_a.Modules.ProtectedLockScr;

import android.app.ActivityThread;
import android.app.AppGlobals;
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
import androidx.annotation.RequiresApi;

import com.android.internal.widget.LockPatternUtils;
import com.dadi590.assist_c_a.GlobalUtils.GL_CONSTS;
import com.dadi590.assist_c_a.GlobalUtils.ObjectClasses;
import com.dadi590.assist_c_a.GlobalUtils.UtilsGeneral;
import com.dadi590.assist_c_a.GlobalUtils.UtilsServices;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * <p>The Protected Lock Screen service, which is basically an insurance that the Protected Lock Screen activity is
 * always running.</p>
 * <p>And, since this is also always running, does other checks too to be 100% sure the user can't leave the
 * Protected Lock Screen.</p>
 */
public class ProtectedLockScrSrv extends Service {

	final Intent intentPLS = UtilsProtectedLockScr.getPLSIntent();

	boolean locked = true;

	@Override
	public final void onCreate() {
		super.onCreate();

		// Do this only once, when the service is created and while it's not destroyed

		final ObjectClasses.NotificationInfo notificationInfo = new ObjectClasses.NotificationInfo(
				GL_CONSTS.CH_ID_PLS_SRV_FOREGROUND,
				"Protected Lock Screen notification",
				"",
				UtilsServices.TYPE_FOREGROUND,
				GL_CONSTS.ASSISTANT_NAME + "'s Protected Lock Screen running",
				"",
				null
		);
		startForeground(GL_CONSTS.NOTIF_ID_PLS_SRV_FOREGROUND, UtilsServices.getNotification(notificationInfo));

		UtilsServices.startMainService();

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
		public void onReceive(final Context context, final Intent intent) {
			UtilsServices.startMainService();

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
				device_is_secured = isLockScreenEnabled22Older();
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
	 * <p>Checks if the lock screen enabled or not for the current user through
	 * {@link LockPatternUtils#isLockScreenDisabled(int)} (though, this uses an older version which does not require the
	 * {@code int} argument), which means, should check the same as {@link KeyguardManager#isDeviceSecure()}, but on
	 * older API versions than Marshmallow (as of which the parameter was introduced).</p>
	 * <br>
	 * <p><strong>TO USE ONLY WITH API 22 OR OLDER!!!!!</strong></p>
	 * <p>It's said on where I took this from (StackOverflow): "It should work with API level 14+", so I'm putting that
	 * as the minimum Android version for this function to work on.</p>
	 *
	 * @return true if the lock screen is enabled for the current user, false otherwise. If there's any error getting
	 * the method mentioned above (gotten through reflection), false will be returned as the worst case.
	 */
	@RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	static boolean isLockScreenEnabled22Older() {
		final String LockPatternUtils_location = "com.android.internal.widget.LockPatternUtils";

		/*Testa isto com a meta-reflection para ver se ficou a funcionar!!!
				Podia ser usado por algum atacante enviar MUITOS broadcasts e a app vai bloquear!
			Arranja isso. Mete numa thread e impede de ser chamada mais que uma vez enquanto estiver a funcionar ou mete
				a forçagem de permissões MEGA rápida --> tal como a função de iniciar o serviço, tenta metê-la ao máximo.*/

		try {
			final ObjectClasses.GetMethodClassObj getMethodClassObj = UtilsGeneral.getMethodClass(LockPatternUtils_location);
			if (getMethodClassObj != null) {
				final Class<?> lockUtilsClass = getMethodClassObj.hidden_class;
				final Object lockUtils = lockUtilsClass.getConstructor(Context.class).newInstance(UtilsGeneral.getContext());
				final Method method = (Method) getMethodClassObj.getMethod.invoke(lockUtilsClass, "isLockScreenDisabled");
				if (method != null) {
					return !Boolean.parseBoolean(String.valueOf(method.invoke(lockUtils)));
				}
			}
		} catch (final InstantiationException ignored) {
		} catch (final InvocationTargetException ignored) {
		} catch (final NoSuchMethodException ignored) {
		} catch (final IllegalAccessException ignored) {
		}

		return false; // In case the method can't be accessed for ANY reason, assume the worst case: no lock screen enabled.
	}

	/**
	 * <p>This will restart the {@link ProtectedLockScr} activity if there is some error and it is shut down if it
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
				System.out.println("LLLLLLLLLLL");
				System.out.println(getApplicationContext());
				System.out.println(getBaseContext());
				System.out.println(AppGlobals.getInitialApplication());
				System.out.println(ActivityThread.currentApplication());
				System.out.println(UtilsGeneral.getContext());
				System.out.println(intentPLS);

				UtilsProtectedLockScr.showPLS(intentPLS);

				UtilsServices.startMainService();

				try {
					Thread.sleep(1000L);
				} catch (final InterruptedException ignored) {
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

		return START_STICKY;
	}

	@Override
	@Nullable
	public final IBinder onBind(@Nullable final Intent intent) {
		return null;
	}

	@Override
	public final void onDestroy() {
		super.onDestroy();
		locked = false;
		try {
			UtilsGeneral.getContext().unregisterReceiver(localBroadcastReceiver);
		} catch (final IllegalArgumentException ignored) {
		}
	}
}
