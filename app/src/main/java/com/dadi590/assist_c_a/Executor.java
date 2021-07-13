package com.dadi590.assist_c_a;

import androidx.annotation.NonNull;

public class Executor {

	// Note: this is not being used at the time.



	// todo And if there's no SIM card on the phone...? What happens? Error? There are methods to check that:
	//  TelephonyManager.hasIccCard(), for example. See if that's enough.



	// None of these variables can be local. They must memorize the last value, so they must always remain in memory.
	// Also, because of that, this class must also remain in memory, as it's done in the Main Service.
	private boolean something_done;
	private boolean something_said;

	/**
	 * Sets the {@code something_said} variable to true.
	 */
	public final void something_doneTrue() {
		something_said = true;
	}

	/**
	 * <p>In case it's needed to send the task from somewhere else other than the Main Service.</p>
	 * <br>
	 * <p>Note: this is supposed to be called only by Main Service's process.</p>
	 *
	 * @param sentence_str <u>[String]</u> --> same as {@link #task(String, boolean, boolean)}
	 * @param partial_results <u>[boolean]</u> --> same as {@link #task(String, boolean, boolean)}
	 * @param only_returning <u>[boolean]</u> --> same as {@link #task(String, boolean, boolean)}
	 *
	 * @return <u>[int]</u> --> same as {@link #task(String, boolean, boolean)}
	 */
	public final int sendTask(@NonNull final String sentence_str,
							  final boolean partial_results, final boolean only_returning) {
		if (!only_returning) {
			//UtilsGeneral.alterar_volume_fala(context, UtilsGeneral.VOLUME_MEDIO);
		}

		return task(sentence_str, partial_results, only_returning);

        /*if (!only_returning) {
            UtilsGeneral.alterar_volume_fala(context, UtilsGeneral.VOLUME_ANTERIOR);
        }*/
	}

	public static final int NOTHING_EXECUTED = 0;
	public static final int SOMETHING_EXECUTED = 1;
	public static final int ERR_PROC_INIT_STR = -1;
	public static final int UNAVBL_EXTERNAL_FUNCTIONS = -2;
	/**
	 * <p>This function checks and executes all tasks included in a string.</p>
	 * <br>
	 * <p>Note: the {@code only_returning} parameter is currently implemented only for partial results.</p>
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link #NOTHING_EXECUTED} --> for the returning value: if no task was detected</p>
	 * <p>- {@link #SOMETHING_EXECUTED} --> for the returning value: if some task was detected</p>
	 * <p>- {@link #ERR_PROC_INIT_STR} --> for the returning value: if there was an internal External Functions error</p>
	 * <p>- {@link #UNAVBL_EXTERNAL_FUNCTIONS} --> for the returning value: if the External Functions are not
	 * available</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @param context Main Service's context
	 * @param sentence_str the string to be analized for commands
	 * @param partial_results true if the function is being called by partial recognition results (onPartialResults),
	 *                           false otherwise (onResults; other).
	 * @param only_returning true if one wants nothing but the return value, false to also execute all the tasks in the
	 *                          string.
	 *
	 * @return one of the constants
	 */
	private int task(final String sentence_str,
					 final boolean partial_results, final boolean only_returning) {
		return 0;
	}
}
