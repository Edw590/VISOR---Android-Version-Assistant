package com.dadi590.assist_c_a;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dadi590.assist_c_a.BroadcastRecvs.MainBroadcastRecv;
import com.dadi590.assist_c_a.BroadcastRecvs.MainRegBroadcastRecv;
import com.dadi590.assist_c_a.GlobalUtils.GL_CONSTS;
import com.dadi590.assist_c_a.GlobalUtils.ObjectClasses;
import com.dadi590.assist_c_a.GlobalUtils.UtilsPermissions;
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
		UtilsPermissions.wrapperRequestPerms(null, false);
		UtilsServices.startService(MainSrv.class, true);

		return (AudioManager) main_app_context.getSystemService(AUDIO_SERVICE);
	}*/

	// Global class instances getters //

	// Do NOT put @NonNull on the returning values. If the service has not started OR CRASHED, everything will return
	// null!!!
	// EDIT: actually, since I started using UtilsGeneral.getContext(), all seems to work just fine. With getSpeech(),
	// all works fine as long as I don't return null. If I return the instance, the service starts normally. I'll just
	// leave it on NonNull. This probably also works because I instantiate everything on the declaration now (because
	// of UtilsGeneral.getContext() being always available).

	///**.
	// * @return the main process' Application Context
	// */
	//@Nullable
	//public static Context getMainAppContext() {
	//	UtilsPermissions.wrapperRequestPerms(null, false);
	//	UtilsServices.startService(MainSrv.class, true);
	//
	//	return main_app_context;
	//}
	/**.
	 * @return the global {@link Speech2} instance
	 */
	@NonNull
	public static Speech2 getSpeech2() {
		UtilsPermissions.wrapperRequestPerms(null, false);
		UtilsServices.startService(MainSrv.class);

		return speech2;
	}
	/**.
	 * @return the global {@link AudioRecorder} instance
	 */
	@NonNull
	public static AudioRecorder getAudioRecorder() {
		UtilsPermissions.wrapperRequestPerms(null, false);
		UtilsServices.startService(MainSrv.class);

		return audioRecorder;
	}
	/**.
	 * @return the global {@link MainBroadcastRecv} instance
	 */
	@NonNull
	public static MainRegBroadcastRecv getMainRegBroadcastRecv() {
		UtilsPermissions.wrapperRequestPerms(null, false);
		UtilsServices.startService(MainSrv.class);

		return mainRegBroadcastRecv;
	}
	/**.
	 * @return the global {@link PhoneCallsProcessor} instance
	 */
	@NonNull
	public static PhoneCallsProcessor getPhoneCallsProcessor() {
		UtilsPermissions.wrapperRequestPerms(null, false);
		UtilsServices.startService(MainSrv.class);

		return phoneCallsProcessor;
	}
	/**.
	 * @return the global {@link BatteryProcessor} instance
	 */
	@NonNull
	public static BatteryProcessor getBatteryProcessor() {
		UtilsPermissions.wrapperRequestPerms(null, false);
		UtilsServices.startService(MainSrv.class);

		return batteryProcessor;
	}

	//////////////////////////////////////


	@Override
	public final void onCreate() {
		super.onCreate();

		// Do this only once, when the service is created and while it's not destroyed

		final ObjectClasses.NotificationInfo notificationInfo = new ObjectClasses.NotificationInfo(
				GL_CONSTS.CH_ID_MAIN_SRV_FOREGROUND,
				"Main notification",
				"",
				UtilsServices.TYPE_FOREGROUND,
				GL_CONSTS.ASSISTANT_NAME + " Systems running",
				"",
				null
		);
		startForeground(GL_CONSTS.NOTIF_ID_MAIN_SRV_FOREGROUND, UtilsServices.getNotification(notificationInfo));

		// Clear the app cache as soon as it starts not to take unnecessary space
		UtilsApp.deleteAppCache();

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

		// Do NOT put ANYTHING here!!!
		// MANY places starting this service don't check if it's already started or not, so this method will be called
		// many times randomly. Put everything on onCreate() which is called only if the service was not running and
		// was just started.

		return START_STICKY;
	}

	@Override
	@Nullable
	public final IBinder onBind(@Nullable final Intent intent) {
		return null;
	}
}
