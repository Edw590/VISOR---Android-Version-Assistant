package com.dadi590.assist_c_a;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.dadi590.assist_c_a.BroadcastRecvs.MainBroadcastRecv;
import com.dadi590.assist_c_a.BroadcastRecvs.MainRegBroadcastRecv;
import com.dadi590.assist_c_a.GlobalUtils.GL_CONSTS;
import com.dadi590.assist_c_a.GlobalUtils.UtilsGeneral;
import com.dadi590.assist_c_a.Modules.AudioRecorder.AudioRecorder;
import com.dadi590.assist_c_a.Modules.BatteryProcessor.BatteryProcessor;
import com.dadi590.assist_c_a.Modules.Telephony.PhoneCallsProcessor.PhoneCallsProcessor;
import com.dadi590.assist_c_a.Modules.Speech.Speech2;
import com.dadi590.assist_c_a.GlobalUtils.UtilsApp;
import com.dadi590.assist_c_a.GlobalUtils.UtilsServices;

/**
 * The main {@link Service} of the application - MainService.
 */
public class MainSrv extends Service {

	//////////////////////////////////////
	// Class variables

	// Private variables //

	// Not bad, since it's the application context (global for the entire process), not a local one
	//private static Context main_app_context = null; - disabled while using UtilsGeneral.getMainAppContext()

	// Modules instances
	private static final Speech2 speech2 = new Speech2();
	private static final AudioRecorder audioRecorder = new AudioRecorder();
	private static final PhoneCallsProcessor phoneCallsProcessor = new PhoneCallsProcessor();
	private static final MainRegBroadcastRecv mainRegBroadcastRecv = new MainRegBroadcastRecv();
	private static final BatteryProcessor batteryProcessor = new BatteryProcessor();

	//////////////////////////////////////

	//////////////////////////////////////
	// Getters and setters

	///**.
	// * @return the {@link AudioManager}
	// */
	/*@Nullable
	public static AudioManager getAudioManager() {
		return (AudioManager) main_app_context.getSystemService(AUDIO_SERVICE);
	}*/

	// Global class instances getters //

	// Do NOT put @NonNull on the returning values. If the service has not started OR CRASHED, everything will return
	// null!!!

	///**.
	// * @return the main process' Application Context
	// */
	//@Nullable
	//public static Context getMainAppContext() {
	//	return main_app_context;
	//}
	/**.
	 * @return the global {@link Speech2} instance
	 */
	@Nullable
	public static Speech2 getSpeech2() {
		return speech2;
	}
	/**.
	 * @return the global {@link AudioRecorder} instance
	 */
	@Nullable
	public static AudioRecorder getAudioRecorder() {
		return audioRecorder;
	}
	/**.
	 * @return the global {@link MainBroadcastRecv} instance
	 */
	@Nullable
	public static MainRegBroadcastRecv getMainRegBroadcastRecv() {
		return mainRegBroadcastRecv;
	}
	/**.
	 * @return the global {@link PhoneCallsProcessor} instance
	 */
	@Nullable
	public static PhoneCallsProcessor getPhoneCallsProcessor() {
		return phoneCallsProcessor;
	}
	/**.
	 * @return the global {@link BatteryProcessor} instance
	 */
	@Nullable
	public static BatteryProcessor getBatteryProcessor() {
		return batteryProcessor;
	}

	//////////////////////////////////////


	@Override
	public final void onCreate() {
		super.onCreate();

		// Do this only once, when the service is created and while it's not destroyed

		startForeground(GL_CONSTS.NOTIF_ID_MAIN_SRV_FOREGROUND, UtilsServices.getNotification(
				UtilsGeneral.getMainAppContext(), UtilsServices.TYPE_FOREGROUND));

		// Clear the app cache as soon as it starts not to take unnecessary space
		UtilsApp.deleteAppCache(UtilsGeneral.getMainAppContext());

		// Before anything else, start the speech module since the assistant must be able to speak.
		// Don't forget inside the speech module there's a function that executes all important things right after
		// the TTS is ready - the second reason this must be in the beginning.
		//speech2 = new Speech2();

		// Put the MainService process' context available statically
		//main_app_context = getApplicationContext();

		// Initialization of all the assistant's modules, after the service was successfully started (means that
		// main_app_context is ready to be used wherever it's needed) along with the speech module.
		//audioRecorder = new AudioRecorder();
	}

	@Override
	public final int onStartCommand(@Nullable final Intent intent, final int flags, final int startId) {
		// Do this below every time the service is started/resumed/whatever

		return START_STICKY;
	}

	@Override
	@Nullable
	public final IBinder onBind(@Nullable final Intent intent) {
		return null;
	}
}
