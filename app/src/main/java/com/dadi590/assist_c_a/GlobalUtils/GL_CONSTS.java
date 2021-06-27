package com.dadi590.assist_c_a.GlobalUtils;

import org.jetbrains.annotations.NonNls;

import java.io.File;

/**
 * <p>Global constants across the project.</p>
 */
public final class GL_CONSTS {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private GL_CONSTS() {
	}

	@NonNls
	public static final String ASSISTANT_NAME = "LEGION";
	public static final String ASSIST_C_A_RECV_PERM = "com.dadi590.assist_c_a.permission.INT_RECEIVERS";

	public static final int NOTIF_ID_MAIN_SRV_FOREGROUND = 1;
	public static final String CH_ID_MAIN_SRV_FOREGROUND = "MainSrv:FOREGROUND";

	public static final String MEDIA_FOLDER = ASSISTANT_NAME + File.separator;
}
