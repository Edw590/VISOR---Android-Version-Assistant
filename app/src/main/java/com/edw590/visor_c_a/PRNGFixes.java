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

/*
 * This software is provided 'as-is', without any express or implied
 * warranty.  In no event will Google be held liable for any damages
 * arising from the use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, as long as the origin is not misrepresented.
 */

package com.edw590.visor_c_a;

import android.os.Build;
import android.os.Process;

import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.SecureRandomSpi;
import java.security.Security;

/**
 * Fixes for the output of the default PRNG having low entropy.
 *
 * The fixes need to be applied via {@link #apply()} before any use of Java
 * Cryptography Architecture primitives. A good place to invoke them is in the
 * application's {@code onCreate}.
 *
 * <p>More about this on this
 * <a href="https://android-developers.googleblog.com/2013/08/some-securerandom-thoughts.html">Google blog post</a></p>
 */
public final class PRNGFixes {

	private static final int VERSION_CODE_JELLY_BEAN = 16;
	private static final int VERSION_CODE_JELLY_BEAN_MR2 = 18;
	private static final byte[] BUILD_FINGERPRINT_AND_DEVICE_SERIAL =
			getBuildFingerprintAndDeviceSerial();

	/** Hidden constructor to prevent instantiation. */
	private PRNGFixes() {}

	/**
	 * Applies all fixes.
	 *
	 * @throws SecurityException if a fix is needed but could not be applied.
	 */
	public static void apply() {
		applyOpenSSLFix();
		installLinuxPRNGSecureRandom();
	}

	/**
	 * Applies the fix for OpenSSL PRNG having low entropy. Does nothing if the
	 * fix is not needed.
	 *
	 * @throws SecurityException if the fix is needed but could not be applied.
	 */
	private static void applyOpenSSLFix() {
		if ((Build.VERSION.SDK_INT < VERSION_CODE_JELLY_BEAN)
				|| (Build.VERSION.SDK_INT > VERSION_CODE_JELLY_BEAN_MR2)) {
			// No need to apply the fix
			return;
		}

		try {
			// Mix in the device- and invocation-specific seed.
			Class.forName("org.apache.harmony.xnet.provider.jsse.NativeCrypto")
					.getMethod("RAND_seed", byte[].class)
					.invoke(null, (Object) generateSeed());

			// Mix output of Linux PRNG into OpenSSL's PRNG
			final Method method = Class.forName("org.apache.harmony.xnet.provider.jsse.NativeCrypto")
					.getMethod("RAND_load_file", String.class, long.class);
			final Integer bytesRead = (Integer) method.invoke(null, "/dev/urandom", 1024);
			assert bytesRead != null;
			if (bytesRead != 1024) {
				throw new IOException(
						"Unexpected number of bytes read from Linux PRNG: "
								+ bytesRead);
			}
		} catch (final Exception e) {
			throw new SecurityException("Failed to seed OpenSSL PRNG", e);
		}
	}

	/**
	 * Installs a Linux PRNG-backed {@code SecureRandom} implementation as the
	 * default. Does nothing if the implementation is already the default or if
	 * there is not need to install the implementation.
	 *
	 * @throws SecurityException if the fix is needed but could not be applied.
	 */
	private static void installLinuxPRNGSecureRandom() {
		if (Build.VERSION.SDK_INT > VERSION_CODE_JELLY_BEAN_MR2) {
			// No need to apply the fix
			return;
		}

		// Install a Linux PRNG-based SecureRandom implementation as the
		// default, if not yet installed.
		final Provider[] secureRandomProviders =
				Security.getProviders("SecureRandom.SHA1PRNG");
		if ((secureRandomProviders == null)
				|| (secureRandomProviders.length < 1)
				|| (LinuxPRNGSecureRandomProvider.class != secureRandomProviders[0].getClass())) {
			Security.insertProviderAt(new LinuxPRNGSecureRandomProvider(), 1);
		}

		// Assert that new SecureRandom() and
		// SecureRandom.getInstance("SHA1PRNG") return a SecureRandom backed
		// by the Linux PRNG-based SecureRandom implementation.
		final SecureRandom rng1 = new SecureRandom();
		if (LinuxPRNGSecureRandomProvider.class != rng1.getProvider().getClass()) {
			throw new SecurityException(
					"new SecureRandom() backed by wrong Provider: "
							+ rng1.getProvider().getClass());
		}

		final SecureRandom rng2;
		try {
			rng2 = SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException e) {
			throw new SecurityException("SHA1PRNG not available", e);
		}
		if (LinuxPRNGSecureRandomProvider.class != rng2.getProvider().getClass()) {
			throw new SecurityException(
					"SecureRandom.getInstance(\"SHA1PRNG\") backed by wrong"
							+ " Provider: " + rng2.getProvider().getClass());
		}
	}

	/**
	 * {@code Provider} of {@code SecureRandom} engines which pass through
	 * all requests to the Linux PRNG.
	 */
	private static class LinuxPRNGSecureRandomProvider extends Provider {

		public LinuxPRNGSecureRandomProvider() {
			super("LinuxPRNG",
					1.0,
					"A Linux-specific random number provider that uses"
							+ " /dev/urandom");
			// Although /dev/urandom is not a SHA-1 PRNG, some apps
			// explicitly request a SHA1PRNG SecureRandom and we thus need to
			// prevent them from getting the default implementation whose output
			// may have low entropy.
			put("SecureRandom.SHA1PRNG", LinuxPRNGSecureRandom.class.getName());
			put("SecureRandom.SHA1PRNG ImplementedIn", "Software");
		}
	}

