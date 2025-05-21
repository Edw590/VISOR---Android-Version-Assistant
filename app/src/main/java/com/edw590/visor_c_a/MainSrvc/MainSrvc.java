/*
 * Copyright 2021-2024 Edw590
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.edw590.visor_c_a.MainSrvc;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.edw590.visor_c_a.ActivitiesFragments.Activities.ActMain;
import com.edw590.visor_c_a.GlobalUtils.GL_CONSTS;
import com.edw590.visor_c_a.GlobalUtils.ObjectClasses;
import com.edw590.visor_c_a.GlobalUtils.UtilsApp;
import com.edw590.visor_c_a.GlobalUtils.UtilsContext;
import com.edw590.visor_c_a.GlobalUtils.UtilsNotifications;
import com.edw590.visor_c_a.GlobalUtils.UtilsPermsAuths;
import com.edw590.visor_c_a.GlobalUtils.UtilsProcesses;
import com.edw590.visor_c_a.GlobalUtils.UtilsRoot;
import com.edw590.visor_c_a.Modules.ModulesManager.ModulesManager;
import com.edw590.visor_c_a.Modules.Speech.CONSTS_BC_Speech;
import com.edw590.visor_c_a.Modules.Speech.Speech2;
import com.edw590.visor_c_a.Modules.Speech.UtilsSpeech2BC;
import com.edw590.visor_c_a.ModulesList;

import GPTComm.GPTComm;

/**
 * The Main {@link Service} of the application, running in foreground.
 */
public final class MainSrvc extends Service {

	@Override
	public void onCreate() {
		super.onCreate();

		// Do this only once, when the service is created and while it's not destroyed

		final NotificationManager notificationManager = (NotificationManager) UtilsContext.
				getSystemService(Context.NOTIFICATION_SERVICE);
		if (notificationManager == null) {
			throw new RuntimeException("No notification service on the device - the app will not proceed");
		}

		/*if (!UtilsPermsAuths.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
			// The app can only work if the storage permission is granted.
			final ObjectClasses.NotificationInfo notificationInfo = new ObjectClasses.NotificationInfo(
					GL_CONSTS.CH_ID_MAIN_SRV_NO_NOTIFS,
					"Storage permission warning",
					"",
					NotificationCompat.PRIORITY_MAX,
					"Warning - external storage not accessible!",
					"This app only works if the storage permission is granted. Grant it and click on Terminate app for " +
							"the app to restart.",
					null
			);
			notificationManager.notify(0, UtilsNotifications.getNotification(notificationInfo).build());

			return;
		}*/

		final Intent intent1 = new Intent(this, ActMain.class);
		intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		final int flag_immutable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0;
		final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent1, flag_immutable |
				PendingIntent.FLAG_CANCEL_CURRENT);
		final ObjectClasses.NotificationInfo notificationInfo = new ObjectClasses.NotificationInfo(
				GL_CONSTS.CH_ID_MAIN_SRV_FOREGROUND,
				"Main notification",
				"",
				NotificationCompat.PRIORITY_MIN,
				"V.I.S.O.R. Systems running",
				"",
				pendingIntent
		);
		startForeground(GL_CONSTS.NOTIF_ID_MAIN_SRV_FOREGROUND, UtilsNotifications.getNotification(notificationInfo).
				setOngoing(true).
				build());

		// Register the receiver before the speech module is started
		try {
			registerReceiver(broadcastReceiver, new IntentFilter(CONSTS_BC_Speech.ACTION_READY));
		} catch (final IllegalArgumentException ignored) {
		}

