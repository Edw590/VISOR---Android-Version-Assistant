/*
 * Copyright 2021-2024 Edw590
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

/**
 * <p>Good Path-related utilities.</p>
 * <br>
 * <p>GPath (GoodPath) is sort of a copy of the string type but that represents a *surely* valid and correct path, also
 * according to the project conventions.</p>
 * <p>It's a "good path" because it's only given by GPath(), which corrects the paths, and because the string component is
 * private to the package and only requested when absolutely necessary, like to communicate with Java's official functions
 * that require a string.</p>
 */
public final class GPath {

	/** p is the string that represents the path. */
	private String p;
	/** dir is true if the path *describes* a directory, false if it *describes* a file (means no matter if it exists
	 * and we have permissions to read it or not). */
	private boolean dir;

	public GPath(final boolean describes_dir, @NonNull final String path) {
		path(describes_dir, path);
	}

	@NonNull
	private GPath path(@NonNull final Boolean describes_dir, @NonNull final Object... sub_paths) {
		final StringBuilder path_joined = new StringBuilder(16 * sub_paths.length); // Size like that because "yes" (better ideas?)
		for (final Object sub_path : sub_paths) {
			if (sub_path instanceof String) {
				path_joined.append((String) sub_path).append("\u0000");
			} else if (sub_path instanceof GPath) {
				path_joined.append(((GPath) sub_path).gPathToStringConversion()).append("\u0000");
			} else {
				throw new IllegalArgumentException("path() received an invalid type of parameter: " + sub_path.getClass());
			}
		}

		final String[] gPath_array = UtilsSWA.pathFILESDIRS(describes_dir, path_joined.toString()).split("\u0000");

		p = gPath_array[0];
		dir = Boolean.parseBoolean(gPath_array[1]);

		return this;
	}

	@NonNull
	public GPath add2(final boolean describes_dir, @NonNull final Object... sub_paths) {
		if (sub_paths.length == 0) {
			return this;
		}

		final Object[] new_args = new Object[sub_paths.length + 1];
		System.arraycopy(sub_paths, 0, new_args, 1, sub_paths.length);
		new_args[0] = gPathToStringConversion();
		path(describes_dir, new_args);

		return this;
	}

	@NonNull
	public String gPathToStringConversion() {
		return p;
	}
}
