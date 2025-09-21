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
import androidx.fragment.app.Fragment;

import com.edw590.visor_c_a.Modules.Speech.Speech2;
import com.edw590.visor_c_a.Modules.Speech.UtilsSpeech2BC;
import com.edw590.visor_c_a.R;

public final class TabSpeechMain extends Fragment {

	@Nullable
	@Override
	public View onCreateView(@android.annotation.NonNull final LayoutInflater inflater,
								   @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.nested_scroll_view, container, false);
	}

	@Override
	public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		int padding = Utils.getDefaultPadding(requireContext());
		LinearLayout linearLayout = view.findViewById(R.id.nested_scroll_view_linear_layout);
		linearLayout.setPadding(padding, padding, padding, padding);

		AppCompatEditText editTxt_to_speak = new AppCompatEditText(requireContext());
		editTxt_to_speak.setHint("Enter text to speak");
		editTxt_to_speak.setText("This is an example.");

		AppCompatButton btn_speak_min = new AppCompatButton(requireContext());
		btn_speak_min.setText("Speak (min priority)");
		btn_speak_min.setOnClickListener(v -> {
			String speak = editTxt_to_speak.getText().toString();
			UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_LOW, 0, UtilsSpeech2BC.SESSION_TYPE_NONE, false, null);
		});

		AppCompatButton btn_speak_high = new AppCompatButton(requireContext());
		btn_speak_high.setText("Speak (high priority)");
		btn_speak_high.setOnClickListener(v -> {
			String speak = editTxt_to_speak.getText().toString();
			UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_HIGH, 0, UtilsSpeech2BC.SESSION_TYPE_NONE, false, null);
		});

		AppCompatButton btn_speak_critical = new AppCompatButton(requireContext());
		btn_speak_critical.setText("Speak (critical priority)");
		btn_speak_critical.setOnClickListener(v -> {
			String speak = editTxt_to_speak.getText().toString();
			UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_CRITICAL, 0, UtilsSpeech2BC.SESSION_TYPE_NONE, false, null);
		});

		AppCompatButton btn_skip_speech = new AppCompatButton(requireContext());
		btn_skip_speech.setText("Skip current speech");
		btn_skip_speech.setOnClickListener(v -> {
			UtilsSpeech2BC.skipCurrentSpeech();
		});

		linearLayout.addView(editTxt_to_speak);
		linearLayout.addView(btn_speak_min);
		linearLayout.addView(btn_speak_high);
		linearLayout.addView(btn_speak_critical);
		linearLayout.addView(btn_skip_speech);
	}
}
