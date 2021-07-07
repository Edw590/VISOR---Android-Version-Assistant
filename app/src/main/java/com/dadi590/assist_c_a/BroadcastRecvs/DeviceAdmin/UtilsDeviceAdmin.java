package com.dadi590.assist_c_a.BroadcastRecvs.DeviceAdmin;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.dadi590.assist_c_a.GlobalUtils.UtilsGeneral;
import com.dadi590.assist_c_a.Modules.ProtectedLockScr.ProtectedLockScr;
import com.dadi590.assist_c_a.Modules.ProtectedLockScr.UtilsProtectedLockScr;

/**
 * <p>Device Administration related utilities.</p>
 */
public final class UtilsDeviceAdmin {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsDeviceAdmin() {
	}

	/**
	 * <p>Locks the device immediately.</p>
	 */
	public static void lockDevice() {
		final DevicePolicyManager devicePolicyManager = (DevicePolicyManager) UtilsGeneral.getContext()
				.getSystemService(Context.DEVICE_POLICY_SERVICE);
		try {
			devicePolicyManager.lockNow();
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				if (devicePolicyManager.getStorageEncryptionStatus() != DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE_PER_USER) {
					devicePolicyManager.lockNow(DevicePolicyManager.FLAG_EVICT_CREDENTIAL_ENCRYPTION_KEY);
					// From https://developer.android.com/reference/android/app/admin/DevicePolicyManager#lockNow(int):
					// "NOTE: In order to lock the parent profile and evict the encryption key of the managed profile, lockNow()
					// must be called twice: First, lockNow() should be called on the DevicePolicyManager instance returned by
					// getParentProfileInstance(android.content.ComponentName), then lockNow(int) should be called on the
					// DevicePolicyManager instance associated with the managed profile, with the
					// FLAG_EVICT_CREDENTIAL_ENCRYPTION_KEY flag. Calling the method twice in this order ensures that all users
					// are locked and does not stop the device admin on the managed profile from issuing a second call to lock
					// its own profile."
				}
			}
		} catch (final SecurityException ignored) {
			// Could not lock the device, for example because the app is not a Device Administrator or (in case the app
			// is running in Android 11 or above) the app does not hold the LOCK_DEVICE permission.
		}
	}
}
