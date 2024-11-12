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
import android.widget.ExpandableListView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

import com.edw590.visor_c_a.R;

import java.util.ArrayList;
import java.util.List;

import SettingsSync.SettingsSync;

/**
 * <p>Fragment that shows the list of the Values Storage values.</p>
 */
public final class TabTasksTasksList extends Fragment {

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

		ExpandableListView expandable_list_view = new ExpandableListView(requireContext());
		GenericExpandableListAdapter adapter = new GenericExpandableListAdapter(requireContext());
		expandable_list_view.setAdapter(adapter);
		expandable_list_view.setLayoutParams(linearLayout.getLayoutParams());
		expandable_list_view.setOnGroupCollapseListener(groupPosition -> {
			Utils.setExpandableListViewSize(expandable_list_view);
		});
		expandable_list_view.setOnGroupExpandListener(groupPosition -> {
			Utils.setExpandableListViewSize(expandable_list_view);
		});

		linearLayout.addView(expandable_list_view);

		String[] tasks_ids = SettingsSync.getIdsListTASKS().split("\\|");
		for (final String feed_id : tasks_ids) {
			ModsFileInfo.Task task = SettingsSync.getTaskTASKS(Integer.parseInt(feed_id));
			String title = task.getMessage();
			if (title.isEmpty()) {
				title = task.getCommand();
			}
			if (!task.getEnabled()) {
				title = "[X] " + title;
			}

			adapter.addItem(title, createTaskSetter(task));
		}

		// After adding all the values, set the size of the ExpandableListView.
		Utils.setExpandableListViewSize(expandable_list_view);
	}

	private List<View> createTaskSetter(final ModsFileInfo.Task task) {
		List<View> child_views = new ArrayList<>(10);

		AppCompatTextView txt_id = new AppCompatTextView(requireContext());
		txt_id.setText("Task ID: " + task.getId());

		AppCompatCheckBox check_enabled = new AppCompatCheckBox(requireContext());
		check_enabled.setText("Task enabled");
		check_enabled.setChecked(task.getEnabled());

		AppCompatCheckBox check_device_active = new AppCompatCheckBox(requireContext());
		check_device_active.setText("Device(s) must be in use");
		check_device_active.setChecked(task.getDevice_active());

		AppCompatEditText editTxt_device_ids = new AppCompatEditText(requireContext());
		editTxt_device_ids.setText(task.getDeviceIDs());
		editTxt_device_ids.setHint("Device IDs where the task is triggered (one per line)");
		editTxt_device_ids.setSingleLine(false);
		editTxt_device_ids.setMaxLines(3);

		AppCompatEditText editTxt_message = new AppCompatEditText(requireContext());
		editTxt_message.setText(task.getMessage());
		editTxt_message.setHint("Message to speak when triggered");
		editTxt_message.setSingleLine();

		AppCompatEditText editTxt_command = new AppCompatEditText(requireContext());
		editTxt_command.setText(task.getCommand());
		editTxt_command.setHint("Command to execute after speaking");
		editTxt_command.setSingleLine();

		AppCompatEditText editTxt_time = new AppCompatEditText(requireContext());
		editTxt_time.setText(task.getTime());
		editTxt_time.setHint("Time trigger (format: \"2024-12-31 -- 23:59:59\")");
		editTxt_time.setSingleLine();

		AppCompatEditText editTxt_repeat_each_min = new AppCompatEditText(requireContext());
		editTxt_repeat_each_min.setText(String.valueOf(task.getRepeat_each_min()));
		editTxt_repeat_each_min.setHint("Repeat each X minutes");
		editTxt_repeat_each_min.setInputType(InputType.TYPE_CLASS_NUMBER);
		editTxt_repeat_each_min.setSingleLine();

		AppCompatEditText editTxt_location = new AppCompatEditText(requireContext());
		editTxt_location.setText(task.getUser_location());
		editTxt_location.setHint("User location trigger");
		editTxt_location.setSingleLine();

		AppCompatEditText editTxt_programmable_condition = new AppCompatEditText(requireContext());
		editTxt_programmable_condition.setText(task.getProgrammable_condition());
		editTxt_programmable_condition.setHint("Programmable condition (in Go)");
		editTxt_programmable_condition.setSingleLine();

		AppCompatButton btn_save = new AppCompatButton(requireContext());
		btn_save.setText("Save");
		btn_save.setOnClickListener(v -> {
			task.setEnabled(check_enabled.isChecked());
			task.setDevice_active(check_device_active.isChecked());
			task.setDeviceIDs(editTxt_device_ids.getText().toString());
			task.setMessage(editTxt_message.getText().toString());
			task.setCommand(editTxt_command.getText().toString());
			task.setTime(editTxt_time.getText().toString());
			task.setRepeat_each_min(Integer.parseInt(editTxt_repeat_each_min.getText().toString()));
			task.setUser_location(editTxt_location.getText().toString());
			task.setProgrammable_condition(editTxt_programmable_condition.getText().toString());

			Utils.refreshFragment(this);
		});

		AppCompatButton btn_delete = new AppCompatButton(requireContext());
		btn_delete.setText("Delete");
		btn_delete.setOnClickListener(v -> {
			Utils.createConfirmation(requireContext(), "Are you sure you want to delete this task?",
					() -> {
						SettingsSync.removeTaskTASKS(task.getId());

						Utils.refreshFragment(this);
					});
		});

		child_views.add(txt_id);
		child_views.add(check_enabled);
		child_views.add(check_device_active);
		child_views.add(editTxt_device_ids);
		child_views.add(editTxt_message);
		child_views.add(editTxt_command);
		child_views.add(editTxt_time);
		child_views.add(editTxt_repeat_each_min);
		child_views.add(editTxt_location);
		child_views.add(editTxt_programmable_condition);
		child_views.add(btn_save);
		child_views.add(btn_delete);

		return child_views;
	}
}
