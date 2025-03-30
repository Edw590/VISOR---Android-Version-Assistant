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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TabOICSettings#newInstance} factory method to
 * create an instance of this fragment.
 */
public final class TabOICSettings extends Fragment {

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

		LinearLayout linearLayout = view.findViewById(R.id.nested_scroll_view_linear_layout);

		AppCompatEditText editTxt_weather_locs = new AppCompatEditText(requireContext());
		editTxt_weather_locs.setHint("The weather locations to check, one per line");
		editTxt_weather_locs.setText(SettingsSync.getTempLocsOIC());
		editTxt_weather_locs.setMaxLines(3);

		AppCompatEditText editTxt_news_locs = new AppCompatEditText(requireContext());
		editTxt_news_locs.setHint("The news locations to check, one per line");
		editTxt_news_locs.setText(SettingsSync.getNewsLocsOIC());
		editTxt_news_locs.setMaxLines(3);

		AppCompatButton btn_save = new AppCompatButton(requireContext());
		btn_save.setText("Save");
		btn_save.setOnClickListener(v -> {
			SettingsSync.setTempLocsOIC(editTxt_weather_locs.getText().toString());
			SettingsSync.setNewsLocsOIC(editTxt_news_locs.getText().toString());
		});

		linearLayout.addView(editTxt_weather_locs);
		linearLayout.addView(editTxt_news_locs);
		linearLayout.addView(btn_save);
	}
}
