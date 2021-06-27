package com.dadi590.assist_c_a.BroadcastRecvs.DeviceAdmin;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.os.Build;

/**
 * <p>Device Administration related utilities.</p>
 */
final class UtilsDeviceAdmin {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsDeviceAdmin() {
	}

	/**
	 * <p>Start the protected lock screen.</p>
	 * <br>
	 * <p>This includes first immediately locking the device to ensure nothing else can be done without inserting a
	 * password.</p>
	 *
	 * @param context {@link DeviceAdminRecv}'s context
	 */
	static void startProtectedLockScr(final Context context) {
		final DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(
				Context.DEVICE_POLICY_SERVICE);
		devicePolicyManager.lockNow();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			if (devicePolicyManager.getStorageEncryptionStatus() != DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE_PER_USER) {
				try {
					devicePolicyManager.lockNow(DevicePolicyManager.FLAG_EVICT_CREDENTIAL_ENCRYPTION_KEY);
				} catch (final SecurityException ignored) {
				}
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

		// todo Missing starting the protected lock screen

		// todo Also try this without any lock security like PIN or whatever and see if all still works perfectly
	}
}
