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

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.NavHost;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.dadi590.assist_c_a.GlobalUtils.UtilsPermsAuths;
import com.dadi590.assist_c_a.MainSrvc.UtilsMainSrvc;
import com.dadi590.assist_c_a.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

/**
 * <p>The app's main activity.</p>
 */
public class ActMain extends AppCompatActivity {

	@Override
	protected final void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_main);

		// Do this only once, when the activity is created and while it's not destroyed

		UtilsMainSrvc.startMainService();
		UtilsPermsAuths.warnPermissions(UtilsPermsAuths.checkRequestPerms(null, false), false);
		UtilsPermsAuths.warnAuthorizations(UtilsPermsAuths.checkRequestAuths(UtilsPermsAuths.CHECK_ONLY), false);



		final NavHost navHostFragment = (NavHost) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
		assert navHostFragment != null; // Will never be null - it's on the XMLs
		final NavController navController = navHostFragment.getNavController();

		final DrawerLayout drawerLayout = findViewById(R.id.drawer);
		final AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph())
				.setOpenableLayout(drawerLayout)
				.build();

		final NavigationView navView = findViewById(R.id.nav_view);
		NavigationUI.setupWithNavController(navView, navController);

		final Toolbar toolbar = findViewById(R.id.toolbar);
		NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);

		final BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
		NavigationUI.setupWithNavController(bottomNav, navController);
	}

	@Override
	public final void onStart() {
		super.onStart();

		// Do this below every time the activity is started/resumed/whatever

		UtilsMainSrvc.startMainService();
	}
}
