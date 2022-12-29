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

import androidx.annotation.NonNull;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
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
	 * <p>Convert a byte array to printable characters in a string.</p>
	 * <p>Note: all bytes will be attempted to be printed, all based on the platform's default charset (on Android is
	 * always the UTF-8 charset).</p>
	 *
	 * @param byte_array the byte array
	 * @param utf7 true if the bytes were encoded using UTF-7, false if they were encoded using UTF-8
	 *
	 * @return a string containing printable characters representative of the provided bytes
	 */
	@NonNull
	public static String bytesToPrintable(@NonNull final byte[] byte_array, final boolean utf7) {
		if (utf7) {
			try {
				return new String(byte_array, GL_CONSTS.UTF7_NAME_LIB);
			} catch (final UnsupportedEncodingException ignored) {
				// Won't happen - UTF-7 is included in the project through com.beetstra.jutf7 library.
				return null;
			}
		} else {
			// The default charset on Android is always UTF-8 as stated in the method documentation, so all is ok
			return new String(byte_array, Charset.defaultCharset());
		}
	}

	private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
	/**
	 * <p>Converts the given byte array into a string of the type "00 00 00", in which the 0s are hexadecimal
	 * digits.</p>
	 *
	 * @param bytes the byte array
	 *
	 * @return the equivalent string
	 */
	@NonNull
	public static String bytesToHex(@NonNull final byte[] bytes) {
		// DO NOT CHANGE THE OUTPUT FORMAT WITHOUT CHECKING ALL USAGES!!!!!! "00 00 00" was chosen because it's easy to
		// replace the spaces by "\x" to write files, for example.

		final int bytes_length = bytes.length;
		final char[] hex_chars = new char[(bytes_length << 1) + bytes_length];
		for (int i = 0; i < bytes_length; i++) {
			final int positive_byte = (int) bytes[i] & 0xFF; // See why it works on byteToIntUnsigned()
			final int curr_idx = (i << 1) + i; // i * 3 but faster (no multiplications)
			hex_chars[curr_idx] = HEX_ARRAY[positive_byte >>> 4];
			hex_chars[curr_idx + 1] = HEX_ARRAY[positive_byte & 0x0F];
			hex_chars[curr_idx + 2] = ' ';
		}

		// Remove the last whitespace with trim().
		return new String(hex_chars).trim();
	}

	private static final char[] OCT_ARRAY = "01234567".toCharArray();
	/**
	 * <p>Converts the given byte array into a string of the type "000 000 000", in which the letters are octal
	 * digits.</p>
	 *
	 * @param bytes the byte array
	 *
	 * @return the equivalent string
	 */
	@NonNull
	public static String bytesToOctal(@NonNull final byte[] bytes) {
		// DO NOT CHANGE THE OUTPUT FORMAT WITHOUT CHECKING ALL USAGES!!!!!! "000 000 000" was chosen because it's easy
		// to replace the spaces by "\0" to write files, for example.

		final int bytes_length = bytes.length;
		final char[] hex_chars = new char[bytes_length << 2];
		for (int i = 0; i < bytes_length; i++) {
			final int positive_byte = (int) bytes[i] & 0xFF;
			final int curr_idx = i << 2;
			hex_chars[curr_idx] = OCT_ARRAY[(positive_byte & 0b11000000) >>> 6];
			hex_chars[curr_idx + 1] = OCT_ARRAY[(positive_byte & 0b00111000) >>> 3];
			hex_chars[curr_idx + 2] = OCT_ARRAY[positive_byte & 0b00000111];
			hex_chars[curr_idx + 3] = ' ';
		}

		// Remove the last whitespace with trim().
		return new String(hex_chars).trim();
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
			if (null != b) {
				dest_array[j] = b;
			}
		}

		return dest_array;
	}
}
