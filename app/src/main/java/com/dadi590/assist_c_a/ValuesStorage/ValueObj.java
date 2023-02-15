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

package com.dadi590.assist_c_a.ValuesStorage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * <p>The class used to hold each value on the storage.</p>
 */
public final class ValueObj {
	// Do NOT change this. Must always be null - Java's initial value of an object.
	public static final Object DEFAULT_VALUE = null;
	// Don't set to 0. Imagine the date is wrong on the device. At most set it to Long.MAX_VALUE - only if -1 is not
	// good for any reason.
	public static final long DEFAULT_TIME = -1L;

	/** The key that identifies the value. */
	@NonNull public final String key;
	/** Name to present to the user. */
	@NonNull public final String pretty_name;
	/** The class of the value's type. */
	@NonNull public final Class<?> type;

	/** The previous value; {@link #DEFAULT_VALUE} by default. */
	@Nullable private Object prev_value = DEFAULT_VALUE;
	/** The last time the previous value was updated at, in milliseconds; {@link #DEFAULT_TIME} by default. */
	private long time_updated_prev = DEFAULT_TIME;
	/** The actual value; {@link #DEFAULT_VALUE} by default. */
	@Nullable private Object value = DEFAULT_VALUE;
	/** The last time the value was updated at, in milliseconds; {@link #DEFAULT_TIME} by default. */
	private long time_updated = DEFAULT_TIME;

	/**
	 * .
	 * @param key {@link #key}
	 * @param pretty_name {@link #pretty_name}
	 * @param type {@link #type}
	 */
	ValueObj(@NonNull final String key, @NonNull final String pretty_name, @NonNull final Class<?> type) {
		this.key = key;
		this.pretty_name = pretty_name;
		this.type = type;
	}

	/**
	 * <p>This function will update the {@link #value} and also the {@link #prev_value} if the new {@code value_param}
	 * is different than the current {@link #value} and if it's not the first time setting the {@link #value}.</p>
	 * <p>Every time this function is called, {@link #time_updated} will be updated, even if {@code value_param} is
	 * equal to the current {@link #value} - but {@link #time_updated_prev} will only be updated when
	 * {@link #prev_value} is, according to the above written.</p>
	 *
	 * @param value_param the new {@link #value}
	 */
	// Leave it package-private! That way, the only possible way is through ValuesStorage's setValue(), which broadcasts
	// the new value, and this function doesn't (as it shouldn't).
	synchronized void setValue(@Nullable final Object value_param) {
		if (value != value_param) {
			if (hasBeenSet(time_updated)) {
				// Update the previous value if the new one is different than the current one and it's not the first time
				// setting the value.
				prev_value = value;
				time_updated_prev = System.currentTimeMillis();
			}
			value = value_param;
		}
		time_updated = System.currentTimeMillis();
	}

	/**
	 * .
	 * @param def_value the value to return if this value has never been set before
	 * @param <T> the type (any type) of the value
	 * @return {@link #value} or {@code def_value}, auto-cast to {@code def_value}'s type
	 */
	// Don't set as @Nullable. It can be null, but only if def_value is null. Else, it won't be null and there's no
	// point in checking for nullability in that case.
	public synchronized <T> T getValue(@Nullable final T def_value) {
		// Keep it using def_value's type. If value's type is int and there's null as default value, then the output
		// must not be an int - instead, an Object (which then will be cast to an Integer).
		return hasBeenSet(time_updated) ? (T) value : def_value;
	}
	/** {@link #getValue(Object)} with {@link #DEFAULT_VALUE} as parameter. */
	public synchronized <T> T getValue() {
		// Keep it using def_value's type. If value's type is int and there's null as default value, then the output
		// must not be an int - instead, an Object (which then will be cast to an Integer).
		return (T) getValue(DEFAULT_VALUE);
	}

	/**
	 * .
	 * @param def_value same as in {@link #getValue(Object)}
	 * @param <T> same as in {@link #getValue(Object)}
	 * @return same as in {@link #getValue(Object)} but for {@link #prev_value}
	 */
	public synchronized <T> T getPrevValue(@Nullable final T def_value) {
		return hasBeenSet(time_updated_prev) ? (T) prev_value : def_value;
	}
	/** {@link #getPrevValue(Object)} with {@link #DEFAULT_VALUE} as parameter. */
	public <T> T getPrevValue() {
		return (T) getPrevValue(DEFAULT_VALUE);
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

	private synchronized boolean hasBeenSet(final long time) {
		return DEFAULT_TIME != time;
	}
}
