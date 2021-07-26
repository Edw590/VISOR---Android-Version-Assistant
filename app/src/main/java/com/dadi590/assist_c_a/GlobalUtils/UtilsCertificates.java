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

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.SigningInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <p>App signing certificate-related utilities.</p>
 */
public final class UtilsCertificates {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsCertificates() {
	}

	// Hashing algorithms to use in order, in case one or more are not available
	private static final String[][] hashing_algorithms = {
			{"SHA-512", "128"},
			{"SHA-384", "96"},
			{"SHA-224", "56"},
			{"SHA-256", "64"},
			{"SHA-1",   "32"},
			{"MD5",     "32"},
	};

	// App certificates fingerprint whitelist (one or more)
	private static final Map[] ASSIST_C_A_RSA_CERT_FINGERPRINT = {
			// Certificate 1
			new HashMap<String, String>() {
				private static final long serialVersionUID = -8864195772334229619L;

				{
					put("SHA-512", "E8AEBF2B968F1A7409D585A0450DAB3E722993F6EAA6C74FDED7841F6957494AAA66650BEA88A24B1" +
							"E3BDDEC884C1C4A40189EE5F732DA047BA11903177DF154");
					put("SHA-384", "9B46CD1EB9AC47C61D9D6525F14FBC11A3E52AD50C7C647ED23159E9CA54862DF455A33C3C0A16050" +
							"58998B054B3DB36");
					put("SHA-224", "1F286D3922CF9981DD56BE2C8F9A516DCF4A539A53E9BC99BEFACCBF781FDC69");
					put("SHA-256", "345791A9BC8B0470451DAFE394BFA18C0F01F4E1A1F76D310F62E5B0");
					put("SHA-1",   "A1D4660F109CE764CFE217562056C830C7C6CF60");
					put("MD5",     "2B66BB01D2D817E26900478AD4B0F7EF");
				}
			},
	};

	/**
	 * <p>Checks if the given package is signed with the same certificate as this app (my certificate).</p>
	 * <br>
	 * <p>NOTE: as this app is signed with a self-generated certificate, and only with that one, this function checks
	 * only if the given package is signed with ONLY this app's certificate and nothing else.</p>
	 *
	 * @param package_name the name of the package to check
	 *
	 * @return true if it's signed with the same certificate, false otherwise; null if the package was not found
	 */
	@Nullable
	public static Boolean isOtherPackageMine(@NonNull final String package_name) {
		String[] hashing_algorithm_to_use = null;
		for (final String[] hashing_algorithm : hashing_algorithms) {
			try {
				MessageDigest.getInstance(hashing_algorithm[0]);
				hashing_algorithm_to_use = hashing_algorithm;
				break;
			} catch (final NoSuchAlgorithmException ignored) {
				// Will never happen. I'm checking all methods. If NONE is available, wow. Someone must have deeply
				// modified the ROM or something.
				return null;
			}
		}

		final Signature[] other_app_signatures = getAppSignatures(package_name);
		if (other_app_signatures == null) {
			return null;
		}

		for (final Signature signature : other_app_signatures) {
			final String other_app_sig_hash = Objects.requireNonNull(getHashOfSignature(signature, hashing_algorithm_to_use));
			// It will always be non-null though, since if it would be null, then we wouldn't get here, since the
			// function would have returned null by now.

			boolean match_found = false;
			for (final Map cert_hash : ASSIST_C_A_RSA_CERT_FINGERPRINT) {
				if (other_app_sig_hash.equals(cert_hash.get(hashing_algorithm_to_use[0]))) {
					match_found = true;
					break;
				}
			}
			if (!match_found) {
				// If ANY of the certificates on the other app are no whitelisted here, it's not signed by me.
				// This mitigates the FakeID vulnerability mentioned on getAppSignatures().
				return false;
			}/* else {
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
					// If we're on Pie or above, then GET_SIGNING_CERTIFICATES was used. That means all certificates are
					// authorized to be on the app (we don't need to make checks for that, as opposite with
					// GET_SIGNATURES. So if one of them are my certificates, cool, since the app is authorized to used
					// them.
					return true;

					EDIT: Actually, there's the option of multiple signers. Just check if all the certificates match
					my whitelist and it's done. Won't be that much more or processing power anyway.
				}
			}*/
		}

		// If nothing made it return false on the for loop, then all certificates are trusted - so it's signed by me.
		return true;
	}

