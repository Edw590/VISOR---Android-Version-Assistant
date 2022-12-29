/*
 * Copyright 2022 DADi590
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

package com.dadi590.assist_c_a.MainSrv;

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

import com.dadi590.assist_c_a.ActivitiesFragments.ActMain;
import com.dadi590.assist_c_a.BroadcastRecvs.MainRegRecv;
import com.dadi590.assist_c_a.GlobalUtils.GL_CONSTS;
import com.dadi590.assist_c_a.GlobalUtils.ObjectClasses;
import com.dadi590.assist_c_a.GlobalUtils.UtilsApp;
import com.dadi590.assist_c_a.GlobalUtils.UtilsGeneral;
import com.dadi590.assist_c_a.GlobalUtils.UtilsNotifications;
import com.dadi590.assist_c_a.GlobalUtils.UtilsPermsAuths;
import com.dadi590.assist_c_a.GlobalUtils.UtilsRoot;
import com.dadi590.assist_c_a.Modules.ModulesManager.ModulesManager;
import com.dadi590.assist_c_a.Modules.Speech.CONSTS_BC_Speech;
import com.dadi590.assist_c_a.Modules.Speech.Speech2;
import com.dadi590.assist_c_a.Modules.Speech.UtilsSpeech2BC;
import com.dadi590.assist_c_a.ModulesList;

/**
 * The main {@link Service} of the application - MainService.
 */
public class MainSrv extends Service {

	//////////////////////////////////////
	// Class variables

	// Private variables //

	//////////////////////////////////////


	@Override
	public final void onCreate() {
		super.onCreate();

		// Do this only once, when the service is created and while it's not destroyed

		final Intent intent1 = new Intent(this, ActMain.class);
		intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		int flag_immutable = 0;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			flag_immutable = PendingIntent.FLAG_IMMUTABLE;
		}
		final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent1, flag_immutable |
				PendingIntent.FLAG_CANCEL_CURRENT);
		final ObjectClasses.NotificationInfo notificationInfo = new ObjectClasses.NotificationInfo(
				GL_CONSTS.CH_ID_MAIN_SRV_FOREGROUND,
				"Main notification",
				"",
				NotificationCompat.PRIORITY_MIN, // Ignore the warning. The API level is checked later.
				// If I'm not forgetting anything (writing this much time after I put the code here), this (only) gets
				// the notification in the bottom of the other ones with MIN importance. If it's MIN, the notification
				// will be on the top of the list when it starts. With UNSPECIFIED, it gets to the bottom.
				GL_CONSTS.ASSISTANT_NAME + " Systems running",
				"",
				pendingIntent
		);
		startForeground(GL_CONSTS.NOTIF_ID_MAIN_SRV_FOREGROUND, UtilsNotifications.getNotification(notificationInfo).
				setOngoing(true).
				build());

		// Register the receiver before the speech module is started
		try {
			final IntentFilter intentFilter = new IntentFilter();

			intentFilter.addAction(CONSTS_BC_Speech.ACTION_READY);

			UtilsGeneral.getContext().registerReceiver(broadcastReceiver, new IntentFilter(intentFilter));
		} catch (final IllegalArgumentException ignored) {
		}

		// The speech module must be started before everything else for now - only way of him to communicate with the
		// user. Later, notifications will be added. Emails would be a good idea too in more extreme notifications.
		// After the speech module is ready, it will send a broadcast for the receiver below to activate the rest of
		// the assistant.
		// EDIT: the Speech2 module has now notifications integrated into it, so it's still the thing to start before
		// everything else.
		ModulesList.startModule(ModulesList.getElementIndex(Speech2.class));
	}

	final Thread infinity_thread = new Thread(new Runnable() {
		@Override
		public void run() {
			final int mods_manager_index = ModulesList.getElementIndex(ModulesManager.class);

			while (true) {
				// Force the permissions every some seconds.
				UtilsPermsAuths.checkRequestPerms(null, true);
				UtilsPermsAuths.checkRequestAuths(UtilsPermsAuths.ALSO_FORCE);

				// Keep checking if the Modules Manager is working and in case it's not, restart it.
				if (!ModulesList.isModuleFullyWorking(mods_manager_index)) {
					ModulesList.restartModule(mods_manager_index);
					final String speak = "WARNING - The Modules Manager stopped working and has been restarted!";
					UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_HIGH, null);
				}

				try {
					Thread.sleep(5_000L);
				} catch (final InterruptedException ignored) {

					// todo Hopefully this won't happen. Not sure what to do here.
					// Can't kill the PID for the system to restart the app - might corrupt ongoing stuff.

					Thread.currentThread().interrupt();

					return;
				}
			}
		}
	});

	/**
	 * <p>The sole purpose of this receiver is detect when the speech module is ready so the Main Service can start
	 * everything else.</p>
	 */
	private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(@Nullable final Context context, @Nullable final Intent intent) {
			if (intent == null || intent.getAction() == null) {
				return;
			}

			System.out.println("PPPPPPPPPPPPPPPPPP-MainSrv - " + intent.getAction());

			if (intent.getAction().equals(CONSTS_BC_Speech.ACTION_READY)) {
				// Start the main broadcast receivers before everything else, so stuff can start sending broadcasts
				// right away after being ready.
				MainRegRecv.registerReceivers();

				// Start the Modules Manager.
				ModulesList.startModule(ModulesList.getElementIndex(ModulesManager.class));
				infinity_thread.start();

				UtilsRoot.checkWarnRootAccess(false);

				switch (UtilsApp.appInstallationType()) {
					case (UtilsApp.PRIVILEGED_WITHOUT_UPDATES): {
						if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
							final String speak = "WARNING - Installed as privileged application but without updates. " +
									"Only emergency code commands will be available below Android Marshmallow.";
							UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_HIGH, null);
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
						UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_HIGH, null);

						break;
					}
				}

				if (!UtilsApp.isDeviceAdmin()) {
					final String speak = "WARNING - The application is not a Device Administrator! Some security " +
							"features may not be available.";
					UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_HIGH, null);
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
				switch (UtilsMainSrv.startLongPwrBtnDetection()) {
					case UtilsMainSrv.UNSUPPORTED_OS_VERSION: {
						final String speak = "The power button long press detection will not be available. Your " +
								"Android version is not supported.";
						UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_HIGH, null);

						break;
					}
					case UtilsMainSrv.UNSUPPORTED_HARDWARE: {
						final String speak = "The power button long press detection will not be available. " +
								"Your hardware does not seem to support the detection.";
						UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_HIGH, null);

						break;
					}
					case UtilsMainSrv.PERMISSION_DENIED: {
						final String speak = "The power button long press detection will not be available. The " +
								"permission to draw a system overlay was denied.";
						UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_HIGH, null);

						break;
					}
				}

				// The Main Service is completely ready, so it warns about it so we can start speaking to it (very
				// useful in case the screen gets broken, for example).
				// It's also said in high priority so the user can know immediately (hopefully) that the assistant is
				// ready.
				final String speak = "Ready, sir.";
				UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_HIGH, null);

				try {
					UtilsGeneral.getContext().unregisterReceiver(this);
				} catch (final IllegalArgumentException ignored) {
				}
			}
		}
	};

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
