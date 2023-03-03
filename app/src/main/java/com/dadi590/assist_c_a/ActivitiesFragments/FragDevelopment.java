/*
 * Copyright 2023 DADi590
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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dadi590.assist_c_a.GlobalUtils.PERMS_CONSTS;
import com.dadi590.assist_c_a.GlobalUtils.UtilsGeneral;
import com.dadi590.assist_c_a.GlobalUtils.UtilsPermsAuths;
import com.dadi590.assist_c_a.GlobalUtils.UtilsProcesses;
import com.dadi590.assist_c_a.GlobalUtils.UtilsSysApp;
import com.dadi590.assist_c_a.Modules.AudioRecorder.UtilsAudioRecorderBC;
import com.dadi590.assist_c_a.Modules.CmdsExecutor.UtilsCmdsExecutorBC;
import com.dadi590.assist_c_a.Modules.ProtectedLockScr.ProtectedLockScrAct;
import com.dadi590.assist_c_a.Modules.Speech.Speech2;
import com.dadi590.assist_c_a.Modules.Speech.UtilsSpeech2BC;
import com.dadi590.assist_c_a.R;

import java.util.Locale;

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

				final Context context = UtilsGeneral.getContext();

				//UtilsLocationRelative.startIndRelDistance();

				Intent intent = new Intent(getActivity(), ProtectedLockScrAct.class);
				//startActivity(intent);

				//System.out.println("HHHHHHHHHHHHHHHHHH");
				//System.out.println(UtilsStaticStorage.getValue(ValuesStorage.last_phone_call_time, 0L));
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

				final byte[] encrypted_message = UtilsCryptoEnDecrypt.encryptBytes(password1, password2, message, associated_authed_data);
				System.out.println(Arrays.toString(encrypted_message));*/
				/*UtilsFilesDirs.writeFile(Environment.getExternalStorageDirectory() + "/V.I.S.O.R./teste.txt", encrypted_message, false);
				System.out.println("---");

				final byte[] file_contents = UtilsFilesDirs.readFileExtStge(Environment.getExternalStorageDirectory() + "/V.I.S.O.R./teste.txt");
				System.out.println(Arrays.toString(file_contents));
				System.out.println("---");

				final byte[] gotten_message = UtilsCryptoEnDecrypt.decryptBytes(password1, password2, file_contents, associated_authed_data);
				System.out.println(Arrays.toString(gotten_message));
				System.out.println(UtilsDataConv.bytesToPrintableChars(gotten_message, true));
				System.out.println(UtilsDataConv.bytesToPrintableChars(gotten_message, false));*/

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

				UtilsAudioRecorderBC.recordAudio(true, MediaRecorder.AudioSource.MIC, false);

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
				System.out.println("------------------------");

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
				UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_LOW, true, null);
			}
		});
		requireView().findViewById(R.id.btn_send_text).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				final String inserted_text = txt_to_send.getText().toString().toLowerCase(Locale.ENGLISH);
				UtilsCmdsExecutorBC.processTask(inserted_text, false, false);
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
