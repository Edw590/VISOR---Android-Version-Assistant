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
public final class TabSMARTDisksList extends Fragment {

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

		String disks_ids_str = SettingsSync.getIdsListSMART();
		if (!disks_ids_str.isEmpty()) {
			String[] disks_ids = disks_ids_str.split("\\|");
			for (final String disk_id : disks_ids) {
				ModsFileInfo.DiskInfo disk = SettingsSync.getDiskSMART(disk_id);
				String title = disk.getLabel();
				if (!disk.getEnabled()) {
					title = "[X] " + title;
				}

				adapter.addItem(title, createDiskSetter(disk));
			}
		}

		// After adding all the values, set the size of the ExpandableListView.
		Utils.setExpandableListViewSize(expandable_list_view);
	}

	private List<View> createDiskSetter(final ModsFileInfo.DiskInfo disk) {
		List<View> child_views = new ArrayList<>(10);

		AppCompatTextView txt_id = new AppCompatTextView(requireContext());
		txt_id.setText("Disk ID: " + disk.getId());

		AppCompatCheckBox check_enabled = new AppCompatCheckBox(requireContext());
		check_enabled.setHint("Disk enabled");
		check_enabled.setChecked(disk.getEnabled());

		AppCompatEditText editTxt_label = new AppCompatEditText(requireContext());
		editTxt_label.setText(disk.getLabel());
		editTxt_label.setHint("Disk label");
		editTxt_label.setSingleLine(true);

		AppCompatCheckBox check_is_hdd = new AppCompatCheckBox(requireContext());
		check_is_hdd.setHint("Is it an HDD? (As opposed to an SSD)");
		check_is_hdd.setChecked(disk.getIs_HDD());

		AppCompatButton btn_save = new AppCompatButton(requireContext());
		btn_save.setText("Save");
		btn_save.setOnClickListener(v -> {
			disk.setEnabled(check_is_hdd.isChecked());
			disk.setLabel(editTxt_label.getText().toString());
			disk.setIs_HDD(check_is_hdd.isChecked());

			Utils.reloadFragment(this);
		});

		AppCompatButton btn_delete = new AppCompatButton(requireContext());
		btn_delete.setText("Delete");
		btn_delete.setOnClickListener(v -> {
			Utils.createConfirmationDialog(requireContext(), "Are you sure you want to delete this disk?",
					() -> {
						SettingsSync.removeDiskSMART(disk.getId());

						Utils.reloadFragment(this);
					});
		});

		child_views.add(txt_id);
		child_views.add(check_enabled);
		child_views.add(editTxt_label);
		child_views.add(check_is_hdd);
		child_views.add(btn_save);
		child_views.add(btn_delete);

		return child_views;
	}
}