	/**
	 * <p>Gets the signatures of an app.</p>
	 * <br>
	 * <p><strong><u>SECURITY WARNING:</u></strong></p>
	 * <p>Read about the FakeID vulnerability. It happens on Android 4.4 KitKat and below. That's a serious security
	 * issue and must be MITIGATED MANUALLY on those Android versions, when checking the app signatures.</p>
	 * <p>Below, a quote from StackOverflow user ashoykh, ID 1917248
	 * (https://stackoverflow.com/questions/52041805/how-to-use-packageinfo-get-signing-certificates-in-api-28):</p>
	 * <p>"The code is returning true [for validated app] if it finds any certificate [coming from GET_SIGNATURES] that
	 * matches one from your whitelist [certificates constant-defined on code]. With the android [FakeID] vulnerability,
	 * if the signatures contained a signature from a malicious signer, your code still returns true. The mitigation for
	 * this vulnerability is to instead check ALL signatures returned from the package manager and return false if any
	 * of them aren't in your whitelist."</p>
	 * <br>
	 * <p>Note: it's said that the vulnerability was patched on KitKat, but a company detected the issue on KitKat too,
	 * so I'm assuming they fixed it in an update. So any device that doesn't receive updates, may update it, according
	 * to my thought. EDIT: Just read it was fixed on Lollipop. Confusion. Assume bug exists until Lollipop 5.0.</p>
	 *
	 * @param package_name the package to get the signatures from
	 *
	 * @return the app signatures, or null if the app is not installed
	 */
	@Nullable
	private static Signature[] getAppSignatures(@NonNull final String package_name) {
		final PackageInfo packageInfo;
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
			try {
				packageInfo = UtilsGeneral.getContext().getPackageManager().getPackageInfo(package_name,
						PackageManager.GET_SIGNING_CERTIFICATES);
			} catch (final PackageManager.NameNotFoundException ignored) {
				return null;
			}

			final SigningInfo signingInfo = packageInfo.signingInfo;
			if (signingInfo.hasMultipleSigners()) {
				return signingInfo.getApkContentsSigners();
			} else {
				return signingInfo.getSigningCertificateHistory();
			}
		} else {
			try {
				packageInfo = UtilsGeneral.getContext().getPackageManager().getPackageInfo(package_name,
						PackageManager.GET_SIGNATURES);

				// The exploit can only happen on Android 4.4 KitKat and below ("According to the researchers who found
				// the mobile security vulnerability, all devices from Android 2.1 to Android 4.4 are affected.").

				// The problem seems to be that it GET_SIGNATURES gets all certificates on the app, even if the app is
				// not authorized to have them. It doesn't have any chain-of-trust checks on it. Just gets them all and
				// developers have to create the chain-of-trust themselves and check it.

				// GET_SIGNING_CERTIFICATES seems to already do this, and only returns validated certificates, so no
				// need to do the above. So with this, any certificate in the return list is valid and we can check if
				// the app is signed by someone by just checking if there's any certificate signed by that person in
				// there. As opposite to GET_SIGNATURES, which gets everything and doesn't check anything, so
				// certificates can be valid or not. That's for the developer to verify.
			} catch (final PackageManager.NameNotFoundException ignored) {
				return null;
			}

			return packageInfo.signatures.clone();
		}
	}

	/**
	 * <p>Gets a Base64 encoded string of the hash of the given signature.</p>
	 *
	 * @param signature the signature to get the hash from
	 * @param hashing_algorithm the hashing algorithm to use
	 * @return the Base64 encoded string
	 */
	@Nullable
	private static String getHashOfSignature(@NonNull final Signature signature, @NonNull final String[] hashing_algorithm) {
		final MessageDigest messageDigest;
		try {
			messageDigest = MessageDigest.getInstance(hashing_algorithm[0]);
		} catch (final NoSuchAlgorithmException ignored) {
			// Will never happen, because from Android Developers or the class doc, SHA-512 is available from 1+
			// onwards on all API levels
			return null;
		}
		messageDigest.update(signature.toByteArray());

		return String.format("%" + hashing_algorithm[1] + "X", new BigInteger(1, messageDigest.digest()));
	}
}
