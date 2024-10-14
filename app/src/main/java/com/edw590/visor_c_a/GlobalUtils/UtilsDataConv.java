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

import androidx.annotation.NonNull;

import java.util.List;

/**
 * <p>Utilities related to converting between data types.</p>
 */
public final class UtilsDataConv {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsDataConv() {
	}

	/**
	 * <p>Converts a {@code byte} to an "unsigned" {@code int}.</p>
	 * <p>Useful if the byte is supposed to be used with indexes on arrays and stuff (must be non-negative then).</p>
	 *
	 * @param b the {@code byte} to be converted
	 *
	 * @return the converted byte to an "unsigned" {@code int}
	 */
	public static int byteToUnsigned(final byte b) {
		// 'Will print a negative int -56 because upcasting byte to int does so called "sign extension" which yields
		// those bits: 1111 1111 1111 1111 1111 1111 1100 1000 (-56)' - https://stackoverflow.com/a/4266841/8228163
		// This below will zero everything out except the first 8 bits - so a positive number (the unsigned byte)
		// remains.
		return (int) b & 0xFF;
	}

	/**
	 * <p>Copies a {@link List}<{@link Byte}> into a {@code byte[]}.</p>
	 *
	 * @param src_array the source array
	 *
	 * @return the byte array
	 */
	@NonNull
	public static byte[] listToBytesArray(@NonNull final List<Byte> src_array) {
		// Way of converting to bytes, since ArrayList won't let me convert to a primitive type
		final int src_array_size = src_array.size();
		final byte[] dest_array;
		dest_array = new byte[src_array_size]; // Possible OutOfMemoryError here
		for (int j = 0; j < src_array_size; ++j) {
			final Byte b = src_array.get(j);
			if (b != null) {
				dest_array[j] = b;
			}
		}

		return dest_array;
	}
}