		// The speech module must be started before everything else for now - only way of him to communicate with the
		// user. Later, notifications will be added. Emails would be a good idea too in more extreme notifications.
		// After the speech module is ready, it will send a broadcast for the receiver below to activate the rest of
		// the assistant.
		// EDIT: the Speech2 module has now notifications integrated into it, so it's still the thing to start before
		// everything else.
		ModulesList.startElement(ModulesList.getElementIndex(Speech2.class));
	}

	final Thread infinity_thread = new Thread(() -> {
		final int mods_manager_index = ModulesList.getElementIndex(ModulesManager.class);

		while (true) {
			// Force the permissions every some seconds.
			UtilsPermsAuths.checkRequestPerms(null, true);
			UtilsPermsAuths.checkRequestAuths(UtilsPermsAuths.ALSO_FORCE);

			// Keep checking if the Modules Manager is working and in case it's not, restart it.
			if (!ModulesList.isElementFullyWorking(mods_manager_index)) {
				ModulesList.restartElement(mods_manager_index);
				final String speak = "WARNING - The Modules Manager stopped working and has been restarted!";
				UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_HIGH, 0, GPTComm.SESSION_TYPE_TEMP, false, null);
			}

			try {
				Thread.sleep(ModulesManager.CHECK_INTERVAL/2);
			} catch (final InterruptedException ignored) {
				// Terminate the app forcefully for the system to restart it.
				UtilsProcesses.terminatePID(UtilsProcesses.getCurrentPID());
			}
		}
	});

	/**
	 * <p>The sole purpose of this receiver is detect when the speech module is ready so the Main Service can start
	 * everything else.</p>
	 */
	final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(@Nullable final Context context, @Nullable final Intent intent) {
			if (intent == null || intent.getAction() == null) {
				return;
			}

			System.out.println("PPPPPPPPPPPPPPPPPP-MainSrv - " + intent.getAction());

			if (!intent.getAction().equals(CONSTS_BC_Speech.ACTION_READY)) {
				return;
			}

			// Start the Modules Manager.
			ModulesList.startElement(ModulesList.getElementIndex(ModulesManager.class));
			infinity_thread.start();

			if (!UtilsApp.isRunningOnTV() && !UtilsApp.isRunningOnWatch()) {
				UtilsRoot.checkWarnRootAccess(false);

				switch (UtilsApp.appInstallationType()) {
					case (UtilsApp.PRIVILEGED_WITHOUT_UPDATES): {
						if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
							final String speak = "WARNING - Installed as privileged application but without updates. " +
									"Only emergency code commands will be available below Android Marshmallow.";
							UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_HIGH, 0, GPTComm.SESSION_TYPE_TEMP, false, null);
						}
						//  todo Remember the user who said you could "potentially" emulate loading from the APK itself?
						//   Try that below Marshmallow... Maybe read the APK? Or extract it to memory and load from
						//   memory? (always from memory, preferably)
						//   Maybe try to extract to the cache partition or folder or something? Not as safe, but more
						//   probable of being possible.

						break;
					}
					case (UtilsApp.NON_PRIVILEGED): {
						final String speak = "WARNING - Installed as non-privileged application! Privileged app " +
								"features may not be available.";
						UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_HIGH, 0, GPTComm.SESSION_TYPE_TEMP, false, null);

						break;
					}
				}

				if (!UtilsApp.isDeviceAdmin()) {
					final String speak = "WARNING - The application is not a Device Administrator! Some security " +
							"features may not be available.";
					UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_HIGH, 0, GPTComm.SESSION_TYPE_TEMP, false, null);
				}

				/* todo if (app_installation_type == UtilsApp.SYSTEM_WITHOUT_UPDATES) {
					switch (Copiar_bibliotecas.copiar_biblioteca_PocketSphinx(getApplicationContext())) {
						case (ARQUITETURA_NAO_DISPONIVEL): {
							// Não é preciso ser fala de emergência, já que isto é das primeiras coisa que ele diz.
							pocketsphinx_disponivel = false;
							fala.speak("WARNING - It was not possible to find a compatible CPU architecture for PocketSphinx " +
									"library to be copied to the device. It will not be possible to have background hotword " +
									"detection.", Fala.SEM_COMANDOS_ADICIONAIS, null, false);
							break;
						}
						case (ERRO_COPIA): {
							// Não é preciso ser fala de emergência, já que isto é das primeiras coisa que ele diz.
							pocketsphinx_disponivel = false;
							fala.speak("WARNING - It was not possible to copy the PocketSphinx library to the device. It will " +
									"not be possible to have background hotword detection.", Fala.SEM_COMANDOS_ADICIONAIS,
									null, false);
							break;
						}
					}
				}*/

				// todo If the overlay permission is granted with the app started, this won't care --> fix it. Put it in
				//  a loop or whatever. Or with some event that the app could broadcast when it detects granted or
				//  denied permissions (this could be useful...).
				// Enable the power button long press detection.
				switch (UtilsMainSrvc.startLongPwrBtnDetection()) {
					case UtilsMainSrvc.UNSUPPORTED_OS_VERSION: {
						final String speak = "The power button long press detection will not be available. Your " +
								"Android version is not supported.";
						UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_HIGH, 0, GPTComm.SESSION_TYPE_TEMP, false, null);

						break;
					}
					case UtilsMainSrvc.UNSUPPORTED_HARDWARE: {
						final String speak = "The power button long press detection will not be available. " +
								"Your hardware does not seem to support the detection.";
						UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_HIGH, 0, GPTComm.SESSION_TYPE_TEMP, false, null);

						break;
					}
					case UtilsMainSrvc.PERMISSION_DENIED: {
						final String speak = "The power button long press detection will not be available. The " +
								"permission to draw a system overlay was denied.";
						UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_HIGH, 0, GPTComm.SESSION_TYPE_TEMP, false, null);

						break;
					}
				}
			}

			UtilsPermsAuths.warnPermissions(UtilsPermsAuths.checkRequestPerms(null, false), false);
			UtilsPermsAuths.warnAuthorizations(UtilsPermsAuths.checkRequestAuths(UtilsPermsAuths.CHECK_ONLY), false);

			// The Main Service is completely ready, so it warns about it so we can start speaking to it (very
			// useful in case the screen gets broken, for example).
			// It's also said in high priority so the user can know immediately (hopefully) that the assistant is
			// ready.
			final String speak = "Ready, sir.";
			UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_HIGH, 0, GPTComm.SESSION_TYPE_TEMP, false, null);

			try {
				unregisterReceiver(broadcastReceiver);
			} catch (final IllegalArgumentException ignored) {
			}
		}
	};

	@Override
	public int onStartCommand(@Nullable final Intent intent, final int flags, final int startId) {
		// Do this below every time the service is started/resumed/whatever

		return START_STICKY;
	}

	@Override
	@Nullable
	public IBinder onBind(@Nullable final Intent intent) {
		return null;
	}
}
