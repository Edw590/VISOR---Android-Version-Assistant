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

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * <p>Utilities related to cryptographic hashing algorithms.</p>
 */
public final class UtilsCryptoHashing {

	// Hashing algorithms to use in order of security.
	// See more here: https://developer.android.com/reference/java/security/MessageDigest.
	// Use only those that are available from at least API 15 onwards.
	/**
	 * <p>Possible algorithms to use with the functions on this class, all available in all supported app API levels.</p>
	 * <p>List of algorithms and respective indexes in parenthesis:</p>
	 * <p>- MD5 (0)</p>
	 * <p>- SHA-1 (1)</p>
	 * <p>- SHA-256 (2)</p>
	 * <p>- SHA-384 (3)</p>
	 * <p>- SHA-512 (4)</p>
	 */
	private static final String[][] POSSIBLE_HASHING_ALGORITHMS = {
			// ATTENTION - keep the safest algorithm on the last position! Its usages are based on that.
			// Format: 1st index: algorithm name; 2nd index: hexadecimal representation length
			// (https://www.php.net/manual/en/function.hash.php)
			{"MD5",     "32"},
			{"SHA-1",   "40"},
			{"SHA-256", "64"},
			{"SHA-384", "96"},
			{"SHA-512", "128"},
	};
	public static final int IDX_MAX_ALGO = POSSIBLE_HASHING_ALGORITHMS.length - 1;

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsCryptoHashing() {
	}

	/**
	 * .
	 * @return a clone of the {@link #POSSIBLE_HASHING_ALGORITHMS} variable
	 */
	@NonNull
	public static String[][] getPossibleHashAlgorithms() {
		return POSSIBLE_HASHING_ALGORITHMS.clone();
	}

	public static final int IDX_MD5 = 0;
	public static final int IDX_SHA1 = 1;
	public static final int IDX_SHA256 = 2;
	public static final int IDX_SHA384 = 3;
	public static final int IDX_SHA512 = 4;
	/**
	 * <p>Gets a byte array of the hash of the given byte array, calculated by one of the algorithms present in
	 * {@link #POSSIBLE_HASHING_ALGORITHMS}.</p>
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link #IDX_MAX_ALGO} --> for {@code index}: index of the last (and safest) algorithm</p>
	 * <p>- {@link #IDX_MD5} --> for {@code index}: index of the MD5 algorithm</p>
	 * <p>- {@link #IDX_SHA1} --> for {@code index}: index of the SHA-1 algorithm</p>
	 * <p>- {@link #IDX_SHA256} --> for {@code index}: index of the SHA-256 algorithm</p>
	 * <p>- {@link #IDX_SHA384} --> for {@code index}: index of the SHA-384 algorithm</p>
	 * <p>- {@link #IDX_SHA512} --> for {@code index}: index of the SHA-512 algorithm</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @param byte_array the bytes to calculate the hash from
	 * @param index the index of one of the String[] arrays inside {@link #POSSIBLE_HASHING_ALGORITHMS} --> one of the
	 * constants
	 *
	 * @return the hash string
	 */
	@NonNull
	public static byte[] getHashBytesOfBytes(@NonNull final byte[] byte_array, final int index) {
		final MessageDigest messageDigest;
		try {
			messageDigest = MessageDigest.getInstance(POSSIBLE_HASHING_ALGORITHMS[index][0]);
		} catch (final NoSuchAlgorithmException ignored) {
			// Will never happen as long as the list of possible algorithms contains only algorithms that are available
			// in all APIs supported by the app.
			return new byte[0];
		}
		messageDigest.update(byte_array);

		return messageDigest.digest();
	}

	/**
	 * <p>Same as in {@link #getHashBytesOfBytes(byte[], int)}, but instead, it returns a string (eg. "32B4A667AA8F").</p>
	 *
	 * @param byte_array same as in {@link #getHashBytesOfBytes(byte[], int)}
	 * @param index same as in {@link #getHashBytesOfBytes(byte[], int)}
	 *
	 * @return the hash string
	 */
	@NonNull
	public static String getHashStringOfBytes(@NonNull final byte[] byte_array, final int index) {
		final String format_specifier = "%" + POSSIBLE_HASHING_ALGORITHMS[index][1] + "X";
		return String.format(Locale.getDefault(), format_specifier, new BigInteger(1,
				getHashBytesOfBytes(byte_array, index)));
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
	 * @param index_hash_algorithm one of the constants (the index of the algorithm used to calculate the given hashes)
	 *
	 * @return true if the file could be read (means it exists and is accessible) and matches at least one of the hashes,
	 * false otherwise
	 */
	public static boolean fileMatchesHash(@NonNull final String file_path, @NonNull final String[] hashes,
										  final int index_hash_algorithm) {
		final byte[] file_bytes;
		try {
			file_bytes = FileUtils.readFileToByteArray(new File(file_path));
		} catch (final IOException ignored) {
			return false;
		}

		final String hash_file = UtilsCryptoHashing.getHashStringOfBytes(file_bytes, index_hash_algorithm);
		for (final String hash : hashes) {
			if (hash.equals(hash_file)) {
				return true;
			}
		}

		return false;
	}
}
