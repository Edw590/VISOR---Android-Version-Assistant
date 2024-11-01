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

package com.edw590.visor_c_a.ActivitiesFragments.Tabs;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.edw590.visor_c_a.GlobalUtils.UtilsPermsAuths;
import com.edw590.visor_c_a.GlobalUtils.UtilsProcesses;
import com.edw590.visor_c_a.GlobalUtils.UtilsShell;
import com.edw590.visor_c_a.Modules.CmdsExecutor.UtilsCmdsExecutorBC;
import com.edw590.visor_c_a.Modules.Speech.Speech2;
import com.edw590.visor_c_a.Modules.Speech.UtilsSpeech2BC;
import com.edw590.visor_c_a.R;

import java.util.Locale;

import UtilsSWA.UtilsSWA;

/**
 * <p>The main fragment to be used for development purposes.</p>
 */
public final class TabSpeechMain extends Fragment {

	@Nullable
	@Override
	public View onCreateView(@android.annotation.NonNull final LayoutInflater inflater,
								   @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.tab_speech_main, container, false);
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

		requireView().findViewById(R.id.btn_tests).setOnClickListener(v -> {
			// BUTTON FOR TESTING
			// BUTTON FOR TESTING
			// BUTTON FOR TESTING



			// BUTTON FOR TESTING
			// BUTTON FOR TESTING
			// BUTTON FOR TESTING
		});
		requireView().findViewById(R.id.btn_perms).setOnClickListener(v -> {
			// Request all missing permissions
			final int perms_left = UtilsPermsAuths.checkRequestPerms(getActivity(), true);
			UtilsPermsAuths.warnPermissions(perms_left, true);

			// Request all missing authorizations
			final int auths_left = UtilsPermsAuths.checkRequestAuths(UtilsPermsAuths.ALSO_REQUEST);
			UtilsPermsAuths.warnAuthorizations(auths_left, true);
		});
		requireView().findViewById(R.id.btn_device_admin).setOnClickListener(v -> {
			startActivity(new Intent().setComponent(new ComponentName("com.android.settings",
					"com.android.settings.DeviceAdminSettings")));
			// Didn't find any constants for these 2 strings above
		});
		requireView().findViewById(R.id.btn_speak_min).setOnClickListener(v -> {
			final String speak = txt_to_speech.getText().toString();
			UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_LOW, 0, false, null);
		});
		requireView().findViewById(R.id.btn_speak_high).setOnClickListener(v -> {
			final String speak = txt_to_speech.getText().toString();
			UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_HIGH, 0, false, null);
		});
		requireView().findViewById(R.id.btn_speak_critical).setOnClickListener(v -> {
			final String speak = txt_to_speech.getText().toString();
			UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_CRITICAL, 0, false, null);
		});
		requireView().findViewById(R.id.btn_send_text).setOnClickListener(v -> {
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
		});
		requireView().findViewById(R.id.btn_force_stop).setOnClickListener(v -> {
			UtilsProcesses.terminatePID(UtilsProcesses.getCurrentPID());
		});
		requireView().findViewById(R.id.btn_skip_speech).setOnClickListener(v -> {
			UtilsSpeech2BC.skipCurrentSpeech();
		});
	}
}
