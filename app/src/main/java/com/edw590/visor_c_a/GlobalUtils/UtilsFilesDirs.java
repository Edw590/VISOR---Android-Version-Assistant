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

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import UtilsSWA.UtilsSWA;
import kotlin.io.FilesKt;

/**
 * <p>Utilities related to files and directories.</p>
 * <p>All the functions try to do what they're supposed through SDK methods. If it's not possible (no permissions, for
 * example), they'll resort to shell commands, in which they will attempt to request SU permission.</p>
 * <p>For the reason above, all functions work with shell exit codes - get some from {@link UtilsShell.ErrCodes}.</p>
 */
public final class UtilsFilesDirs {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsFilesDirs() {
	}





	// ALWAYS TEST SHELL COMMANDS IN OLD ANDROID VERSIONS!!!!! For example, hex in echo doesn't work in old versions.





	/**
	 * <p>Removes a path (to whatever), empty or not, using the rm shell command.</p>
	 *
	 * @param path the path
	 * @param recursive true to apply recursively, false to apply only to the given path
	 *
	 * @return same as {@link UtilsShell#executeShellCmd(boolean, String)} or -1 also in case there's some error
	 * deleting the path
	 */
	public static int removePath(@NonNull final GPath path, final boolean recursive) {
		try {
			final File path_file = new File(path.toString());
			if (recursive ? FilesKt.deleteRecursively(path_file) : path_file.delete()) {
				return UtilsShell.ErrCodes.NO_ERR;
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}

		final String command = "rm -f" + (recursive ? "r" : "") + "'" + path + "'";

		return UtilsShell.executeShellCmd(true, command).exit_code;
	}

	/**
	 * <p>Creates a path and all necessary but non-existent parent directories.</p>
	 *
	 * @param path the path to the create
	 *
	 * @return same as {@link UtilsShell#executeShellCmd(boolean, String)}
	 */
	public static int createDirectory(@NonNull final GPath path) {
		try {
			final File path_file = new File(path.toString());
			if (path_file.mkdirs()) {
				return UtilsShell.ErrCodes.NO_ERR;
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}

		final String command = "mkdir -p '" + path + "'";

		return UtilsShell.executeShellCmd(true, command).exit_code;
	}

	/**
	 * <p>Move file or directory to directory.</p>
	 *
	 * @param src_path the source path
	 * @param dest_path the destination path
	 *
	 * @return same as {@link UtilsShell#executeShellCmd(boolean, String)}
	 */
	public static int movePath(@NonNull final GPath src_path, @NonNull final GPath dest_path) {
		try {
			final File src_path_file = new File(src_path.toString());
			final File dest_path_file = new File(dest_path.toString());
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				Files.move(src_path_file.toPath(), dest_path_file.toPath(), StandardCopyOption.REPLACE_EXISTING);

				return UtilsShell.ErrCodes.NO_ERR;
			} else if (src_path_file.renameTo(dest_path_file)) {
				return UtilsShell.ErrCodes.NO_ERR;
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}

		final String command = "mv -f '" + src_path + "' '" + dest_path + "'";

		return UtilsShell.executeShellCmd(true, command).exit_code;
	}

	/**
	 * <p>Gets the size of a file.</p>
	 *
	 * @param file_path the path to the file
	 *
	 * @return the file size in bytes, or -1 if an error occurred (no permissions or no file)
	 */
	public static long getFileSize(@NonNull final GPath file_path) {
		try {
			final File file = new File(file_path.toString());
			if (file.exists()) {
				return file.length();
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}

		final String command = "ls -l '" + file_path + "'";
		final UtilsShell.CmdOutput cmdOutput = UtilsShell.executeShellCmd(true, command);

		if (cmdOutput.exit_code != 0) {
			return -1L;
		}

		final String output_data = UtilsSWA.bytesToPrintableDATACONV(cmdOutput.output_stream, false);

		return Long.parseLong(output_data.split(" ")[3]);
	}

	/**
	 * <p>Reads the bytes from the given file using the cat shell command.</p>
	 *
	 * @param file_path the path to the file
	 *
	 * @return the bytes of the file or null in case there was an error reading the file or an error occurred
	 */
	@Nullable
	public static byte[] readFileBytes(@NonNull final GPath file_path) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			try {
				return Files.readAllBytes(new File(file_path.toString()).toPath());
			} catch (final Exception e) {
				e.printStackTrace();
			} catch (final OutOfMemoryError ignored) {
				return null;
			}
		}

		final String command = "cat '" + file_path + "'";

		// Ignore the warning about the clone. No problem with it. This is a utility method. It won't use the object
		// for anything other than to return it. Performance gained (imagine it's a big file...).
		return UtilsShell.executeShellCmd(true, command).output_stream;
	}

	/**
	 * <p>Writes the given files bytes to a file (replaces all file contents).</p>
	 * <p>ATTENTION: try not to give too big files to this function without checking write permissions, because the
	 * function that uses shell commands is (notice the name) {@link #writeSmallFile(GPath, byte[])}.</p>
	 * <p>ATTENTION: if {@link FileUtils#writeByteArrayToFile(File, byte[])} can't write the file,
	 * {@link #writeSmallFile(GPath, byte[])} will be called which does NOT create parent directories!</p>
	 *
	 * @param file_path the path to the file
	 * @param file_bytes the bytes to write
	 *
	 * @return same as {@link UtilsShell#executeShellCmd(boolean, String)}
	 */
	public static int writeFile(@NonNull final GPath file_path, @NonNull final byte[] file_bytes) {
		try {
			FileUtils.writeByteArrayToFile(new File(file_path.toString()), file_bytes);

			return UtilsShell.ErrCodes.NO_ERR;
		} catch (final Exception e) {
			e.printStackTrace();
		}

		return writeSmallFile(file_path, file_bytes);
	}

	/**
	 * <p>Same as {@link #writeFile(GPath, byte[])}, but only uses a shell command.</p>
	 * <p>ONLY with small files!!! <strong>This function allocates 4-5 times the file size into memory!</strong></p>
	 * <p>Allocates 4 times more on KitKat+, and 5 times more below that.</p>
	 * <p>ATTENTION: this function does NOT create parent directories!</p>
	 */
	private static int writeSmallFile(@NonNull final GPath file_path, @NonNull final byte[] file_bytes) {
		final String bytes_data;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			// This is here because hex takes less size on memory than octal when in string representation (2 vs 3 chars).
			// On miTab Advance (KitKat 4.4.2), hex is supported, so I'm putting it as minimum for it (didn't check
			// APIs 16-18 though).
			bytes_data = "\\x" + UtilsSWA.bytesToHexDATACONV(file_bytes).replace(" ", "\\x"); // 4 * file size
		} else {
			// Leave it in octal form here. Android 4.0.3 doesn't support hex with echo, it seems - but supports octal.
			bytes_data = "\\0" + UtilsSWA.bytesToOctalDATACONV(file_bytes).replace(" ", "\\0"); // 5 * file size
		}

		final String command = "echo -ne '" + bytes_data + "' > '" + file_path + "'";

		return UtilsShell.executeShellCmd(true, command).exit_code;
	}

