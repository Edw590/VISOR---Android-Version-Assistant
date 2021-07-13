package com.dadi590.assist_c_a.BroadcastRecvs.DeviceAdmin;

import com.dadi590.assist_c_a.GlobalUtils.GL_CONSTS;

/**
 * <p>Constants related to the Device Administrator mode.</p>
 */
final class CONSTS {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private CONSTS() {
	}

	static final String SPEAK_ENABLED = "Administrator mode successfully enabled to " +
			GL_CONSTS.ASSISTANT_NAME_WO_DOTS + ".";

	// Shorter version of RET_STR_DISABLE_REQUESTED since devices may have an option to disable and force stop the app
	// all in one button, and then have a confirmation box to uninstall the app. Clicking on the option to Uninstall
	// will make the phone automatically disable the device administration mode and uninstall the app all in once.
	// Means make the speech as small as possible but still decently warning the user.
	static final String SPEAK_DISABLED = "WARNING, ADMIN MODE REVOKED FROM " + GL_CONSTS.ASSISTANT_NAME_WO_DOTS + "!!!";
	// With "WARNING, ADMIN MODE REVOKED FROM LEGION!!!", the last word said on BV9500 is "revoked". Not that bad.
	// todo In case the app gets published on some store, put a warning saying what the app will say if the Admin Mode
	//  gets revoked and explain why such an abbreviated speech.

	static final String RET_STR_DISABLE_REQUESTED =
			"SECURITY WARNING!!!" +
			"\n" +
			"\n" +
			"THE ADMINISTRATOR MODE WILL BE REVOKED FROM " + GL_CONSTS.ASSISTANT_NAME + "!!!"
			;
	static final String SPEAK_DISABLE_REQUESTED = "SECURITY WARNING!!! THE ADMINISTRATOR MODE WILL BE REVOKED FROM " +
			GL_CONSTS.ASSISTANT_NAME_WO_DOTS + "!!!";
}
