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

import com.edw590.visor_c_a.R;

import SettingsSync.SettingsSync;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TabCommunicatorSettings#newInstance} factory method to
 * create an instance of this fragment.
 */
public final class TabCommunicatorSettings extends Fragment {

	@Nullable
	@Override
	public View onCreateView(@android.annotation.NonNull final LayoutInflater inflater,
								   @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.nested_scroll_view, container, false);
	}

	@Override
	public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		ModsFileInfo.Mod7UserInfo mod_7_user_info = SettingsSync.getMod7InfoUSERSETS();

		LinearLayout linearLayout = view.findViewById(R.id.nested_scroll_view_linear_layout);

		AppCompatEditText editTxt_smart_model_loc = new AppCompatEditText(requireContext());
		editTxt_smart_model_loc.setHint("GGUF location for the smart LLM (used for normal conversation)");
		editTxt_smart_model_loc.setText(mod_7_user_info.getModel_smart_loc());
		editTxt_smart_model_loc.setSingleLine();

		AppCompatEditText editTxt_dumb_model_loc = new AppCompatEditText(requireContext());
		editTxt_dumb_model_loc.setHint("GGUF location for the dumb LLM (used to summarize things)");
		editTxt_dumb_model_loc.setText(mod_7_user_info.getModel_dumb_loc());
		editTxt_dumb_model_loc.setSingleLine();

		AppCompatEditText editTxt_system_info = new AppCompatEditText(requireContext());
		editTxt_system_info.setHint("LLM system information");
		editTxt_system_info.setText(mod_7_user_info.getSystem_info());
		editTxt_system_info.setSingleLine();

		AppCompatEditText editTxt_user_nickname = new AppCompatEditText(requireContext());
		editTxt_user_nickname.setHint("User nickname");
		editTxt_user_nickname.setText(mod_7_user_info.getUser_nickname());
		editTxt_user_nickname.setSingleLine();

		AppCompatButton btn_save = new AppCompatButton(requireContext());
		btn_save.setText("Save");
		btn_save.setOnClickListener(v -> {
			mod_7_user_info.setModel_smart_loc(editTxt_smart_model_loc.getText().toString());
			mod_7_user_info.setModel_dumb_loc(editTxt_dumb_model_loc.getText().toString());
			mod_7_user_info.setSystem_info(editTxt_system_info.getText().toString());
			mod_7_user_info.setUser_nickname(editTxt_user_nickname.getText().toString());
		});

		linearLayout.addView(editTxt_smart_model_loc);
		linearLayout.addView(editTxt_dumb_model_loc);
		linearLayout.addView(editTxt_system_info);
		linearLayout.addView(editTxt_user_nickname);
		linearLayout.addView(btn_save);
	}
}
