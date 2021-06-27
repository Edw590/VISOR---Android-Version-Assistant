package com.dadi590.assist_c_a.Modules.ProtectedLockScr;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.dadi590.assist_c_a.GlobalUtils.ExtClasses.SystemAppChecker;
import com.dadi590.assist_c_a.BroadcastRecvs.DeviceAdmin.DeviceAdminRecv;
import com.dadi590.assist_c_a.GlobalUtils.UtilsGeneral;
import com.dadi590.assist_c_a.R;
import com.dadi590.assist_c_a.GlobalUtils.UtilsApp;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * <p>The activity of the assistant's Protected Lock Screen.</p>
 */
public class ProtectedLockScr extends AppCompatActivity {



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
	// it, at least without root access / system permissions (no idea it there are permissions or commands for that).
	//
	// Really gotta make another app to restart this one. See ActivityManager#forceStopPackageAsUser() and then
	// https://developer.android.com/reference/android/content/Intent.html#ACTION_PACKAGE_RESTARTED.
	//
	// As soon as this app would restart, it would start the protected screen and all would be safe again.



	// To keep track of activity's window focus
	boolean currentFocus;

	// To keep track of activity's foreground/background status
	boolean isPaused;

	Object statusBarService = null;
	Method collapseStatusBar = null;

	@Override
	protected final void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.protected_lock_scr);

		//Set up our Lockscreen
		makeFullScreen();
		//startService(new Intent(this, ProtectedLockScr.class));

		// todo Missing disable the home key

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && UtilsApp.isDeviceAdmin(UtilsGeneral.getMainAppContext())) {
			System.out.println("OOOOOOOOOOOOOOO");
			final ComponentName componentName = new ComponentName(UtilsGeneral.getMainAppContext(), DeviceAdminRecv.class);
			final DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
			devicePolicyManager.setStatusBarDisabled(componentName, true);
			// todo Not working ^^^^^
		} else {
			// todo This method wastes too much battery... Think of something
			System.out.println("ÇÇÇÇÇÇÇÇÇÇÇÇÇÇÇ");
			readyMethod();
			colapse_infinity.start();
		}

		findViewById(R.id.btn_unlock).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				unlockScreen();
			}
		});
	}

	/**
	 * <p>Collapses the status bar every some time.</p>
	 */
	Thread colapse_infinity = new Thread(new Runnable() {
		@Override
		public void run() {
			while (true) {
				collapseNow();

				/*try {
					Thread.sleep(50L);
				} catch (final InterruptedException ignored) {
				}*/
			}
		}
	});

	/**
	 * A simple method that sets the screen to fullscreen.  It removes the Notifications bar,
	 *   the Actionbar and the virtual keys (if they are on the phone)
	 */
	public final void makeFullScreen() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			if (SystemAppChecker.isSystemApp(UtilsGeneral.getMainAppContext())) {
				getWindow().setType(WindowManager.LayoutParams.TYPE_PRIORITY_PHONE);
			} else {
				getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
			}
		} else {
			getWindow().setType(WindowManager.LayoutParams.TYPE_PRIORITY_PHONE);
		}

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
				| WindowManager.LayoutParams.FLAG_FULLSCREEN
				| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		getWindow().getDecorView().setSystemUiVisibility(
						View.SYSTEM_UI_FLAG_LAYOUT_STABLE
						| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
						| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
						| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
						| View.SYSTEM_UI_FLAG_FULLSCREEN
						| View.SYSTEM_UI_FLAG_IMMERSIVE);

		if (Build.VERSION.SDK_INT >= 19) {
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE);
		}
	}

	@Override
	public final void onBackPressed() {
		// Ignore
	}

	public void unlockScreen() {
		//Instead of using finish(), this totally destroys the process
		finish();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {

		currentFocus = hasFocus;

		if (!hasFocus) {
			// Method that handles loss of window focus
			collapseNow();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		// Activity's been paused
		isPaused = true;
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Activity's been resumed
		isPaused = false;
	}

	public void collapseNow() {
		System.out.println("JJJJJJJJJJJ");
		try {
			collapseStatusBar.invoke(statusBarService);
		} catch (IllegalAccessException ignored) {
		} catch (InvocationTargetException ignored) {
		}
	}

	private Method readyMethod() {

		// Use reflection to trigger a method from 'StatusBarManager'

		/*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
			final StatusBarManager statusBarManager = (StatusBarManager) getSystemService(STATUS_BAR_SERVICE);
			statusBarManager.collapsePanels();
		}*/

		statusBarService = getSystemService("statusbar");
		Class<?> statusBarManager = null;

		try {
			statusBarManager = Class.forName("android.app.StatusBarManager");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		try {

			// Prior to API 17, the method to call is 'collapse()'
			// API 17 onwards, the method to call is `collapsePanels()`

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
				collapseStatusBar = statusBarManager .getMethod("collapsePanels");
			} else {
				collapseStatusBar = statusBarManager .getMethod("collapse");
			}
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}

		collapseStatusBar.setAccessible(true);

		return collapseStatusBar;
	}
}
