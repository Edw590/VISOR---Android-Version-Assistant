package com.dadi590.assist_c_a.Modules.Telephony.PhoneCallsProcessor;

import android.content.Context;

/**
 * <p>A class to be used as an "multi-type array" for the returning value of
 * {@link PhoneCallsProcessor#getCallPhase(Context, int, String)}.</p>
 */
class NumAndPhase {

	final String phone_number;
	final int call_phase;

	/**
	 * <p>Main class constructor.</p>
	 *
	 * @param phoneNumber the phone number directly from the intent extra (null or not --> directly)
	 * @param callPhase one of the {@code CALL_PHASE_} constants in {@link PhoneCallsProcessor}
	 */
	NumAndPhase(final String phoneNumber, final int callPhase) {
		phone_number = phoneNumber;
		call_phase = callPhase;
	}
}
