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

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Utility class with functions that use directly a shell.</p>
 */
public final class UtilsShell {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsShell() {
	}

	/**
	 * <p>Gets the access rights of a file or folder.</p>
	 *
	 * @param path the path to the file or folder
	 * @param human_readable true to return "drwxr-xr-x", for example; false to return in octal form (for example, 755)
	 *
	 * @return one of the strings mentioned in {@code human_readable} parameter
	 */
	@NonNull
	public static String getAccessRights(@NonNull final String path, final boolean human_readable) {
		final String parameter;
		if (human_readable) {
			parameter = "%A";
		} else {
			parameter = "%a";
		}

		final List<String> commands = new ArrayList<>(2);
		commands.add("su");
		commands.add("stat -c " + parameter + " " + path);

		UtilsRoot.CmdOuputObj commands_output = UtilsRoot.executeShellCmd(commands);
		if (commands_output == null) {
			commands.remove(0);
			commands_output = UtilsRoot.executeShellCmd(commands);
		}

		assert commands_output != null; // Just want the warning out. It won't be null if the "su" command is not there.
		return UtilsGeneral.convertBytes2Printable(commands_output.output_stream);
	}

	/*public static boolean createFile(@NonNull final String complete_name) {
		final String partition = complete_name.split("/")[1];
	}*/
}
