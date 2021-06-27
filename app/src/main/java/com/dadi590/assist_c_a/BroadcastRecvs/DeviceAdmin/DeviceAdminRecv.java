package com.dadi590.assist_c_a.BroadcastRecvs.DeviceAdmin;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;

import android.annotation.NonNull;

import com.dadi590.assist_c_a.MainSrv;
import com.dadi590.assist_c_a.Modules.Speech.Speech2;

/**
 * <p>The Device Administration Receiver, which handles all received administration actions.</p>
 */
public class DeviceAdminRecv extends DeviceAdminReceiver {

	static final int PRIORITY_ADMIN_ENABLED = Speech2.PRIORITY_MEDIUM;

	@Override
	public final void onEnabled(@NonNull final Context context, @NonNull final Intent intent) {
		super.onEnabled(context, intent);
		//final ComponentName componentName = new ComponentName(context, DeviceAdminRecv.class);
		//final DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(DEVICE_POLICY_SERVICE);
		//devicePolicyManager.setStatusBarDisabled(componentName, true);
		// todo Not working ^^^^^

		// The assistant may not be able to speak speak, but he'll try anyways to warn the user of any changes (try/catch).
		try {
			if (MainSrv.getSpeech2() != null) {
				MainSrv.getSpeech2().speak(CONSTS.SPEAK_ENABLED, Speech2.NO_ADDITIONAL_COMMANDS, PRIORITY_ADMIN_ENABLED,
						null);
			}
		} catch (final Throwable ignored) {
		}
	}

	@NonNull
	@Override
	public final CharSequence onDisableRequested(@NonNull final Context context, @NonNull final Intent intent) {
		UtilsDeviceAdmin.startProtectedLockScr(context);

		// The assistant may not be able to speak speak, but he'll try anyways to warn the user of any changes (try/catch).

		try {
			if (MainSrv.getSpeech2() != null) {
				MainSrv.getSpeech2().speak(CONSTS.RET_STR_DISABLE_REQUESTED, Speech2.NO_ADDITIONAL_COMMANDS,
						Speech2.PRIORITY_HIGH, null);
			}
			// Why PRIORITY_CRITICAL? Because onDisabled() also has it, so they have the same priority. And onDisabled()
			// skips this speech in case it's being spoken, so it's all good.
			// EDIT: it's on HIGH now. Why CRITICAL... Critical thing is when it's disabled. If the user is just
			// checking something, they don't need to have the phone screaming. If CRITICAL is to be set again, don't
			// forget of skipping this speech because onDisabled() has top priority since the app will shut down.
		} catch (final Throwable ignored) {
		}

		return CONSTS.RET_STR_DISABLE_REQUESTED;
	}

	@Override
	public final void onDisabled(@NonNull final Context context, @NonNull final Intent intent) {
		super.onDisabled(context, intent);

		UtilsDeviceAdmin.startProtectedLockScr(context);

		// The assistant may not be able to speak speak, but he'll try anyways to warn the user of any changes (try/catch).

		try {
			if (MainSrv.getSpeech2() != null) {
				MainSrv.getSpeech2().speak(CONSTS.SPEAK_DISABLED, Speech2.NO_ADDITIONAL_COMMANDS, Speech2.PRIORITY_CRITICAL,
						null);
				// Why PRIORITY_CRITICAL? Refer to CONSTS.SPEAK_DISABLED.

				// This below is in case the administrator mode was enabled, but was disabled right after. The assistant
				// would still say the administrator mode is enabled after saying it was disable --> wtf. This fixes that.
				final String admin_enabled_speech_id = MainSrv.getSpeech2().getSpeechIdBySpeech(CONSTS.SPEAK_ENABLED,
						PRIORITY_ADMIN_ENABLED, true);
				if (admin_enabled_speech_id != null) {
					MainSrv.getSpeech2().removeSpeechById(admin_enabled_speech_id);
				}
			}
		} catch (final Throwable ignored) {
		}
	}
}
