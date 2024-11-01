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
import androidx.viewpager2.widget.ViewPager2;

import com.edw590.visor_c_a.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragHome#newInstance} factory method to
 * create an instance of this fragment.
 */
public final class FragHome extends Fragment {

	@Nullable
	@Override
	public View onCreateView(@android.annotation.NonNull final LayoutInflater inflater,
								   @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.frag_main, container, false);
	}

	@Override
	public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		ViewPager2 viewPager = view.findViewById(R.id.view_pager);
		TabLayout tabLayout = view.findViewById(R.id.tab_layout);

		// Set the adapter
		viewPager.setAdapter(new PagerAdapterHome(requireActivity()));

		// Link TabLayout and ViewPager2 with TabLayoutMediator
		new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
			@Override
			public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
				switch (position) {
					case 0:
						tab.setText("Main");
						break;
				}
			}
		}).attach();
	}
}
