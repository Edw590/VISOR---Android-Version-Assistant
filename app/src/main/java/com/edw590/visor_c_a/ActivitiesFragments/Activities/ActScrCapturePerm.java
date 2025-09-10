/*
 * Copyright 2021-2025 Edw590
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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.edw590.visor_c_a.GlobalUtils.UtilsContext;
import com.edw590.visor_c_a.Modules.ScreenRecorder.ScreenRecorder;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ActScrCapturePerm extends AppCompatActivity {
	private static final int REQUEST_CODE = 1234;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Request screen capture permission
		MediaProjectionManager media_projection_manager = (MediaProjectionManager) UtilsContext.getContext()
				.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
		startActivityForResult(media_projection_manager.createScreenCaptureIntent(), REQUEST_CODE);
	}

	@Override
	public void onActivityResult(final int requestCode, final int resultCode, @Nullable final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK && data != null) {
				// Store the permission data for later use with the Screen Recorder
				ScreenRecorder.token_data = data;
			}
		}

		finish();
	}
}
