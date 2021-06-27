package com.dadi590.assist_c_a.BroadcastRecvs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.telephony.TelephonyManager;

import com.dadi590.assist_c_a.GlobalUtils.UtilsGeneral;

/**
 * The class to be used for all main broadcasts that need registering.
 */
public class MainRegBroadcastRecv {

	/**
	 * <p>Main class constructor.</p>
	 */
	public MainRegBroadcastRecv() {
	}

	/**
	 * <p>Registers all the receivers in the class.</p>
	 *
	 */
	public final void registerReceivers() {
		// Note: don't put as a constructor, since the receivers must be registered only after TTS is ready.

		final IntentFilter intentFilter = new IntentFilter();

		// Power
		intentFilter.addAction(Intent.ACTION_SHUTDOWN);
		intentFilter.addAction(Intent.ACTION_REBOOT);
		intentFilter.addAction(CONSTS.ACTION_HTC_QCK_POFF);
		intentFilter.addAction(CONSTS.ACTION_ANDR_QCK_POFF);

		// SMS and phone calls
		intentFilter.addAction(CONSTS.ACTION_SMS_RECEIVED_ALL_API);
		intentFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
		intentFilter.addAction(CONSTS.ACTION_PRECISE_CALL_STATE_CHANGED);

		// Battery
		intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
		intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
		intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);

		// Lock screen - for testing
		//intentFilter.addAction(Intent.ACTION_SCREEN_OFF);

		// Volume changes
		intentFilter.addAction(AudioManager.VOLUME_CHANGED_ACTION);

		UtilsGeneral.getMainAppContext().registerReceiver(mainRegBroadcastReceiver, intentFilter);
	}

	private final BroadcastReceiver mainRegBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			UtilsIntentWhatToDo.intentWhatToDo(UtilsGeneral.getMainAppContext(), intent);
		}
	};
}
