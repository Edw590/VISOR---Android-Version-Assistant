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

import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

import com.edw590.visor_c_a.R;

import java.util.Objects;

import GMan.GMan;
import SettingsSync.SettingsSync;
import UtilsSWA.UtilsSWA;

public final class TabGManSettings extends Fragment {

	private AppCompatTextView txt_token_valid = null;

	private Thread infinity_checker = null;

	@Override
	public void onStop() {
		super.onStop();

		if (infinity_checker != null) {
			infinity_checker.interrupt();
		}
	}

	@Nullable
	@Override
	public View onCreateView(@android.annotation.NonNull final LayoutInflater inflater,
								   @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.nested_scroll_view, container, false);
	}

	@Override
	public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		ModsFileInfo.Mod14UserInfo mod_14_user_info = SettingsSync.getMod14InfoUSERSETS();

		int padding = Utils.getDefaultPadding(requireContext());
		LinearLayout linearLayout = view.findViewById(R.id.nested_scroll_view_linear_layout);
		linearLayout.setPadding(padding, padding, padding, padding);

		txt_token_valid = new AppCompatTextView(requireContext());
		txt_token_valid.setText("Token valid: error");
		txt_token_valid.setPadding(0, 0, 0, padding);

		AppCompatTextView txt_link_google = new AppCompatTextView(requireContext());
		String link_google_str = "<a href=\"https://console.cloud.google.com/projectcreate\">Click here and watch the video on the link below</a>";
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			txt_link_google.setText(Html.fromHtml(link_google_str, Html.FROM_HTML_MODE_LEGACY));
		} else {
			txt_link_google.setText(Html.fromHtml(link_google_str));
		}
		txt_link_google.setPadding(0, 0, 0, padding);
		txt_link_google.setMovementMethod(LinkMovementMethod.getInstance());

		AppCompatTextView txt_link_video = new AppCompatTextView(requireContext());
		String link_video_str = "<a href=\"https://youtu.be/B2E82UPUnOY?t=95\">How to obtain the Google credentials JSON</a>";
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			txt_link_video.setText(Html.fromHtml(link_video_str, Html.FROM_HTML_MODE_LEGACY));
		} else {
			txt_link_video.setText(Html.fromHtml(link_video_str));
		}
		txt_link_video.setPadding(0, 0, 0, padding);
		txt_link_video.setMovementMethod(LinkMovementMethod.getInstance());

		AppCompatTextView txt_additional_info = new AppCompatTextView(requireContext());
		txt_additional_info.setText("Activate the Calendar, Gmail and Tasks APIs by looking them up in the Search bar " +
				"and in the Scopes, choose \"auth/calendar\", \"auth/tasks\" and \"auth/gmail.modify\".");
		txt_additional_info.setPadding(0, 0, 0, padding);

		AppCompatEditText editTxt_credentials_json = new AppCompatEditText(requireContext());
		editTxt_credentials_json.setHint("Google credentials JSON file contents");
		editTxt_credentials_json.setText(mod_14_user_info.getCredentials_JSON());
		editTxt_credentials_json.setTypeface(Typeface.MONOSPACE);
		editTxt_credentials_json.setSingleLine();
		editTxt_credentials_json.setPadding(0, 0, 0, padding);

		AppCompatButton btn_save = new AppCompatButton(requireContext());
		btn_save.setText("Save");
		btn_save.setOnClickListener(v -> {
			mod_14_user_info.setCredentials_JSON(editTxt_credentials_json.getText().toString());
		});

		AppCompatTextView txt_additional_info2 = new AppCompatTextView(requireContext());
		txt_additional_info2.setText("To get the authorization code, when you get to an error page (it's normal - " +
				"Google stuff), look at the URL bar. Look for \"code=\" and copy what's after the = sign until just " +
				"before the next & sign.\n\n" +
				"" +
				"NOTICE: if you've set the app as a test app on the link above, the token will EXPIRE every 7 days. " +
				"Just click on Authorize below and do the same steps and you're ready to go for another week.");

		AppCompatButton btn_authorize = new AppCompatButton(requireContext());
		btn_authorize.setText("Authorize");
		btn_authorize.setOnClickListener(v -> {
			if (mod_14_user_info.getCredentials_JSON().isEmpty()) {
				Utils.createErrorDialog(requireContext(), "No credentials JSON saved");

				return;
			}

			if (!UtilsSWA.isCommunicatorConnectedSERVER()) {
				Utils.createErrorDialog(requireContext(), "Not connected to the server");

				return;
			}

			String auth_url = "";
			try {
				auth_url = GMan.getAuthUrl();
			} catch (final Exception e) {
				// The message is never null coming from the VISOR Libraries
				Utils.createErrorDialog(requireContext(), Objects.requireNonNull(e.getMessage()));

				return;
			}

			AppCompatTextView txtView  = new AppCompatTextView(requireContext());
			txtView.setMovementMethod(LinkMovementMethod.getInstance());
			txtView.setPadding(padding, padding, padding, padding);
			String url_str = "<a href=" + auth_url + ">External authorization prompt</a>";
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
				txtView.setText(Html.fromHtml(url_str, Html.FROM_HTML_MODE_LEGACY));
			} else {
				txtView.setText(Html.fromHtml(url_str));
			}
			new AlertDialog.Builder(requireContext())
					.setTitle("Open the following Google link")
					.setView(txtView)
					.setPositiveButton(android.R.string.ok, (dialog, which) -> {
						LinearLayout form_view = new LinearLayout(requireContext());
						form_view.setOrientation(LinearLayout.VERTICAL);
						form_view.setPadding(padding, padding, padding, padding);

						AppCompatTextView textView = new AppCompatTextView(requireContext());
						textView.setText("Code");
						textView.setPadding(0, 0, 0, padding / 3);
						form_view.addView(textView);
						AppCompatEditText editText = new AppCompatEditText(requireContext());
						editText.setInputType(InputType.TYPE_CLASS_TEXT);
						editText.setPadding(0, 0, 0, padding);
						form_view.addView(editText);

						new AlertDialog.Builder(requireContext()).
								setTitle("Google authorization code").
								setView(form_view).
								setPositiveButton(android.R.string.ok, (dialog1, which1) -> {
									if (editText.getText().toString().isEmpty()) {
										return;
									}

									try {
										GMan.storeTokenFromAuthCode(editText.getText().toString());
									} catch (final Exception e) {
										// The message is never null coming from the VISOR Libraries
										Utils.createErrorDialog(requireContext(), Objects.requireNonNull(e.getMessage()));

										return;
									}

									Utils.createInfoDialog(requireContext(), "Authorization code saved. You're all set!");
								}).
								setNegativeButton(android.R.string.cancel, null).
								show();
					}).show();
		});

		linearLayout.addView(txt_token_valid);
		linearLayout.addView(txt_link_google);
		linearLayout.addView(txt_link_video);
		linearLayout.addView(txt_additional_info);
		linearLayout.addView(editTxt_credentials_json);
		linearLayout.addView(btn_save);
		linearLayout.addView(txt_additional_info2);
		linearLayout.addView(btn_authorize);

		createStartInfinityChecker();
	}

	void createStartInfinityChecker() {
		infinity_checker = new Thread(() -> {
			while (true) {
				String validity;
				if (UtilsSWA.isCommunicatorConnectedSERVER()) {
					if (GMan.isTokenValid()) {
						validity = "valid";
					} else {
						validity = "INVALID";
					}
				} else {
					validity = "[Not connected to the server to get the token validity]";
				}
				requireActivity().runOnUiThread(() -> {
					txt_token_valid.setText("Token is: " + validity + " (refreshes at most every 60 seconds)");
				});

				try {
					Thread.sleep(1000);
				} catch (final InterruptedException ignored) {
					return;
				}
			}
		});
		infinity_checker.start();
	}
}
