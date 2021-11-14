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

import org.spongycastle.crypto.generators.SCrypt;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * <p>This utility class encrypts and decrypts the given data, using the method Scrypt-16384-8-1 + AES-256/CBC/PKCS#7 +
 * HMAC-SHA512 + UTF-7 + randomization.</p>
 * <br>
 * <p>Additional information:</p>
 * <p>- Initialization Vector of 128 bits (16 bytes) for both AES-CBC-PKCS#7 and HMAC.</p>
 * <p>- Key of 256 bits (32 bytes) to encrypt the data using AES-CBC-PKCS#7.</p>
 * <p>- Key of 512 bits (64 bytes) to calculate the HMAC tag.</p>
 * <p>- The keys are wiped from memory as soon as they're no longer needed.</p>
 * <p>- Constant-time preventions not taken in consideration.</p>
 * <p>- Data must be encoded in UTF-7 before being encrypted, and therefore, the result of the decryption will be UTF-7
 * encoded too.</p>
 * <p>- Random bytes with values from 128 to 255 are added to a random index between 0 and 15 each 16 bytes of the given
 * data (hence the UTF-7 encoding), to mitigate the issue that might happen if the IV is reused by chance, even though
 * it's generated using {@link SecureRandom}.</p>
 * <p><a href="https://crypto.stackexchange.com/questions/95061">This may be unnecessary</a>, but for now it's not
 * causing issues and {@link SecureRandom} seems not to be a TRNG... It might be an issue if it starts being too much
 * data to encrypt. In that case, this might be changed.</p>
 * <br>
 * <p>Note: to calculate the keys for AES (256 bits) and HMAC (512 bits), the SHA-512 hashes of 2 passwords are used.
 * Those hashes are then used to calculate the keys using Scrypt (N = 16384; r = 8; p = 1).</p>
 * <br>
 * <p>Various things learned from: <a href="https://proandroiddev.com/security-best-practices-symmetric-encryption-with-aes-in-java-7616beaaade9">
 * Security Best Practices: Symmetric Encryption with AES in Java and Android</a> and
 * <a href="https://proandroiddev.com/security-best-practices-symmetric-encryption-with-aes-in-java-and-android-part-2-b3b80e99ad36">
 * Security Best Practices: Symmetric Encryption with AES in Java and Android: Part 2: AES-CBC + HMAC</a>.</p>
 */
public final class UtilsCryptoEnDecrypt {

	private static final int IV_LENGTH_BYTES = 16; // 128 bits
	private static final int HMAC_TAG_LENGTH_BYTES = 64; // 512 bits
	private static final int AES_KEY_SIZE = 32; // 256 bits (for use with AES-256)
	private static final int HMAC_KEY_SIZE = 64; // 512 bits --> ALWAYS AT LEAST THE OUTPUT LENGTH FOR SECURITY (rules)
	private static final int INIT_LEN_ENCRYPTED_MSG = 1 + IV_LENGTH_BYTES + 1 + HMAC_TAG_LENGTH_BYTES;
	private static final String mac_algorithm = "HmacSHA512"; // API 1+
	private static final String cipher_algorithm = "AES/CBC/PKCS5Padding"; // API 1+
	// Actually uses PCCS#7. It seems PKCS#5 is equivalent to PKCS#7 in the Cipher specification, as said here:
	// https://stackoverflow.com/questions/20770072/#comment31139784_20770158.

	private static final String RAW_AAD_SEPARATOR_STR = " \\\\\\/// ";
	private static final byte[] RAW_AAD_PREFIX = ("Scrypt-16384-8-1 + AES-256/CBC/PKCS#7 + HMAC-SHA512 + UTF-7 +" +
			"randomization" + RAW_AAD_SEPARATOR_STR).getBytes(Charset.defaultCharset());
	private static final byte[] RAW_AAD_SEPARATOR = RAW_AAD_SEPARATOR_STR.getBytes(Charset.defaultCharset());

	// Scrypt parameters - tested a bit slow on MiTab Advance and slower on BV9500 (wtf). Good enough, I think.
	// Memory required to calculate the keys: 128 * N * r * p --> 128 * 16_384 * 8 * 1 = 16_777_216 B = 16.8 MB
	private static final int SCRYPT_N = 16_384;
	private static final int SCRYPT_R = 8;
	private static final int SCRYPT_P = 1;

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsCryptoEnDecrypt() {
	}

