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
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.edw590.visor_c_a.R;

import GMan.GMan;
import UtilsSWA.UtilsSWA;

public final class TabGManActiveCals extends Fragment {

	@Nullable
	@Override
	public View onCreateView(@android.annotation.NonNull final LayoutInflater inflater,
								   @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.nested_scroll_view, container, false);
	}

	@Override
	public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		int padding = Utils.getDefaultPadding(requireContext());
		LinearLayout linearLayout = view.findViewById(R.id.nested_scroll_view_linear_layout);
		linearLayout.setPadding(padding, padding, padding, padding);

		AppCompatTextView txt_description = new AppCompatTextView(requireContext());
		txt_description.setText("List of calendar associated with the Google account:");

		AppCompatTextView txt_not_connected = new AppCompatTextView(requireContext());
		txt_not_connected.setText("[Not connected to the server to get the calendars]");

		linearLayout.addView(txt_description);

		if (!UtilsSWA.isCommunicatorConnectedSERVER()) {
			linearLayout.addView(txt_not_connected);

			return;
		}

		String calendar_ids = GMan.getCalendarsIdsList();
		String[] calendar_ids_split = calendar_ids.split("\\|");
		for (final String cal_id : calendar_ids_split) {
			ModsFileInfo.GCalendar calendar = GMan.getCalendar(cal_id);
			if (calendar == null) {
				continue;
			}

			SwitchCompat switchCompat = new SwitchCompat(requireContext());
			switchCompat.setText(calendar.getTitle());
			switchCompat.setChecked(calendar.getEnabled());
			switchCompat.setPadding(0, padding, 0, padding);
			switchCompat.setOnCheckedChangeListener((buttonView, is_checked) -> {
				if (!GMan.setCalendarEnabled(cal_id, is_checked)) {
					Utils.createErrorDialog(requireContext(), "Failed to set the calendar state");
					buttonView.setChecked(!is_checked);
				}
			});

			linearLayout.addView(switchCompat);
		}
	}
}
