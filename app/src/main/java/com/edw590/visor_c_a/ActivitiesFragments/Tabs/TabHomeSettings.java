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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.edw590.visor_c_a.R;

import SettingsSync.SettingsSync;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TabHomeSettings#newInstance} factory method to
 * create an instance of this fragment.
 */
public final class TabHomeSettings extends Fragment {

	@Nullable
	@Override
	public View onCreateView(@android.annotation.NonNull final LayoutInflater inflater,
								   @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.empty, container, false);
	}

	@Override
	public void onResume() {
		super.onResume();

		System.out.println("RRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR");
	}

	@Override
	public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		System.out.println("IIIIIIIIIIIIIIIIIIIIIIIIII");

		ModsFileInfo.GeneralConsts general_consts = SettingsSync.getInfoGENERAL();

		final LinearLayout linearLayout = view.findViewById(R.id.empty_linear_layout);

		EditText editTxt_pin = new EditText(requireContext());
		editTxt_pin.setHint("App protection PIN (any number of digits or empty to disable)");
		editTxt_pin.setText(general_consts.getPin());
		editTxt_pin.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);

		EditText editTxt_visor_email_addr = new EditText(requireContext());
		editTxt_visor_email_addr.setHint("V.I.S.O.R. email address");
		editTxt_visor_email_addr.setText(general_consts.getVISOR_email_addr());
		editTxt_visor_email_addr.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

		EditText editTxt_visor_email_pw = new EditText(requireContext());
		editTxt_visor_email_pw.setHint("V.I.S.O.R. email password");
		editTxt_visor_email_pw.setText(general_consts.getVISOR_email_pw());
		editTxt_visor_email_pw.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

		EditText editTxt_user_email_addr = new EditText(requireContext());
		editTxt_user_email_addr.setHint("User email address (used for all communication)");
		editTxt_user_email_addr.setText(general_consts.getUser_email_addr());
		editTxt_user_email_addr.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

		EditText editTxt_server_domain = new EditText(requireContext());
		editTxt_server_domain.setHint("Server domain or IP");
		editTxt_server_domain.setText(general_consts.getWebsite_domain());
		editTxt_server_domain.setSingleLine();

		EditText editTxt_server_pw = new EditText(requireContext());
		editTxt_server_pw.setHint("Server password");
		editTxt_server_pw.setText(general_consts.getWebsite_pw());
		editTxt_server_pw.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

		EditText editTxt_wolframalpha_appid = new EditText(requireContext());
		editTxt_wolframalpha_appid.setHint("WolframAlpha App ID");
		editTxt_wolframalpha_appid.setText(general_consts.getWolframAlpha_AppID());
		editTxt_wolframalpha_appid.setSingleLine();

		EditText editTxt_picovoice_api_key = new EditText(requireContext());
		editTxt_picovoice_api_key.setHint("Picovoice API key");
		editTxt_picovoice_api_key.setText(general_consts.getPicovoice_API_key());
		editTxt_picovoice_api_key.setSingleLine();

		Button btn_save = new Button(requireContext());
		btn_save.setText("Save");
		btn_save.setOnClickListener(v -> {
			general_consts.setPin(editTxt_pin.getText().toString());
			general_consts.setVISOR_email_addr(editTxt_visor_email_addr.getText().toString());
			general_consts.setVISOR_email_pw(editTxt_visor_email_pw.getText().toString());
			general_consts.setUser_email_addr(editTxt_user_email_addr.getText().toString());
			general_consts.setWebsite_domain(editTxt_server_domain.getText().toString());
			general_consts.setWebsite_pw(editTxt_server_pw.getText().toString());
			general_consts.setWolframAlpha_AppID(editTxt_wolframalpha_appid.getText().toString());
			general_consts.setPicovoice_API_key(editTxt_picovoice_api_key.getText().toString());
		});

		linearLayout.addView(editTxt_pin);
		linearLayout.addView(editTxt_visor_email_addr);
		linearLayout.addView(editTxt_visor_email_pw);
		linearLayout.addView(editTxt_user_email_addr);
		linearLayout.addView(editTxt_server_domain);
		linearLayout.addView(editTxt_server_pw);
		linearLayout.addView(editTxt_wolframalpha_appid);
		linearLayout.addView(editTxt_picovoice_api_key);
		linearLayout.addView(btn_save);
	}
}
