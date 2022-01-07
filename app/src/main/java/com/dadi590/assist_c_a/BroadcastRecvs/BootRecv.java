/*
 * Copyright 2021 DADi590
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

import com.dadi590.assist_c_a.GlobalUtils.UtilsServices;

/**
 * <p>The {@link BroadcastReceiver} to be used ONLY to start the app at the device boot.</p>
 * <p>Explanation of the why on a comment on the manifest.</p>
 */
public class BootRecv extends BroadcastReceiver {

	@Override
	public final void onReceive(@Nullable final Context context, @Nullable final Intent intent) {
		UtilsServices.startMainService();

		System.out.println("PPPPPPPPPPPPPPPPPP-BootRcv - " + (null != intent ? intent.getAction() : null));
	}
}
