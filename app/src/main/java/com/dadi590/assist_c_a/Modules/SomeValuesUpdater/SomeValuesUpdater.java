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

package com.dadi590.assist_c_a.Modules.SomeValuesUpdater;

import com.dadi590.assist_c_a.GlobalInterfaces.IModule;
import com.dadi590.assist_c_a.ValuesStorage.CONSTS;
import com.dadi590.assist_c_a.ValuesStorage.ValuesStorage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * <p>The module that periodically updates some values which no other module updates by an event.</p>
 * <p>Examples: time and weather (can't be updated by an event - there isn't one).</p>
 */
public class SomeValuesUpdater implements IModule {

	private boolean is_module_destroyed = false;
	@Override
	public final boolean isModuleFullyWorking() {
		if (is_module_destroyed) {
			return false;
		}

		return infinity_thread.isAlive();
	}
	@Override
	public final void destroyModule() {
		infinity_thread.interrupt();
		is_module_destroyed = true;
	}

	/**
	 * <p>Main class constructor.</p>
	 */
	public SomeValuesUpdater() {
		infinity_thread.start();
	}

	private final Thread infinity_thread = new Thread(new Runnable() {
		@Override
		public void run() {
			while (true) {
				{
					final SimpleDateFormat time = new SimpleDateFormat(CONSTS.CURRENT_TIME_FORMAT, Locale.getDefault());
					time.setTimeZone(TimeZone.getDefault());
					ValuesStorage.updateValue(CONSTS.current_time, time.format(new Date()));

					// Keep the timezone in English here so he can say the weekday in English.
					final SimpleDateFormat date = new SimpleDateFormat(CONSTS.CURRENT_DATE_FORMAT, Locale.US);
					date.setTimeZone(TimeZone.getDefault());
					ValuesStorage.updateValue(CONSTS.current_date, date.format(new Date()));
				}


				try {
					Thread.sleep(10_000L);
				} catch (final InterruptedException ignored) {
					Thread.currentThread().interrupt();

					return;
				}
			}
		}
	});
}
