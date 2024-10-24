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

package com.edw590.visor_c_a.ActivitiesFragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.edw590.visor_c_a.GlobalUtils.PERMS_CONSTS;
import com.edw590.visor_c_a.GlobalUtils.UtilsContext;
import com.edw590.visor_c_a.GlobalUtils.UtilsPermsAuths;
import com.edw590.visor_c_a.GlobalUtils.UtilsProcesses;
import com.edw590.visor_c_a.GlobalUtils.UtilsShell;
import com.edw590.visor_c_a.GlobalUtils.UtilsSysApp;
import com.edw590.visor_c_a.Modules.CmdsExecutor.UtilsCmdsExecutorBC;
import com.edw590.visor_c_a.Modules.Speech.Speech2;
import com.edw590.visor_c_a.Modules.Speech.UtilsSpeech2BC;
import com.edw590.visor_c_a.R;

import java.util.Locale;

import SettingsSync.SettingsSync;
import UtilsSWA.UtilsSWA;

/**
 * <p>The main fragment to be used for development purposes.</p>
 */
public final class FragDevelopment extends Fragment {

	@Nullable
	@Override
	public View onCreateView(@android.annotation.NonNull final LayoutInflater inflater,
								   @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.frag_development, container, false);
	}

	@Override
	public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
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

				final Context context = UtilsContext.getContext();

				//Intent intent = new Intent(getActivity(), ProtectedLockScrAct.class);
				//startActivity(intent);

				//System.out.println("AAAAAAAAAAAAAAAAAAAAAAAA");
				//System.out.println("Weather: " + OIG.getWeather());
				//System.out.println();
				//System.out.println("News: " + OIG.getNews());

				/*String[] weather_data = {
						"Lisboa",
						"14ºC",
						"70%",
						"89%",
						"39 km/h",
						"Wind and rain"
				};
				final String speak = "The weather in " + weather_data[0] + " is " + weather_data[1] +
						" with " + weather_data[5] + ", precipitation of " + weather_data[2] + ", humidity of " + weather_data[3] + ", and wind of " +
						weather_data[4] + ".";
				UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_MEDIUM, Speech2.MODE2_BYPASS_NO_SND, null);*/

				//UtilsCmdsExecutorBC.processTask("tell me the weather and the news", false, false, false);

				//System.out.println("HHHHHHHHHHHHHHHHHH");
				//System.out.println(UtilsStaticStorage.getValue(ValuesStorage.last_phone_call_time, 0));
				//UtilsCmdsExecutorBC.processTask("take a picture", false, false);

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

				final byte[] encrypted_message = UtilsSWA.encryptBytesCRYPTOENDECRYPT(password1, password2, message, associated_authed_data);
				System.out.println(Arrays.toString(encrypted_message));
				UtilsFilesDirs.writeFile(Environment.getExternalStorageDirectory() + "/VISOR/teste.txt", encrypted_message);
				System.out.println("---");*/

				/*final byte[] file_contents = UtilsFilesDirs.readFileBytes(Environment.getExternalStorageDirectory() + "/VISOR/teste.txt");
				System.out.println(Arrays.toString(file_contents));
				System.out.println("---");

				final byte[] gotten_message = UtilsSWA.decryptBytesCRYPTOENDECRYPT(password1, password2, file_contents, associated_authed_data);
				System.out.println(Arrays.toString(gotten_message));
				System.out.println(UtilsSWA.bytesToPrintableDATACONV(gotten_message, true));
				System.out.println(UtilsSWA.bytesToPrintableDATACONV(gotten_message, false));*/

				//final byte[] decrypted_message = UtilsSWA.decryptBytesCRYPTOENDECRYPT(password1, password2, encrypted_message, associated_authed_data);
				//System.out.println(UtilsSWA.bytesToPrintableDATACONV(decrypted_message, true));

				//final List<String> commands = new ArrayList<>(2);
				//commands.add("svc power reboot deviceowner");
				//commands.add("am broadcast -a " + Intent.ACTION_REBOOT);

				//*Isto não funciona. Vê porquê

				// As with the shutdown, execution will only get here if there was some error - but it can get here.
				//UtilsShell.executeShellCmd(commands, true);

				//System.out.println("FFFFFFFFFFFFFFFFFFFFFFFF");
				//final UtilsShell.CmdOutputObj cmd_output = UtilsShell.executeShellCmd("su -c id", false);
				//System.out.println(cmd_output.exit_code);
				//System.out.println(UtilsSWA.bytesToPrintableDATACONV(cmd_output.output_stream, false));
				//System.out.println(UtilsSWA.bytesToPrintableDATACONV(cmd_output.error_stream, false));

				//System.out.println(UtilsShell.getAccessRights("", true));
				//System.out.println(UtilsShell.getAccessRights("/oe", true));
				//System.out.println(UtilsShell.getAccessRights("/oem", true));
				//System.out.println(UtilsShell.getAccessRights("/", true));
				//System.out.println(UtilsShell.getAccessRights("/system", true));
				//System.out.println(UtilsShell.getAccessRights("/storage/emulated/0", true));

				/*System.out.println("PPPPPPPPPPPPPPPPPPPPPPPPPP");
				System.out.println(UtilsSWA.getAveragePingRTTLOCATIONRELATIVE("192.168.1.254"));
				try {
					System.out.println(UtilsSWA.getAveragePingRTTLOCATIONRELATIVE("localhost"));
				} catch (final Throwable e) {
					e.printStackTrace();
				}*/
				//List<String> commands = new ArrayList<>(1);
				//commands.add("ping -c 50 -i 0.5 -n -s 56 -t 1 -v 192.168.1.254");
				//System.out.println(UtilsGeneral.convertBytes2Printable(UtilsShell.executeShellCmd(commands).output_stream));

				//MainActTests.for_tests();

				//UtilsAudioRecorderBC.recordAudio(true, MediaRecorder.AudioSource.MIC, false);

				System.out.println("------------------------");
				System.out.println("IS_SYSTEM_APP: " + UtilsSysApp.mainFunction(null, UtilsSysApp.IS_SYSTEM_APP));
				System.out.println("IS_UPDATED_SYSTEM_APP: " + UtilsSysApp.mainFunction(null, UtilsSysApp.IS_UPDATED_SYSTEM_APP));
				System.out.println("IS_ORDINARY_SYSTEM_APP: " + UtilsSysApp.mainFunction(null, UtilsSysApp.IS_ORDINARY_SYSTEM_APP));
				System.out.println("IS_PRIVILEGED_SYSTEM_APP: " + UtilsSysApp.mainFunction(null, UtilsSysApp.IS_PRIVILEGED_SYSTEM_APP));
				System.out.println("-----");
				for (final String[][] permissions : PERMS_CONSTS.list_of_perms_lists) {
					for (final String[] permission : permissions) {
						System.out.println(permission[0] + ": " + UtilsPermsAuths.checkSelfPermission(permission[0]));
					}
				}
				System.out.println("android.permission.CAPTURE_AUDIO_HOTWORD: " + UtilsPermsAuths.checkSelfPermission("android.permission.CAPTURE_AUDIO_HOTWORD"));
				System.out.println("android.permission.MANAGE_VOICE_KEYPHRASES: " + UtilsPermsAuths.checkSelfPermission("android.permission.MANAGE_VOICE_KEYPHRASES"));
				System.out.println("------------------------");

				System.out.println(SettingsSync.getJsonUserSettings());
				System.out.println("------------------------");
				System.out.println(SettingsSync.getJsonDeviceSettings());
				System.out.println("------------------------");


				//KeyphraseEnrollmentInfo keyphraseEnrollmentInfo = new KeyphraseEnrollmentInfo(UtilsContext.getContext().getPackageManager());
				//System.out.println("VVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV");
				//System.out.println(Arrays.toString(keyphraseEnrollmentInfo.listKeyphraseMetadata()));

				// BUTTON FOR TESTING
				// BUTTON FOR TESTING
				// BUTTON FOR TESTING
			}
		});
		requireView().findViewById(R.id.btn_perms).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				// Request all missing permissions
				final int perms_left = UtilsPermsAuths.checkRequestPerms(getActivity(), true);
				UtilsPermsAuths.warnPermissions(perms_left, true);

				// Request all missing authorizations
				final int auths_left = UtilsPermsAuths.checkRequestAuths(UtilsPermsAuths.ALSO_REQUEST);
				UtilsPermsAuths.warnAuthorizations(auths_left, true);
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
				UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_LOW, 0, true, null);
				//speech3.speak(speak, SpeechQueue.PRIORITY_LOW, SpeechQueue.MODE_DEFAULT, 0);
			}
		});
		requireView().findViewById(R.id.btn_send_text).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				final String inserted_text = txt_to_send.getText().toString().toLowerCase(Locale.ENGLISH);
				if (inserted_text.startsWith("$ ")) {
					final UtilsShell.CmdOutput cmdOutput = UtilsShell.executeShellCmd(false, inserted_text.substring(2));
					System.out.println("----------");
					System.out.println(cmdOutput.exit_code);
					System.out.println("-----");
					System.out.println(UtilsSWA.bytesToPrintableDATACONV(cmdOutput.output_stream, false));
					System.out.println("-----");
					System.out.println(UtilsSWA.bytesToPrintableDATACONV(cmdOutput.error_stream, false));
					System.out.println("----------");
				} else {
					UtilsCmdsExecutorBC.processTask(inserted_text, false, false, false);
				}
			}
		});
		requireView().findViewById(R.id.btn_force_stop).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				UtilsProcesses.terminatePID(UtilsProcesses.getCurrentPID());
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
