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

package com.dadi590.assist_c_a.GlobalUtils.AndroidSystem;

import androidx.annotation.Nullable;

import com.dadi590.assist_c_a.GlobalUtils.UtilsShell;

import java.util.List;

/**
 * <p>General utilities for this Android system utilities class.</p>
 */
public final class UtilsAndroid {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsAndroid() {
	}

	public static final int NO_ERRORS = -50;
	public static final int ERROR = -51;
	public static final int NO_ROOT = -52;
	/**
	 * <p>Checks the error code returned by {@link UtilsShell#executeShellCmd(List, boolean)} and decides what it means
	 * (no root available, an error, or no errors).</p>
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link #NO_ERRORS} --> for the returning value: no errors in the operation completed successfully</p>
	 * <p>- {@link #ERROR} --> for the returning value: an error occurred and the operation did not succeed</p>
	 * <p>- {@link #NO_ROOT} --> for the returning value: root user rights are not available but are required for the
	 * operation</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @param error_code the value of {@link UtilsShell.CmdOutputObj#error_code}
	 *
	 * @return one of the constants
	 */
	static int checkCmdOutputObjErrCode(@Nullable final Integer error_code) {
		// These checks were made based on the way the root commands availability is done.
		if (error_code == null || error_code == 13) {
			// Error 13 is "Permission denied" in UNIX
			return NO_ROOT;
		} else if (error_code != 0) {
			return ERROR;
		} else {
			return NO_ERRORS;
		}
	}
}
