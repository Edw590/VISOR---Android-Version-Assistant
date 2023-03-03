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

package com.dadi590.assist_c_a.GlobalUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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
	 * <p>Same as in {@link #executeShellCmd(List, boolean, boolean)}, but that allows to send a string instead of a
	 * list.</p>
	 *
	 * @param command the command(s - separated by new lines then) to execute
	 * @param retrieve_streams same as in {@link #executeShellCmd(List, boolean, boolean)}
	 * @param attempt_su same as in {@link #executeShellCmd(List, boolean, boolean)}
	 *
	 * @return same as in {@link #executeShellCmd(List, boolean, boolean)}
	 */
	@NonNull
	public static CmdOutputObj executeShellCmd(@NonNull final String command, final boolean retrieve_streams,
											   final boolean attempt_su) {
		final List<String> commands = new ArrayList<>(1);
		commands.add(command);

		return executeShellCmd(commands, retrieve_streams, attempt_su);
	}

	/**
	 * <p>Executes any given command and returns the outputs.</p>
	 * <br>
	 * <p>Attention: a call to this function is not "instantaneous". It takes a bit. Don't call this a lot of times.
	 * It will make the app slow and waste battery.</p>
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
	 * {@link UtilsDataConv#bytesToPrintable(byte[], boolean)}.</p>
	 *
	 * @param commands_list list of commands to execute, each in a new index
	 * @param retrieve_streams true to ignore the output and error streams and jump over the code that retrieves
	 *                              them, making this method finish much faster, false to retrieve both streams
	 * @param attempt_su true to, in case the app has root permissions, call su before the given commands, false
	 *                   otherwise (useful to execute commands with or without root allowed without wanting the error
	 *                   from calling su to appear on the output stream)
	 *
	 * @return an instance of {@link CmdOutputObj}, and if any error occurs, -1 will be returned
	 */
	@NonNull
	public static CmdOutputObj executeShellCmd(@NonNull final List<String> commands_list,
											   final boolean retrieve_streams, final boolean attempt_su) {
		int exit_code = -1;
		final byte[][] ret_streams = {null, null};

		Process process = null;
		try {
			// Here is created a sub-shell to be able to print the output of the all the commands. The function will
			// still only print the output of the first command - which is this one below, and this one will have
			// all commands inside it printed, and the result is the output of the first ever command --> the shell
			// initialization command.
			// The error code is also transmitted from shell to shell, so the main shell error code is the same as the
			// error code that outputs from this created shell or any other subsequently created shells.
			process = Runtime.getRuntime().exec("/system/bin/sh", null, null);

			try (final DataOutputStream dataOutputStream = new DataOutputStream(process.getOutputStream())) {

				if (attempt_su && UtilsRoot.isRootAvailable()) {
					dataOutputStream.writeBytes("su\n");
					dataOutputStream.flush();
				}

				final int commands_list_size = commands_list.size();
				for (int i = 0; i < commands_list_size; ++i) {
					final String command = commands_list.get(i);
					if (!command.isEmpty()) {
						dataOutputStream.writeBytes(command);

						// In case a new line wasn't in the end of the command, one must be inserted (below).
						if ((int) '\n' != (int) command.charAt(command.length() - 1)) {
							// This above checks if the last *char* is a \n. The way it is written in is for mega
							// optimization, advised by Android Studio (Java ME).
							dataOutputStream.write((int) '\n');
						}
						dataOutputStream.flush();
					}
				}
			}

			// If it's to ignore the output streams (output and error streams), return now with null on both streams.
			if (!retrieve_streams) {
				exit_code = process.waitFor();

				return new CmdOutputObj(exit_code, null, null);
			}

			final InputStream[] streams = {process.getInputStream(), process.getErrorStream()};

			final ByteArrayOutputStream storage_array = new ByteArrayOutputStream(64);
			// WARNING: THIS IS INCREASING THE SIZE OF THE ARRAY TO THE SIZE OF THE FILE!!!!
			// An OutOfMemoryError catch was put in place for this, and I've also set largeHeap to true on the Manifest.
			final int streams_length = streams.length;
			for (int i = 0; i < streams_length; i++) {
				// Don't put too high. Try and see the Inspection error ("Large array allocation with no
				// OutOfMemoryError check"). Also it seems that the JIT compiler will put arrays with <= 64 elements on
				// the stack instead of the heap with > 64 elements. Hopefully even without JIT compilation (vmSafeMode)
				// this optimization is still used.
				final int buffer_length = 64;
				final byte[] buffer = new byte[buffer_length];
				while (true) {
					final int n_bytes = streams[i].read(buffer);
					if (-1 == n_bytes) { // The last read of buffer_length bytes finished the stream
						// Everything was read (0 bytes were retrieved)
						break;
					}

					// Possible OutOfMemoryError here
					storage_array.write(buffer, 0, n_bytes);
				}
				// Possible OutOfMemoryError here
				ret_streams[i] = storage_array.toByteArray();
			}

			exit_code = process.waitFor();
		} catch (final Throwable ignored) {
			exit_code = -1;
		}

		return new CmdOutputObj(exit_code, ret_streams[0], ret_streams[1]);
	}
	/**
	 * <p>Class to use for the returning value of {@link #executeShellCmd(List, boolean, boolean)}.</p>
	 * <p>Read the documentation of the class constructor to know more about it.</p>
	 */
	public static final class CmdOutputObj {
		/** The SH shell exit code. */
		public final int error_code;
		// Don't add @Nullable or @NonNull to the streams. Let the developer decide which is the case.
		/** The output stream of the terminal. */
		public final byte[] output_stream;
		/** The error stream of the terminal. */
		public final byte[] error_stream;

		/**
		 * <p>Main class constructor.</p>
		 *
		 * @param error_code {@link #error_code}
		 * @param output_stream {@link #output_stream}
		 * @param error_stream {@link #error_stream}
		 */
		public CmdOutputObj(final int error_code, @Nullable final byte[] output_stream,
							@Nullable final byte[] error_stream) {
			this.error_code = error_code;
			this.output_stream = output_stream;
			this.error_stream = error_stream;
		}
	}
}