	/**
	 * <p>Encrypts the given data <strong>(encode with UTF-7 first)</strong> using the parameters defined in the class
	 * doc.</p>
	 * <br>
	 * <p><strong>ATTENTION:</strong> the passwords' order must NOT be changed once the passwords are used to encrypt
	 * some data! Use them always in the same order they were entered!</p>
	 *
	 * @param raw_password1 the first character sequence to calculate the 2 keys from
	 * @param raw_password2 the second character sequence to calculate the 2 keys from
	 * @param raw_data the data to encrypt
	 * @param raw_aad_suffix additional not encrypted metadata suffix to include in the encrypted message, right after
	 *                       {@link #RAW_AAD_PREFIX}; null if not to be used
	 *
	 * @return the encrypted message using the mentioned method, or null if the chosen algorithm was unable to process
	 * the data provided, or in case the device is running on low memory and probably won't be able to complete this
	 * function - check that with {@link UtilsGeneral#isDeviceRunningOnLowMemory()}.
	 */
	@Nullable
	public static byte[] encryptBytes(@NonNull final byte[] raw_password1, @NonNull final byte[] raw_password2,
									  @NonNull final byte[] raw_data, @NonNull final byte[] raw_aad_suffix) {
		if (UtilsGeneral.isDeviceRunningOnLowMemory()) {
			return null;
		}

		final byte[] raw_aad_ready = getAADReady(raw_aad_suffix);
		final byte[] randomized_raw_data = randomizeData(raw_data);
		final byte[] iv = getIv();
		final byte[][] keys = getKeys(raw_password1, raw_password2);
		final Cipher cipher = getCipher(keys[0], iv, true);
		Arrays.fill(keys[0], (byte) 0); // Wipe the AES key

		final byte[] cipher_text;
		try {
			cipher_text = cipher.doFinal(randomized_raw_data);
		} catch (final BadPaddingException ignored) {
			// Won't happen. Nothing ever changes on this class. It's all constant. Works here, will work everywhere
			// else.
			return new byte[0];
		} catch (final IllegalBlockSizeException ignored) {
			// Again, won't happen - EXCEPT the last part where it says the exception will be thrown if the chosen
			// encryption algorithm is unable to process the data provided.
			return null;
		}
		final byte[] hmac_tag = getHmacTag(keys[1], iv, cipher_text, raw_aad_ready);
		Arrays.fill(keys[1], (byte) 0); // Wipe the HMAC key

		final ByteBuffer byteBuffer = ByteBuffer.allocate(INIT_LEN_ENCRYPTED_MSG + cipher_text.length);
		byteBuffer.put((byte) IV_LENGTH_BYTES);
		byteBuffer.put(iv);
		byteBuffer.put((byte) HMAC_TAG_LENGTH_BYTES);
		byteBuffer.put(hmac_tag);
		byteBuffer.put(cipher_text);

		return byteBuffer.array();
	}

