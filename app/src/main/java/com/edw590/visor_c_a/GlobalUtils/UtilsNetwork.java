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

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.spongycastle.util.Arrays;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

/**
 * <p>Utilities related to network functions.</p>
 */
public final class UtilsNetwork {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsNetwork() {
	}

	/**
	 * <p>Gets the IPv4 address of the current Wi-Fi connection's router.</p>
	 *
	 * @return the IPv4 string, or an empty string if there is no Wi-Fi connection
	 */
	@Nullable
	public static String getRouterIpv4() {
		final byte[] myIPAddress = BigInteger.valueOf((long) getWifiManager().getDhcpInfo().gateway).toByteArray();
		try {
			return InetAddress.getByAddress(Arrays.reverse(myIPAddress)).getHostAddress();
		} catch (final UnknownHostException ignored) {
			// Won't happen, just supply an IPv4
			return "";
		}
	}

	/**
	 * <p>Get the device network's external IP address (Wi-Fi public IP, mobile data IP, etc.).</p>
	 *
	 * @return the IP or an empty string in case an error happened (like no connection)
	 */
	@NonNull
	public static String getExternalIpAddress() {
		final URL whatismyip_website;
		try {
			whatismyip_website = new URL("https://checkip.amazonaws.com");
		} catch (final MalformedURLException ignored) {
			// Won't happen.
			return "";
		}

		try (final BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip_website.openStream(),
				Charset.defaultCharset()))) {

			return in.readLine();
		} catch (final IOException ignored) {
			return "";
		}
	}

	/**
	 * <p>Gets a {@link WifiManager} instance.</p>
	 * <p>The reason of this is because getContext() already gets the Application Context. This could be avoided by
	 * renaming it to getApplicationContext() (Android Studio pays attention to the name), but that's a big name and I
	 * wanted it to be smaller. So just use this function to suppress warnings.</p>
	 *
	 * @return the instance
	 */
	@Nullable
	public static WifiManager getWifiManager() {
		// The only warning will be here.
		// EDIT: not anymore because I replaced all getSystemService() and getService() calls.
		return (WifiManager) UtilsContext.getSystemService(Context.WIFI_SERVICE);
	}

	/**
	 * <p>Get the current network type.</p>
	 *
	 * @return same as {@link NetworkInfo#getType()} or {@link ConnectivityManager#TYPE_NONE} if there's no network
	 */
	public static int getCurrentNetworkType() {
		final ConnectivityManager connectivityManager = (ConnectivityManager) UtilsContext.
				getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivityManager == null) {
			return ConnectivityManager.TYPE_NONE;
		}

		final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

		return networkInfo == null ? ConnectivityManager.TYPE_NONE : networkInfo.getType();
	}
}
