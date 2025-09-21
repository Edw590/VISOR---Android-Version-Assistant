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

package com.edw590.visor_c_a.ActivitiesFragments.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.edw590.visor_c_a.ActivitiesFragments.Tabs.TabHomeLocalSettings;
import com.edw590.visor_c_a.ActivitiesFragments.Tabs.TabHomeMain;
import com.edw590.visor_c_a.ActivitiesFragments.Tabs.TabHomeSettings;
import com.edw590.visor_c_a.GlobalUtils.UtilsApp;
import com.edw590.visor_c_a.R;
import com.google.android.material.tabs.TabLayout;

public final class FragHome extends Fragment {

	Object[][] tab_fragments = {
			{new TabHomeMain(), "Main"},
			{new TabHomeSettings(), "Settings"},
			{new TabHomeLocalSettings(), "Local settings"},
	};

	@Nullable
	@Override
	public View onCreateView(@android.annotation.NonNull final LayoutInflater inflater,
							 @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
		if (UtilsApp.isRunningOnWatch()) {
			return inflater.inflate(R.layout.frag_main_watch, container, false);
		} else {
			return inflater.inflate(R.layout.frag_main, container, false);
		}
	}

	@Override
	public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		TabLayout tabLayout = view.findViewById(R.id.tab_layout);

		Fragment curr_frag = this;

		// Add tabs with titles
		for (final Object[] tab : tab_fragments) {
			tabLayout.addTab(tabLayout.newTab().setText((String) tab[1]));
		}

		// Set default fragment when fragment is created
		Utils.replaceFragment(curr_frag, (Fragment) tab_fragments[0][0]);

		// Set up a listener for tab selection events
		tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
			@Override
			public void onTabSelected(final TabLayout.Tab tab) {
				Utils.replaceFragment(curr_frag, (Fragment) tab_fragments[tab.getPosition()][0]);
			}

			@Override
			public void onTabUnselected(final TabLayout.Tab tab) {
			}

			@Override
			public void onTabReselected(final TabLayout.Tab tab) {
			}
		});
	}
}
