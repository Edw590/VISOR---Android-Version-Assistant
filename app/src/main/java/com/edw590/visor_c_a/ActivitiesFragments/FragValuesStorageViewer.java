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

package com.edw590.visor_c_a.ActivitiesFragments;

import android.content.res.Resources;
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

import com.edw590.visor_c_a.R;

import UtilsSWA.UtilsSWA;

/**
 * <p>Fragment that shows the list of the Values Storage values.</p>
 */
public final class FragValuesStorageViewer extends Fragment {

	@Nullable
	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
								   @Nullable final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.frag_values_storage_viewer, container, false);
	}

	@Override
	public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		final LinearLayout linearLayout = view.findViewById(R.id.frag_values_storage_viewer_linear_layout);
		final LayoutInflater layoutInflater = LayoutInflater.from(requireContext());

		final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		// Below, convert DP to PX to input on setMargins(), which takes pixels only.
		// 15 SP seems to be enough as margins.
		final Resources resources = requireActivity().getResources();
		final int padding_px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15.0F,
				resources.getDisplayMetrics());

		/*int i = 0;
		for (final Value value : ValuesRegistry.getArray()) { // Add a TextView for each value.
			final TextView textView = new TextView(requireContext());
			textView.setId(i); // Set the ID to be the index of the value in the list
			textView.setLayoutParams(layoutParams);
			textView.setPadding(padding_px, padding_px, padding_px, 0);
			textView.setTextColor(Color.BLACK);
			textView.setTextIsSelectable(true);
			final String text = "Name: " + value.pretty_name + "\nType: " + value.type +
					"\nTime: " + getTimeDateStr(value.getPrevTime()) + "\nPrev: " + value.getPrevData() +
					"\nTime: " + getTimeDateStr(value.getTime()) + "\nCurr: " + value.getData();
			textView.setText(text);
			linearLayout.addView(textView);

			final ExpandableTextView expandableTextView = (ExpandableTextView) layoutInflater
					.inflate(R.layout.expandable_text_view, null);
			expandableTextView.setText(value.description);
			expandableTextView.setPadding(padding_px, 0, padding_px, padding_px);
			linearLayout.addView(expandableTextView);

			++i;
		}*/

		TextView textView = new TextView(requireContext());
		textView.setText(UtilsSWA.getRegistryTextREGISTRY());
		linearLayout.addView(textView);
	}
}
