/*
 * Copyright 2023 DADi590
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

package com.dadi590.assist_c_a.ActivitiesFragments;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.dadi590.assist_c_a.ModulesList;
import com.dadi590.assist_c_a.R;

/**
 * <p>Fragment that shows the status of each module of the assistant.</p>
 */
public final class FragModulesStatus extends Fragment {

	View current_view = null;

	String color_accent;
	String color_primary;

	@Nullable
	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
								   @Nullable final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.frag_modules_status, container, false);
	}

	@Override
	public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		current_view = view;

		color_primary = "#" + Integer.toHexString(ContextCompat.getColor(requireActivity(),
				R.color.colorPrimary));
		color_accent = "#" + Integer.toHexString(ContextCompat.getColor(requireActivity(),
				R.color.colorAccent));

		final LinearLayout linearLayout = current_view.findViewById(R.id.frag_modules_status_linear_layout);

		final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		// Below, convert DP to PX to input on setMargins(), which takes pixels only.
		// 15 DP seems to be enough as margins.
		final Resources resources = requireActivity().getResources();
		final int padding_px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15.0F,
				resources.getDisplayMetrics());

		for (int module_index = 0; module_index < ModulesList.elements_list_length; ++module_index) { // Add a Switch for each module.
			final int elem_type2 = (int) ModulesList.getElementValue(module_index, ModulesList.ELEMENT_TYPE2);
			final CharSequence elem_name = (CharSequence) ModulesList.getElementValue(module_index, ModulesList.ELEMENT_NAME);

			final SwitchCompat switchCompat = new SwitchCompat(requireContext());
			switchCompat.setId(module_index); // Set the ID to be the index of the module in the list
			switchCompat.setText(ModulesList.TYPE2_MODULE == elem_type2 ? elem_name : "- " + elem_name);
			switchCompat.setEnabled(false);
			switchCompat.setLayoutParams(layoutParams);
			switchCompat.setTypeface(null, Typeface.BOLD);
			switchCompat.setTextSize(20.0F);
			switchCompat.setPadding(padding_px, padding_px, padding_px, padding_px);
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
				final int color;
				if (ModulesList.TYPE2_MODULE == elem_type2) {
					color = ModulesList.isElementFullyWorking(module_index) ? Color.parseColor(color_accent)
							: Color.parseColor("#FFFF8800");
				} else {
					color = Color.parseColor(color_accent);
				}
				switchCompat.setTextColor(color);
			} else {
				// If it's not running, color it red (Primary Color).
				switchCompat.setTextColor(Color.parseColor(color_primary));
			}

			linearLayout.addView(switchCompat);
		}

		// Thread disabled temporarily. Put it stopping after the user leaves this fragment.
		//infinity_checker.start();
	}

	private final Thread infinity_checker = new Thread(new Runnable() {
		@Override
		public void run() {
			while (true) { // Keep checking the modules' status.
				System.out.println("+++++++++++++++++++++++++++++++++++++++++++");
				for (int module_index = 0; module_index < ModulesList.elements_list_length; ++module_index) {
					final SwitchCompat switchCompat = current_view.findViewById(module_index);

					final boolean module_running = ModulesList.isElementRunning(module_index);
					switchCompat.setChecked(module_running); // --> "Animators may only be run on Looper threads"
					if (module_running) {
						// If the module is running, color the text green (Accent Color).
						switchCompat.setTextColor(Color.parseColor(color_accent));
					} else {
						// If it's not running, color it red (Primary Color).
						switchCompat.setTextColor(Color.parseColor(color_primary));
					}
				}

				try {
					Thread.sleep(1_000L);
				} catch (final InterruptedException ignored) {
					return;
				}
			}
		}
	});
}
