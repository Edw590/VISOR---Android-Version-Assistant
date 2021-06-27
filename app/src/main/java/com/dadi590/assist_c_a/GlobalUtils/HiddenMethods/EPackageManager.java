package com.dadi590.assist_c_a.GlobalUtils.HiddenMethods;

import android.annotation.NonNull;
import android.app.AppGlobals;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Process;
import android.os.RemoteException;
import android.os.UserHandle;

/**
 * <p>Original class: {@link PackageManager}.</p>
 */
public final class EPackageManager {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private EPackageManager() {
	}

	/**.
	 * @return an instance of {@link IPackageManager}
	 */
	@androidx.annotation.NonNull
	private static IPackageManager getIPackageManager() {
		return AppGlobals.getPackageManager();
	}

	/**
	 * <p>See {@link PackageManager#getPackageInfo(String, int)} ()}.</p>
	 * <br>
	 * <p>Changes:</p>
	 * <p>- Method implemented with the same technique as in {@link EAudioManager}, including an implementation to
	 * throw the exception on the signature (hopefully the correct implementation).
	 * <p>- Method made static as it doesn't use Context</p>
	 *
	 * @param packageName .
	 * @param flags .
	 *
	 * @return .
	 *
	 * @throws PackageManager.NameNotFoundException .
	 */
	@androidx.annotation.NonNull
	public static PackageInfo getPackageInfo(@NonNull final String packageName,
											 @PackageManager.PackageInfoFlags final int flags)
			throws PackageManager.NameNotFoundException {
		final IPackageManager iPackageManager = getIPackageManager();
		try {
			final PackageInfo ret_value = iPackageManager.getPackageInfo(packageName, flags,
					UserHandle.getUserId(Process.myUid())); // Hopefully this is the user ID the method wants.
			if (ret_value == null) {
				// I tested with a random string and the result was null. So I'm guessing this is the correct
				// implementation for when the package doesn't exist (check if the result is null or not and throw the
				// exception).
				throw new PackageManager.NameNotFoundException(packageName);
			}
			return iPackageManager.getPackageInfo(packageName, flags, UserHandle.getUserId(Process.myUid()));
		} catch (final RemoteException e) {
			throw e.rethrowFromSystemServer();
		}
	}
}
