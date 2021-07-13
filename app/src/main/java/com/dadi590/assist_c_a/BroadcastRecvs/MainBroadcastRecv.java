package com.dadi590.assist_c_a.BroadcastRecvs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dadi590.assist_c_a.GlobalUtils.UtilsPermissions;
import com.dadi590.assist_c_a.GlobalUtils.UtilsServices;
import com.dadi590.assist_c_a.MainSrv;

/**
 * <p>The {@link BroadcastReceiver} to be used to start the Main Service with ANY broadcast detection --> do NOT use to
 * decide what to do with the broadcasts!!! For that, use {@link MainRegBroadcastRecv}.</p>
 */
public class MainBroadcastRecv extends BroadcastReceiver {

	@Override
	public final void onReceive(@Nullable final Context context, @Nullable final Intent intent) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				UtilsPermissions.wrapperRequestPerms(null, false);
				UtilsServices.startService(MainSrv.class);
			}
		}).start();

		/*
		UtilsIntentWhatToDo.intentWhatToDo(intent);
		Do NOT enable this!!! I'm ignoring safety measures (see the Manifest where I'm ignoring possible spoofing of SMS)
		with this received since ANY action received is supposed to get the app to start the main service and NOTHING ELSE.
		*/
	}
}
