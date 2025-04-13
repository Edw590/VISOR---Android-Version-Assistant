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
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.SwitchCompat;
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

		AppCompatEditText editTxt_server_url = new AppCompatEditText(requireContext());
		editTxt_server_url.setHint("GPT Server URL (example: localhost:11434)");
		editTxt_server_url.setText(mod_7_user_info.getServer_url());
		editTxt_server_url.setSingleLine();

		AppCompatEditText editTxt_models_to_use = new AppCompatEditText(requireContext());
		editTxt_models_to_use.setHint("GPT model names and types one per line in order of preference\n" +
				"Example: \"llama3.2 - TEXT\" - can be TEXT or VISION)");
		editTxt_models_to_use.setText(mod_7_user_info.getModels_to_use());

		SwitchCompat switch_model_has_tool_role = new SwitchCompat(requireContext());
		switch_model_has_tool_role.setText("Is the tool role available for the model?");
		switch_model_has_tool_role.setChecked(mod_7_user_info.getModel_has_tool_role());

		SwitchCompat switch_prioritize_clients = new SwitchCompat(requireContext());
		switch_prioritize_clients.setText("Give priority to models on the clients?");
		switch_prioritize_clients.setChecked(mod_7_user_info.getPrioritize_clients_models());

		AppCompatEditText editTxt_ctx_size = new AppCompatEditText(requireContext());
		editTxt_ctx_size.setHint("GPT context size (example: 4096)");
		editTxt_ctx_size.setText(Integer.toString(mod_7_user_info.getContext_size()));
		editTxt_ctx_size.setSingleLine();
		editTxt_ctx_size.setInputType(InputType.TYPE_CLASS_NUMBER);

		AppCompatEditText editTxt_temperature = new AppCompatEditText(requireContext());
		editTxt_temperature.setHint("GPT temperature (example: 0.8)");
		editTxt_temperature.setText(Float.toString(mod_7_user_info.getTemperature()));
		editTxt_temperature.setSingleLine();
		editTxt_temperature.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

		AppCompatEditText editTxt_system_info = new AppCompatEditText(requireContext());
		editTxt_system_info.setHint("LLM system information");
		editTxt_system_info.setText(mod_7_user_info.getSystem_info());

		AppCompatEditText editTxt_user_nickname = new AppCompatEditText(requireContext());
		editTxt_user_nickname.setHint("User nickname (Sir, for example)");
		editTxt_user_nickname.setText(mod_7_user_info.getUser_nickname());
		editTxt_user_nickname.setSingleLine();

		AppCompatButton btn_save = new AppCompatButton(requireContext());
		btn_save.setText("Save");
		btn_save.setOnClickListener(v -> {
			mod_7_user_info.setServer_url(editTxt_server_url.getText().toString());
			mod_7_user_info.setModels_to_use(editTxt_models_to_use.getText().toString());
			mod_7_user_info.setModel_has_tool_role(switch_model_has_tool_role.isChecked());
			mod_7_user_info.setPrioritize_clients_models(switch_prioritize_clients.isChecked());
			mod_7_user_info.setContext_size(Integer.parseInt(editTxt_ctx_size.getText().toString()));
			mod_7_user_info.setTemperature(Float.parseFloat(editTxt_temperature.getText().toString()));
			mod_7_user_info.setSystem_info(editTxt_system_info.getText().toString());
			mod_7_user_info.setUser_nickname(editTxt_user_nickname.getText().toString());
		});

		linearLayout.addView(editTxt_server_url);
		linearLayout.addView(editTxt_models_to_use);
		linearLayout.addView(switch_model_has_tool_role);
		linearLayout.addView(switch_prioritize_clients);
		linearLayout.addView(editTxt_ctx_size);
		linearLayout.addView(editTxt_temperature);
		linearLayout.addView(editTxt_system_info);
		linearLayout.addView(editTxt_user_nickname);
		linearLayout.addView(btn_save);
	}
}
