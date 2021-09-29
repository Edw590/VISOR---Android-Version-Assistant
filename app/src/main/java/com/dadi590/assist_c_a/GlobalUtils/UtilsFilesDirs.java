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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

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
	 * <p>Deletes a file or directory (empty or not).</p>
	 *
	 * @param abs_file_path the absolute file or directory path
	 *
	 * @return true if deletion was completely successful, including all files if a non-empty folder was selected for
	 * deletion; false if the app has no read and/or write permissions on the chosen file, directory, or at least one of
	 * child element(s)
	 */
	public static boolean deletePath(@NonNull final File abs_file_path) {
		if (abs_file_path.isDirectory()) {
			final String[] children = abs_file_path.list();
			boolean success = true;
			if (children == null) {
				return false;
			} else {
				for (final String child : children) {
					success = success && deletePath(new File(abs_file_path, child));
				}
			}

			return success && abs_file_path.delete();
		} else if (abs_file_path.isFile()) {
			return abs_file_path.delete();
		} else {
			return false;
		}
	}

	/**
	 * <p>Write a file to the External Storage.</p>
	 *
	 * @param abs_file_path the absolute file path
	 * @param data the data to be written to the file
	 * @param append true to append, false to write from the beginning
	 *
	 * @return true if the operation exited successfully, false otherwise; null if the app has no write permissions on
	 * the chosen file
	 */
	@Nullable
	public static Boolean writeFile(@NonNull final String abs_file_path, @NonNull final byte[] data,
									final boolean append) {
		final String abs_file_path_corrected = abs_file_path.replace('\\', '/');
		final File file = new File(abs_file_path_corrected);
		try {
			// This creates all directories needed to get to the file.
			new File(abs_file_path_corrected.substring(0, abs_file_path_corrected.lastIndexOf((int) '/'))).mkdirs();
		} catch (final SecurityException ignored) {
			return null;
		}

		try (final FileOutputStream fileOutputStream = new FileOutputStream(file, append)) {
			fileOutputStream.write(data);

			System.out.println("GGGGGGGGGGGGGGGG");

			return true;
		} catch (final FileNotFoundException ignored) {
			return null;
		} catch (final IOException ignored) {
		}

		return false;
	}

	/**
	 * <p>Reads a file and stores its data in a byte array.</p>
	 * <p>ATTENTION: no more than 10 MiB can be loaded using this function, to prevent bugs (and associated crashes).</p>
	 *
	 * @param abs_file_path the absolute file path
	 *
	 * @return a byte array with the contents of the file; null if more than the said file size was requested to be
	 * loaded, there was no file, the path is to a folder, there are no read permissions on the file for the app, or
	 * there is no memory available to read the file to RAM
	 */
	@Nullable
	public static byte[] readFileExtStge(@NonNull final String abs_file_path) {
		final File file = new File(abs_file_path);
		final int file_size = (int) file.length();
		if (file_size > 10_485_760) {
			// Not larger than 10 MiB, at least for now (not needed).
			return null;
		}
		final byte[] file_bytes;
		try {
			file_bytes = new byte[file_size];
		} catch (final OutOfMemoryError ignored) {
			return null;
		}
		try (final BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file))) {
			// Don't use read(byte[]) here. That one says it blocks waiting for input (not sure what that means). This
			// one I think it doesn't.
			bufferedInputStream.read(file_bytes, 0, file_bytes.length);

			return file_bytes;
		} catch (final FileNotFoundException ignored) {
		} catch (final IOException ignored) {
		} catch (final SecurityException ignored) {
		}

		return null;
	}

	/**
	 * <p>Creates a directory in the external storage.</p>
	 *
	 * @param abs_folder_path the absolute folder path
	 *
	 * @return the {@link File} instance for the chosen directory if the operation exited successfully (the directory
	 * already existed or was created); null if the app has no read and/or write permissions on the chosen directory or
	 * there was some other problem with {@link File#mkdirs()}
	 */
	@Nullable
	public static File createDirectory(@NonNull final String abs_folder_path) {
		final File folder = new File(abs_folder_path);

		try {
			// Create the storage directory if it does not exist
			if (!folder.exists()) {
				if (folder.mkdirs()) {
					return folder;
				}
			}
		} catch (final SecurityException ignored) {
			// No read and/or write permissions.
		}

		return null;
	}
}
