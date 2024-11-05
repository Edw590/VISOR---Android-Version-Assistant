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
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.edw590.visor_c_a.ActivitiesFragments.Tabs.TabHomeMain;
import com.edw590.visor_c_a.ActivitiesFragments.Tabs.TabModManagerModsStatus;
import com.edw590.visor_c_a.R;
import com.google.android.material.tabs.TabLayout;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragModulesManager#newInstance} factory method to
 * create an instance of this fragment.
 */
public final class FragModulesManager extends Fragment {

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

		TabLayout tabLayout = view.findViewById(R.id.tab_layout);

		// Add tabs with titles
		tabLayout.addTab(tabLayout.newTab().setText("Modules status"));

		// Set default fragment when fragment is created
		replaceFragment(new TabHomeMain());

		// Set up a listener for tab selection events
		tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
			@Override
			public void onTabSelected(final TabLayout.Tab tab) {
				Fragment selectedFragment;
				switch (tab.getPosition()) {
					case 0:
						selectedFragment = new TabModManagerModsStatus();
						break;
					default:
						return;
				}
				replaceFragment(selectedFragment);
			}

			@Override
			public void onTabUnselected(final TabLayout.Tab tab) {
			}

			@Override
			public void onTabReselected(final TabLayout.Tab tab) {
			}
		});
	}

	void replaceFragment(final Fragment fragment) {
		FragmentManager fragmentManager = getChildFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		transaction.replace(R.id.frame_layout, fragment);
		transaction.commit();
	}
}
