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

import android.Manifest;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GlobalPosition;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Geodesy-related utilities.</p>
 */
public final class UtilsLocationAbsolute {

	// Units: seconds
	//private static final int MAX_AGE_LOCATION_ROOM = 1 * 60;
	//private static final int MAX_AGE_LOCATION_HOUSE = 1 * 60;
	//private static final int MAX_AGE_LOCATION_BUILDING = (int) (2.5 * 60.0);
	private static final int MAX_AGE_LOCATION_CITY = 5 * 60;
	private static final int MAX_AGE_LOCATION_COUNTRY = 10 * 60;

	// Units: meters
	//private static final float MAX_LOCATION_DEVIATION_ROOM = 0.5f;
	//private static final float MAX_LOCATION_DEVIATION_HOUSE = 1.0f;
	//private static final float MAX_LOCATION_DEVIATION_BUILDING = 5.0f;
	private static final float MAX_LOCATION_DEVIATION_CITY = 25.0f;
	private static final float MAX_LOCATION_DEVIATION_COUNTRY = 5_000.0f;


	private static final Map<Integer, Integer> map_LOCATION_to_AGE = new LinkedHashMap<Integer, Integer>() {
		private static final long serialVersionUID = 2268708824566655410L;
		@NonNull @Override public LinkedHashMap<Integer, Integer> clone() throws AssertionError {
			throw new AssertionError();
		}

		{
			put(LOCATION_COUNTRY, MAX_AGE_LOCATION_COUNTRY);
			put(LOCATION_CITY, MAX_AGE_LOCATION_CITY);
			//put(LOCATION_BUILDING, MAX_AGE_LOCATION_BUILDING);
			//put(LOCATION_HOUSE, MAX_AGE_LOCATION_HOUSE);
			//put(LOCATION_ROOM, MAX_AGE_LOCATION_ROOM);
		}
	};

	private static final Map<Integer, Float> map_LOCATION_to_DEVIATION = new LinkedHashMap<Integer, Float>() {
		private static final long serialVersionUID = 4089662568764366621L;
		@NonNull @Override public LinkedHashMap<Integer, Float> clone() throws AssertionError {
			throw new AssertionError();
		}

		{
			put(LOCATION_COUNTRY, MAX_LOCATION_DEVIATION_COUNTRY);
			put(LOCATION_CITY, MAX_LOCATION_DEVIATION_CITY);
			//put(LOCATION_BUILDING, MAX_LOCATION_DEVIATION_BUILDING);
			//put(LOCATION_HOUSE, MAX_LOCATION_DEVIATION_HOUSE);
			//put(LOCATION_ROOM, MAX_LOCATION_DEVIATION_ROOM);
		}
	};

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsLocationAbsolute() {
	}

	// To add a new location, update the switches in getDistanceBetweenCoordinates() and getCurrentLocation().
	public static final int LOCATION_COUNTRY = 0;
	public static final int LOCATION_CITY = 1;
	//public static final int LOCATION_BUILDING = 2;
	//public static final int LOCATION_HOUSE = 3;
	//public static final int LOCATION_ROOM = 4;

