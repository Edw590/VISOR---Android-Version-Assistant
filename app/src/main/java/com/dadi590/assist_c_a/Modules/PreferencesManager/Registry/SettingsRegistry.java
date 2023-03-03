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

/**
 * <p>The static storage of all app settings.</p>
 */
public final class SettingsRegistry {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private SettingsRegistry() {
	}

	static final String PREFIX = "SETTINGS_";

	public static class Keys {

		// Add new keys to the settings_list array below too.

		// Telephony
		public static final String CONTACTS_1ST_MATCH = PREFIX + "CONTACTS_1ST_MATCH";
		public static final String CONTACTS_SIM_ONLY = PREFIX + "CONTACTS_SIM_ONLY";
	}

	static final Value[] SETTINGS_LIST = {
			// Note: if the setting is not being updated, remove it from the list

			new Value(Keys.CONTACTS_1ST_MATCH, "Contacts - Use 1st name match", Value.TYPE_BOOLEAN, false,
					"Use the 1st match on the contacts when getting the name from a phone number (or else warn about multiple matches)"),
			new Value(Keys.CONTACTS_SIM_ONLY, "Contacts - Only use SIM contacts", Value.TYPE_BOOLEAN, true,
					"Search only the SIM card contacts"),
	};

	/**
	 * <p>Get a clone of {@link #SETTINGS_LIST}.</p>
	 *
	 * @return .
	 */
	@NonNull
	public static synchronized Value[] getArray() {
		return SETTINGS_LIST.clone();
	}

	@NonNull
	public static synchronized Value getValueObj(final int index) {
		return SETTINGS_LIST[index];
	}
}
