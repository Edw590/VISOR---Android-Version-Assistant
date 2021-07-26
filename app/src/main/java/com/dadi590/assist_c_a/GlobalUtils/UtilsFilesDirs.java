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

package com.dadi590.assist_c_a.GlobalUtils;

import androidx.annotation.NonNull;

import java.io.File;

/**
 * <p>Utilities related with files and directories.</p>
 */
public final class UtilsFilesDirs {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsFilesDirs() {
	}

	/**
	 * <p>Deletes a directory (either file or folder).</p>
	 *
	 * @param dir the path to delete
	 *
	 * @return true if deletion was completely successful, including all files if a non-empty folder was selected for
	 * deletion; false otherwise
	 */
	public static boolean deletePath(@NonNull final File dir) {
		if (dir.isDirectory()) {
			final String[] children = dir.list();
			boolean success = true;
			if (children == null) {
				return false;
			} else {
				for (final String child : children) {
					success = success && deletePath(new File(dir, child));
				}
			}
			return success && dir.delete();
		} else if (dir.isFile()) {
			return dir.delete();
		} else {
			return false;
		}
	}
}
