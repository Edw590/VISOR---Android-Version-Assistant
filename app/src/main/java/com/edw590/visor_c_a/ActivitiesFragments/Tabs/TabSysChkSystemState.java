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
import androidx.fragment.app.Fragment;

import com.edw590.visor_c_a.R;

import SettingsSync.SettingsSync;

public final class TabSysChkSystemState extends Fragment {

	AppCompatTextView txt_sys_state;

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
	public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
							 @Nullable final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.nested_scroll_view, container, false);
	}

	@Override
	public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		LinearLayout linearLayout = view.findViewById(R.id.nested_scroll_view_linear_layout);

		txt_sys_state = new AppCompatTextView(requireContext());

		linearLayout.addView(txt_sys_state);

		createStartInfinityChecker();
	}

	void createStartInfinityChecker() {
		infinity_checker = new Thread(() -> {
			Runnable runnable = () -> {
				txt_sys_state.setText(SettingsSync.getDeviceInfoJsonSYSCHK());
			};
			while (true) {
				requireActivity().runOnUiThread(runnable);

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
