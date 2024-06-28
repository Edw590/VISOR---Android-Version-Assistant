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

package com.edw590.visor_c_a.Registry;

import androidx.annotation.NonNull;

/**
 * <p>Utilities related to the Static Storage.</p>
 */
public final class UtilsRegistry {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsRegistry() {
	}

	/**
	 * <p>Update the value of the given key - this is a general function, which searches the key in all lists.</p>
	 * <p>Don't forget the key must already exist on the lists (must be hard-coded).</p>
	 * <br>
	 * <p>This function also broadcasts the new value through the app with an app-internal broadcast.</p>
	 *
	 * @param key the key
	 * @param new_value the new value
	 * @param update_if_same if the value should be updated even if it's the same as the current one
	 */
	public static void setData(@NonNull final String key, @NonNull final Object new_value, final boolean update_if_same) {
		Registry.Value value = Registry.Registry.getValue(key);
		if (new_value instanceof Boolean) {
			value.setBool((boolean) new_value, update_if_same);
		} else if (new_value instanceof Integer) {
			value.setInt((int) new_value, update_if_same);
		} else if (new_value instanceof String) {
			value.setString((String) new_value, update_if_same);
		} else if (new_value instanceof Long) {
			value.setLong((long) new_value, update_if_same);
		} else if (new_value instanceof Float) {
			value.setFloat((float) new_value, update_if_same);
		} else if (new_value instanceof Double) {
			value.setDouble((double) new_value, update_if_same);
		} else {
			throw new IllegalArgumentException("The new value must be a boolean, int, String, long, float or double.");
		}
	}

	/**
	 * <p>Returns the value for the given key, in the appropriate type.</p>
	 *
	 * @param key the key associated with the wanted value
	 * @param curr_data if the current data is wanted or the previous data
	 *
	 * @return the value for the given key, in the appropriate type, or the default value
	 */
	@NonNull
	public static Object getData(@NonNull final String key, final boolean curr_data) {
		Registry.Value value = Registry.Registry.getValue(key);
		switch (value.getType()) {
			case Registry.Registry.TYPE_BOOL:
				return value.getBool(curr_data);
			case Registry.Registry.TYPE_INT:
				return Math.toIntExact(value.getInt(curr_data));
			case Registry.Registry.TYPE_STRING:
				return value.getString(curr_data);
			case Registry.Registry.TYPE_LONG:
				return value.getLong(curr_data);
			case Registry.Registry.TYPE_FLOAT:
				return value.getFloat(curr_data);
			case Registry.Registry.TYPE_DOUBLE:
				return value.getDouble(curr_data);
			default:
				throw new IllegalArgumentException("The value type is not supported: " + value.getType());
		}
	}
}
