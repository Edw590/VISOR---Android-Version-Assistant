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

public final class TabLocatorSettings extends Fragment {

	@Nullable
	@Override
	public View onCreateView(@android.annotation.NonNull final LayoutInflater inflater,
								   @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.nested_scroll_view, container, false);
	}

	@Override
	public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		ModsFileInfo.Mod12UserInfo mod_12_user_info = SettingsSync.getMod12InfoUSERSETS();

		int padding = Utils.getDefaultPadding(requireContext());
		LinearLayout linearLayout = view.findViewById(R.id.nested_scroll_view_linear_layout);
		linearLayout.setPadding(padding, padding, padding, padding);

		AppCompatEditText editTxt_always_with_device = new AppCompatEditText(requireContext());
		editTxt_always_with_device.setHint("ID of the device always with the user (user's phone for example) or " +
				"empty if none");
		editTxt_always_with_device.setText(mod_12_user_info.getAlwaysWith_device());
		editTxt_always_with_device.setSingleLine();

		AppCompatButton btn_save = new AppCompatButton(requireContext());
		btn_save.setText("Save");
		btn_save.setOnClickListener(v -> {
			mod_12_user_info.setAlwaysWith_device(editTxt_always_with_device.getText().toString());
		});

		linearLayout.addView(editTxt_always_with_device);
		linearLayout.addView(btn_save);
	}
}
