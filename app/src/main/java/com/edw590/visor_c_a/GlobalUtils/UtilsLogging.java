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

package com.edw590.visor_c_a.GlobalUtils;

import androidx.annotation.NonNull;

import UtilsSWA.UtilsSWA;

public class UtilsLogging {

	/**
	 * <p>Logs a debug message.</p>
	 *
	 * @param message The message to log
	 */
	public static void logLnError(@NonNull final Object message) {
		System.out.println("++ E:" + ___8drrd3148796d_Xaf() + message);
	}

	/**
	 * <p>Logs a debug message.</p>
	 *
	 * @param message The message to log
	 */
	public static void logLnWarning(@NonNull final Object message) {
		System.out.println("++ W:" + ___8drrd3148796d_Xaf() + message);
	}

	/**
	 * <p>Logs a debug message.</p>
	 *
	 * @param message The message to log
	 */
	public static void logLnInfo(@NonNull final Object message) {
		System.out.println("++ I:" + ___8drrd3148796d_Xaf() + message);
	}

	/**
	 * <p>Logs a debug message.</p>
	 *
	 * @param message The message to log
	 */
	public static void logLnDebug(@NonNull final Object message) {
		System.out.println("++ D:" + ___8drrd3148796d_Xaf() + message);
	}

	/** This methods name is ridiculous on purpose to prevent any other method
	 * names in the stack trace from potentially matching this one.
	 *
	 * @return The line number of the code that called the method that called
	 *         this method(Should only be called by getLineNumber()).
	 * @author Brian_Entei */
	@NonNull
	private static String ___8drrd3148796d_Xaf() {
		// Copied and adapted from: https://stackoverflow.com/a/26410435/8228163.

		String file_name = "ERROR";
		int line_number = -1;

		boolean thisOne = false;
		int thisOneCountDown = 1;
		StackTraceElement[] elements = Thread.currentThread().getStackTrace();
		for(final StackTraceElement element : elements) {
			String methodName = element.getMethodName();
			if(thisOne && (thisOneCountDown == 0)) {
				file_name = element.getFileName();
				line_number = element.getLineNumber();
				break;
			} else if(thisOne) {
				thisOneCountDown--;
			}
			if(methodName.equals("___8drrd3148796d_Xaf")) {
				thisOne = true;
			}
		}

		String caps_file_name = UtilsSWA.getInitialsOfFileNameLOGGING(file_name);
		String line_str = UtilsSWA.formatLineNumberLOGGING(line_number);

		return caps_file_name + ":" + line_str + "|> ";
	}
}