	/**
	 * <p>Decrypts the given data using the parameters defined in the class doc.</p>
	 * <br>
	 * <p><strong>ATTENTION:</strong> the passwords' order must NOT be changed once the passwords are used to encrypt
	 * some data! Use them always in the same order they were entered!</p>
	 *
	 * @param raw_password1 the first character sequence used to encrypt the data
	 * @param raw_password2 the second character sequence used to encrypt the data
	 * @param raw_encrypted_message the encrypted message
	 * @param raw_aad_suffix the associated authenticated data suffix used with the encrypted message; or null if not to
	 *                       be used
	 *
	 * @return the original message text; null in case either the message was not encrypted using the parameters
	 * defined in the class doc, in case it has been tampered with, or in case the device is running on low memory and
	 * probably won't be able to complete this function - check that with
	 * {@link UtilsGeneral#isDeviceRunningOnLowMemory()}.
	 */
	@Nullable
	public static byte[] decryptBytes(@NonNull final byte[] raw_password1, @NonNull final byte[] raw_password2,
									  @NonNull final byte[] raw_encrypted_message, @NonNull final byte[] raw_aad_suffix) {
		if (UtilsGeneral.isDeviceRunningOnLowMemory()) {
			return null;
		}

		final ByteBuffer byteBuffer = ByteBuffer.wrap(raw_encrypted_message);

		// If the encrypted message doesn't have more than the initial length for an encrypted message that this class
		// generates, stop now - not encrypted using this class's method then, so won't decrypt.
		// This will make no ByteBuffer.get() calls return underflow errors.
		if (raw_encrypted_message.length <= INIT_LEN_ENCRYPTED_MSG) {
			return null;
		}

		// Get data from the encrypted message //

		// Check the message IV length and get it
		if ((int) byteBuffer.get() != IV_LENGTH_BYTES) { // Get 1 byte
			// If it's not IV_LENGTH_BYTES, it wasn't encrypted by this class's method.
			return null;
		}
		final byte[] iv = new byte[IV_LENGTH_BYTES];
		byteBuffer.get(iv); // Get IV_LENGTH_BYTES bytes

		// Check the message HMAC length and get it
		if ((int) byteBuffer.get() != HMAC_TAG_LENGTH_BYTES) {
			// If it's not HMAC_TAG_LENGTH_BYTES, it wasn't encrypted by this class's method.
			return null;
		}
		final byte[] message_hmac = new byte[HMAC_TAG_LENGTH_BYTES];
		byteBuffer.get(message_hmac); // Get HMAC_TAG_LENGTH_BYTES bytes

		// Get the message cipher text
		final byte[] cipher_text = new byte[byteBuffer.remaining()]; // Get the rest of the bytes (at least one byte, or
		// no message was sent)
		byteBuffer.get(cipher_text);

		// Use the data to analyze and (if all is OK) decrypt the message //

		// Generate the keys from the password and get the AAD
		final byte[] raw_aad_ready = getAADReady(raw_aad_suffix);
		final byte[][] keys = getKeys(raw_password1, raw_password2);
		final byte[] supposed_hmac = getHmacTag(keys[1], iv, cipher_text, raw_aad_ready);
		Arrays.fill(keys[1], (byte) 0); // Wipe the HMAC key

		// Do NOT replace MessageDigest.isEqual() by ANY other unless it's a constant-time comparing function!
		// Else, Timing Attacks. I don't think this is needed here, but the article had it here. Better safe than sorry,
		// I guess. I don't understand enough of this to be sure it's not needed here.
		// EDIT: pretty sure it's not needed here. Both HMACs are known anyways. Just need to read the original encrypted
		// message, modify it and we have the modified HMAC. Then compare them.
		if (!Arrays.equals(supposed_hmac, message_hmac)) {
			// Check if the 2 MACs are equal. If they're not, the message has been tampered with.
			return null;
		}

		// All is OK, so let's decrypt the message

		// Prepare the cipher to decrypt
		final Cipher cipher = getCipher(keys[0], iv, false);
		Arrays.fill(keys[0], (byte) 0); // Wipe the AES key

		final byte[] randomized_message;

		try {
			randomized_message = cipher.doFinal(cipher_text);
		} catch (final IllegalStateException | BadPaddingException | IllegalBlockSizeException ignored) {
			// Will not happen. Nothing changes on the class, it's all constant. Works here, will work everywhere else.
			// About the not being able to process the data provided part - if the data was encrypted by this class's
			// method and the result was not null, then it's able to process the data. If it gets here and it's not able,
			// then either the decryption function is not well implemented (can't be - works here), or the message was
			// tempered with again - except it wasn't when it gets here, because of the MAC check. So won't happen.
			return null;
		}

		return derandomizeData(randomized_message);
	}

	/**
	 * <p>Randomizes UTF-7 encoded data for even safer encryption using this classes method.</p>
	 * <p>This is done by adding a random byte value from 128 to 255 in a random index each 16 elements. So, for example,
	 * on index 5 a 173 byte is added, and on index 18 a 209 byte is added.</p>
	 * <p>This means the new byte array will have {@code length = data.length + Math.ceil(data.length/16)}.</p>
	 * <p>Call {@link #derandomizeData(byte[])} to undo this.</p>
	 *
	 * @param data UFT-7 encoded data to be randomized
	 *
	 * @return the randomized data
	 */
	@NonNull
	private static byte[] randomizeData(@NonNull final byte[] data) {
		SecureRandom secureRandom = new SecureRandom();
		int random_index = -1;

		final int randomized_data_length = data.length + (int) Math.ceil((double) data.length / 16.0);
		final byte[] randomized_data = new byte[randomized_data_length];

		int num_added_bytes = 0;
		for (int i = 0; i < randomized_data_length; i++) {
			if (i % 16 == 0) {
				secureRandom = new SecureRandom(); // Reseed SecureRandom for security every 2 usages (here and below)
				random_index = i + secureRandom.nextInt(Math.min(randomized_data_length - i, 16)); // Up until index 15. From
				// 16 to 31 is another random byte and so on. But only if the remaining length is >= 16. If not, use the
				// remaining length and put a new byte in that range.
			}
			if (i == random_index) {
				// If it's to add a random byte at the chosen random index, add one between the values 128 and 255.
				// This means the data can NOT use byte values inside that range.
				randomized_data[i] = (byte) (secureRandom.nextInt(128) + 128);
				num_added_bytes++;
			} else {
				randomized_data[i] = data[i - num_added_bytes];
			}
		}

		return randomized_data;
	}

