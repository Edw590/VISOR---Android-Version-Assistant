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

import java.text.DateFormat;
import java.text.ParseException;

import SettingsSync.SettingsSync;

/**
 * <p>Fragment that shows the list of the Values Storage values.</p>
 */
public final class TabTasksAddTask extends Fragment {

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
		check_enabled.setText("Task enabled");
		check_enabled.setChecked(true);

		AppCompatCheckBox check_device_active = new AppCompatCheckBox(requireContext());
		check_device_active.setText("Device(s) must be in use");

		AppCompatEditText editTxt_device_ids = new AppCompatEditText(requireContext());
		editTxt_device_ids.setHint("Device IDs where the task is triggered (one per line)");
		editTxt_device_ids.setMaxLines(3);

		AppCompatEditText editTxt_message = new AppCompatEditText(requireContext());
		editTxt_message.setHint("Message to speak when triggered");
		editTxt_message.setSingleLine();

		AppCompatEditText editTxt_command = new AppCompatEditText(requireContext());
		editTxt_command.setHint("Command to execute after speaking");
		editTxt_command.setSingleLine();

		AppCompatEditText editTxt_time = new AppCompatEditText(requireContext());
		editTxt_time.setHint("Time trigger (format: \"2024-12-31 -- 23:59:59\")");
		editTxt_time.setSingleLine();

		AppCompatEditText editTxt_repeat_each_min = new AppCompatEditText(requireContext());
		editTxt_repeat_each_min.setText("0");
		editTxt_repeat_each_min.setHint("Repeat each X minutes");
		editTxt_repeat_each_min.setInputType(InputType.TYPE_CLASS_NUMBER);
		editTxt_repeat_each_min.setSingleLine();

		AppCompatEditText editTxt_location = new AppCompatEditText(requireContext());
		editTxt_location.setHint("User location trigger");
		editTxt_location.setSingleLine();

		AppCompatEditText editTxt_programmable_condition = new AppCompatEditText(requireContext());
		editTxt_programmable_condition.setHint("Programmable condition (in Go)");
		editTxt_programmable_condition.setSingleLine();

		AppCompatButton btn_add = new AppCompatButton(requireContext());
		btn_add.setText("Add");
		btn_add.setOnClickListener(v -> {
			long time = 0;
			try {
				DateFormat dateFormat = DateFormat.getDateTimeInstance();
				time = dateFormat.parse(editTxt_time.getText().toString()).getTime();
			} catch (final ParseException ignored) {
			}
			SettingsSync.addTaskTASKS(
					check_enabled.isChecked(),
					check_device_active.isChecked(),
					editTxt_device_ids.getText().toString(),
					editTxt_message.getText().toString(),
					editTxt_command.getText().toString(),
					time,
					Integer.parseInt(editTxt_repeat_each_min.getText().toString()),
					editTxt_location.getText().toString(),
					editTxt_programmable_condition.getText().toString()
			);

			Utils.reloadFragment(this);
		});

		linearLayout.addView(check_enabled);
		linearLayout.addView(check_device_active);
		linearLayout.addView(editTxt_device_ids);
		linearLayout.addView(editTxt_message);
		linearLayout.addView(editTxt_command);
		linearLayout.addView(editTxt_time);
		linearLayout.addView(editTxt_repeat_each_min);
		linearLayout.addView(editTxt_location);
		linearLayout.addView(editTxt_programmable_condition);
		linearLayout.addView(btn_add);
	}
}
