package com.dadi590.assist_c_a.BroadcastRecvs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.Nullable;

import com.dadi590.assist_c_a.GlobalUtils.UtilsGeneral;

/**
 * The {@link BroadcastReceiver} to be used for all main broadcasts that don't need registering.
 */
public class MainBroadcastRecv extends BroadcastReceiver {

	@Override
	public final void onReceive(@Nullable final Context context, @Nullable final Intent intent) {
		UtilsIntentWhatToDo.intentWhatToDo(UtilsGeneral.getMainAppContext(), intent);
	}
}
