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

package com.edw590.visor_c_a.ActivitiesFragments.Activities;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.NavHost;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.edw590.visor_c_a.GlobalUtils.UtilsApp;
import com.edw590.visor_c_a.MainSrvc.UtilsMainSrvc;
import com.edw590.visor_c_a.R;
import com.google.android.material.navigation.NavigationView;

import SettingsSync.SettingsSync;

/**
 * <p>The app's main activity.</p>
 */
public final class ActMain extends AppCompatActivity {

	@Override
	protected void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Do this only once, when the activity is created and while it's not destroyed

		UtilsMainSrvc.startMainService();



		ModsFileInfo.GeneralConsts generalConsts = SettingsSync.getGeneralSettingsGENERAL();

		if (!generalConsts.getPin().isEmpty()) {
			setContentView(R.layout.nested_scroll_view);

			LinearLayout linear_layout = findViewById(R.id.nested_scroll_view_linear_layout);

			String color_accent = "#" + Integer.toHexString(ContextCompat.getColor(this, R.color.colorAccent));

			AppCompatTextView txtView_title = new AppCompatTextView(this);
			txtView_title.setText("V.I.S.O.R. Systems");
			txtView_title.setTextColor(Color.parseColor(color_accent));
			txtView_title.setTextSize(40);
			txtView_title.setTypeface(null, Typeface.BOLD);
			txtView_title.setGravity(Gravity.CENTER);
			txtView_title.setHeight(200);

			AppCompatTextView txtView_insert_pin = new AppCompatTextView(this);
			txtView_insert_pin.setText("Insert PIN:");

			AppCompatEditText editTxt_pin = new AppCompatEditText(this);
			editTxt_pin.setHint("PIN");
			editTxt_pin.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);

			AppCompatButton btn_unlock = new AppCompatButton(this);
			btn_unlock.setText("Unlock");
			btn_unlock.setOnClickListener(v -> {
				if (editTxt_pin.getText().toString().equals(generalConsts.getPin())) {
					startActivity();
				} else {
					editTxt_pin.setText("");
				}
			});

			linear_layout.addView(txtView_title);
			linear_layout.addView(txtView_insert_pin);
			linear_layout.addView(editTxt_pin);
			linear_layout.addView(btn_unlock);
		} else {
			startActivity();
		}
	}

	private void startActivity() {
		if (UtilsApp.isRunningOnWatch()) {
			setContentView(R.layout.act_main_watch);
		} else {
			setContentView(R.layout.act_main);
		}

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

		//if (!UtilsApp.isRunningOnWatch()) {
		//	final BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
		//	NavigationUI.setupWithNavController(bottomNav, navController);
		//}
	}

	@Override
	public void onStart() {
		super.onStart();

		// Do this below every time the activity is started/resumed/whatever

		UtilsMainSrvc.startMainService();
	}
}