	public static final double NO_ERRORS = 0.0;
	public static final double MISSING_PERMS = 1.0;
	public static final double NO_LOCATION_FOR_PARAMETERS = 1.0;
	/**
	 * <p>Gets the current device location coordinates (latitude and longitude).</p>
	 * <p>This function can get the device location through various ways. Which one will be decided by the specified
	 * parameters.</p>
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link #LOCATION_ROOM} --> for {@code accuracy_type}: accuracy of inside a room (to know exactly where the
	 * device is inside a room - like near a desk, the bed...)</p>
	 * <p>- {@link #LOCATION_HOUSE} --> for {@code accuracy_type}: accuracy of inside a house (deciding in which room
	 * the device is, for example)</p>
	 * <p>- {@link #LOCATION_BUILDING} --> for {@code accuracy_type}: accuracy of inside a bigger building than a normal
	 * house (same as in {@link #LOCATION_HOUSE}, but with less required accuracy)</p>
	 * <p>- {@link #LOCATION_CITY} --> for {@code accuracy_type}: accuracy of inside a city (moving on foot/car/bus...)</p>
	 * <p>- {@link #LOCATION_COUNTRY} --> for {@code accuracy_type}: accuracy of inside a country (like traveling)</p>
	 * <br>
	 * <p>- {@link #NO_ERRORS} --> for the returning value: the function was executed with no problems</p>
	 * <p>- {@link #MISSING_PERMS} --> for the returning value: there are permissions missing, and hence, the location
	 * could not be obtained (the latitude and longitude will be 0.0)</p>
	 * <p>- {@link #NO_LOCATION_FOR_PARAMETERS} --> for the returning value: no location was found for the selected
	 * parameters (the latitude and longitude will be 0.0)</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @param accuracy_type one of the constants (presented in order of accuracy) - if any constant is non-existent,
	 *                      then it's not ready for use yet
	 * @param update_location true to request a location update, false to get the best last known location
	 *
	 * @return on index 0, one of the constants; on index 1, latitude; on index 2, longitude
	 */
	@NonNull
	public static double[] getCurrentLocation(final int accuracy_type, final boolean update_location) {
		Double latitude = null;
		Double longitude = null;

		/*
		Read "Android Location Providers (gps, network, passive)" from a Google engineer at
		https://developerlife.com/2010/10/20/gps/:
		- Accuracy of GPS ~ 6 m
		- Accuracy of NETWORK ~ 61 m
		- Accuracy of PASSIVE ~ 1.6 km

		----------------------------------------------------------------------------------------------------------------
		Note about BUILDING, HOUSE and ROOM

		Can't make those 3 happen yet. Bluetooth RSSI calculated distance varies, depending on walls and stuff - because
		it's signal strength. Wi-Fi signal strength is the same thing. The problem is using the strength, which is
		dependent on the objects on the signal path.

		On the other hand, checking the time the signal takes to go from the phone to the other device would be a good
		idea - if it's detected, it go there, no matter the signal strength. But that's still being developed and not
		available on many devices, it seems (2021). So it's a no go too (for now, at least). When you have a phone with
		Android Pie (9), check this out, which explains how to implement RTT, which is available on Android only from
		Pie onwards: https://developer.android.com/guide/topics/connectivity/wifi-rtt.

		The best idea seems to be to use sound. But that requires a sender and a receiver, and it's supposed for the
		phone to be the sender and the receiver. The other device doesn't have to do anything. Just be present and have
		Bluetooth or whatever turned on. And still, a wall in the middle and it's doomed. So can't go that route either.

		This means the best way is really the signal strength, which will not be that accurate inside a house, I guess.
		1 wall in the way might mean being 10 meters away with no walls, for example (no idea, just an example).
		Read this: https://stackoverflow.com/questions/36399927/distance-calculation-from-rssi-ble-android - big
		possibly good answers there, with ideas of using filters and stuff.
		EDIT: not a good idea, I think (even with filters, I guess) --> https://www.youtube.com/watch?v=SAi24ctpyZQ and
		https://medium.com/personaldata-io/inferring-distance-from-bluetooth-signal-strength-a-deep-dive-fe7badc2bb6d.
		From the video (mentioned on the Medium article), 1 m real was transformed into 10 m measured... Just by putting
		the phone in the back pocket! Nope, forget about it. Wi-Fi RTT. Until then, nothing indoors, I guess.
		----------------------------------------------------------------------------------------------------------------
		*/

		// 3 providers which work on all Android smartphones: GPS, NETWORK and PASSIVE. FUSED is not included, since
		// that requires Google Play Services, and this is supposed to work on devices which don't have Google stuff on
		// them, for whatever reason (LineageOS removed Google Play Services, for example) - completely independent app.
		// Might be possible to detect if they're installed, and in that case, use those services, but that's for some
		// other time - and still, people might not like that much because of privacy... Might need some switch to
		// enable or disable the use of their services or something.
		final int ordered_providers_size = 3;
		final String[] ordered_providers = new String[ordered_providers_size];
		switch (accuracy_type) {
			case (LOCATION_COUNTRY): {
				ordered_providers[0] = LocationManager.PASSIVE_PROVIDER;
				ordered_providers[1] = LocationManager.NETWORK_PROVIDER;
				ordered_providers[2] = LocationManager.GPS_PROVIDER;

				break;
			}
			case (LOCATION_CITY): {
				ordered_providers[0] = LocationManager.NETWORK_PROVIDER;
				ordered_providers[1] = LocationManager.GPS_PROVIDER;
				ordered_providers[2] = LocationManager.PASSIVE_PROVIDER;

				break;
			}
			/*case (LOCATION_BUILDING):
			case (LOCATION_HOUSE):
			case (LOCATION_ROOM): {
				ordered_providers[0] = LocationManager.GPS_PROVIDER;
				ordered_providers[1] = LocationManager.NETWORK_PROVIDER;
				ordered_providers[2] = LocationManager.PASSIVE_PROVIDER;

				break;
			}*/
		}

		if (update_location) {
			final LocationManager locationManager = (LocationManager) UtilsGeneral.getContext()
					.getSystemService(Context.LOCATION_SERVICE);

			final boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			final boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			final boolean isPassiveEnabled = locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER);


		} else {
			final List<Location> last_locations = getLastLocationsUpToDate(accuracy_type);
			if (last_locations == null) {
				return new double[]{MISSING_PERMS, 0.0, 0.0};
			} else {
				for (final String provider : ordered_providers) {
					for (final Location location : last_locations) {
						if (location != null && provider.equals(location.getProvider())) {
							return new double[]{NO_ERRORS, location.getLatitude(), location.getLongitude()};
						}
					}
				}
			}
		}

