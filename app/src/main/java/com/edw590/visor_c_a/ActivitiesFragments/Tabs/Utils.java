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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.text.InputType;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.edw590.visor_c_a.R;
import com.edw590.visor_c_a.Registry.UtilsRegistry;
import com.ms.square.android.expandabletextview.ExpandableTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Utils {

	static List<View> createValue(@NonNull final Context context, @NonNull final UtilsSWA.Value value) {
		List<View> child_views = new ArrayList<>(10);

		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		// Below, convert DP to PX to input on setMargins(), which takes pixels only.
		// 15 SP seems to be enough as margins.
		Resources resources = context.getResources();
		int padding_px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15.0F,
				resources.getDisplayMetrics());

		AppCompatTextView textView = new AppCompatTextView(context);
		textView.setLayoutParams(layoutParams);
		textView.setPadding(padding_px, padding_px, padding_px, 0);
		textView.setTextColor(Color.BLACK);
		textView.setTextIsSelectable(true);
		String text = "Name: " + value.getPretty_name().substring(value.getPretty_name().indexOf('-') + 2) +
				"\nType: " + value.getType_().substring("TYPE_".length()).toLowerCase(Locale.ROOT);
		textView.setText(text);
		child_views.add(textView);

		ExpandableTextView expandableTextView = (ExpandableTextView) LayoutInflater.from(context)
				.inflate(R.layout.expandable_text_view, null);
		expandableTextView.setText(value.getDescription());
		expandableTextView.setPadding(padding_px, 0, padding_px, padding_px);
		child_views.add(expandableTextView);

		SwitchCompat switchCompat = null;
		AppCompatEditText editText = null;
		if (value.getType_().equals(UtilsSWA.UtilsSWA.TYPE_BOOL)) {
			switchCompat = new SwitchCompat(context);
			switchCompat.setChecked(value.getBool(true));
			child_views.add(switchCompat);
		} else {
			editText = new AppCompatEditText(context);
			editText.setText(value.getCurr_data());
			switch (value.getType_()) {
				case (UtilsSWA.UtilsSWA.TYPE_INT):
				case (UtilsSWA.UtilsSWA.TYPE_LONG): {
					editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);

					break;
				}
				case (UtilsSWA.UtilsSWA.TYPE_FLOAT):
				case (UtilsSWA.UtilsSWA.TYPE_DOUBLE): {
					editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED |
							InputType.TYPE_NUMBER_FLAG_DECIMAL);

					break;
				}
				case (UtilsSWA.UtilsSWA.TYPE_STRING): {
					editText.setInputType(InputType.TYPE_CLASS_TEXT);
					editText.setSingleLine();

					break;
				}
			}

			child_views.add(editText);
		}

		// Add a save button for each setting
		AppCompatButton button_save_setting = new AppCompatButton(context);
		button_save_setting.setText("Save");
		final SwitchCompat finalSwitchCompat = switchCompat;
		final AppCompatEditText finalEditText = editText;
		button_save_setting.setOnClickListener(v -> {
			if (finalSwitchCompat != null) {
				UtilsRegistry.setData(value.getKey(), finalSwitchCompat.isChecked(), false);
			} else {
				UtilsRegistry.setData(value.getKey(), finalEditText.getText().toString(), false);
			}
		});
		child_views.add(button_save_setting);

		return child_views;
	}

	static void createConfirmation(final Context context, final CharSequence message, final Runnable on_yes) {
		new AlertDialog.Builder(context)
				.setTitle("Confirmation")
				.setMessage(message)
				.setPositiveButton(android.R.string.yes, (dialog, which) -> {
					on_yes.run();
				})
				.setNegativeButton(android.R.string.no, null)
				.show();
	}

	static void refreshFragment(@NonNull final Fragment fragment) {
		FragmentTransaction ft1 = fragment.getParentFragmentManager().beginTransaction();
		ft1.detach(fragment).commit();
		FragmentTransaction ft2 = fragment.getParentFragmentManager().beginTransaction();
		ft2.attach(fragment).commit();
	}

	static void setExpandableListViewSize(@NonNull final ExpandableListView myListView) {
		// Got it from https://stackoverflow.com/a/43177241/8228163.

		ListAdapter myListAdapter = myListView.getAdapter();
		if (myListAdapter == null) {
			//do nothing return null
			return;
		}
		//set listAdapter in loop for getting final size
		int totalHeight = 0;
		for (int size = 0; size < myListAdapter.getCount(); size++) {
			View listItem = myListAdapter.getView(size, null, myListView);
			listItem.measure(0, 0);
			totalHeight += listItem.getMeasuredHeight();
		}
		totalHeight *= 1.5; // Correction of mine (Edw590)
		//setting listview item in adapter
		ViewGroup.LayoutParams params = myListView.getLayoutParams();
		params.height = totalHeight + (myListView.getDividerHeight() * (myListAdapter.getCount() - 1));
		myListView.setLayoutParams(params);
		// print height of adapter on log
		//Log.i("height of listItem:", String.valueOf(totalHeight));
	}
}
