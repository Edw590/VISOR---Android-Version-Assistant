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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.edw590.visor_c_a.GlobalUtils.UtilsContext;
import com.edw590.visor_c_a.GlobalUtils.UtilsPermsAuths;
import com.edw590.visor_c_a.GlobalUtils.UtilsProcesses;
import com.edw590.visor_c_a.R;

import SettingsSync.SettingsSync;
import UtilsSWA.UtilsSWA;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TabHomeMain#newInstance} factory method to
 * create an instance of this fragment.
 */
public final class TabHomeMain extends Fragment {

	private static final int REQUEST_CODE = 1234;

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

		String color_primary = "#" + Integer.toHexString(ContextCompat.getColor(requireActivity(),
				R.color.colorPrimary));
		String color_accent = "#" + Integer.toHexString(ContextCompat.getColor(requireActivity(),
				R.color.colorAccent));

		AppCompatTextView txt_title = new AppCompatTextView(requireContext());
		txt_title.setText("V.I.S.O.R. Systems");
		txt_title.setTextColor(Color.parseColor(color_accent));
		txt_title.setTextSize(40);
		txt_title.setTypeface(null, Typeface.BOLD);
		txt_title.setGravity(Gravity.CENTER);
		txt_title.setHeight(200);

		AppCompatTextView txt_comm_connected = new AppCompatTextView(requireContext());
		txt_comm_connected.setTextColor(Color.parseColor(color_primary));
		txt_comm_connected.setTextSize(20);
		txt_comm_connected.setPadding(20, 20, 20, 20);

		AppCompatTextView txt_site_info_exists = new AppCompatTextView(requireContext());
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

		AppCompatButton btn_perms = new AppCompatButton(requireContext());
		btn_perms.setText("Click here and on Back until you see the Desktop and nothing left to authorize");
		btn_perms.setOnClickListener(v -> {
			// Request all missing permissions
			final int perms_left = UtilsPermsAuths.checkRequestPerms(getActivity(), true);
			UtilsPermsAuths.warnPermissions(perms_left, true);

			// Request all missing authorizations
			final int auths_left = UtilsPermsAuths.checkRequestAuths(UtilsPermsAuths.ALSO_REQUEST);
			UtilsPermsAuths.warnAuthorizations(auths_left, true);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				// Request screen capture permission
				MediaProjectionManager media_projection_manager = (MediaProjectionManager) UtilsContext.getContext()
						.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
				startActivityForResult(media_projection_manager.createScreenCaptureIntent(), REQUEST_CODE);
			}
		});

		AppCompatButton btn_device_admin = new AppCompatButton(requireContext());
		btn_device_admin.setText("Open the Device Admins list");
		btn_device_admin.setOnClickListener(v -> {
			startActivity(new Intent().setComponent(new ComponentName("com.android.settings",
					"com.android.settings.DeviceAdminSettings")));
			// Didn't find any constants for these 2 strings above
		});

		AppCompatButton btn_force_stop = new AppCompatButton(requireContext());
		btn_force_stop.setText("Terminate the app");
		btn_force_stop.setOnClickListener(v -> {
			UtilsProcesses.killPID(UtilsProcesses.getCurrentPID());
		});

		linearLayout.addView(txt_title);
		linearLayout.addView(txt_comm_connected);
		linearLayout.addView(txt_site_info_exists);
		linearLayout.addView(btn_perms);
		linearLayout.addView(btn_device_admin);
		linearLayout.addView(btn_force_stop);
	}
}
