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

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.edw590.visor_c_a.R;

import SettingsSync.SettingsSync;
import UtilsSWA.UtilsSWA;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TabHomeMain#newInstance} factory method to
 * create an instance of this fragment.
 */
public final class TabHomeMain extends Fragment {

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

		final LinearLayout linearLayout = view.findViewById(R.id.nested_scroll_view_linear_layout);

		String color_primary = "#" + Integer.toHexString(ContextCompat.getColor(requireActivity(),
				R.color.colorPrimary));
		String color_accent = "#" + Integer.toHexString(ContextCompat.getColor(requireActivity(),
				R.color.colorAccent));

		TextView txt_title = new TextView(requireContext());
		txt_title.setText("V.I.S.O.R. Systems");
		txt_title.setTextColor(Color.parseColor(color_accent));
		txt_title.setTextSize(40);
		txt_title.setTypeface(null, Typeface.BOLD);
		txt_title.setGravity(Gravity.CENTER);
		txt_title.setHeight(200);

		TextView txt_comm_connected = new TextView(requireContext());
		txt_comm_connected.setTextColor(Color.parseColor(color_primary));
		txt_comm_connected.setTextSize(20);
		txt_comm_connected.setPadding(20, 20, 20, 20);

		TextView txt_site_info_exists = new TextView(requireContext());
		txt_site_info_exists.setPadding(20, 20, 20, 20);

		String color;
		String text;
		if (UtilsSWA.isCommunicatorConnectedSERVER()) {
			color = color_accent;
			text = "Connected to the server";
		} else {
			color = color_primary;
			text = "Not connected to the server";
		}
		txt_comm_connected.setText(text);
		txt_comm_connected.setTextColor(Color.parseColor(color));

		if (SettingsSync.isWebsiteInfoEmpty()) {
			txt_site_info_exists.setText("No server info exists. Enter it to activate full functionality.");
		} else {
			txt_site_info_exists.setText("Server info exists");
		}

		linearLayout.addView(txt_title);
		linearLayout.addView(txt_comm_connected);
		linearLayout.addView(txt_site_info_exists);
	}
}
