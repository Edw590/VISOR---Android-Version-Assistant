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

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import UtilsSWA.UtilsSWA;

/**
 * <p>Utilities related to cryptographic hashing algorithms.</p>
 */
public final class UtilsCryptoHashing {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsCryptoHashing() {
	}

	/**
	 * <p>Checks if a file matches at least one of the given hashes.</p>
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link #IDX_MD5} --> for {@code index_hash_algorithm}: index of the MD5 algorithm</p>
	 * <p>- {@link #IDX_SHA1} --> for {@code index_hash_algorithm}: index of the SHA-1 algorithm</p>
	 * <p>- {@link #IDX_SHA256} --> for {@code index_hash_algorithm}: index of the SHA-256 algorithm</p>
	 * <p>- {@link #IDX_SHA384} --> for {@code index_hash_algorithm}: index of the SHA-384 algorithm</p>
	 * <p>- {@link #IDX_SHA512} --> for {@code index_hash_algorithm}: index of the SHA-512 algorithm</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @param file_path the complete file path
	 * @param hashes a list of hashes, all calculated with the same algorithm, to check the file against
	 *
	 * @return true if the file could be read (means it exists and is accessible) and matches at least one of the hashes,
	 * false otherwise
	 */
	public static boolean fileMatchesHash(@NonNull final String file_path, @NonNull final String[] hashes) {
		final byte[] file_bytes;
		try {
			file_bytes = FileUtils.readFileToByteArray(new File(file_path));
		} catch (final IOException ignored) {
			return false;
		}

		final String hash_file = UtilsSWA.getHashStringOfBytesCRYPTOHASHING(file_bytes);
		for (final String hash : hashes) {
			if (hash.equals(hash_file)) {
				return true;
			}
		}

		return false;
	}
}