	/**
	 * <p>Copies the source path to the destination path (file, directory, whatever).</p>
	 * <p>ATTENTION: try not to give too big files to this function without checking write permissions and SDK version,
	 * because... read this: {@link #writeFile(GPath, byte[])} - can only happen below Android KitKat.</p>
	 *
	 * @param src_path the source file path
	 * @param dest_path the destination file path
	 *
	 * @return if on KitKat+, same as {@link UtilsShell#executeShellCmd(boolean, String)}; else, aside from that,
	 * -1 if the source file could not be read
	 */
	public static int copyPath(@NonNull final GPath src_path, @NonNull final GPath dest_path) {
		try {
			FileUtils.copyFile(new File(src_path.toString()), new File(dest_path.toString()));

			return UtilsShell.ErrCodes.NO_ERR;
		} catch (final Exception e) {
			e.printStackTrace();
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			// cp was added on KitKat
			final String command = "cp -rf '" + src_path + "' '" + dest_path + "'";

			return UtilsShell.executeShellCmd(true, command).exit_code;
		} else {
			final byte[] src_bytes = readFileBytes(src_path);
			if (src_bytes == null) {
				return -1;
			}

			return writeFile(dest_path, src_bytes); // 4 times the size
		}
	}

	/**
	 * <p>Sets the permissions of a path (to a file or folder or whatever it is).</p>
	 *
	 * @param path the path
	 * @param permissions the permissions
	 * @param recursive true to apply recursively, false to apply only to the given path
	 *
	 * @return same as {@link UtilsShell#executeShellCmd(boolean, String)}
	 */
	public static int chmodMISSING_SDK_METHOD(@NonNull final GPath path, final int permissions, final boolean recursive) {
		final String command = "chmod " + permissions + (recursive ? " -R " : " ") + "'" + path + "'";

		return UtilsShell.executeShellCmd(true, command).exit_code;
	}

	/**
	 * <p>Checks if a path exists (file, directory, whatever).</p>
	 *
	 * @param path the path
	 *
	 * @return same as {@link UtilsShell#executeShellCmd(boolean, String)}, with 0 meaning the path exists
	 */
	public static int checkPathExists(@NonNull final GPath path) {
		try {
			return new File(path.toString()).exists() ?
					UtilsShell.ErrCodes.NO_ERR : UtilsShell.ErrCodes.WRONG_USAGE;
		} catch (final Exception e) {
			e.printStackTrace();
		}

		final String command = "ls '" + path + "'";

		return UtilsShell.executeShellCmd(true, command).exit_code;
	}
}