	/**
	 * {@link SecureRandomSpi} which passes all requests to the Linux PRNG
	 * ({@code /dev/urandom}).
	 */
	public static class LinuxPRNGSecureRandom extends SecureRandomSpi {

		/*
		 * IMPLEMENTATION NOTE: Requests to generate bytes and to mix in a seed
		 * are passed through to the Linux PRNG (/dev/urandom). Instances of
		 * this class seed themselves by mixing in the current time, PID, UID,
		 * build fingerprint, and hardware serial number (where available) into
		 * Linux PRNG.
		 *
		 * Concurrency: Read requests to the underlying Linux PRNG are
		 * serialized (on sLock) to ensure that multiple threads do not get
		 * duplicated PRNG output.
		 */

		private static final File URANDOM_FILE = new File("/dev/urandom");

		private static final Object sLock = new Object();

		/**
		 * Input stream for reading from Linux PRNG or {@code null} if not yet
		 * opened.
		 *
		 * @GuardedBy("sLock")
		 */
		private static DataInputStream sUrandomIn = null;

		/**
		 * Output stream for writing to Linux PRNG or {@code null} if not yet
		 * opened.
		 *
		 * @GuardedBy("sLock")
		 */
		private static OutputStream sUrandomOut = null;

		/**
		 * Whether this engine instance has been seeded. This is needed because
		 * each instance needs to seed itself if the client does not explicitly
		 * seed it.
		 */
		private boolean mSeeded = false;

		@Override
		protected final void engineSetSeed(final byte[] bytes) {
			try {
				final OutputStream out;
				synchronized (sLock) {
					out = getUrandomOutputStream();
				}
				out.write(bytes);
				out.flush();
			} catch (final IOException ignored) {
				// On a small fraction of devices /dev/urandom is not writable.
				// Log and ignore.
				//Log.iw(PRNGFixes.class.getSimpleName(), "Failed to mix seed into " + URANDOM_FILE);
			} finally {
				mSeeded = true;
			}
		}

		@Override
		protected final void engineNextBytes(final byte[] bytes) {
			if (!mSeeded) {
				// Mix in the device- and invocation-specific seed.
				engineSetSeed(generateSeed());
			}

			try {
				final DataInputStream in;
				synchronized (sLock) {
					in = getUrandomInputStream();
				}
				synchronized (in) {
					in.readFully(bytes);
				}
			} catch (final IOException e) {
				throw new SecurityException(
						"Failed to read from " + URANDOM_FILE, e);
			}
		}

		@Override
		protected final byte[] engineGenerateSeed(final int size) {
			final byte[] seed = new byte[size];
			engineNextBytes(seed);
			return seed;
		}

		private static DataInputStream getUrandomInputStream() {
			synchronized (sLock) {
				if (sUrandomIn == null) {
					// NOTE: Consider inserting a BufferedInputStream between
					// DataInputStream and FileInputStream if you need higher
					// PRNG output performance and can live with future PRNG
					// output being pulled into this process prematurely.
					try {
						sUrandomIn = new DataInputStream(
								new FileInputStream(URANDOM_FILE));
					} catch (final IOException e) {
						throw new SecurityException("Failed to open "
								+ URANDOM_FILE + " for reading", e);
					}
				}
				return sUrandomIn;
			}
		}

		private static OutputStream getUrandomOutputStream() throws IOException {
			synchronized (sLock) {
				if (sUrandomOut == null) {
					sUrandomOut = new FileOutputStream(URANDOM_FILE);
				}
				return sUrandomOut;
			}
		}
	}

	/**
	 * Generates a device- and invocation-specific seed to be mixed into the
	 * Linux PRNG.
	 */
	private static byte[] generateSeed() {
		try {
			final ByteArrayOutputStream seedBuffer = new ByteArrayOutputStream();
			final DataOutputStream seedBufferOut =
					new DataOutputStream(seedBuffer);
			seedBufferOut.writeLong(System.currentTimeMillis());
			seedBufferOut.writeLong(System.nanoTime());
			seedBufferOut.writeInt(Process.myPid());
			seedBufferOut.writeInt(Process.myUid());
			seedBufferOut.write(BUILD_FINGERPRINT_AND_DEVICE_SERIAL);
			seedBufferOut.close();
			return seedBuffer.toByteArray();
		} catch (IOException e) {
			throw new SecurityException("Failed to generate seed", e);
		}
	}

	/**
	 * Gets the hardware serial number of this device.
	 *
	 * @return serial number or {@code null} if not available.
	 */
	@Nullable
	private static String getDeviceSerialNumber() {
		// We're using the Reflection API because Build.SERIAL is only available
		// since API Level 9 (Gingerbread, Android 2.3).
		try {
			return (String) Build.class.getField("SERIAL").get(null);
		} catch (final Exception ignored) {
			return null;
		}
	}

	private static byte[] getBuildFingerprintAndDeviceSerial() {
		final StringBuilder result = new StringBuilder();
		final String fingerprint = Build.FINGERPRINT;
		if (fingerprint != null) {
			result.append(fingerprint);
		}
		final String serial = getDeviceSerialNumber();
		if (serial != null) {
			result.append(serial);
		}

		// This below was set to UTF-8, but I've changed to defaultCharset(), which is the same in Android.
		return result.toString().getBytes(Charset.defaultCharset());
	}
}
