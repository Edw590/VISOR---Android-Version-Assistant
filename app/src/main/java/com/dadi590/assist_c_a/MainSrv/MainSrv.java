/*
 * Copyright 2021 DADi590
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

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.dadi590.assist_c_a.BroadcastRecvs.MainRegBroadcastRecv;
import com.dadi590.assist_c_a.GlobalUtils.GL_CONSTS;
import com.dadi590.assist_c_a.GlobalUtils.ObjectClasses;
import com.dadi590.assist_c_a.GlobalUtils.UtilsApp;
import com.dadi590.assist_c_a.GlobalUtils.UtilsGeneral;
import com.dadi590.assist_c_a.GlobalUtils.UtilsServices;
import com.dadi590.assist_c_a.Modules.ModulesManager.ModulesManager;
import com.dadi590.assist_c_a.Modules.ModulesManager.UtilsModulesManager;
import com.dadi590.assist_c_a.Modules.Speech.CONSTS_BC;
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

		// Register the receiver before the speech module is started
		try {
			final IntentFilter intentFilter = new IntentFilter();

			intentFilter.addAction(CONSTS_BC.ACTION_READY);

			UtilsGeneral.getContext().registerReceiver(broadcastReceiver, new IntentFilter(intentFilter));
		} catch (final IllegalArgumentException ignored) {
		}

		// The speech module must be started before everything else for now - only way of him to communicate with the
		// user. Later, notifications will be added. Emails would be a good idea too in more extreme notifications.
		// After the speech module is ready, it will send a broadcast for the receiver below to activate the rest of
		// the assistant.
		ModulesList.startModule(ModulesList.getModuleIndex(Speech2.class));
	}

	final Thread infinity_thread = new Thread(new Runnable() {
		@Override
		public void run() {
			final int index_modules_manager = ModulesList.getModuleIndex(ModulesManager.class);
			final Object[][] modules_list = ModulesList.getModulesList();
			if (ModulesList.MODULE_TYPE_SERVICE != (int) modules_list[index_modules_manager][1] &&
					ModulesList.MODULE_TYPE_INSTANCE != (int) modules_list[index_modules_manager][1]) {
				final String speak = "WARNING - IT'S NOT POSSIBLE TO CHECK AND RESTART THE MODULES MANAGER IN CASE IT" +
						"STOPS WORKING!!!";
				UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_CRITICAL, null);

				return;
			}

			// Keep checking if the Modules Manager is working and in case it's not, restart it.
			while (true) {
				if (UtilsModulesManager.checkRestartModule(index_modules_manager)) {
					final String speak = "WARNING - Modules Manager!";
					UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_HIGH, null);
				}

				try {
					Thread.sleep(10_000L);
				} catch (final InterruptedException ignored) {
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

			if (intent.getAction().equals(CONSTS_BC.ACTION_READY)) {
				// Start the main broadcast receivers before everything else, so stuff can start sending broadcasts
				// right away after being ready.
				MainRegBroadcastRecv.registerReceivers();

				//UtilsGeneral.checkWarnRootAccess(false); Not supposed to be needed root access. Only system permissions.

				switch (UtilsApp.appInstallationType()) {
					case (UtilsApp.PRIVILEGED_WITHOUT_UPDATES): {
						final String speak = "WARNING - Installed as privileged application but without updates. Only " +
								"emergency code commands will be available.";
						// todo Is it so? Even on Marshmallow and above with extractNativeLibs=false...? Test that.
						//  Remember the user who said you could "potentially" emulate loading from the APK itself? Try
						//  that below Marshmallow... Maybe read the APK? Or extract it to memory and load from memory?
						//  (always from memory, preferably)
						UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_HIGH, null);
						break;
					}
					case (UtilsApp.NON_PRIVILEGED): {
						final String speak = "WARNING - Installed as non-privileged application! System features may " +
								"not be available.";
						UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_HIGH, null);
						break;
					}
				}

				if (!UtilsApp.isDeviceAdmin()) {
					final String speak = "WARNING - The application is not a Device Administrator! Some security " +
							"features may not be available.";
					UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_HIGH, null);
				}

				/*if (app_installation_type == UtilsApp.SYSTEM_WITHOUT_UPDATES) {
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

				//Utils_reconhecimentos_voz.iniciar_reconhecimento_pocketsphinx();

				//pressao_longa_botoes.ativar_detecao(Build.VERSION.SDK_INT);

				// Start the Modules Manager.
				ModulesList.startModule(ModulesList.getModuleIndex(ModulesManager.class));
				infinity_thread.start();

				// Enable the power button long press detection.
				switch (LongBtnsPressDetector.startDetector()) {
					case LongBtnsPressDetector.UNSUPPORTED_OS_VERSION: {
						final String speak = "The power button long press detection will not be available. Your " +
								"Android version is not supported.";
						UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_HIGH, null);

						break;
					}
					case LongBtnsPressDetector.UNSUPPORTED_HARDWARE: {
						final String speak = "The power button long press detection will not be available. " +
								"Your hardware does not seem to support the detection.";
						UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_HIGH, null);

						break;
					}
					case LongBtnsPressDetector.PERMISSION_DENIED: {
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

		// Do NOT put ANYTHING here!!!
		// MANY places starting this service don't check if it's already started or not, so this method will be called
		// many times randomly. Put everything on onCreate(), which is called only if the service was not running and
		// was just started.

		return START_STICKY;
	}

	@Override
	@Nullable
	public final IBinder onBind(@Nullable final Intent intent) {
		return null;
	}
}
