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

import android.content.res.Resources;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
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

import com.edw590.visor_c_a.GlobalUtils.UtilsShell;
import com.edw590.visor_c_a.Modules.CmdsExecutor.UtilsCmdsExecutorBC;
import com.edw590.visor_c_a.R;

import java.util.Locale;

import GPTComm.GPTComm;
import UtilsSWA.UtilsSWA;

/**
 * <p>Fragment that shows the status of each module of the assistant.</p>
 */
public final class TabCommunicatorMain extends Fragment {

	AppCompatTextView txt_response;

	@Override
	public void onStart() {
		super.onStart();

		try {
			infinity_checker.start();
		} catch (final IllegalThreadStateException ignored) {
		}
	}

	@Override
	public void onStop() {
		super.onStop();

		infinity_checker.interrupt();
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

		LinearLayout linearLayout = view.findViewById(R.id.nested_scroll_view_linear_layout);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		// Below, convert DP to PX to input on setMargins(), which takes pixels only.
		// 15 DP seems to be enough as margins.
		final Resources resources = requireActivity().getResources();
		final int padding_px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15.0F,
				resources.getDisplayMetrics());

		AppCompatEditText editTxt_txt_to_send = new AppCompatEditText(requireContext());
		editTxt_txt_to_send.setSingleLine(false);
		editTxt_txt_to_send.setHint("Text to send to VISOR (commands or normal text to the LLM)");

		AppCompatButton btn_send_text = new AppCompatButton(requireContext());
		btn_send_text.setText("Send text");
		btn_send_text.setOnClickListener(v -> {
			final String inserted_text = editTxt_txt_to_send.getText().toString().toLowerCase(Locale.ENGLISH);
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

		txt_response = new AppCompatTextView(requireContext());
		txt_response.setPadding(padding_px, padding_px, padding_px, padding_px);
		txt_response.setText("Response from the smart LLM");
		txt_response.setTextIsSelectable(true);

		linearLayout.addView(editTxt_txt_to_send);
		linearLayout.addView(btn_send_text);
		linearLayout.addView(txt_response);
	}

	private final Thread infinity_checker = new Thread(new Runnable() {
		@Override
		public void run() {
			Runnable runnable = () -> {
				txt_response.setText(GPTComm.getLastText());
			};
			String old_text = "";
			while (true) {
				String new_text = GPTComm.getLastText();
				if (!new_text.equals(old_text)) {
					old_text = new_text;
					requireActivity().runOnUiThread(runnable);
				}

				try {
					Thread.sleep(1000);
				} catch (final InterruptedException ignored) {
					return;
				}
			}
		}
	});
}
