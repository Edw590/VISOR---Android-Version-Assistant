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

import android.annotation.SuppressLint;
import android.app.StatusBarManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.dadi590.assist_c_a.BroadcastRecvs.DeviceAdmin.UtilsDeviceAdmin;
import com.dadi590.assist_c_a.GlobalUtils.UtilsApp;
import com.dadi590.assist_c_a.GlobalUtils.UtilsGeneral;
import com.dadi590.assist_c_a.GlobalUtils.UtilsServices;
import com.dadi590.assist_c_a.GlobalUtils.UtilsSysApp;
import com.dadi590.assist_c_a.R;

/**
 * <p>The activity of the assistant's Protected Lock Screen (PLS for abbreviation).</p>
 */
public class ProtectedLockScr extends AppCompatActivity {

	// For an explanation on how the Protected Lock Screen works, please go to the package-info.java file.



	// todo Put the Main Service checking if the PLS service is running or not!!!
	// Use SharedPreferences or something like that to know if the PLS should be working or not.

	// todo Throw some error here (like calling a non-existent method with reflection or calling something null
	//  and the PLS must restart!!! Not die forever as it's doing now!!!

	// todo Missing to put the app knowing it's in alert mode, so if the phone is restarted, the Protected Lock Screen
	//  will appear right after that.


	// todo ATTENTION!!! On some devices (for example on BV9500), clicking on Uninstall with Admin Mode enabled
	// is equivalent to removing Admin Mode without request + Force stop (in this order). This means that once the user
	// clicks here, even if the protected screen is enabled, the app will no longer be a Device Admin! Think on
	// something about that! See if you can do everything on the protected lock screen without Device Admin perks!

	// Actually, since the app will be forced to stop, maybe you could start another app before this one is stopped...?
	// That app would have a copy of the code of the protected lock screen. Then maybe we could still benefit from
	// Device Admin perks, since stopping one doesn't mean stopping the other at the same time... Unless it's a rooted
	// device - so that actually means the 2nd app must restart this one as soon as possible because an app that would
	// force stop the 2 apps would do it sequentially --> unless it would be done in multi-processor task, but maybe not.
	//
	// Or idea B, postpone an app restart from inside the app to restart as soon as possible before the user can click
	// "OK" to uninstall the app. --> Can't be done. Force stop will remove any Alarms tied to the app, so can't restart
	// it, at least without root access / system permissions (no idea if there are permissions or commands for that).
	//
	// Really gotta make another app to restart this one. See ActivityManager#forceStopPackageAsUser() and then
	// https://developer.android.com/reference/android/content/Intent.html#ACTION_PACKAGE_RESTARTED.
	//
	// As soon as this app would restart, it would start the protected screen and all would be safe again.


	customViewGroup view = null;

	final Intent intentPLS = UtilsProtectedLockScr.getPLSIntent();

	Thread collapse_infinity;

	boolean locked = true;
	boolean system_overlay = false; // No system overlay, then plan B: collapse the status bar.
	boolean has_focus = true;
	boolean runnable_running = false;

	// Meaningless error. The class exists since at least, API 15. Ignore the StatusBarManager error of only above
	// API 29 - that's on public APIs only, not internal ones.
	// Also the "WrongConstant" STATUS_BAR_SERVICE works, so can't be that wrong - ignore too.
	@SuppressLint({"NewApi", "WrongConstant"})
	StatusBarManager statusBarManager = (StatusBarManager) UtilsGeneral.getContext().getSystemService(STATUS_BAR_SERVICE);
	// At least compiling with API 29, the method on StatusBarManager is still collapsePanels(), so all cool using the
	// internal API directly without reflection.

