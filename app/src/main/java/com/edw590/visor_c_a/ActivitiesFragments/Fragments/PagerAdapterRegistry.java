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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.edw590.visor_c_a.ActivitiesFragments.Tabs.TabRegistryAutomaticValues;
import com.edw590.visor_c_a.ActivitiesFragments.Tabs.TabRegistryManualValues;

public class PagerAdapterRegistry extends FragmentStateAdapter {

	PagerAdapterRegistry(@NonNull FragmentActivity fragmentActivity) {
		super(fragmentActivity);
	}

	@NonNull
	@Override
	public Fragment createFragment(int position) {
		switch (position) {
			case 0:
				return new TabRegistryAutomaticValues();
			case 1:
				return new TabRegistryManualValues();
		}

		// Won't happen
		return null;
	}

	@Override
	public int getItemCount() {
		return 2; // Number of tabs
	}
}
