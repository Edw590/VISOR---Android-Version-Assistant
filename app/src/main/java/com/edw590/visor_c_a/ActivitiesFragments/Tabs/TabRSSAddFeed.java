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

public final class TabRSSAddFeed extends Fragment {

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
		check_enabled.setText("Feed enabled");
		check_enabled.setChecked(true);

		AppCompatEditText editTxt_name = new AppCompatEditText(requireContext());
		editTxt_name.setHint("Feed name");
		editTxt_name.setSingleLine();

		AppCompatEditText editTxt_type = new AppCompatEditText(requireContext());
		editTxt_type.setHint("Feed type (\"General\" or \"YouTube [CH|PL] [+S]\")");
		editTxt_type.setSingleLine();

		AppCompatEditText editTxt_url = new AppCompatEditText(requireContext());
		editTxt_url.setHint("Feed URL or YouTube playlist/channel ID");
		editTxt_url.setSingleLine();

		AppCompatEditText editTxt_custom_msg_subject = new AppCompatEditText(requireContext());
		editTxt_custom_msg_subject.setHint("Custom message subject (for YT it's automatic)");
		editTxt_custom_msg_subject.setSingleLine();

		AppCompatButton btn_add = new AppCompatButton(requireContext());
		btn_add.setText("Add");
		btn_add.setOnClickListener(v -> {
			SettingsSync.addFeedRSS(check_enabled.isChecked(), editTxt_name.getText().toString(),
					editTxt_url.getText().toString(), editTxt_type.getText().toString(),
					editTxt_custom_msg_subject.getText().toString());

			Utils.reloadFragment(this);
		});

		linearLayout.addView(check_enabled);
		linearLayout.addView(editTxt_name);
		linearLayout.addView(editTxt_type);
		linearLayout.addView(editTxt_url);
		linearLayout.addView(editTxt_custom_msg_subject);
		linearLayout.addView(btn_add);
	}
}
