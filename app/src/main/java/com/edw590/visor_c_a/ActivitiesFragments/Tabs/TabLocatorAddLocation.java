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
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.fragment.app.Fragment;

import com.edw590.visor_c_a.R;

import SettingsSync.SettingsSync;

/**
 * <p>Fragment that shows the list of the Values Storage values.</p>
 */
public final class TabLocatorAddLocation extends Fragment {

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

		AppCompatCheckBox check_enabled = new AppCompatCheckBox(requireContext());
		check_enabled.setText("Location enabled");
		check_enabled.setChecked(true);

		AppCompatEditText editTxt_type = new AppCompatEditText(requireContext());
		editTxt_type.setHint("Beacon type (\"wifi\" or \"bluetooth\")");
		editTxt_type.setSingleLine();

		AppCompatEditText editTxt_name = new AppCompatEditText(requireContext());
		editTxt_name.setHint("Beacon name (Wi-Fi SSID or Bluetooth device name)");
		editTxt_name.setSingleLine();

		AppCompatEditText editTxt_address = new AppCompatEditText(requireContext());
		editTxt_address.setHint("Beacon address (Wi-Fi BSSID or Bluetooth device address");
		editTxt_address.setSingleLine();

		AppCompatEditText editTxt_last_detection_s = new AppCompatEditText(requireContext());
		editTxt_last_detection_s.setText("0");
		editTxt_last_detection_s.setHint("How long the beacon is not found but user may still be in the location " +
				"(in seconds)");
		editTxt_last_detection_s.setSingleLine();
		editTxt_last_detection_s.setInputType(InputType.TYPE_CLASS_NUMBER);

		AppCompatEditText editTxt_max_distance = new AppCompatEditText(requireContext());
		editTxt_max_distance.setText("0");
		editTxt_max_distance.setHint("Maximum distance from the beacon to the user (in meters)");
		editTxt_max_distance.setSingleLine();
		editTxt_max_distance.setInputType(InputType.TYPE_CLASS_NUMBER);

		AppCompatEditText editTxt_location_name = new AppCompatEditText(requireContext());
		editTxt_location_name.setHint("Location name");
		editTxt_location_name.setSingleLine();

		AppCompatButton btn_add = new AppCompatButton(requireContext());
		btn_add.setText("Add");
		btn_add.setOnClickListener(v -> {
			SettingsSync.addLocationLOCATIONS(
					check_enabled.isChecked(),
					editTxt_type.getText().toString(),
					editTxt_name.getText().toString(),
					editTxt_address.getText().toString(),
					Integer.parseInt(editTxt_last_detection_s.getText().toString()),
					Integer.parseInt(editTxt_max_distance.getText().toString()),
					editTxt_location_name.getText().toString()
			);

			Utils.refreshFragment(this);
		});

		linearLayout.addView(check_enabled);
		linearLayout.addView(editTxt_type);
		linearLayout.addView(editTxt_name);
		linearLayout.addView(editTxt_address);
		linearLayout.addView(editTxt_last_detection_s);
		linearLayout.addView(editTxt_max_distance);
		linearLayout.addView(editTxt_location_name);
		linearLayout.addView(btn_add);
	}
}
