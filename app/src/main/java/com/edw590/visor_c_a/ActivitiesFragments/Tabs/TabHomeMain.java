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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
		return inflater.inflate(R.layout.tab_home_home, container, false);
	}

	@Override
	public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		String color_primary = "#" + Integer.toHexString(ContextCompat.getColor(requireActivity(),
				R.color.colorPrimary));
		String color_accent = "#" + Integer.toHexString(ContextCompat.getColor(requireActivity(),
				R.color.colorAccent));

		TextView txt_comm_connected = requireView().findViewById(R.id.txt_comm_connected);
		EditText editTxt_site_domain = requireView().findViewById(R.id.editTxt_site_domain);
		EditText editTxt_site_password = requireView().findViewById(R.id.editTxt_site_password);
		Button btn_save_site_info = requireView().findViewById(R.id.btn_save_site_info);
		TextView txt_site_info_exists = requireView().findViewById(R.id.txt_site_info_exists);
		assert txt_comm_connected != null;
		assert editTxt_site_domain != null;
		assert editTxt_site_password != null;
		assert btn_save_site_info != null;
		assert txt_site_info_exists != null;

		btn_save_site_info.setOnClickListener(v -> {
			SettingsSync.setWebsiteInfo(editTxt_site_domain.getText().toString(),
					editTxt_site_password.getText().toString());
			editTxt_site_domain.setText("");
			editTxt_site_password.setText("");
		});

		String color;
		String text;
		if (UtilsSWA.isCommunicatorConnectedSERVER()) {
			color = color_accent;
			text = "Communicator connected";
		} else {
			color = color_primary;
			text = "Communicator not connected";
		}
		txt_comm_connected.setText(text);
		txt_comm_connected.setTextColor(Color.parseColor(color));

		if (SettingsSync.isWebsiteInfoEmpty()) {
			editTxt_site_domain.setEnabled(true);
			editTxt_site_password.setEnabled(true);
			btn_save_site_info.setEnabled(true);
			txt_site_info_exists.setText("No website info exists. Please enter it to activate full functionality.");
		} else {
			editTxt_site_domain.setEnabled(false);
			editTxt_site_password.setEnabled(false);
			btn_save_site_info.setEnabled(false);
			txt_site_info_exists.setText("Website info exists");
		}
	}
}
