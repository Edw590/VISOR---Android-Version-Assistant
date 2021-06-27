package com.dadi590.assist_c_a.BroadcastRecvs;

import android.provider.Telephony;

/**
 * <p>Constants related to the broadcast receivers.</p>
 */
public final class CONSTS {

	// These 2 below appeared in a StackOverflow answer. Maybe it's the same explanation as the POWERON one. Keep it.
	static final String ACTION_HTC_QCK_POFF = "com.htc.intent.action.QUICKBOOT_POWEROFF";
	static final String ACTION_ANDR_QCK_POFF = "android.intent.action.QUICKBOOT_POWEROFF";

	// Below Android KitKat, there's no constant, but the string appears to exist, even without constant,
	// so it should still work below KitKat. I'll leave it as a string, or the switch won't like 2 equal values.
	// I'll leave the new one here uncommented and unused, but only for Android Studio to throw a warning in case it
	// stops existing.
	private static final String NO_USE = Telephony.Sms.Intents.SMS_RECEIVED_ACTION; // API 19 (KitKat) and above
	static final String ACTION_SMS_RECEIVED_ALL_API = "android.provider.Telephony.SMS_RECEIVED";

	// TelephonyManager.ACTION_PRECISE_CALL_STATE_CHANGED - Android Studio doesn't find it, even though I see it there
	// (???)
	static final String ACTION_PRECISE_CALL_STATE_CHANGED = "android.intent.action.PRECISE_CALL_STATE";

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private CONSTS() {
	}
}