	@Override
	protected final void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.protected_lock_scr);

		// Do this only once, when the activity is created and while it's not destroyed

		UtilsServices.startMainService();

		// Lock the device immediately.
		UtilsDeviceAdmin.lockDevice();

		// Then enter Full Screen mode.
		enterFullScreen();

		// And start the service to be sure this never stops - don't check so it's faster to start it.
		// Keep it starting in foreground, so if there is any error on the Main Service, this one still runs.
		UtilsServices.startService(ProtectedLockScrSrv.class, true);

		findViewById(R.id.btn_unlock).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				locked = false;
				UtilsServices.stopService(ProtectedLockScrSrv.class);

				if (view != null) {
					final WindowManager windowManager = ((WindowManager) getApplicationContext()
							.getSystemService(Context.WINDOW_SERVICE));
					windowManager.removeView(view); // Remove the status bar overlay in case it has been created
				}

				finish();
			}
		});
	}

	@Override
	protected final void onStart() {
		super.onStart();

		// Do this below every time the activity is started/resumed/whatever

		UtilsServices.startMainService();
	}

	@Override
	protected final void onStop() {
		super.onStop();

		UtilsServices.startMainService();
	}

	/**
	 * <p>Collapses the status bar every some time.</p>
	 */
	final Runnable collapse_infinity_runnable = new Runnable() {
		@Override
		public void run() {
			runnable_running = true;
			int count = 1;
			while (locked && !has_focus) {
				// The onWindowFocusChanged is not fast enough to detect expansion of the status bar. So plan B...
				// Always collapsing it, no matter what.
				statusBarManager.collapsePanels();

				if (count >= 4) { // >= just in case it goes above and passes this no idea why
					// If after some time this is still running - imagine the user clicked many times on Home and PLS
					// didn't come again (already happened) --> this will restart it.
					// If the activity is deleted, the service will restart it, so no worries about that part.
					// That "some time" is 25ms * 4 = 100ms. More than enough for the status bar to go. More than that
					// and the user either left the PLS or is still holding the status bar.
					UtilsProtectedLockScr.lockAndShowPLS(intentPLS);
					count = 0;
				}

				System.out.println("KKKKKKKKKKK");

				if (hasWindowFocus()) {
					// If the activity is focused again, stop the thread in case onWindowFocusChanged() doesn't do it.
					// Never mind. The less things here the better.
					has_focus = true;
					System.out.println("LLLLLLLLLL");
				}

				try {
					// It seems the fastest human conscious reaction time is 0.15s (150ms) and unconscious is 0.08s
					// (80ms). So almost 1/4 of that should be fine, hopefully.
					// Also beware not to put 0 or too low values, or that will get the app slower. 10ms I'd say is the
					// limit, at least on BV9500. 20ms should be fine. To be above "fine", 25ms. Should be good.
					Thread.sleep(25L);
				} catch (final InterruptedException ignored) {
				}
				count++;
			}
			runnable_running = false;
		}
	};

	@Override
	public final void onBackPressed() {
		// Ignore (disable the back button then).
	}

	@Override
	public final void onWindowFocusChanged(final boolean hasFocus) {
		if (!hasFocus && locked) {
			has_focus = false;
			UtilsProtectedLockScr.lockAndShowPLS(intentPLS); // This is enough, it seems. No service, no loops.
			// Only this. At least as long as the app is a Device Administrator. Wouldn't even need the system overlay.
			if (!system_overlay && !UtilsApp.isDeviceAdmin()) { // If it's a Device Admin, lockNow() will suffice.
				// collapsePanels() only exists from API 17 onwards. Before that, it was collapse(). On API 22 and
				// below, SYSTEM_ALERT_WINDOW is granted normally. So we can always draw the view to block the status
				// bar on API 22 and below --> which includes the non-existent collapse() method.
				// This means we'll only get here from API 23 and above, never below (so never below 17 --> so
				// there won't be an error about collapsePanels()).

				// Collapse the status bar immediately (doesn't work well calling it here only, since it will only close
				// it once and in the beginning. If the user hold the status bar open for some time, it won't close it.
				// That's the reason for the "collapse_infinity" thread.
				statusBarManager.collapsePanels();

				if (!runnable_running) {
					collapse_infinity = new Thread(new Runnable() {
						@Override
						public void run() {
							collapse_infinity_runnable.run();
						}
					});
					collapse_infinity.start();
				}
			}
		} else if (hasFocus) {
			// In case the activity has focus again, stop the thread.
			//has_focus = true;
			//System.out.println("JJJJJJJJJ");
		}
	}

	/**
	 * <p>Custom ViewGroup to steal the motion events on the status bar.</p>
	 */
	public static class customViewGroup extends ViewGroup {

		/**
		 * <p>Main class's constructor.</p>
		 *
		 * @param context a context
		 */
		public customViewGroup(@NonNull final Context context) {
			super(context);
		}

		@Override
		protected void onLayout(final boolean changed, final int l, final int t, final int r, final int b) {
		}

		@Override
		public final boolean onInterceptTouchEvent(@Nullable final MotionEvent ev) {
			//Log.v("customViewGroup", "**********Intercepted");
			return true;
		}
	}

	/**
	 * <p>Enables full screen on the app. The status bar is kept and it is prepared to be able for the touch events to
	 * be stolen and redirected to do nothing by the custom ViewGroup class.</p>
	 */
	public final void enterFullScreen() {
		// Prepare a new window
		final WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
		// SYSTEM_ALERT_WINDOW is not available from API 23 onwards automatically. Before that, everything is alright.
		// On 23 and above, only if the app is allowed to draw overlays (OR comes from the Play Store, in which case,
		// the permission will be granted automatically - some times, not always, it's confusing).
		// From API 26 and above, TYPE_SYSTEM_ERROR (and others) can only be used if the app is a system app.
		final int window_type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			System.out.println("RRRRRRRRRRRRRRRRR");
			System.out.println(Settings.canDrawOverlays(UtilsGeneral.getContext()));
			if (Settings.canDrawOverlays(UtilsGeneral.getContext())) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
					System.out.println(UtilsSysApp.isSystemApp());
					if (UtilsSysApp.isSystemApp()) {
						layoutParams.type = window_type;
						system_overlay = true;
					}
				} else {
					layoutParams.type = window_type;
					system_overlay = true;
				}
			}
		} else {
			layoutParams.type = window_type;
			system_overlay = true;
		}

		// Update current window using the window type selected above
		getWindow().setType(layoutParams.type);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				//| WindowManager.LayoutParams.FLAG_FULLSCREEN - the status bar needs to be showing (battery, for example)
				// It won't stop people from pulling the status bar on API 23 onwards anyways.
				//| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON - let the phone rest after whatever seconds or minutes...
				| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);

		if (system_overlay) {
			// Keep preparing a new window if the type is SYSTEM_ERROR. If it's not, the view will do nothing, so don't
			// create it.
			layoutParams.gravity = Gravity.TOP;
			layoutParams.flags =
					WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |

							// this is to enable the notification to receive touch events
							WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |

							// Draws over status bar
							WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN

							| WindowManager.LayoutParams.FLAG_FULLSCREEN
							| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
							| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
							| WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON;

			layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
			layoutParams.height = (int) (50.0F * getResources().getDisplayMetrics().scaledDensity);
			layoutParams.format = PixelFormat.TRANSPARENT;

			final WindowManager windowManager = ((WindowManager) getApplicationContext()
					.getSystemService(Context.WINDOW_SERVICE));
			view = new customViewGroup(UtilsGeneral.getContext());
			windowManager.addView(view, layoutParams);
		}
	}
}
