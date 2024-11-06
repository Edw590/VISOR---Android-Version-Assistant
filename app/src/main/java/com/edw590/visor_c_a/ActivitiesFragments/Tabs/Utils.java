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
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;

import com.edw590.visor_c_a.R;
import com.edw590.visor_c_a.Registry.UtilsRegistry;
import com.ms.square.android.expandabletextview.ExpandableTextView;

import java.util.Locale;

public class Utils {

	static void createValue(@NonNull final Context context, @NonNull final LinearLayout linearLayout,
							@NonNull final UtilsSWA.Value value) {
		LayoutInflater layoutInflater = LayoutInflater.from(context);

		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		// Below, convert DP to PX to input on setMargins(), which takes pixels only.
		// 15 SP seems to be enough as margins.
		Resources resources = context.getResources();
		int padding_px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15.0F,
				resources.getDisplayMetrics());

		TextView textView = new TextView(context);
		textView.setLayoutParams(layoutParams);
		textView.setPadding(padding_px, padding_px, padding_px, 0);
		textView.setTextColor(Color.BLACK);
		textView.setTextIsSelectable(true);
		String text = "Name: " + value.getPretty_name().substring(value.getPretty_name().indexOf('-') + 2) +
				"\nType: " + value.getType_().substring("TYPE_".length()).toLowerCase(Locale.ROOT);
		textView.setText(text);
		linearLayout.addView(textView);

		ExpandableTextView expandableTextView = (ExpandableTextView) layoutInflater
				.inflate(R.layout.expandable_text_view, null);
		expandableTextView.setText(value.getDescription());
		expandableTextView.setPadding(padding_px, 0, padding_px, padding_px);
		linearLayout.addView(expandableTextView);

		SwitchCompat switchCompat = null;
		EditText editText = null;
		if (value.getType_().equals(UtilsSWA.UtilsSWA.TYPE_BOOL)) {
			switchCompat = new SwitchCompat(context);
			switchCompat.setChecked(value.getBool(true));
			linearLayout.addView(switchCompat);
		} else {
			editText = new EditText(context);
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

			linearLayout.addView(editText);
		}

		// Add a save button for each setting
		Button button_save_setting = new Button(context);
		button_save_setting.setText("Save");
		final SwitchCompat finalSwitchCompat = switchCompat;
		final EditText finalEditText = editText;
		button_save_setting.setOnClickListener(v -> {
			if (finalSwitchCompat != null) {
				UtilsRegistry.setData(value.getKey(), finalSwitchCompat.isChecked(), false);
			} else {
				UtilsRegistry.setData(value.getKey(), finalEditText.getText().toString(), false);
			}
		});
		linearLayout.addView(button_save_setting);
	}
}
