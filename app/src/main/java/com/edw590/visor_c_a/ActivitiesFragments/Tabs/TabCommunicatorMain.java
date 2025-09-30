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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

import com.edw590.visor_c_a.GlobalUtils.UtilsLogging;
import com.edw590.visor_c_a.GlobalUtils.UtilsShell;
import com.edw590.visor_c_a.Modules.CmdsExecutor.UtilsCmdsExecutorBC;
import com.edw590.visor_c_a.Modules.SpeechRecognitionCtrl.UtilsSpeechRecognizersBC;
import com.edw590.visor_c_a.R;

import java.util.Locale;

import GPTComm.GPTComm;
import ModsFileInfo.ModsFileInfo;
import UtilsSWA.UtilsSWA;

public final class TabCommunicatorMain extends Fragment {

	AppCompatTextView txt_gpt_comm_state;
	AppCompatTextView txt_response;

	private Thread infinity_checker = null;

	@Override
	public void onStop() {
		super.onStop();

		if (infinity_checker != null) {
			infinity_checker.interrupt();
		}
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
								   @Nullable final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.nested_scroll_view, container, false);
	}

	@Override
	public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		int padding = Utils.getDefaultPadding(requireContext());
		LinearLayout linearLayout = view.findViewById(R.id.nested_scroll_view_linear_layout);
		linearLayout.setPadding(padding, padding, padding, padding);

		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		txt_gpt_comm_state = new AppCompatTextView(requireContext());
		txt_gpt_comm_state.setText("GPT state: error");
		txt_gpt_comm_state.setTextIsSelectable(true);

		AppCompatEditText editTxt_txt_to_send = new AppCompatEditText(requireContext());
		editTxt_txt_to_send.setHint("Text to send to VISOR");

		AppCompatButton btn_listen = new AppCompatButton(requireContext());
		btn_listen.setText("Listen");
		btn_listen.setOnClickListener(v -> {
			UtilsSpeechRecognizersBC.startCommandsRecognition();
		});

		AppCompatButton btn_send_text = new AppCompatButton(requireContext());
		btn_send_text.setText("Send text");
		btn_send_text.setOnClickListener(v -> {
			final String inserted_text = editTxt_txt_to_send.getText().toString();
			if (inserted_text.startsWith("$ ")) {
				final UtilsShell.CmdOutput cmdOutput = UtilsShell.executeShellCmd(false, inserted_text.substring(2));
				UtilsLogging.logLnDebug("----------");
				UtilsLogging.logLnDebug(cmdOutput.exit_code);
				UtilsLogging.logLnDebug("-----");
				UtilsLogging.logLnDebug(UtilsSWA.bytesToPrintableDATACONV(cmdOutput.output_stream, false));
				UtilsLogging.logLnDebug("-----");
				UtilsLogging.logLnDebug(UtilsSWA.bytesToPrintableDATACONV(cmdOutput.error_stream, false));
				UtilsLogging.logLnDebug("----------");
			} else {
				UtilsCmdsExecutorBC.processTask(inserted_text, false, false, false);
			}
		});

		txt_response = new AppCompatTextView(requireContext());
		txt_response.setText("Response");
		txt_response.setTextIsSelectable(true);

		linearLayout.addView(txt_gpt_comm_state);
		linearLayout.addView(editTxt_txt_to_send);
		linearLayout.addView(btn_listen);
		linearLayout.addView(btn_send_text);
		linearLayout.addView(txt_response);

		createStartInfinityChecker();
	}

	void createStartInfinityChecker() {
		infinity_checker = new Thread(() -> {
			Runnable runnable = () -> {
				txt_response.setText(GPTComm.getLastText());
			};
			Runnable runnable1 = () -> {
				String gpt_state = "[Not connected to the server to get the GPT state]";
				if (UtilsSWA.isCommunicatorConnectedSERVER()) {
					switch (GPTComm.getModuleState()) {
						case (ModsFileInfo.MOD_7_STATE_STOPPED): {
							gpt_state = "stopped";

							break;
						}
						case (ModsFileInfo.MOD_7_STATE_STARTING): {
							gpt_state = "starting";

							break;
						}
						case (ModsFileInfo.MOD_7_STATE_READY): {
							gpt_state = "ready";

							break;
						}
						case (ModsFileInfo.MOD_7_STATE_BUSY): {
							gpt_state = "busy";

							break;
						}
						default: {
							gpt_state = "invalid";

							break;
						}
					}
				}
				txt_gpt_comm_state.setText("GPT state: " + gpt_state);
			};
			String old_text = "";
			while (true) {
				String new_text = GPTComm.getLastText();
				if (!new_text.equals(old_text)) {
					old_text = new_text;
					requireActivity().runOnUiThread(runnable);
				}

				requireActivity().runOnUiThread(runnable1);

				try {
					Thread.sleep(1000);
				} catch (final InterruptedException ignored) {
					return;
				}
			}
		});
		infinity_checker.start();
	}
}
