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

package com.edw590.visor_c_a.Modules.PreferencesManager.Registry;

import android.content.Intent;

import androidx.annotation.NonNull;

import com.edw590.visor_c_a.GlobalUtils.UtilsApp;

/**
 * <p>Utilities related to the Static Storage.</p>
 */
public final class UtilsRegistry {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsRegistry() {
	}

	public static final int LIST_VALUES = 0;
	public static final int LIST_SETTINGS = 1;

	/**
	 * <p>Update the value of the given key - this is a general function, which searches the key in all lists.</p>
	 * <p>Don't forget the key must already exist on the lists (must be hard-coded).</p>
	 * <br>
	 * <p>This function also broadcasts the new value through the app with an app-internal broadcast.</p>
	 *
	 * @param key the key
	 * @param new_value the new value
	 */
	public static void setValue(@NonNull final String key, @NonNull final Object new_value) {
		final int list;
		if (key.startsWith(ValuesRegistry.PREFIX)) {
			list = LIST_VALUES;
		} else {
			list = LIST_SETTINGS;
		}

		setValue(list, key, new_value);
	}

	/**
	 * <p>Same as {@link #setValue(String, Object)} but for the given list.</p>
	 *
	 * @param list the list where the value belongs to
	 */
	public static void setValue(final int list, @NonNull final String key, @NonNull final Object new_value) {
		final Value[] list_to_use;
		if (LIST_VALUES == list) {
			list_to_use = ValuesRegistry.VALUES_LIST;
		} else {
			list_to_use = SettingsRegistry.SETTINGS_LIST;
		}

		for (final Value value : list_to_use) {
			if (key.equals(value.key)) {
				// Only update the value if it's different
				if (!new_value.equals(value.getData())) {
					value.setDataInternal(new_value);

					final Intent intent = new Intent(CONSTS_BC_Registry.ACTION_VALUE_UPDATED);
					intent.putExtra(CONSTS_BC_Registry.EXTRA_VALUE_UPDATED_1, key);
					UtilsApp.sendInternalBroadcast(intent);
				}

				break;
			}
		}
	}

	/**
	 * <p>Returns the {@link Value} stored for the given key.</p>
	 *
	 * @param key the key associated with the wanted value
	 *
	 * @return the value for the given key, in the appropriate type, or the default value
	 */
	@NonNull
	public static Value getValue(@NonNull final String key) {
		final Value[] list;
		if (key.startsWith(ValuesRegistry.PREFIX)) {
			list = ValuesRegistry.VALUES_LIST;
		} else {
			list = SettingsRegistry.SETTINGS_LIST;
		}

		for (final Value value : list) {
			if (value.key.equals(key)) {
				return value;
			}
		}

		// Won't get here. Just request a valid key.
		return null;
	}
}
