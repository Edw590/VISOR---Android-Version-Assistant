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

package com.edw590.visor_c_a.Modules.PreferencesManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.edw590.visor_c_a.Modules.PreferencesManager.Registry.SettingsRegistry;
import com.edw590.visor_c_a.Modules.PreferencesManager.Registry.ValuesRegistry;
import com.edw590.visor_c_a.Modules.PreferencesManager.Registry.Value;

import org.json.JSONException;
import org.json.JSONObject;

public final class PrefsFileFormat {



	// todo ALL SYNCHRONIZED HERE!!!!!!!!!!!!!!!



	static final class Keys {
		static final String FILE_HEADER = "JSON_FILE_HEADER";
		static final String GLOBAL_VALUES = "JSON_GLOBAL_VALUES";
		static final String SETTINGS = "JSON_SETTINGS";

		static final class FileHeader {
			static final String TYPE = "JSON_TYPE";
			static final String TYPE_VERSION = "JSON_TYPE_VERSION";
		}
	}

	static final class Values {
		static final class FileHeader {
			static final String TYPE = "PrefsFile";
			static final double TYPE_VERSION = 1.0;
		}
	}

	@Nullable private static JSONObject preferences = null;

	private static final Object[][] keys_objects_map = {
			{Keys.FILE_HEADER, new JSONObject()},
			{Keys.SETTINGS, new JSONObject()},
			{Keys.GLOBAL_VALUES, new JSONObject()},
	};

	/**
	 * <p>Set the {@link #preferences} to a default initial state.</p>
	 */
	static synchronized void setInitState() {
		final JSONObject file_header = new JSONObject();
		try {
			file_header.putOpt(Keys.FileHeader.TYPE, Values.FileHeader.TYPE);
			file_header.putOpt(Keys.FileHeader.TYPE_VERSION, Values.FileHeader.TYPE_VERSION);
			keys_objects_map[0][1] = file_header;
			preferences = new JSONObject();
			for (final Object[] object : keys_objects_map) {
				preferences.put((String) object[0], object[1]);
			}
		} catch (final JSONException ignored) {
			// Won't happen
		}
	}

	/**
	 * <p>Check if the {@link #preferences} have been set.</p>
	 * <p>Once they are set, they won't be null ever again until an app restart.</p>
	 *
	 * @return true if they've been set, false otherwise
	 */
	static synchronized boolean arePrefsReady() {
		return null != preferences;
	}

	/**
	 * <p>Set the {@link #preferences}.</p>
	 *
	 * @param json the object to set the preferences to
	 */
	static synchronized void setPreferences(@NonNull final JSONObject json) {
		preferences = json;

		for (final Object[] object : keys_objects_map) {
			try {
				object[1] = preferences.getJSONArray((String) object[0]);
			} catch (final JSONException ignored) {
				// If it fails, an empty one remains
			}
		}
	}

	static synchronized void updatePreferences() {
		if (null == preferences) {
			return;
		}

		final Value[][] lists = {
				null,
				SettingsRegistry.getArray(),
				ValuesRegistry.getArray(),
		};
		final int lists_length = lists.length;

		for (int i = 0; i < lists_length; ++i) {
			if (null == lists[i]) {
				continue;
			}

			for (final Value value : lists[i]) {
				try {
				final JSONObject jsonArray = new JSONObject().
						//put(ValueObj.JSONKeys.TYPE, ijasdf).
						put(Value.JSONKeys.VALUE, value.getData(JSONObject.NULL)).
						put(Value.JSONKeys.VALUE_TIME, value.getTime()).
						put(Value.JSONKeys.PREV_VALUE, value.getPrevData(JSONObject.NULL)).
						put(Value.JSONKeys.PREV_VALUE_TIME, value.getPrevTime());
					((JSONObject) keys_objects_map[i][1]).put(value.key, jsonArray);
				} catch (final JSONException ignored) {
					// Won't happen. The values are of supported types always.
				}
			}
		}
	}

	/**
	 * <p>Get the {@link #preferences} as a JSON-formatted string.</p>
	 *
	 * @return the JSON-formatted string
	 */
	@Nullable
	static synchronized String getPreferences() {
		if (null != preferences) {
			return preferences.toString();
		}

		return null;
	}



	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private PrefsFileFormat() {
	}
}
