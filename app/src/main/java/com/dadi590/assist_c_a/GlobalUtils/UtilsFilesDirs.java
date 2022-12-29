/*
 * Copyright 2022 DADi590
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

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.io.File;

/**
 * <p>Utilities related to files and directories.</p>
 */
public final class UtilsFilesDirs {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsFilesDirs() {
	}



	// todo Use SDK methods (faster) unless some error happens, and only then use shell commands



	/**
	 * <p>Removes a path (to whatever), empty or not, using the rm shell command.</p>
	 *
	 * @param path the path
	 * @param recursive same as rm's "r" parameter
	 *
	 * @return a SH shell exit code
	 */
	public static int removePath(@NonNull final File path, final boolean recursive) {
		final String command = "rm -f" + (recursive ? "r" : "") + "'" + path + "'";

		return UtilsShell.executeShellCmd(command, false, true).error_code;
	}

	/**
	 * <p>Creates a path and all necessary but non-existent parent directories.</p>
	 *
	 * @param path the path to the create
	 *
	 * @return a SH shell exit code
	 */
	public static int createDirectory(@NonNull final String path) {
		final String command = "mkdir -p '" + path + "'";

		return UtilsShell.executeShellCmd(command, false, true).error_code;
	}

	/**
	 * <p>Gets the size of a file.</p>
	 *
	 * @param file_path the path to the file
	 *
	 * @return the file size in bytes, or -1 if an error occurred (no permissions or no file)
	 */
	public static int getFileSize(@NonNull final String file_path) {
		final String command = "ls -l '" + file_path + "'";
		final UtilsShell.CmdOutputObj cmdOutputObj = UtilsShell.executeShellCmd(command, true, true);
		final String output_data = UtilsDataConv.bytesToPrintable(cmdOutputObj.output_stream, false);

		if (0 != cmdOutputObj.error_code) {
			return -1;
		}

		return Integer.parseInt(output_data.split(" ")[3]);
	}

	/**
	 * <p>Reads the bytes from the given file using the cat shell command.</p>
	 * <p>ATTENTION: no more than 10 MiB can be read using this function, to prevent out of memory errors.</p>
	 *
	 * @param file_path the path to the file
	 *
	 * @return the bytes of the file or null in case there was an error reading the file or the file size was greater
	 * than 10 MiB
	 */
	@Nullable
	public static byte[] readFileBytes(@NonNull final String file_path) {
		final int file_size = getFileSize(file_path);
		if (-1 == file_size || file_size > 10_485_760) {
			// Not larger than 10 MiB, at least for now (not needed).
			return null;
		}

		final String command = "cat '" + file_path + "'";

		// Ignore the warning about the clone. No problem with it. This is a utility method. It won't use the object
		// for anything other than to return it. Performance gained (imagine it's a big file...).
		return UtilsShell.executeShellCmd(command, true, true).output_stream;
	}

	/**
	 * <p>Writes the given files bytes to a file (replaces all file contents).</p>
	 * <p>Only ONLY with small files!!! <strong>This function requires allocating 3-4 times the file size into
	 * memory!</strong></p>
	 *
	 * @param file_path the path to the file
	 * @param file_bytes the bytes to write
	 *
	 * @return a SH shell exit code
	 */
	public static int writeSmallFile(@NonNull final String file_path, @NonNull final byte[] file_bytes) {
		final String bytes_data;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			// This is here because hex takes less size on memory than octal when in string representation (2 vs 3 chars).
			// On miTab Advance (KitKat 4.4.2), hex is supported, so I'm putting it as minimum for it (didn't check
			// APIs 16-18 though).
			bytes_data = "\\x" + UtilsDataConv.bytesToHex(file_bytes).replace(" ", "\\x"); // 3 * file size
		} else {
			// Leave it in octal form here. Android 4.0.3 doesn't support hex with echo, it seems - but supports octal.
			bytes_data = "\\0" + UtilsDataConv.bytesToOctal(file_bytes).replace(" ", "\\0"); // 4 * file size
		}

		final String command = "echo -ne '" + bytes_data + "' > '" + file_path + "'";

		return UtilsShell.executeShellCmd(command, false, true).error_code;
	}

	/**
	 * <p>Copies the file on the source path to another file on the destination path.</p>
	 * <p>Only ONLY with small files!!! <strong>This function requires allocating 4-5 times the file size into
	 * memory!</strong></p>
	 *
	 * @param src_path the source file path
	 * @param dest_path the destination file path
	 *
	 * @return a SH shell exit code, or also -1 if the source file could not be read
	 */
	public static int copySmallFile(@NonNull final String src_path, @NonNull final String dest_path) {
		final byte[] src_bytes = readFileBytes(src_path); // First time with the file bytes
		if (null == src_bytes) {
			return -1;
		}

		return writeSmallFile(src_path, src_bytes); // Plus 3 or 4 times the size, depending on the API level
	}

	/**
	 * <p>Copies the source path to the destination path (file, directory, whatever).</p>
	 *
	 * @param src_path the source path
	 * @param dest_path the destination path
	 *
	 * @return a SH shell exit code
	 */
	@RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
	public static int copyPath(@NonNull final String src_path, @NonNull final String dest_path) {
		final String command = "cp -rf '" + src_path + "' '" + dest_path + "'";

		return UtilsShell.executeShellCmd(command, false, true).error_code;
	}

	/**
	 * <p>Sets the permissions of a path (to a file or folder or whatever it is).</p>
	 *
	 * @param path the path
	 * @param permissions the permissions
	 *
	 * @return a SH shell exit code
	 */
	public static int chmod(@NonNull final String path, final int permissions, final boolean recursive) {
		final String command = "chmod " + permissions + (recursive ? " -R " : " ") + "'" + path + "'";

		return UtilsShell.executeShellCmd(command, false, true).error_code;
	}

	/**
	 * <p>Checks if a path exists (file, directory, whatever).</p>
	 *
	 * @param path the path
	 *
	 * @return a SH shell exit code
	 */
	public static int checkPathExists(@NonNull final String path) {
		final String command = "ls '" + path+ "'";

		return UtilsShell.executeShellCmd(command, false, true).error_code;
	}
}
