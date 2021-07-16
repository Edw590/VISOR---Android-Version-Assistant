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

package com.dadi590.assist_c_a.MainAct;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.dadi590.assist_c_a.GlobalUtils.UtilsApp;
import com.dadi590.assist_c_a.GlobalUtils.UtilsGeneral;
import com.dadi590.assist_c_a.GlobalUtils.UtilsNetwork;
import com.dadi590.assist_c_a.GlobalUtils.UtilsPermissions;
import com.dadi590.assist_c_a.GlobalUtils.UtilsServices;
import com.dadi590.assist_c_a.GlobalUtils.UtilsShell;
import com.dadi590.assist_c_a.MainSrv;
import com.dadi590.assist_c_a.Modules.ProtectedLockScr.ProtectedLockScr;
import com.dadi590.assist_c_a.Modules.Speech.Speech2;
import com.dadi590.assist_c_a.R;

import java.util.Locale;

/**
 * The main {@link Activity} of the application - MainActivity.
 */
public class MainAct extends AppCompatActivity {

	@Override
	protected final void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_act);

		// Do this only once, when the activity is created and while it's not destroyed

		final int perms_left = UtilsPermissions.wrapperRequestPerms(null, false)[1];
		UtilsPermissions.warnPermissions(perms_left, false);
		UtilsServices.startService(MainSrv.class);

		setButtonsClickListeners();

		// To request focus to the EditText that sends text to the assistant
		final EditText editText = findViewById(R.id.txt_to_send);
		editText.requestFocus();
	}

	@Override
	protected final void onStart() {
		super.onStart();

		// Do this below every time the activity is started/resumed/whatever

		UtilsPermissions.wrapperRequestPerms(null, false);
		UtilsServices.startService(MainSrv.class);
	}

	/**
	 * Sets all the listeners for buttons of the activity.
	 */
	private void setButtonsClickListeners() {
		final EditText txt_to_speech = findViewById(R.id.txt_to_speech);
		final EditText txt_to_send = findViewById(R.id.txt_to_send);

		findViewById(R.id.btn_tests).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				// BUTTON FOR TESTING
				// BUTTON FOR TESTING
				// BUTTON FOR TESTING

				final Intent intent = new Intent(MainAct.this, ProtectedLockScr.class);
				//startActivity(intent);

				System.out.println(UtilsShell.getAccessRights("", true));
				System.out.println(UtilsShell.getAccessRights("/oe", true));
				System.out.println(UtilsShell.getAccessRights("/oem", true));
				System.out.println(UtilsShell.getAccessRights("/", true));
				System.out.println(UtilsShell.getAccessRights("/system", true));
				System.out.println(UtilsShell.getAccessRights("/storage/emulated/0", true));

				System.out.println("PPPPPPPPPPPPPPPPPPPPPPPPPP");
				UtilsNetwork.getAveragePingRTT("192.168.1.254");
				//List<String> commands = new ArrayList<>(1);
				//commands.add("ping -c 50 -i 0.5 -n -s 56 -t 1 -v 192.168.1.254");
				//System.out.println(UtilsGeneral.convertBytes2Printable(UtilsShell.executeShellCmd(commands).output_stream));

				//MainActTests.for_tests();

				// BUTTON FOR TESTING
				// BUTTON FOR TESTING
				// BUTTON FOR TESTING
			}
		});
		findViewById(R.id.btn_perms).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				int missing_authorizations = 0;

				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
					final String speak = "No manual permission authorizations needed below Android Marshmallow.";
					MainSrv.getSpeech2().speak(speak, Speech2.NO_ADDITIONAL_COMMANDS, Speech2.PRIORITY_USER_ACTION, null);
				} else {
					// Request all missing permissions
					final int perms_left = UtilsPermissions.wrapperRequestPerms(MainAct.this, true)[1];
					UtilsPermissions.warnPermissions(perms_left, true);

					// Check if the notification policy access has been granted for the app and if not, open the settings
					// screen for the user to grant it.
					final NotificationManager mNotificationManager = (NotificationManager)
							getSystemService(Context.NOTIFICATION_SERVICE);
					if (!mNotificationManager.isNotificationPolicyAccessGranted()) {
						missing_authorizations++;
						final Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
						startActivity(intent);
					}

					// Check if the app can draw system overlays and open the settings screen if not
					if (!Settings.canDrawOverlays(UtilsGeneral.getContext())) {
						missing_authorizations++;
						final Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
								Uri.parse("package:" + getPackageName()));
						startActivity(intent);
					}

					// Check if the app can bypass battery optimizations and request it if not
					final PowerManager powerManager = (PowerManager) UtilsGeneral.getContext()
							.getSystemService(Context.POWER_SERVICE);
					if (!powerManager.isIgnoringBatteryOptimizations(UtilsGeneral.getContext().getPackageName())) {
						missing_authorizations++;
						final Intent intent = new Intent();
						intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
						intent.setData(Uri.parse("package:" + UtilsGeneral.getContext().getPackageName()));
						UtilsGeneral.getContext().startActivity(intent);
					}
				}

				//Mete-o a verificar estas coisas na inicialização da app também... Mas só verificar, não pedir.

				if (!UtilsApp.isDeviceAdmin()) {
					missing_authorizations++;
					startActivity(new Intent().setComponent(new ComponentName("com.android.settings",
							"com.android.settings.DeviceAdminSettings")));
				}

				final String speak;
				if (missing_authorizations == 0) {
					speak = "No authorizations left to grant.";
				} else {
					speak = "Warning - Not all authorizations have been granted to the application! Number of " +
							"authorizations left to grant: " + missing_authorizations + ".";
				}
				MainSrv.getSpeech2().speak(speak, Speech2.NO_ADDITIONAL_COMMANDS, Speech2.PRIORITY_USER_ACTION, null);
			}
		});
		findViewById(R.id.btn_device_admin).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				startActivity(new Intent().setComponent(new ComponentName("com.android.settings",
						"com.android.settings.DeviceAdminSettings")));
				// Didn't find any constants for these 2 strings above
			}
		});
		findViewById(R.id.btn_speak_min).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				final String speak = txt_to_speech.getText().toString();
				MainSrv.getSpeech2().speak(speak, Speech2.NO_ADDITIONAL_COMMANDS, Speech2.PRIORITY_LOW, null);
			}
		});
		findViewById(R.id.btn_speak_high).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				final String speak = txt_to_speech.getText().toString();
				MainSrv.getSpeech2().speak(speak, Speech2.NO_ADDITIONAL_COMMANDS, Speech2.PRIORITY_HIGH, null);
				// Leave PRIORITY_HIGH there because CRITICAL will get the volume in the maximum, and this is probably
				// just to test if the priority implementation is working.
			}
		});
		findViewById(R.id.btn_send_text).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				if ("start".equals(txt_to_send.getText().toString().toLowerCase(Locale.ENGLISH))) {
					MainSrv.getAudioRecorder().record(true, MediaRecorder.AudioSource.MIC);
				} else if ("stop".equals(txt_to_send.getText().toString().toLowerCase(Locale.ENGLISH))) {
					MainSrv.getAudioRecorder().record(false, -1);
				}

				/*final Intent broadcast_intent = new Intent(BroadcastReceivers_com_registo.ENVIAR_TAREFA);
				broadcast_intent.putExtra("extras_frase_str", txt_to_send.getText().toString());
				broadcast_intent.putExtra("extras_resultados_parciais", false);
				sendBroadcast(broadcast_intent, GL_CONSTS.ASSIST_C_A_RECV_PERM);*/
			}
		});
		findViewById(R.id.btn_skip_speech).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				MainSrv.getSpeech2().skipCurrentSpeech();
			}
		});
	}

	@Override
	protected final void onStop() {
		super.onStop();

		UtilsPermissions.wrapperRequestPerms(null, false);
		UtilsServices.startService(MainSrv.class);
	}
}
