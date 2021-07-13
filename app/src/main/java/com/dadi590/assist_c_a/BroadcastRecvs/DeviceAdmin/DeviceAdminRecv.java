package com.dadi590.assist_c_a.BroadcastRecvs.DeviceAdmin;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.dadi590.assist_c_a.GlobalUtils.UtilsPermissions;
import com.dadi590.assist_c_a.GlobalUtils.UtilsServices;
import com.dadi590.assist_c_a.MainSrv;
import com.dadi590.assist_c_a.Modules.ProtectedLockScr.UtilsProtectedLockScr;
import com.dadi590.assist_c_a.Modules.Speech.Speech2;

/**
 * <p>The Device Administration Receiver, which handles all received administration actions.</p>
 */
public class DeviceAdminRecv extends DeviceAdminReceiver {

	static final int PRIORITY_ADMIN_ENABLED = Speech2.PRIORITY_MEDIUM;

	@Override
	public final void onEnabled(@android.annotation.NonNull final Context context,
								@android.annotation.NonNull final Intent intent) {
		super.onEnabled(context, intent);

		UtilsPermissions.wrapperRequestPerms(null, false);
		UtilsServices.startService(MainSrv.class);

		// The assistant may not be able to speak speak (service not working or whatever), but he'll try anyways to warn
		// the user of any changes (try/catch).
		try {
			MainSrv.getSpeech2().speak(CONSTS.SPEAK_ENABLED, Speech2.NO_ADDITIONAL_COMMANDS, PRIORITY_ADMIN_ENABLED, null);
		} catch (final Throwable ignored) {
		}
	}

	@NonNull
	@Override
	public final CharSequence onDisableRequested(@android.annotation.NonNull final Context context,
												 @android.annotation.NonNull final Intent intent) {
		super.onDisableRequested(context, intent);

		UtilsPermissions.wrapperRequestPerms(null, false);
		UtilsServices.startService(MainSrv.class);

		UtilsProtectedLockScr.lockAndShowPLS(UtilsProtectedLockScr.getPLSIntent());

		// The assistant may not be able to speak speak (service not working or whatever), but he'll try anyways to warn
		// the user of any changes (try/catch).
		try {
			MainSrv.getSpeech2().speak(CONSTS.SPEAK_DISABLE_REQUESTED, Speech2.NO_ADDITIONAL_COMMANDS,
					Speech2.PRIORITY_HIGH, null);
			// Why PRIORITY_CRITICAL? Because onDisabled() also has it, so they have the same priority. And onDisabled()
			// skips this speech in case it's being spoken, so it's all good.
			// EDIT: it's on HIGH now. Why CRITICAL... Critical thing is when it's disabled. If the user is just
			// checking something, they don't need to have the phone screaming. If CRITICAL is to be set again, don't
			// forget of skipping this speech because onDisabled() has top priority since the app might shut down.
		} catch (final Throwable ignored) {
		}

		return CONSTS.RET_STR_DISABLE_REQUESTED;
	}

	@Override
	public final void onDisabled(@android.annotation.NonNull final Context context,
								 @android.annotation.NonNull final Intent intent) {
		//super.onDisabled(context, intent); - the less things here the better (Why? Refer to CONSTS.SPEAK_DISABLED.)

		UtilsPermissions.wrapperRequestPerms(null, false);
		UtilsServices.startService(MainSrv.class);

		UtilsProtectedLockScr.lockAndShowPLS(UtilsProtectedLockScr.getPLSIntent());

		// The assistant may not be able to speak speak (service not working or whatever), but he'll try anyways to warn
		// the user of any changes (try/catch).
		try {
			MainSrv.getSpeech2().speak(CONSTS.SPEAK_DISABLED, Speech2.NO_ADDITIONAL_COMMANDS, Speech2.PRIORITY_CRITICAL,
					null);
			// Why PRIORITY_CRITICAL? Refer to CONSTS.SPEAK_DISABLED.
			// todo HE'LL SPEAK AND LEAVE THE PHONE WITH THE DO NOT DISTURB AND THE MAX VOLUME IF IT'S STOPPED IN
			//  THE MIDDLE!!!!!! How do you fix that.....? You don't, right? xD Cool. No idea.
			// GET THE SECONDARY APP RESETTING IT!!! (The one which will restart this one...)

			// This below is in case the administrator mode was enabled, but was disabled right after. The assistant
			// would still say the administrator mode is enabled after saying it was disable --> wtf. This fixes that.
			final String admin_enabled_speech_id = MainSrv.getSpeech2().getSpeechIdBySpeech(CONSTS.SPEAK_ENABLED,
					PRIORITY_ADMIN_ENABLED, true);
			if (admin_enabled_speech_id != null) {
				MainSrv.getSpeech2().removeSpeechById(admin_enabled_speech_id);
			}

			// todo This is not removing the speech, I think.....
		} catch (final Throwable ignored) {
		}
	}
}
