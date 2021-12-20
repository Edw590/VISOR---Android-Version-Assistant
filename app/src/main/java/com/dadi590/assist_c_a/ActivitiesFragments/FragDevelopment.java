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

package com.dadi590.assist_c_a.ActivitiesFragments;

import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dadi590.assist_c_a.GlobalUtils.UtilsApp;
import com.dadi590.assist_c_a.GlobalUtils.UtilsGeneral;
import com.dadi590.assist_c_a.GlobalUtils.UtilsPermissions;
import com.dadi590.assist_c_a.MainSrv.MainSrv;
import com.dadi590.assist_c_a.Modules.ProtectedLockScr.ProtectedLockScrAct;
import com.dadi590.assist_c_a.Modules.Speech.Speech2;
import com.dadi590.assist_c_a.Modules.Speech.UtilsSpeech2BC;
import com.dadi590.assist_c_a.Modules.ValuesStorage.CONSTS;
import com.dadi590.assist_c_a.Modules.ValuesStorage.ValuesStorage;
import com.dadi590.assist_c_a.R;

import java.util.Locale;

/**
 * <p>The main fragment to be used for development purposes.</p>
 */
public class FragDevelopment extends Fragment {

	@Nullable
	@Override
	public final View onCreateView(@android.annotation.NonNull final LayoutInflater inflater,
								   @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.frag_development, container, false);
	}

	@Override
	public final void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setButtonsClickListeners();

		// To request focus to the EditText that sends text to the assistant
		// Below (and anywhere else) will never throw an exception because there exists a view
		final EditText editText = requireView().findViewById(R.id.txt_to_send);
		editText.requestFocus();
	}

	/**
	 * Sets all the listeners for buttons of the activity.
	 */
	private void setButtonsClickListeners() {
		final EditText txt_to_speech = requireView().findViewById(R.id.txt_to_speech);
		final EditText txt_to_send = requireView().findViewById(R.id.txt_to_send);

		requireView().findViewById(R.id.btn_tests).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				// BUTTON FOR TESTING
				// BUTTON FOR TESTING
				// BUTTON FOR TESTING

				//UtilsLocationRelative.startIndRelDistance();

				final Intent intent = new Intent(getActivity(), ProtectedLockScrAct.class);
				//startActivity(intent);

				System.out.println("HHHHHHHHHHHHHHHHHH");
				System.out.println(ValuesStorage.getValue(CONSTS.last_phone_call_time));
				MainSrv.getExecutor().processTask("turn on wifi", false, false);

				/*System.out.println("HHHHHHHHHHHHHHHHHH");
				final byte[] password1 = "this is a test".getBytes(Charset.defaultCharset());
				final byte[] password2 = "this is one other test".getBytes(Charset.defaultCharset());
				byte[] message = new byte[0];
				try {
					message = "this is another test ´1ºªá¨nñë€§«".getBytes(GL_CONSTS.UTF7_NAME_LIB);
				} catch (final UnsupportedEncodingException ignored) {
				}
				final byte[] associated_authed_data = "Test 44".getBytes(Charset.defaultCharset());
				System.out.println(Arrays.toString(password1));
				System.out.println(Arrays.toString(password2));
				System.out.println(Arrays.toString(message));
				System.out.println(Arrays.toString(associated_authed_data));
				System.out.println("---");

				final byte[] encrypted_message = UtilsCryptoEnDecrypt.encryptBytes(password1, password2, message, associated_authed_data);
				System.out.println(Arrays.toString(encrypted_message));*/
				/*UtilsFilesDirs.writeFile(Environment.getExternalStorageDirectory() + "/V.I.S.O.R./teste.txt", encrypted_message, false);
				System.out.println("---");

				final byte[] file_contents = UtilsFilesDirs.readFileExtStge(Environment.getExternalStorageDirectory() + "/V.I.S.O.R./teste.txt");
				System.out.println(Arrays.toString(file_contents));
				System.out.println("---");

				final byte[] gotten_message = UtilsCryptoEnDecrypt.decryptBytes(password1, password2, file_contents, associated_authed_data);
				System.out.println(Arrays.toString(gotten_message));
				System.out.println(UtilsGeneral.bytesToPrintableChars(gotten_message, true));
				System.out.println(UtilsGeneral.bytesToPrintableChars(gotten_message, false));*/

				//System.out.println(UtilsShell.getAccessRights("", true));
				//System.out.println(UtilsShell.getAccessRights("/oe", true));
				//System.out.println(UtilsShell.getAccessRights("/oem", true));
				//System.out.println(UtilsShell.getAccessRights("/", true));
				//System.out.println(UtilsShell.getAccessRights("/system", true));
				//System.out.println(UtilsShell.getAccessRights("/storage/emulated/0", true));

				//System.out.println("PPPPPPPPPPPPPPPPPPPPPPPPPP");
				//UtilsNetwork.getAveragePingRTT("192.168.1.254");
				//List<String> commands = new ArrayList<>(1);
				//commands.add("ping -c 50 -i 0.5 -n -s 56 -t 1 -v 192.168.1.254");
				//System.out.println(UtilsGeneral.convertBytes2Printable(UtilsShell.executeShellCmd(commands).output_stream));

				//MainActTests.for_tests();

				// BUTTON FOR TESTING
				// BUTTON FOR TESTING
				// BUTTON FOR TESTING
			}
		});
		requireView().findViewById(R.id.btn_perms).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				int missing_authorizations = 0;

				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
					final String speak = "No manual permission authorizations needed below Android Marshmallow.";
					UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_USER_ACTION, null);
				} else {
					// Request all missing permissions
					final int perms_left = UtilsPermissions.wrapperRequestPerms(getActivity(), true)[1];
					UtilsPermissions.warnPermissions(perms_left, true);

					// Check if the notification policy access has been granted for the app and if not, open the settings
					// screen for the user to grant it.
					final NotificationManager mNotificationManager = (NotificationManager)
							UtilsGeneral.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
					if (!mNotificationManager.isNotificationPolicyAccessGranted()) {
						++missing_authorizations;
						final Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
						startActivity(intent);
					}

					// Check if the app can draw system overlays and open the settings screen if not
					if (!Settings.canDrawOverlays(UtilsGeneral.getContext())) {
						++missing_authorizations;
						final Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
								Uri.parse("package:" + UtilsGeneral.getContext().getPackageName()));
						startActivity(intent);
					}

					// Check if the app can bypass battery optimizations and request it if not
					final PowerManager powerManager = (PowerManager) UtilsGeneral.getContext()
							.getSystemService(Context.POWER_SERVICE);
					if (!powerManager.isIgnoringBatteryOptimizations(UtilsGeneral.getContext().getPackageName())) {
						++missing_authorizations;
						final Intent intent = new Intent();
						intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
						intent.setData(Uri.parse("package:" + UtilsGeneral.getContext().getPackageName()));
						UtilsGeneral.getContext().startActivity(intent);
					}
				}

				//Mete-o a verificar estas coisas na inicialização da app também... Mas só verificar, não pedir.

				if (!UtilsApp.isDeviceAdmin()) {
					++missing_authorizations;
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
				UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_USER_ACTION, null);
			}
		});
		requireView().findViewById(R.id.btn_device_admin).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				startActivity(new Intent().setComponent(new ComponentName("com.android.settings",
						"com.android.settings.DeviceAdminSettings")));
				// Didn't find any constants for these 2 strings above
			}
		});
		requireView().findViewById(R.id.btn_speak_min).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				final String speak = txt_to_speech.getText().toString();
				UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_LOW, null);
			}
		});
		requireView().findViewById(R.id.btn_speak_high).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				final String speak = txt_to_speech.getText().toString();
				UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_HIGH, null);
				// Leave PRIORITY_HIGH there because CRITICAL will get the volume in the maximum, and this is probably
				// just to test if the priority implementation is working.
			}
		});
		requireView().findViewById(R.id.btn_send_text).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				final String inserted_text = txt_to_send.getText().toString().toLowerCase(Locale.ENGLISH);
				if ("stop".equals(inserted_text)) {
					MainSrv.getAudioRecorder().recordAudio(false, -1);
				} else {
					MainSrv.getExecutor().processTask(inserted_text, false, false);
				}

				/*final Intent broadcast_intent = new Intent(BroadcastReceivers_com_registo.ENVIAR_TAREFA);
				broadcast_intent.putExtra("extras_frase_str", txt_to_send.getText().toString());
				broadcast_intent.putExtra("extras_resultados_parciais", false);
				sendBroadcast(broadcast_intent, GL_CONSTS.ASSIST_C_A_RECV_PERM);*/
			}
		});
		requireView().findViewById(R.id.btn_skip_speech).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				UtilsSpeech2BC.skipCurrentSpeech();
			}
		});
	}
}
