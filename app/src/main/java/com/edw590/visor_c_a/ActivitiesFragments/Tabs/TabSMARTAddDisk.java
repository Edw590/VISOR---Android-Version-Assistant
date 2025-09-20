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
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.fragment.app.Fragment;

import com.edw590.visor_c_a.R;

import SettingsSync.SettingsSync;

public final class TabSMARTAddDisk extends Fragment {

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

		AppCompatEditText editTxt_id = new AppCompatEditText(requireContext());
		editTxt_id.setHint("Disk ID");
		editTxt_id.setSingleLine(true);

		AppCompatCheckBox check_enabled = new AppCompatCheckBox(requireContext());
		check_enabled.setHint("Disk enabled");
		check_enabled.setChecked(true);

		AppCompatEditText editTxt_label = new AppCompatEditText(requireContext());
		editTxt_label.setHint("Disk label");
		editTxt_label.setSingleLine(true);

		AppCompatCheckBox check_is_hdd = new AppCompatCheckBox(requireContext());
		check_is_hdd.setHint("Is it an HDD? (As opposed to an SSD)");
		check_is_hdd.setChecked(true);

		AppCompatButton btn_save = new AppCompatButton(requireContext());
		btn_save.setText("Save");
		btn_save.setOnClickListener(v -> {
			SettingsSync.addDiskSMART(
				editTxt_id.getText().toString(),
				check_enabled.isChecked(),
				editTxt_label.getText().toString(),
				check_is_hdd.isChecked()
			);

			Utils.reloadFragment(this);
		});

		linearLayout.addView(editTxt_id);
		linearLayout.addView(check_enabled);
		linearLayout.addView(editTxt_label);
		linearLayout.addView(check_is_hdd);
		linearLayout.addView(btn_save);
	}
}
