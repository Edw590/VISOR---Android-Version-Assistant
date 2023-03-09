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

package com.dadi590.assist_c_a.Modules.PreferencesManager.Registry;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * <p>The class used to hold each value on the storage.</p>
 * <p>A value of null means the value has never been set before.</p>
 * <p>This class is thread-safe.</p>
 */
public final class Value {

	public static final class JSONKeys {
		public static final String TYPE = "TYPE";
		public static final String PREV_VALUE = "PREV_VALUE";
		public static final String PREV_VALUE_TIME = "PREV_VALUE_TIME";
		public static final String VALUE = "VALUE";
		public static final String VALUE_TIME = "VALUE_TIME";
	}

	// Do NOT change this. Must always be null - Java's initial value of an object.
	private static final Object DEFAULT_DATA = null;
	// Don't set to 0. Imagine the date is wrong on the device. At most set it to Long.MAX_VALUE - only if -1 is not
	// good for any reason.
	public static final long DEFAULT_TIME = -1L;

	/** The key that identifies the value. */
	@NonNull public final String key;
	/** Name to present to the user. */
	@NonNull public final String pretty_name;
	/** Description to present to the user. */
	@NonNull public final String description;
	/** The class of the value's type. */
	@NonNull public final String type;

	/** The previous value; null by default, and never again after being set. */
	@Nullable private Object prev_data = DEFAULT_DATA;
	/** The last time the previous value was updated at, in milliseconds; {@link #DEFAULT_TIME} by default. */
	private long time_updated_prev = DEFAULT_TIME;
	/** The actual value; null by default, and never again after being set. */
	@Nullable private Object data = DEFAULT_DATA;
	/** The last time the value was updated at, in milliseconds; {@link #DEFAULT_TIME} by default. */
	private long time_updated = DEFAULT_TIME;

	// ONLY TYPES THAT CAN BE put() ON A JSONObject!!!!!!
	// List of types: Strings, Booleans, Integers, Longs, Doubles or NULL.
	public static final String TYPE_BOOLEAN = "Boolean";
	public static final String TYPE_INTEGER = "Integer";
	public static final String TYPE_DOUBLE = "Double";
	public static final String TYPE_LONG = "Long";
	public static final String TYPE_STRING = "String";

	/**.
	 * @param key {@link #key}
	 * @param pretty_name {@link #pretty_name}
	 * @param type {@link #type}
	 * @param description {@link #description}
	 */
	Value(@NonNull final String key, @NonNull final String pretty_name, @NonNull final String type,
		  @NonNull final String description) {
		this.key = key;
		this.pretty_name = pretty_name;
		this.type = type;
		this.description = description;
	}

	/**
	 * <p>Same as {@link #Value(String, String, String, String)} but sets an initial value.</p>
	 *
	 * @param init_data the initial {@link #data}, which from now one will never be null
	 */
	Value(@NonNull final String key, @NonNull final String pretty_name, @NonNull final String type,
		  @NonNull final Object init_data, @NonNull final String description) {
		this.key = key;
		this.pretty_name = pretty_name;
		this.type = type;
		this.description = description;

		data = init_data;
	}

	/**
	 * <p>This function will update the {@link #data} and also the {@link #prev_data} if the new {@code data_param}
	 * is different than the current {@link #data} and if it's not the first time setting the {@link #data}.</p>
	 * <p>Every time this function is called, {@link #time_updated} will be updated, even if {@code data_param} is
	 * equal to the current {@link #data} - but {@link #time_updated_prev} will only be updated when
	 * {@link #prev_data} is, according to the above written.</p>
	 *
	 * @param data_param the new {@link #data}
	 */
	// Leave it package-private! That way, the only possible way is through ValuesStorage's setValue(), which broadcasts
	// the new value, and this function doesn't (as it shouldn't).
	synchronized void setDataInternal(@NonNull final Object data_param) {
		if (data != data_param) {
			if (null != data) {
				// Update the previous value if the new one is different than the current one and it's not the first time
				// setting the value.
				prev_data = data;
				time_updated_prev = System.currentTimeMillis();
			}
			data = data_param;
		}
		time_updated = System.currentTimeMillis();
	}

	/**
	 * .
	 * @param def_data the value to return if this value has never been set before
	 * @param <T> the type (any type) of the value
	 * @return {@link #data} or {@code def_data}, auto-cast to {@code def_data}'s type
	 */
	// Don't set as @Nullable. It can be null, but only if def_data is null. Else, it won't be null and there's no
	// point in checking for nullability in that case. Or will never be null if the 2nd constructor is used.
	public synchronized <T> T getData(@Nullable final T def_data) {
		// Keep it using def_data's type. If value's type is int and there's null as default value, then the output
		// must not be an int - instead, an Object (which then will be cast to an Integer).
		return null != data ? (T) data : def_data;
	}
	/** {@link #getData(Object)} with {@link #DEFAULT_DATA} as parameter. */
	// Do NOT put as @Nullable! If the 2nd constructor is used, this will never return null!
	public synchronized <T> T getData() {
		// Keep it using def_value's type. If value's type is int and there's null as default value, then the output
		// must not be an int - instead, an Object (which then will be cast to an Integer).
		return (T) getData(DEFAULT_DATA);
	}

	/**
	 * .
	 * @param def_data same as in {@link #getData(Object)}
	 * @param <T> same as in {@link #getData(Object)}
	 * @return same as in {@link #getData(Object)} but for {@link #prev_data}
	 */
	public synchronized <T> T getPrevData(@Nullable final T def_data) {
		return null != prev_data ? (T) prev_data : def_data;
	}
	/** {@link #getPrevData(Object)} with {@link #DEFAULT_DATA} as parameter. */
	public synchronized <T> T getPrevData() {
		return (T) getPrevData(DEFAULT_DATA);
	}

	/**
	 * .
	 * @return {@link #time_updated}
	 */
	public synchronized long getTime() {
		return time_updated;
	}
	/**
	 * .
	 * @return {@link #time_updated_prev}
	 */
	public synchronized long getPrevTime() {
		return time_updated_prev;
	}
}
