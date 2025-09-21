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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.edw590.visor_c_a.ModulesList;
import com.edw590.visor_c_a.R;

public final class TabModManagerModsStatus extends Fragment {

	View current_view = null;

	String color_accent;
	String color_primary;

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

		current_view = view;

		color_primary = "#" + Integer.toHexString(ContextCompat.getColor(requireActivity(),
				R.color.colorPrimary));
		color_accent = "#" + Integer.toHexString(ContextCompat.getColor(requireActivity(),
				R.color.colorAccent));

		int padding = Utils.getDefaultPadding(requireContext());
		LinearLayout linearLayout = current_view.findViewById(R.id.nested_scroll_view_linear_layout);
		linearLayout.setPadding(padding, padding, padding, padding);

		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		for (int module_index = 0; module_index < ModulesList.ELEMENTS_LIST_LENGTH; ++module_index) { // Add a Switch for each module.
			final boolean is_module = (boolean) ModulesList.getElementValue(module_index, ModulesList.ELEMENT_IS_MODULE);
			final CharSequence elem_name = (CharSequence) ModulesList.getElementValue(module_index, ModulesList.ELEMENT_NAME);

			final SwitchCompat switchCompat = new SwitchCompat(requireContext());
			switchCompat.setId(module_index); // Set the ID to be the index of the module in the list
			switchCompat.setText(is_module ? elem_name : "- " + elem_name);
			switchCompat.setEnabled(false);
			switchCompat.setLayoutParams(layoutParams);
			switchCompat.setTypeface(null, Typeface.BOLD);
			switchCompat.setTextSize(20.0F);
			switchCompat.setPadding(0, 0, 0, padding);
			switchCompat.setTextIsSelectable(true);
			switchCompat.setBackgroundColor(Color.WHITE);

			if (!(boolean) ModulesList.getElementValue(module_index, ModulesList.ELEMENT_SUPPORTED)) {
				switchCompat.setTextColor(Color.parseColor(color_primary));
				switchCompat.setBackgroundColor(Color.GRAY);
			}
			// This below was supposed to be in the else statement of the module supported part, but leave it separated.
			// In case something was done wrong in the Modules Manager, the modules may start even if they're not
			// supported, as just happened. This way I can still see there's something wrong (gray background with
			// green letters is not supposed to happen).
			final boolean element_running = ModulesList.isElementRunning(module_index);
			switchCompat.setChecked(element_running);
			if (element_running) {
				// If the module is fully working, color the text green (Accent Color), else with orange (holo_orange_dark).
				// If it's not a module, then always green (always "supported" - its main module is the checked one).
				switchCompat.setTextColor(ModulesList.isElementFullyWorking(module_index) ?
						Color.parseColor(color_accent) : Color.parseColor("#FFFF8800"));
			} else {
				// If it's not running, color it red (Primary Color).
				switchCompat.setTextColor(Color.parseColor(color_primary));
			}

			linearLayout.addView(switchCompat);
		}

		createStartInfinityChecker();
	}

	void createStartInfinityChecker() {
		infinity_checker = new Thread(() -> {
			while (true) { // Keep checking the modules status.
				for (int module_index = 0; module_index < ModulesList.ELEMENTS_LIST_LENGTH; ++module_index) {
					final SwitchCompat switchCompat = current_view.findViewById(module_index);

					final boolean module_running = ModulesList.isElementRunning(module_index);
					requireActivity().runOnUiThread(() -> {
						switchCompat.setChecked(module_running);
					});
					if (module_running) {
						// If the module is running, color the text green (Accent Color).
						switchCompat.setTextColor(Color.parseColor(color_accent));
					} else {
						// If it's not running, color it red (Primary Color).
						switchCompat.setTextColor(Color.parseColor(color_primary));
					}
				}

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
