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

package com.edw590.visor_c_a.ActivitiesFragments;

import android.content.res.Resources;
import android.graphics.Color;
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

import com.edw590.visor_c_a.GlobalUtils.UtilsTimeDate;
import com.edw590.visor_c_a.Modules.SystemChecker.BluetoothChecker;
import com.edw590.visor_c_a.Modules.SystemChecker.ExtDevice;
import com.edw590.visor_c_a.Modules.SystemChecker.WifiChecker;
import com.edw590.visor_c_a.R;

import java.util.List;

import UtilsSWA.UtilsSWA;

/**
 * <p>Fragment that shows the list of the Values Storage values.</p>
 */
public final class FragNearbyDevices extends Fragment {


	@Nullable
	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
								   @Nullable final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.frag_nearby_devices, container, false);
	}

	@Override
	public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		final LinearLayout linearLayout = view.findViewById(R.id.frag_nearby_devices_linear_layout);

		final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		// Below, convert DP to PX to input on setMargins(), which takes pixels only.
		// 15 DP seems to be enough as margins.
		final Resources resources = requireActivity().getResources();
		final int padding_px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15.0F,
				resources.getDisplayMetrics());

		final TextView textView1 = new TextView(requireContext());
		textView1.setLayoutParams(layoutParams);
		textView1.setPadding(padding_px, padding_px, padding_px, padding_px);
		textView1.setTextColor(Color.BLACK);
		textView1.setTextIsSelectable(true);
		textView1.setText("Type (0, Bluetooth; 1, Wi-Fi)\nAddress\nLast detection (ms)\nRounded distance (m)\nName\nGiven name\nIs linked/saved network");
		linearLayout.addView(textView1);

		final List<ExtDevice>[] lists = new List[]{BluetoothChecker.nearby_devices_bt, WifiChecker.nearby_aps_wifi};
		for (final List<ExtDevice> list : lists) {
			for (final ExtDevice device : list) { // Add a TextView for each value.
				final TextView textView = new TextView(requireContext());
				textView.setLayoutParams(layoutParams);
				textView.setPadding(padding_px, padding_px, padding_px, padding_px);
				textView.setTextColor(Color.BLACK);
				textView.setTextIsSelectable(true);

				final long last_detection = device.last_detection;
				final String text = device.type + "\n" + device.address + "\n" + UtilsTimeDate.getTimeDateStr(last_detection) +
						"\n" + UtilsSWA.getRealDistanceRssiLOCRELATIVE(device.rssi, UtilsSWA.DEFAULT_TX_POWER) +
						"\n" + device.name + "\n" + device.given_name + "\n" + device.is_linked;
				textView.setText(text);

				linearLayout.addView(textView);
			}
		}
	}
}
