/*
 * Copyright 2022 DADi590
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

package com.dadi590.assist_c_a.Modules.CameraManager;

import android.content.Intent;

import com.dadi590.assist_c_a.GlobalUtils.UtilsApp;

/**
 * <p>Functions to call to send information to {@link CameraManagement}, by using broadcasts.</p>
 */
public final class UtilsCameraManagerBC {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsCameraManagerBC() {
	}

	/**
	 * <p>Broadcasts a request to execute {@link CameraManagement#useCamera(int)}.</p>
	 *
	 * @param usage same as in {@link CameraManagement#useCamera(int)}
	 */
	public static void useCamera(final int usage) {
		final Intent broadcast_intent = new Intent(CONSTS_BC_CameraManag.ACTION_USE_CAMERA);
		broadcast_intent.putExtra(CONSTS_BC_CameraManag.EXTRA_USE_CAMERA_1, usage);

		UtilsApp.sendInternalBroadcast(broadcast_intent);
	}
}
