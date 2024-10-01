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

import UtilsSWA.UtilsSWA;

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
		public static final String K_CONTACTS_1ST_MATCH = PREFIX + "CONTACTS_1ST_MATCH";
		public static final String K_CONTACTS_SIM_ONLY = PREFIX + "CONTACTS_SIM_ONLY";
	}

	/**
	 * <p>Registers all the keys in the registry.</p>
	 */
	public static void registerRegistryKeys() {
		UtilsSWA.registerValueREGISTRY(Keys.K_CONTACTS_1ST_MATCH, "Contacts - Use 1st name match",
				"Use the 1st match on the contacts when getting the name from a phone number (or else warn about multiple matches)",
				UtilsSWA.TYPE_BOOL);
		UtilsSWA.registerValueREGISTRY(Keys.K_CONTACTS_SIM_ONLY, "Contacts - Only use SIM contacts",
				"Search only the SIM card contacts", UtilsSWA.TYPE_BOOL);
	}
}
