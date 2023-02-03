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
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dadi590.assist_c_a.R;
import com.dadi590.assist_c_a.ValuesStorage.ValuesStorage;

/**
 * <p>Fragment that shows the list of the Values Storage values.</p>
 */
public final class FragValuesStorageViewer extends Fragment {

	View current_view = null;

	final String[][] values_list = ValuesStorage.getValuesArrays();

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

		final LinearLayout linearLayout = current_view.findViewById(R.id.frag_modules_status_linear_layout);

		final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		// Below, convert DP to PX to input on setMargins(), which takes pixels only.
		// 15 DP seems to be enough as margins.
		final Resources resources = requireActivity().getResources();
		final int padding_px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15.0F,
				resources.getDisplayMetrics());

		int i = 0;
		for (final String[] value : values_list) { // Add a TextView for each value.
			final TextView textView = new TextView(requireContext());
			textView.setId(i); // Set the ID to be the index of the value in the list
			textView.setLayoutParams(layoutParams);
			textView.setPadding(padding_px, padding_px, padding_px, padding_px);
			textView.setTextColor(Color.BLACK);
			textView.setTextIsSelectable(true);

			final String text = value[1] + "\n" + value[2] + "\n" + value[3];
			textView.setText(text);

			linearLayout.addView(textView);
			++i;
		}
	}
}