	/**
	 * <p>Undoes the randomization done by {@link #randomizeData(byte[])}.</p>
	 *
	 * @param randomized_data the randomized data
	 *
	 * @return the original data
	 */
	@NonNull
	private static byte[] derandomizeData(@NonNull final byte[] randomized_data) {
		final int data_length = (int) ((double) randomized_data.length / (1.0 + (1.0 / 16.0)));
		final byte[] data = new byte[data_length];
		int i = 0;
		for (final byte _byte : randomized_data) {
			if (UtilsGeneral.byteToIntUnsigned(_byte) <= 127) {
				data[i] = _byte;
				i++;
			}
		}

		return data;
	}

	/**
	 * <p>Get the Authenticated Associated Data (AAD) ready for use with correct prefixes.</p>
	 *
	 * @param raw_aad_suffix the additional AAD to add to the final one
	 *
	 * @return the ready AAD byte vector
	 */
	private static byte[] getAADReady(@NonNull final byte[] raw_aad_suffix) {
		final byte[] raw_aad_ready = new byte[RAW_AAD_PREFIX.length + raw_aad_suffix.length];
		System.arraycopy(RAW_AAD_PREFIX, 0, raw_aad_ready, 0, RAW_AAD_PREFIX.length);
		System.arraycopy(raw_aad_suffix, 0, raw_aad_ready, RAW_AAD_PREFIX.length, raw_aad_suffix.length);

		return raw_aad_ready;
	}

	/**
	 * <p>Creates an initialization vector as randomly as possible, with length of {@link #IV_LENGTH_BYTES} bytes to use
	 * with AES.</p>
	 *
	 * @return the initialization vector
	 */
	@NonNull
	private static byte[] getIv() {
		final SecureRandom secureRandom = new SecureRandom();
		final byte[] iv = new byte[IV_LENGTH_BYTES];
		secureRandom.nextBytes(iv); // As random as possible!

		return iv.clone();
	}

	/**
	 * <p>Get the keys to be used with AES and MAC according with the class doc.</p>
	 * <br>
	 * <p><strong>ATTENTION:</strong> the passwords' order must NOT be changed once the passwords are used to encrypt
	 * some data! Use them always in the same order they were entered!</p>
	 *
	 * @param password1 one of the passwords to use to create the keys
	 * @param password2 the other password to use to create the keys
	 *
	 * @return 1st index AES key; 2nd index, MAC key; null if there's not enough memory for the keys calculation
	 */
	@NonNull
	private static byte[][] getKeys(@NonNull final byte[] password1, @NonNull final byte[] password2) {
		final byte[] password1_sha512 = UtilsCryptoHashing.getHashBytesOfBytes(password1, 5);
		final byte[] password2_sha512 = UtilsCryptoHashing.getHashBytesOfBytes(password2, 5);

		//try {
			return new byte[][]{
					SCrypt.generate(password1_sha512, password2_sha512, SCRYPT_N, SCRYPT_R, SCRYPT_P, AES_KEY_SIZE),
					SCrypt.generate(password2_sha512, password1_sha512, SCRYPT_N, SCRYPT_R, SCRYPT_P, HMAC_KEY_SIZE),
			};
		/*} catch (final OutOfMemoryError ignored) {
			return null;
		}*/

		/*// This enormous list below is because I read multiple hashing is safer. And this doesn't impact at all on the
		// performance (since this is not supposed to be done very much anyways - once in hours or something).
		final byte[] md5 = UtilsCryptoHashing.getHashBytesOfBytes(password1, 0);
		final byte[] sha1 = UtilsCryptoHashing.getHashBytesOfBytes(md5, 1);
		final byte[] sha224 = UtilsCryptoHashing.getHashBytesOfBytes(sha1, 2);
		final byte[] sha256 = UtilsCryptoHashing.getHashBytesOfBytes(sha224, 3);
		final byte[] sha384 = UtilsCryptoHashing.getHashBytesOfBytes(sha256, 4);
		final byte[] sha512 = UtilsCryptoHashing.getHashBytesOfBytes(sha384, 5);

		final byte[] key_aes = Arrays.copyOfRange(sha512, sha512.length/2, sha512.length); // 256 bits of the last hash
		// to hopefully be the safest (this protects the data, so it's the most important one - the MAC one is less
		// important, because it protects the integrity and authenticity of the data, and if that goes public, at least
		// the data doesn't).
		final byte[] key_mac_part = Arrays.copyOfRange(sha512, 0, sha512.length/2);
		final byte[] key_mac = new byte[HMAC_KEY_SIZE]; // 512 bits
		System.arraycopy(key_mac_part, 0, key_mac, 0, key_mac_part.length); // Get bytes from the last hash and...
		System.arraycopy(sha384, 16, key_mac, key_mac_part.length, 32); // ... bytes from the second-last hash.

		return new byte[][]{key_aes, key_mac};*/
	}

