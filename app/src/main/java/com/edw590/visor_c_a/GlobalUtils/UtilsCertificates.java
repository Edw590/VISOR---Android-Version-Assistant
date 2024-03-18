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

import android.annotation.SuppressLint;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.SigningInfo;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Map;
import java.util.Objects;

import UtilsSWA.UtilsSWA;

/**
 * <p>App signing certificate-related utilities.</p>
 */
public final class UtilsCertificates {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsCertificates() {
	}

	/** App certificates fingerprints whitelist (one or more), calculated with SHA-512. */
	private static final String[] VISOR_C_A_RSA_CERTS_FINGERPRINTS = {
			"E8AEBF2B968F1A7409D585A0450DAB3E722993F6EAA6C74FDED7841F6957494AAA66650BEA88A24B1E3BDDEC884C1C4A40189EE5" +
					"F732DA047BA11903177DF154",
	};

	/**
	 * <p>Checks if the given package is signed with the same certificate(s) as this app.</p>
	 *
	 * @param package_name same as in {@link #checkCertsPkg(String, Map[])}
	 *
	 * @return same as in {@link #checkCertsPkg(String, Map[])}
	 */
	@Nullable
	public static Boolean isOtherPkgSignedSame(@NonNull final String package_name) {
		return checkCertsPkg(package_name, VISOR_C_A_RSA_CERTS_FINGERPRINTS);
	}

	/**
	 * <p>Checks if the app became corrupt (done by checking its signatures).</p>
	 *
	 * @return true if it's not corrupt, false otherwise
	 */
	public static boolean isThisAppCorrupt() {
		// Below will never be null. Else, how is the app installed.
		return !Objects.requireNonNull(checkCertsPkg(UtilsContext.getContext().getPackageName(),
				VISOR_C_A_RSA_CERTS_FINGERPRINTS));
	}

	/**
	 * <p>Checks if the given package is signed ONLY with certificates present in the given list.</p>
	 *
	 * @param package_name the name of the package to check
	 * @param list_cert_hashes a list of certificate hashes the package can be signed with, calculated with SHA-512
	 *
	 * @return true if it's signed with the same certificate, false otherwise; null if the package was not found
	 */
	@Nullable
	public static Boolean checkCertsPkg(@NonNull final String package_name, @NonNull final String[] list_cert_hashes) {
		final Signature[] other_app_signatures = getAppSignatures(package_name);
		if (other_app_signatures == null) {
			return null;
		}

		// For each certificate of the other app, check if it matches any of the certificates in the given list.
		boolean no_matches_at_all = true;
		for (final Signature signature : other_app_signatures) {
			final String other_app_sig_hash = UtilsSWA.getHashStringOfBytesCRYPTOHASHING(signature.toByteArray());

			boolean match_found = false;
			for (final String cert_hash : list_cert_hashes) {
				if (other_app_sig_hash.equals(cert_hash)) {
					match_found = true;
					no_matches_at_all = false;
					break;
				}
			}

			if (match_found) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
					// If we're on Pie or above, then GET_SIGNING_CERTIFICATES was used. That means all certificates
					// are authorized to be on the app (we don't need to make checks for that, as opposite to
					// GET_SIGNATURES). So if one of them are my certificates, cool, since the app is authorized to used
					// it.
					return true;
					// So return true already. One match is enough here.
				}
				// Else, keep searching for at least one certificate without whitelist matches.
			} else {
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
					// Below Pie (using GET_SIGNATURES then), if ANY of the certificates on the other app are not
					// whitelisted here, it's not signed by me. This mitigates the FakeID vulnerability mentioned on
					// getAppSignatures().
					return false;
					// So return false already. One no-match is enough here.
				}
				// Else, keep searching for at least one match.
			}
		}

		if (no_matches_at_all) {
			// If there were no matches, not my app for sure. Gets here if the Android version is Pie or above and no
			// match was found at all (or the function would have returned true already).
			return false;
		} else {
			// Else, if it's ALL matches, it's my app for sure. Gets here if the Android version is below Pie and all
			// certificates have a match with the given list of certificates (or the function would have returned false
			// already).
			return true;
		}
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
	 * <br>
	 * <p>Note 2: this problem is only found when an app is signed by multiple certificates. If it's signed by only one,
	 * there's no problem at all, and of course, the certificate can be checked to see if it's the correct one or not.</p>
	 *
	 * @param package_name the package to get the signatures from
	 *
	 * @return the app signatures, or null if the app is not installed
	 */
	@SuppressLint("PackageManagerGetSignatures")
	@Nullable
	private static Signature[] getAppSignatures(@NonNull final String package_name) {
		final PackageInfo packageInfo;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
			try {
				packageInfo = UtilsContext.getContext().getPackageManager().getPackageInfo(package_name,
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
				packageInfo = UtilsContext.getContext().getPackageManager().getPackageInfo(package_name,
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
}
