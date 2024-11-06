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
import androidx.fragment.app.Fragment;

import com.edw590.visor_c_a.R;

import java.util.ArrayList;
import java.util.List;

import SettingsSync.SettingsSync;

/**
 * <p>Fragment that shows the list of the Values Storage values.</p>
 */
public final class TabLocatorLocationsList extends Fragment {

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

		String[] locs_info_ids = SettingsSync.getIdsListLOCATIONS().split("\\|");
		for (final String loc_info_id : locs_info_ids) {
			ModsFileInfo.LocInfo loc_info = SettingsSync.getLocationLOCATIONS(Integer.parseInt(loc_info_id));
			String title = loc_info.getName();
			if (title.isEmpty()) {
				title = loc_info.getAddress();
			}
			title = loc_info.getLocation() + " - " + title;
			if (!loc_info.getEnabled()) {
				title = "[X] " + title;
			}

			adapter.addItem(title, createLocationSetter(loc_info));
		}

		// After adding all the values, set the size of the ExpandableListView.
		Utils.setExpandableListViewSize(expandable_list_view);
	}

	private List<View> createLocationSetter(final ModsFileInfo.LocInfo loc_info) {
		List<View> child_views = new ArrayList<>(10);

		AppCompatCheckBox check_enabled = new AppCompatCheckBox(requireContext());
		check_enabled.setText("Location enabled");
		check_enabled.setChecked(loc_info.getEnabled());

		AppCompatEditText editTxt_type = new AppCompatEditText(requireContext());
		editTxt_type.setText(loc_info.getType());
		editTxt_type.setHint("Beacon type (\"wifi\" or \"bluetooth\")");
		editTxt_type.setSingleLine();

		AppCompatEditText editTxt_name = new AppCompatEditText(requireContext());
		editTxt_name.setText(loc_info.getName());
		editTxt_name.setHint("Beacon name (Wi-Fi SSID or Bluetooth device name)");
		editTxt_name.setSingleLine();

		AppCompatEditText editTxt_address = new AppCompatEditText(requireContext());
		editTxt_address.setText(loc_info.getAddress());
		editTxt_address.setHint("Beacon address (Wi-Fi BSSID or Bluetooth device address");
		editTxt_address.setSingleLine();

		AppCompatEditText editTxt_last_detection_s = new AppCompatEditText(requireContext());
		editTxt_last_detection_s.setText(String.valueOf(loc_info.getLast_detection_s()));
		editTxt_last_detection_s.setHint("How long the beacon is not found but user may still be in the location " +
				"(in seconds)");
		editTxt_last_detection_s.setSingleLine();
		editTxt_last_detection_s.setInputType(InputType.TYPE_CLASS_NUMBER);

		AppCompatEditText editTxt_max_distance = new AppCompatEditText(requireContext());
		editTxt_max_distance.setText(String.valueOf(loc_info.getMax_distance_m()));
		editTxt_max_distance.setHint("Maximum distance from the beacon to the user (in meters)");
		editTxt_max_distance.setSingleLine();
		editTxt_max_distance.setInputType(InputType.TYPE_CLASS_NUMBER);

		AppCompatEditText editTxt_location_name = new AppCompatEditText(requireContext());
		editTxt_location_name.setText(loc_info.getLocation());
		editTxt_location_name.setHint("Location name");
		editTxt_location_name.setSingleLine();

		AppCompatButton btn_save = new AppCompatButton(requireContext());
		btn_save.setText("Save");
		btn_save.setOnClickListener(v -> {
			loc_info.setEnabled(check_enabled.isChecked());
			loc_info.setType(editTxt_type.getText().toString());
			loc_info.setName(editTxt_name.getText().toString());
			loc_info.setAddress(editTxt_address.getText().toString());
			loc_info.setLast_detection_s(Integer.parseInt(editTxt_last_detection_s.getText().toString()));
			loc_info.setMax_distance_m(Integer.parseInt(editTxt_max_distance.getText().toString()));
			loc_info.setLocation(editTxt_location_name.getText().toString());

			Utils.refreshFragment(this);
		});

		AppCompatButton btn_delete = new AppCompatButton(requireContext());
		btn_delete.setText("Delete");
		btn_delete.setOnClickListener(v -> {
			Utils.createConfirmation(requireContext(), "Are you sure you want to delete this location?",
					() -> {
						SettingsSync.removeLocationLOCATIONS(loc_info.getId());

						Utils.refreshFragment(this);
					});
		});

		child_views.add(check_enabled);
		child_views.add(editTxt_type);
		child_views.add(editTxt_name);
		child_views.add(editTxt_address);
		child_views.add(editTxt_last_detection_s);
		child_views.add(editTxt_max_distance);
		child_views.add(editTxt_location_name);
		child_views.add(btn_save);
		child_views.add(btn_delete);

		return child_views;
	}
}
