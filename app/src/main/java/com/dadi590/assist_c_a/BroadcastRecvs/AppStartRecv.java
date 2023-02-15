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

package com.dadi590.assist_c_a.BroadcastRecvs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.Nullable;

import com.dadi590.assist_c_a.GlobalUtils.UtilsApp;
import com.dadi590.assist_c_a.GlobalUtils.UtilsPermsAuths;
import com.dadi590.assist_c_a.MainSrvc.UtilsMainSrvc;

/**
 * <p>The {@link BroadcastReceiver} to be used to start the app by detecting ANY system-wide broadcast.</p>
 * <p>Do NOT use to detect things in particular - only to start the app and nothing else. Explanation of the why on a
 * comment on the manifest.</p>
 */
public final class AppStartRecv extends BroadcastReceiver {

	@Override
	public void onReceive(@Nullable final Context context, @Nullable final Intent intent) {
		// Important to attempt to force Device Administration mode because the app might have been killed (or just
		// restarted, but supposed it's killed) by the system for uninstallation and this will start it again and enable
		// Device Administration again, which will stop it from uninstalling - if the user is slow enough to insert the
		// pin again (if there is any...) and click "Ok" to confirm the uninstallation.
		if (!UtilsApp.isDeviceAdmin()) {
			UtilsPermsAuths.forceDeviceAdmin();
		}

		UtilsMainSrvc.startMainService();

		System.out.println("PPPPPPPPPPPPPPPPPP-AppStartRecv - " + (null != intent ? intent.getAction() : null));

		// Do NOT enable this!!! I'm ignoring safety measures (see the Manifest where I'm ignoring possible spoofing of
		// SMS) with this receiver since ANY intent (null or not) received is supposed to get the app to start the main
		// service and NOTHING ELSE.
		//UtilsIntentWhatToDo.intentWhatToDo(intent);
	}
}
