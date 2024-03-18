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

package com.edw590.visor_c_a.GlobalUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import UtilsSWA.UtilsSWA;

/**
 * <p>Utility class with functions that directly use a shell.</p>
 */
public final class UtilsShell {

	/**
	 * <p>Some useful error SH error codes.</p>
	 * <p>Note that only positive codes are possible. This means functions can return internal negative codes and those
	 * won't be mixed up with shell codes.</p>
	 */
	public static final class ErrCodes {
		/** No error in the shell operations. */
		public static final int NO_ERR = 0;
		/** "Catchall for general errors." */
		public static final int GEN_ERR = 1;
		/** "Misuse of shell builtins (according to Bash documentation)." */
		public static final int WRONG_USAGE = 2;
		/** No permission to access the path. */
		public static final int PERM_DENIED = 13;
	}

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsShell() {
	}
	/**
	 * <p>Checks if the error code is equal to 0, meaning there was no error.</p>
	 *
	 * @param error_code the error code to check
	 *
	 * @return true if it's 0, false otherwise
	 */
	public static boolean noErr(final int error_code) {
		return UtilsShell.ErrCodes.NO_ERR == error_code;
	}

	/**
	 * <p>Same as in {@link #executeShellCmd(List, boolean)}, but that allows to send a string instead of a
	 * list.</p>
	 *
	 * @param command the command(s - separated by new lines then) to execute
	 * @param attempt_su same as in {@link #executeShellCmd(List, boolean)}
	 *
	 * @return same as in {@link #executeShellCmd(List, boolean)}
	 */
	@NonNull
	public static CmdOutputObj executeShellCmd(@NonNull final String command, final boolean attempt_su) {
		final List<String> commands = new ArrayList<>(1);
		commands.add(command);

		return executeShellCmd(commands, attempt_su);
	}

	/**
	 * <p>Executes any given command and returns the outputs.</p>
	 * <br>
	 * <p><u><strong>SECURITY WARNING:</strong></u></p>
	 * <p>Do NOT use this to execute commands saved in some file or whatever. Execute ONLY constants or generated
	 * strings on the code <em>still from constants</em> - NEVER generate from something that can be gotten outside
	 * constants. NEVER something that can be saved on the device storage.</p>
	 * <br>
	 * <p>Considerations to have:</p>
	 * <p>- Don't put a new line at the end of each command since the function will automatically do that. In case for
	 * some reason there's a new line character already as the last character, it won't put another one. In case the
	 * command is empty, a new line will not be added.</p>
	 * <p>- An empty command will not be recognized as a new line - it will be ignored. To enter a new line, simply
	 * write yourself "\n" on the command string.</p>
	 * <p>- The return values are byte arrays. To get their printable form, use
	 * {@link UtilsSWA#bytesToPrintableGENERAL(byte[], boolean)}.</p>
	 *
	 * @param commands_list list of commands to execute, each in a new index
	 * @param attempt_su true to, in case the app has root permissions, call su before the given commands, false
	 * otherwise (useful to execute commands with or without root allowed without wanting the error
	 * from calling su to appear on the output stream)
	 *
	 * @return an instance of {@link CmdOutputObj}, and if any error occurs, {@link UtilsSWA#GENERIC_ERR} will be returned
	 */
	@NonNull
	public static CmdOutputObj executeShellCmd(@NonNull final List<String> commands_list, final boolean attempt_su) {
		int exit_code;
		final byte[][] ret_streams = {null, null};

		try {
			final byte[] cmd_output = UtilsSWA.execCmdSHELL(String.join("\n", commands_list), attempt_su);

			exit_code = UtilsSWA.getExitCodeSHELL(cmd_output);
			ret_streams[0] = UtilsSWA.getStdoutSHELL(cmd_output);
			ret_streams[1] = UtilsSWA.getStderrSHELL(cmd_output);
		} catch (final Exception e) {
			e.printStackTrace();

			exit_code = UtilsSWA.GENERIC_ERR;
		}

		return new CmdOutputObj(exit_code, ret_streams[0], ret_streams[1]);
	}
	/**
	 * <p>Class to use for the returning value of {@link #executeShellCmd(List, boolean)}.</p>
	 * <p>Read the documentation of the class constructor to know more about it.</p>
	 */
	public static final class CmdOutputObj {
		/** The SH shell exit code. */
		public final int exit_code;
		// Don't add @Nullable or @NonNull to the streams. Let the developer decide which is the case.
		/** The output stream of the terminal. */
		public final byte[] output_stream;
		/** The error stream of the terminal. */
		public final byte[] error_stream;

		/**
		 * <p>Main class constructor.</p>
		 *
		 * @param exit_code {@link #exit_code}
		 * @param output_stream {@link #output_stream}
		 * @param error_stream {@link #error_stream}
		 */
		public CmdOutputObj(final int exit_code, @Nullable final byte[] output_stream,
							@Nullable final byte[] error_stream) {
			this.exit_code = exit_code;
			this.output_stream = output_stream;
			this.error_stream = error_stream;
		}
	}
}
