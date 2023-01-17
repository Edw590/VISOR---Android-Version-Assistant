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

package com.dadi590.assist_c_a.GlobalUtils.AndroidSystem;

/**
 * <p>General utilities for this Android system utilities class.</p>
 */
public final class UtilsAndroid {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsAndroid() {
	}

	/** Generic error. */
	public static final int GEN_ERR = 987670;
	public static final int NO_ERR = 987671;
	public static final int PERM_DENIED = 987672;

	public static final int NO_BLUETOOTH_ADAPTER = -987673;

	public static final int ALREADY_ENABLED = -987674;
	public static final int ALREADY_DISABLED = -987675;

	public static final int MODE_NORMAL = -987676;
	public static final int MODE_SAFE = -987677;
	public static final int MODE_RECOVERY = -987678;
	public static final int MODE_BOOTLOADER = -987679;
	public static final int MODE_FAST = -987680;
}
