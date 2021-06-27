package com.dadi590.assist_c_a.GlobalUtils.HiddenMethods;

import android.app.INotificationManager;
import android.app.NotificationManager;
import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;

import androidx.annotation.NonNull;

/**
 * <p>Original class: {@link NotificationManager}.</p>
 */
public final class ENotificationManager {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private ENotificationManager() {
	}

	/**.
	 * @return an instance of {@link INotificationManager}
	 */
	@NonNull
	private static INotificationManager getService() {
		final IBinder iBinder = ServiceManager.getService(Context.NOTIFICATION_SERVICE);
		return INotificationManager.Stub.asInterface(iBinder);
	}

	/**
	 * <p>See {@link NotificationManager#getCurrentInterruptionFilter()}.</p>
	 * <br>
	 * <p>Changes:</p>
	 * <p>- Method made static as it doesn't use Context</p>
	 *
	 * @return .
	 */
	@NotificationManager.InterruptionFilter
	public static int getCurrentInterruptionFilter() {
		final INotificationManager service = getService();
		try {
			return NotificationManager.zenModeToInterruptionFilter(service.getZenMode());
		} catch (final RemoteException e) {
			throw e.rethrowFromSystemServer();
		}
	}

	/**
	 * <p>See {@link NotificationManager#setInterruptionFilter(int)}.</p>
	 * <br>
	 * <p>Changes:</p>
	 * <p>- Removed the need for the Context instance by replacing a method call by a constant</p>
	 *
	 * @param interruptionFilter .
	 */
	public static void setInterruptionFilter(@NotificationManager.InterruptionFilter final int interruptionFilter) {
		final INotificationManager service = getService();
		try {
			service.setInterruptionFilter(EContext.getOpPackageName(), interruptionFilter);
		} catch (final RemoteException e) {
			throw e.rethrowFromSystemServer();
		}
	}

	/**
	 * <p>See {@link NotificationManager#isNotificationPolicyAccessGranted()}.</p>
	 * <br>
	 * <p>Changes:</p>
	 * <p>- Removed the need for the Context instance by replacing a method call by a constant</p>
	 *
	 * @return .
	 */
	public static boolean isNotificationPolicyAccessGranted() {
		final INotificationManager service = getService();
		try {
			return service.isNotificationPolicyAccessGranted(EContext.getOpPackageName());
		} catch (final RemoteException e) {
			throw e.rethrowFromSystemServer();
		}
	}
}
