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
import android.widget.Button;
import android.widget.EditText;
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
 * Use the {@link TabHomeLocalSettings#newInstance} factory method to
 * create an instance of this fragment.
 */
public final class TabHomeLocalSettings extends Fragment {

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

		ModsFileInfo.DeviceSettings device_settings = SettingsSync.getDeviceSettingsGENERAL();

		final LinearLayout linearLayout = view.findViewById(R.id.nested_scroll_view_linear_layout);

		AppCompatEditText editTxt_password = new AppCompatEditText(requireContext());
		editTxt_password.setHint("Settings encryption password or empty to disable");
		editTxt_password.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
		// TODO

		AppCompatButton btn_save_temp = new AppCompatButton(requireContext());
		btn_save_temp.setText("Save for this session");
		btn_save_temp.setOnClickListener(v -> {
			// TODO
		});
		AppCompatButton btn_save_perm = new AppCompatButton(requireContext());
		btn_save_perm.setText("Save permanently");
		btn_save_perm.setOnClickListener(v -> {
			// TODO
		});

		AppCompatEditText editTxt_device_id = new AppCompatEditText(requireContext());
		editTxt_device_id.setHint("Unique device ID (for example \"MyPhone\")");
		editTxt_device_id.setText(device_settings.getId());
		editTxt_device_id.setSingleLine();

		AppCompatEditText editTxt_device_type = new AppCompatEditText(requireContext());
		editTxt_device_type.setHint("Device type (for example \"phone\")");
		editTxt_device_type.setText(device_settings.getType_());
		editTxt_device_type.setSingleLine();

		AppCompatEditText editTxt_device_description = new AppCompatEditText(requireContext());
		editTxt_device_description.setHint("Device description (for example the model, \"BV9500\")");
		editTxt_device_description.setText(device_settings.getDescription());
		editTxt_device_description.setSingleLine();

		AppCompatButton btn_save = new AppCompatButton(requireContext());
		btn_save.setText("Save");
		btn_save.setOnClickListener(v -> {
			device_settings.setId(editTxt_device_id.getText().toString());
			device_settings.setType_(editTxt_device_type.getText().toString());
			device_settings.setDescription(editTxt_device_description.getText().toString());
		});

		linearLayout.addView(editTxt_password);
		linearLayout.addView(btn_save_temp);
		linearLayout.addView(btn_save_perm);
		linearLayout.addView(editTxt_device_id);
		linearLayout.addView(editTxt_device_type);
		linearLayout.addView(editTxt_device_description);
		linearLayout.addView(btn_save);
	}
}
