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
import androidx.annotation.Nullable;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Utility class with functions that directly use a shell.</p>
 */
public final class UtilsShell {

	public static final byte[] empty_byte_array = new byte[0];

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsShell() {
	}

	/**
	 * <p>Executes any given command and returns the outputs.</p>
	 * <br>
	 * <p><u><strong>SECURITY WARNING:</strong></u></p>
	 * <p>Do NOT use this to execute commands saved in some file or whatever. Execute ONLY constants or generated
	 * strings on the code <em>still from constants</em> - NEVER generate from something that can be gotten outside
	 * constants. NEVER something that can be saved on the device storage.</p>
	 * <br>
	 * <p><u>ATTENTION:</u></p>
	 * <p>This function will return the output and error streams of the FIRST command on the list ONLY. In case that 1st
	 * command creates a new shell, like the "su" command does, the output will still be that of the 1st command -
	 * though, that 1st command has a sub-shell in which all is printed, so the output of the first command of the
	 * original shell is the output written to the sub-shell created by that 1st command. If other commands are inserted
	 * after exiting from the sub-shell, those are subsequent commands in the original shell and their output will not
	 * be printed.</p>
	 * <p>To print regardless of this, there is a special parameter, with a consequence - it will call /system/bin/sh as
	 * a sub-shell. With root or WITH updates to AOSP, that could (or not) be removed - and the command will fail. So...
	 * <u>USE ONLY IF <strong>REALLY</strong> NECESSARY!!!</u></p>
	 * <br>
	 * <p>Considerations to have:</p>
	 * <p>- Don't put a new line at the end of each command since the function will automatically do that. In case for
	 * some reason there's a new line character already as the last character, it won't put another one. In case the
	 * command is empty (read below), a new line will not be added.</p>
	 * <p>- An empty command will not be recognized as a new line - it will be ignored. To enter a new line, simply
	 * write yourself "\n" on the command string.</p>
	 * <p>- The function will input "exit\n" as the last command in case the command to execute su was issued (not doing
	 * so would result in an infinite wait for it to return).</p>
	 * <p>- The return values are byte arrays. To get their printable form, use
	 * {@link UtilsGeneral#bytesToPrintableChars(byte[], boolean)}.</p>
	 *
	 * @param commands_list list of commands to execute, each in a new index
	 * @param print_only_1st_cmd true to print the output of only the output of the 1st command, false to print the output
	 *                         of all commands (read above for more about this)
	 *
	 * @return an instance of {@link CmdOutputObj}
	 */
	@NonNull
	public static CmdOutputObj executeShellCmd(@NonNull final List<String> commands_list,
											   final boolean print_only_1st_cmd) {
		final List<byte[]> ret_streams = new ArrayList<>(2);
		ret_streams.add(empty_byte_array);
		ret_streams.add(empty_byte_array);
		@Nullable Integer exit_code = null;

		main_try: try {
			final Process process;
			if (print_only_1st_cmd) {
				// If it's not to print all the commands, just use the first command to start the process (don't forget
				// it will will print only the output from that command).
				process = Runtime.getRuntime().exec(commands_list.get(0), null, null);
			} else {
				// Here is created a sub-shell to be able to print the output of the all the commands. The function will
				// still only print the output of the first command - which is this one below, and this one will have
				// all commands inside it printed, and the result is the output of the first ever command --> the shell
				// initialization command.
				process = Runtime.getRuntime().exec("/system/bin/sh", null, null);
			}

			try (final DataOutputStream dataOutputStream = new DataOutputStream(process.getOutputStream())) {

				for (int i = print_only_1st_cmd ? 1 : 0, size = commands_list.size(); i < size; ++i) {
					// From index 1 because the 1st command was already executed
					final String command = commands_list.get(i);
					if (!command.isEmpty()) {
						dataOutputStream.writeBytes(command);

						// In case a new line wasn't in the end of the command, one must be inserted (below).
						if ((int) command.charAt(command.length() - 1) != (int) '\n') {
							// This above checks if the last *char* is a \n. The way it is written in is for mega
							// optimization, advised by Android Studio (Java ME).
							dataOutputStream.write((int) '\n');
						}
						dataOutputStream.flush();
					}
				}
			} catch (final IOException ignored) {
				// This is here to leave the try statement in a way that doesn't required throwing an exception.
				// If there was an error inserting all the commands, exit immediately with error.
				break main_try;
			}

			final InputStream[] streams = {process.getInputStream(), process.getErrorStream()};

			int number_bytes_read;
			final ArrayList<Byte> storage_array = new ArrayList<>(64);
			//final int buffer_length = 1; // Don't put too high. Try and see the Inspection error ("Large array
			// allocation with no OutOfMemoryError check") - also if you change this, look below. I have buffer[0]
			// because right now it's only one element per buffer (so no null bytes are appended and invalidate a file,
			// that's being read, for example)
			// EDIT: in any case, append to a byte array must be byte by byte, so yeah.
			int stream_counter = 0;
			for (final InputStream stream : streams) {
				final byte[] buffer = new byte[1];

				try {
					while (true) {
						number_bytes_read = stream.read(buffer);

						storage_array.add(buffer[0]);

						if (number_bytes_read < 1) {
							// Everything was read (less than the buffer was filled)
							break;
						}
					}
				} catch (final IOException ignored) {
					// This catches the exception on the read() function - happened when "su" was requested and the
					// stream was closed when read() was called.
				}
				// Way of converting to bytes, since ArrayList won't let me convert to a primitive type
				final int storage_array_size = storage_array.size();
				final byte[] ret_array = new byte[storage_array_size];
				for (int j = 0; j < storage_array_size; ++j) {
					ret_array[j] = storage_array.get(j);
				}
				ret_streams.set(stream_counter, ret_array);
				storage_array.clear();

				++stream_counter;
			}

			exit_code = process.waitFor();
		} catch (final IOException | SecurityException ignored) {
		} catch (final InterruptedException ignored) {
		}

		return new CmdOutputObj(exit_code, ret_streams.get(0), ret_streams.get(1));
		}
	/**
	 * <p>Class to use for the returning value of {@link #executeShellCmd(List, boolean)}.</p>
	 * <p>Always check if the error_code is null. If it is, the streams will be of size 0.</p>
	 * <p>Read the documentation of the class constructor to know more about it.</p>
	 */
	public static class CmdOutputObj {
		public final Integer error_code;
		public final byte[] output_stream;
		public final byte[] error_stream;

		/**
		 * <p>Main class constructor.</p>
		 *
		 * @param error_code the exit code returned by {@link Process#waitFor()}; or null in case an exception was
		 *                   thrown processing the commands and the execution was aborted at some point
		 * @param output_stream the output stream of the terminal
		 * @param error_stream the error stream of the terminal
		 */
		public CmdOutputObj(@Nullable final Integer error_code, @NonNull final byte[] output_stream,
							@NonNull final byte[] error_stream) {
			this.error_code = error_code;
			this.output_stream = output_stream.clone();
			this.error_stream = error_stream.clone();
		}
	}
}