		return new double[]{NO_LOCATION_FOR_PARAMETERS, 0.0, 0.0};
	}

	/**
	 * <p>Gets the age of the given fix in seconds.</p>
	 *
	 * @param location a location
	 *
	 * @return the age in seconds
	 */
	private static long getAgeOfFix(@NonNull final Location location) {
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
			return ((SystemClock.elapsedRealtimeNanos() - location.getElapsedRealtimeNanos()) * 1000L * 1000L);
		} else {
			return System.currentTimeMillis() - location.getTime();
		}
	}

	/**
	 * <p>Gets the last location from 3 location providers: {@link LocationManager#GPS_PROVIDER},
	 * {@link LocationManager#NETWORK_PROVIDER} and {@link LocationManager#PASSIVE_PROVIDER}, in this order - IN CASE
	 * those locations are up to date ("up to date" depends on the {@code accuracy_type} parameter).</p>
	 *
	 * @param accuracy_type same as in {@link #getCurrentLocation(int, boolean)}
	 *
	 * @return on each index, the location from each the 3 mentioned providers, in the mentioned order - each of the
	 * locations may be null, depending if there was a location available from that provider or not; or null if the
	 * required permissions by {@link LocationManager#getLastKnownLocation(String)} have not been granted to the app
	 */
	@Nullable
	private static List<Location> getLastLocationsUpToDate(final int accuracy_type) {
		// Providers to try to get the location from in order from the most accurate to the less accurate
		final String[] providers_to_try = {LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER,
				LocationManager.PASSIVE_PROVIDER};
		final List<Location> locations = new ArrayList<>(3);
		for (int i = 0, length = providers_to_try.length; i < length; ++i) {
			// Fill all with nulls, in case there
			locations.add(null);
		}

		final LocationManager locationManager = (LocationManager) UtilsGeneral.getContext()
				.getSystemService(Context.LOCATION_SERVICE);

		if (UtilsPermissions.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
				&& UtilsPermissions.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
			for (int i = 0, length = providers_to_try.length; i < length; ++i) {
				final Location location = locationManager.getLastKnownLocation(providers_to_try[i]);
				if (location != null && getAgeOfFix(location) <= (long) map_LOCATION_to_AGE.get(accuracy_type)) {
					locations.set(i, location);
				}
			}

			return locations;
		} else {
			return null;
		}
	}

	/**
	 * <p>Gets the distance between 2 coordinates using one of 2 methods: haversine or Vincenty's formula.</p>
	 * <p><strong>Attention:</strong> if altitude is provided and low power is required, it will NOT enter into
	 * consideration on the distance calculation!</p>
	 *
	 * @param first_point same as in {@link #getVincentyDistance(double[], double[])}
	 * @param second_point same as in {@link #getVincentyDistance(double[], double[])}
	 * @param accuracy_type same as in {@link #getCurrentLocation(int, boolean)}
	 *
	 * @return the distance between the 2 coordinates
	 */
	public static double getDistanceBetweenCoordinates(@NonNull final double[] first_point,
													   @NonNull final double[] second_point, final int accuracy_type) {

		// Between Pune and Sydney, haversine formula returned 10036.2603, and Vincenty's returned 10029.258. Not that
		// much (7 meters). But too much in a house or much less in a room. In a building, maybe. City, no problem.
		// Country much less.

		switch (accuracy_type) {
			case (LOCATION_COUNTRY):
			case (LOCATION_CITY): {
				return getHaversineDistance(first_point, second_point);
			}
			/*case (LOCATION_BUILDING):
			case (LOCATION_HOUSE):
			case (LOCATION_ROOM):*/
			default: { // Default just to be sure, if new locations are not added here, at least they have maximum
				       // precision on them (until the location is added here).
				return getVincentyDistance(first_point, second_point);
			}
		}
	}

	/**
	 * <p>Gets the distance between 2 coordinates using Hhversine formula - less precise, less power needed.</p>
	 * <p>Only latitude and longitude values allowed, no altitude.</p>
	 *
	 * @param first_point on index 0, latitude; on index 1, longitude
	 * @param second_point same as {@code first_point}
	 *
	 * @return the distance between the 2 coordinates
	 */
	private static double getHaversineDistance(@NonNull final double[] first_point, @NonNull final double[] second_point) {
		final double R = 6372.8; // Earth radius in kilometers (one of the radius - don't change, use this one)
		final double dLat = Math.toRadians(first_point[0] - second_point[0]);
		final double dLon = Math.toRadians(first_point[1] - second_point[1]);
		final double lat1_radians = Math.toRadians(first_point[0]);
		final double lat2_radians = Math.toRadians(second_point[0]);

		final double a = StrictMath.pow(StrictMath.sin(dLat / 2.0), 2.0) + StrictMath.pow(StrictMath.sin(dLon / 2.0), 2.0)
				* StrictMath.cos(lat1_radians) * StrictMath.cos(lat2_radians);
		final double c = 2.0 * StrictMath.asin(Math.sqrt(a));

		return R * c;
	}

	/**
	 * <p>Gets the distance between 2 coordinates using Vincenty's formula - more precise, more power needed.</p>
	 * <p>Latitude, longitude and altitude values allowed.</p>
	 * <br>
	 * <p><strong>Note:</strong> altitude (at least) can be left 0 if it's to calculate based on the other 2 values
	 * only.</p>
	 *
	 * @param first_point on index 0, latitude; on index 1, longitude; on index 2, altitude
	 * @param second_point same as {@code first_point}
	 *
	 * @return the distance between the 2 coordinates
	 */
	private static double getVincentyDistance(@NonNull final double[] first_point, @NonNull final double[] second_point) {
		final GeodeticCalculator geodeticCalculator = new GeodeticCalculator();
		final Ellipsoid ellipsoid_reference = Ellipsoid.WGS84;
		final GlobalPosition point_A = new GlobalPosition(first_point[0], first_point[1], first_point[2]);
		final GlobalPosition point_B = new GlobalPosition(second_point[0], second_point[1], second_point[2]);

		return geodeticCalculator.calculateGeodeticCurve(ellipsoid_reference, point_B, point_A).getEllipsoidalDistance();
	}
}
