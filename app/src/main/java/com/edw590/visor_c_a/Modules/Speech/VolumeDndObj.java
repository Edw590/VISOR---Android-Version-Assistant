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

package com.edw590.visor_c_a.Modules.Speech;

import android.app.NotificationManager;
import android.media.AudioManager;

/**
 * <p>Class to instantiate to keep the values of the volume and Do Not Disturb interruption filter.</p>
 */
final class VolumeDndObj {

	/**
	 * <p>Default value for all the attributes of the object.</p>
	 * <br>
	 * <p>Can't be a possible value of any of:</p>
	 * <p>- The {@code STREAM_} constants defined in {@link AudioManager}</p>
	 * <p>- The parameters of {@link AudioManager#setStreamVolume(int, int, int)}</p>
	 * <p>- The return of {@link AudioManager#getStreamVolume(int)}</p>
	 * <p>- The parameter of {@link NotificationManager#setInterruptionFilter(int)}</p>
	 * <p>- The return of {@link NotificationManager#getCurrentInterruptionFilter()}</p>
	 * <p>- Any possible value of {@link System#currentTimeMillis()}</p>
	 */
	static final int DEFAULT_VALUE = -3234;

	// To add a new attribute, update the DEFAULT_VALUE doc and the setDefaultValues() function.

	/** One of the {@code STREAM_} constants in {@link AudioManager}. */
	int audio_stream;
	/** The result of {@link AudioManager#getStreamVolume(int)} before making any changes. */
	int old_volume;

	/** The result of {@link NotificationManager#getCurrentInterruptionFilter()} before making any changes. */
	int old_interruption_filter;
	/** The interruption filter to be set with {@link NotificationManager#setInterruptionFilter(int)}. */
	int new_interruption_filter;

	/** The result of {@link AudioManager#getRingerMode()} before making any changes. */
	int old_ringer_mode;

	// To add a new attribute, update the DEFAULT_VALUE doc and the setDefaultValues() function.

	/**
	 * <p>Main class constructor - calls {@link #setDefaultValues()}.</p>
	 */
	VolumeDndObj() {
		setDefaultValues();
	}

	/**
	 * <p>Sets all the attributes to their default values.</p>
	 */
	void setDefaultValues() {
		audio_stream = DEFAULT_VALUE;
		old_volume = DEFAULT_VALUE;
		old_interruption_filter = DEFAULT_VALUE;
		new_interruption_filter = DEFAULT_VALUE;
		old_ringer_mode = DEFAULT_VALUE;
	}
}
