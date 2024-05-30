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
import java.util.Collection;

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
	 * <p>Same as in {@link #executeShellCmd(boolean, Iterable)}, but that allows to send a string instead of a
	 * list.</p>
	 *
	 * @param attempt_su same as in {@link #executeShellCmd(boolean, Iterable)}
	 * @param command same as in {@link #executeShellCmd(boolean, Iterable)}
	 *
	 * @return same as in {@link #executeShellCmd(boolean, Iterable)}
	 */
	@NonNull
	public static CmdOutput executeShellCmd(final boolean attempt_su, @NonNull final String command) {
		final Collection<String> commands = new ArrayList<>(1);
		commands.add(command);

		return executeShellCmd(attempt_su, commands);
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
	 * <p>- The function will join the list with " && " after each command, so make sure they can be written as
	 * one-liners.</p>
	 * <p>- Don't put a new line at the end of each command.</p>
	 * <p>- Don't add empty commands or just new lines.</p>
	 * <p>- The return values are byte arrays. To get their printable form, use
	 * {@link UtilsSWA#bytesToPrintableDATACONV(byte[], boolean)}.</p>
	 *
	 * @param attempt_su true to, in case the app has root permissions, call su before the given commands, false
	 * otherwise (useful to execute commands with or without root allowed without wanting the error
	 * from calling su to appear on the output stream)
	 * @param commands_list list of commands to execute
	 *
	 * @return an instance of {@link CmdOutput}, and if any error occurs, {@link UtilsSWA#GENERIC_ERR} will be returned
	 */
	@NonNull
	public static CmdOutput executeShellCmd(final boolean attempt_su, @NonNull final Iterable<String> commands_list) {
		int exit_code;
		byte[] output_stream = null;
		byte[] error_stream = null;

		try {
			final byte[] cmd_output = UtilsSWA.execCmdSHELL(attempt_su, String.join("\n", commands_list));

			exit_code = UtilsSWA.getExitCodeSHELL(cmd_output);
			output_stream = UtilsSWA.getStdoutSHELL(cmd_output);
			error_stream = UtilsSWA.getStderrSHELL(cmd_output);
		} catch (final Exception e) {
			e.printStackTrace();

			exit_code = UtilsSWA.GENERIC_ERR;
		}

		return new CmdOutput(exit_code, output_stream, error_stream);
	}
	/**
	 * <p>Class to use for the returning value of {@link #executeShellCmd(boolean, String)}.</p>
	 * <p>Read the documentation of the class constructor to know more about it.</p>
	 */
	public static final class CmdOutput {
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
		public CmdOutput(final int exit_code, @Nullable final byte[] output_stream,
						 @Nullable final byte[] error_stream) {
			this.exit_code = exit_code;
			this.output_stream = output_stream;
			this.error_stream = error_stream;
		}
	}
}