	/**
	 * <p>Encrypt a byte array using the algorithm "AES/CBC/PKCS5Padding".</p>
	 *
	 * @param key_aes the key to be used to create the cipher text
	 * @param iv the initialization vector to create the cipher text
	 * @param encrypt true to prepare the Cipher to encrypt, false to prepare to decrypt
	 *
	 * @return the encrypted bytes
	 */
	@NonNull
	private static Cipher getCipher(@NonNull final byte[] key_aes, @NonNull final byte[] iv, final boolean encrypt) {
		// "AES" will choose which key size to be ready for (128, 196 or 256) depending on the given key.
		final Key secretKey_aes = new SecretKeySpec(key_aes, "AES");

		final Cipher cipher;
		try {
			cipher = Cipher.getInstance(cipher_algorithm);
		} catch (final NoSuchAlgorithmException | NoSuchPaddingException ignored) {
			// Not happening as long as the chosen algorithm exists since at least API 15.
			return null;
		}

		final int mode;
		if (encrypt) {
			mode = Cipher.ENCRYPT_MODE;
		} else {
			mode = Cipher.DECRYPT_MODE;
		}

		try {
			cipher.init(mode, secretKey_aes, new IvParameterSpec(iv));
		} catch (final UnsupportedOperationException | InvalidAlgorithmParameterException | InvalidKeyException ignored) {
			// Not happening also. The Cipher is being initialized with a constant method. Nothing ever changes. And
			// it works here, so will work everywhere else.
			return null;
		}

		return cipher;
	}

	/**
	 * <p>Creates a HMAC tag from the given bytes data using the algorithm defined in the class doc.</p>
	 *
	 * @param key_mac the key to create the tag
	 * @param iv the initialization vector to get the tag from
	 * @param cipher_text the encrypted data to get the tag from
	 * @param associated_authed_data additional not encrypted metadata to add to the tag, or null if not to use
	 *
	 * @return the tag bytes
	 */
	@NonNull
	private static byte[] getHmacTag(@NonNull final byte[] key_mac, @NonNull final byte[] iv,
									 @NonNull final byte[] cipher_text, @Nullable final byte[] associated_authed_data) {
		Key secretKey_hmac = new SecretKeySpec(key_mac, mac_algorithm);

		final Mac hmac;
		try {
			hmac = Mac.getInstance(mac_algorithm);
		} catch (final NoSuchAlgorithmException ignored) {
			// Not happening as long as the chosen algorithm exists since at least API 15.
			return new byte[0];
		}
		try {
			hmac.init(secretKey_hmac);
		} catch (final InvalidKeyException ignored) {
			// Again, nothing ever changes. The Mac is being initialized with a constant method. Works here, will work
			// everywhere else.
			return new byte[0];
		}

		// Don't delete this! It's wiping the variable.
		secretKey_hmac = new SecretKeySpec(new byte[]{(byte) 0}, mac_algorithm);

		hmac.update(iv);
		hmac.update(cipher_text);
		if (associated_authed_data != null) {
			hmac.update(associated_authed_data);
		}

		return hmac.doFinal();
	}
}
