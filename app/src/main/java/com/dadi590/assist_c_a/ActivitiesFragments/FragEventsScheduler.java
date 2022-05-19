/*
 * Copyright 2021 DADi590
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
 * <p>Fragment that shows and creates events for the assistant accomplish.</p>
 */
public class FragEventsScheduler extends Fragment {

	private static final String[] temp = {

	};

	@Nullable
	@Override
	public final View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
								   @Nullable final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.frag_events_scheduler, container, false);
	}

	@Override
	public final void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		final LinearLayout linearLayout = view.findViewById(R.id.frag_events_scheduler_linear_layout);

		final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		// Below, convert DP to PX to input on setMargins(), which takes pixels only.
		// 15 DP seems to be enough as margins.
		final Resources resources = requireActivity().getResources();
		final int padding_px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15.0F,
				resources.getDisplayMetrics());

		final String color_accent = "#" + Integer.toHexString(ContextCompat.getColor(requireActivity(),
				R.color.colorAccent));
		final String color_primary = "#" + Integer.toHexString(ContextCompat.getColor(requireActivity(),
				R.color.colorPrimary));

		for (int i = 0; i < ModulesList.modules_list_length; ++i) { // Add a switch for each module.
			final SwitchCompat switchCompat = new SwitchCompat(requireContext());
			switchCompat.setEnabled(false);
			switchCompat.setLayoutParams(layoutParams);
			switchCompat.setTypeface(null, Typeface.BOLD);
			switchCompat.setTextSize(20.0F);
			switchCompat.setPadding(padding_px, padding_px, padding_px, padding_px);

			switchCompat.setText((CharSequence) ModulesList.getModuleValue(i, ModulesList.MODULE_NAME)); // Name the switch the module name.
			final boolean module_running = ModulesList.isModuleRunning(i);
			switchCompat.setChecked(module_running);
			if (module_running) {
				// If the module is running, color the text green (Accent Color).
				switchCompat.setTextColor(Color.parseColor(color_accent));
			} else {
				// If it's not running, color it red (Primary Color).
				switchCompat.setTextColor(Color.parseColor(color_primary));
			}

			linearLayout.addView(switchCompat);
		}
	}
}
