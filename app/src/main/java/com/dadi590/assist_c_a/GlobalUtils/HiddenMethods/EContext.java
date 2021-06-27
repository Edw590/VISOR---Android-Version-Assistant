package com.dadi590.assist_c_a.GlobalUtils.HiddenMethods;

import android.content.Context;

import androidx.annotation.NonNull;

import com.dadi590.assist_c_a.BuildConfig;

/**
 * <p>Original class: {@link Context}.</p>
 */
final class EContext {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private EContext() {
	}

	/**
	 * <p>See {@link Context#getOpPackageName()}.</p>
	 * <br>
	 * <p>Changes:</p>
	 * <p>- Method made static as it doesn't use Context</p>
	 *
	 * @return .
	 */
	@NonNull
	static String getOpPackageName() {
		return BuildConfig.APPLICATION_ID;
		// When I printed the result of the original function, it returned "com.dadi590.assist_c_a", which is the name
		// of the main app package. When I inserted that as a string constant, Android Studio said it was a duplicate
		// of BuildConfig.APPLICATION_ID. So I'm using it, hoping in case I change the ID, BuildConfig.APPLICATION_ID
		// will be the one the function would return.
	}
}
