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
import android.widget.ExpandableListView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.edw590.visor_c_a.R;
import com.edw590.visor_c_a.Registry.UtilsRegistry;

public final class TabSpeechLocalSettings extends Fragment {

	@Nullable
	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
							 @Nullable final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.nested_scroll_view, container, false);
	}

	@Override
	public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		int padding = Utils.getDefaultPadding(requireContext());
		LinearLayout linearLayout = view.findViewById(R.id.nested_scroll_view_linear_layout);
		linearLayout.setPadding(padding, padding, padding, padding);

		ExpandableListView expandable_list_view = new ExpandableListView(requireContext());
		GenericExpandableListAdapter adapter = new GenericExpandableListAdapter(requireContext());
		expandable_list_view.setAdapter(adapter);
		expandable_list_view.setLayoutParams(linearLayout.getLayoutParams());
		expandable_list_view.setOnGroupCollapseListener(groupPosition -> {
			Utils.setExpandableListViewSize(expandable_list_view, false);
		});
		expandable_list_view.setOnGroupExpandListener(groupPosition -> {
			Utils.setExpandableListViewSize(expandable_list_view, false);
		});

		linearLayout.addView(expandable_list_view);

		for (final UtilsSWA.Value value : UtilsRegistry.getValues()) {
			if (value.getAuto_set()) {
				continue;
			}
			if (!value.getPretty_name().startsWith("Speech - ")) {
				continue;
			}

			String title = value.getPretty_name().substring(value.getPretty_name().indexOf('-') + 2);
			adapter.addItem(title, Utils.createValue(requireContext(), value));
		}

		// After adding all the values, set the size of the ExpandableListView.
		Utils.setExpandableListViewSize(expandable_list_view, false);
	}
}
