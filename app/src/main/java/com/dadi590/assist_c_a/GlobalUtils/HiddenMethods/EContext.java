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

package com.dadi590.assist_c_a.GlobalUtils.HiddenMethods;

import android.content.Context;

import androidx.annotation.NonNull;

import com.dadi590.assist_c_a.BuildConfig;

/**
 * <p>Original class: {@link Context}.</p>
 */
final class EContext {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private EContext() {
	}

	/**
	 * <p>See {@link Context#getOpPackageName()}.</p>
	 * <br>
	 * <p>Changes:</p>
	 * <p>- Method made static as it doesn't use Context</p>
	 *
	 * @return .
	 */
	@NonNull
	static String getOpPackageName() {
		return BuildConfig.APPLICATION_ID;
		// When I printed the result of the original function, it returned "com.dadi590.assist_c_a", which is the name
		// of the main app package. When I inserted that as a string constant, Android Studio said it was a duplicate
		// of BuildConfig.APPLICATION_ID. So I'm using it, hoping in case I change the ID, BuildConfig.APPLICATION_ID
		// will be the one the function would return.
	}
}
