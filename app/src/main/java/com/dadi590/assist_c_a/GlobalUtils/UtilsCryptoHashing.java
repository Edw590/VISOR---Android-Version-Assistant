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

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * <p>Utilities related with cryptographic hashing algorithms.</p>
 */
public final class UtilsCryptoHashing {

	// Hashing algorithms to use in order, in case one or more are not available.
	// See more here: https://developer.android.com/reference/java/security/MessageDigest.
	// Preferably, use only those that are available from at least API 15 onwards.
	/**
	 * <p>Possible algorithms to use with the functions on this class.</p>
	 * <p>List of algorithms and respective indexes in parenthesis:</p>
	 * <p>- MD5 (0)</p>
	 * <p>- SHA-1 (1)</p>
	 * <p>- SHA-224 (2)</p>
	 * <p>- SHA-256 (3)</p>
	 * <p>- SHA-384 (4)</p>
	 * <p>- SHA-512 (5)</p>
	 */
	private static final String[][] possible_hashing_algorithms = {
			// ATTENTION - the implementation of UtilsCertificates#checkCertsPkg() depends on the format of this array.
			// If it's to be changed, check that function too.
			{"MD5",     "32"},
			{"SHA-1",   "32"},
			{"SHA-224", "56"},
			{"SHA-256", "64"},
			{"SHA-384", "96"},
			{"SHA-512", "128"},
	};

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsCryptoHashing() {
	}

	/**
	 * .
	 * @return the {@link #possible_hashing_algorithms} variable
	 */
	@NonNull
	public static String[][] getPossibleHashAlgorithms() {
		return possible_hashing_algorithms.clone();
	}

	/**
	 * <p>Same as in {@link #getHashBytesOfBytes(byte[], int)}, but instead, it returns a string.</p>
	 *
	 * @param byte_array same as in {@link #getHashBytesOfBytes(byte[], int)}
	 * @param index same as in {@link #getHashBytesOfBytes(byte[], int)}
	 *
	 * @return the hash string
	 */
	@NonNull
	public static String getHashStringOfBytes(@NonNull final byte[] byte_array, final int index) {
		return String.format("%" + possible_hashing_algorithms[index][1] + "X", new BigInteger(1,
				getHashBytesOfBytes(byte_array, index)));
	}

	/**
	 * <p>Gets a byte array of the hash of the given byte array, calculated by one of the algorithms present in
	 * {@link #possible_hashing_algorithms}.</p>
	 *
	 * @param byte_array the bytes to calculate the hash from
	 * @param index the index of one of the String[] arrays inside {@link #possible_hashing_algorithms}
	 *
	 * @return the hash string
	 */
	@NonNull
	public static byte[] getHashBytesOfBytes(@NonNull final byte[] byte_array, final int index) {
		final MessageDigest messageDigest;
		try {
			messageDigest = MessageDigest.getInstance(possible_hashing_algorithms[index][0]);
		} catch (final NoSuchAlgorithmException ignored) {
			// Will never happen as long as the list of possible algorithms contains only algorithms that are available
			// in all APIs supported by the app.
			return new byte[0];
		}
		messageDigest.update(byte_array);

		return messageDigest.digest();
	}
}
