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

package com.edw590.visor_c_a.Modules.UserLocator;

import androidx.annotation.NonNull;

import com.edw590.visor_c_a.GlobalInterfaces.IModuleInst;
import com.edw590.visor_c_a.GlobalUtils.UtilsGeneral;
import com.edw590.visor_c_a.Modules.DeviceLocator.DeviceLocator;
import com.edw590.visor_c_a.Modules.DeviceLocator.ExtDeviceObj;
import com.edw590.visor_c_a.Modules.PreferencesManager.Registry.UtilsRegistry;
import com.edw590.visor_c_a.Modules.PreferencesManager.Registry.Value;
import com.edw590.visor_c_a.Modules.PreferencesManager.Registry.ValuesRegistry;

import kotlin.jvm.functions.Function0;

public class UserLocator implements IModuleInst {

	///////////////////////////////////////////////////////////////
	// IModuleInst stuff
	private boolean is_module_destroyed = false;
	@Override
	public boolean isFullyWorking() {
		if (is_module_destroyed) {
			return false;
		}

		return UtilsGeneral.isThreadWorking(infinity_thread);
	}
	@Override
	public void destroy() {
		infinity_thread.interrupt();

		is_module_destroyed = true;
	}
	@Override
	public int wrongIsSupported() {return 0;}
	/**.
	 * @return read all here {@link IModuleInst#wrongIsSupported()} */
	public static boolean isSupported() {
		return true;
	}
	// IModuleInst stuff
	///////////////////////////////////////////////////////////////

	/**
	 * <p>Main class constructor.</p>
	 */
	public UserLocator() {
		infinity_thread.start();
	}

	private static final int USER_LOCATION_UNKNOWN = -1;
	private static final int USER_LOCATION_HOME = 0;
	private static final int USER_LOCATION_SCHOOL = 1;

	private class Condition {
		@NonNull Object to_check;
		@NonNull Object value;
		long duration; // In seconds
		@NonNull String key_to_update;
		@NonNull Object value_to_update;

		Condition(@NonNull final Object to_check, @NonNull final Object value, final long duration, @NonNull String key_to_update,
				  @NonNull Object value_to_update) {
			this.to_check = to_check;
			this.value = value;
			this.duration = duration;
			this.key_to_update = key_to_update;
			this.value_to_update = value_to_update;
		}
	}

	Condition[] conditions = {
			new Condition(ValuesRegistry.Keys.AIRPLANE_MODE_ON, true, 0L, ValuesRegistry.Keys.IS_USER_SLEEPING, true),
			new Condition(ValuesRegistry.Keys.AIRPLANE_MODE_ON, false, 0, ValuesRegistry.Keys.IS_USER_SLEEPING, false),
			new Condition((Function0<Boolean>) () -> {
				for (final ExtDeviceObj wifi_ap : DeviceLocator.nearby_aps_wifi) {
					if ("eduroam".equals(wifi_ap.name)) {
						return true;
					}
				}
				return false;
			}, true, 0L, ValuesRegistry.Keys.CURR_USER_LOCATION, USER_LOCATION_SCHOOL),
			new Condition((Function0<Boolean>) () -> {
				for (final ExtDeviceObj wifi_ap : DeviceLocator.nearby_aps_wifi) {
					if ("Vodafone-A18391".equals(wifi_ap.name) || "Vodafone-3052B4".equals(wifi_ap.name) ||
							"NOS-3440".equals(wifi_ap.name)) {
						return true;
					}
				}
				return false;
			}, true, 0L, ValuesRegistry.Keys.CURR_USER_LOCATION, USER_LOCATION_HOME),
			// Else condition below
			new Condition((Function0<Boolean>) () -> {
				for (final ExtDeviceObj wifi_ap : DeviceLocator.nearby_aps_wifi) {
					if ("eduroam".equals(wifi_ap.name) || "Vodafone-A18391".equals(wifi_ap.name) ||
							"Vodafone-3052B4".equals(wifi_ap.name) || "NOS-3440".equals(wifi_ap.name)) {
						return false;
					}
				}
				return true;
			}, true, 0L, ValuesRegistry.Keys.CURR_USER_LOCATION, USER_LOCATION_UNKNOWN),
	};

	private final Thread infinity_thread = new Thread(() -> {
		while (true) {
			for (final Condition condition : conditions) {
				if (condition.to_check instanceof String) {
					final Value value = UtilsRegistry.getValue((String) condition.to_check);
					final long time_between_changes = System.currentTimeMillis() - value.getTime();
					if (condition.value.equals(value.getData()) && time_between_changes >= condition.duration * 1000) {
						UtilsRegistry.setValue(condition.key_to_update, condition.value_to_update);
					}
				} else if (condition.to_check instanceof Function0) {
					final Function0<Boolean> function = (Function0<Boolean>) condition.to_check;
					if (function.invoke() == condition.value) {
						final Value value = UtilsRegistry.getValue(condition.key_to_update);
						final long time_between_changes = System.currentTimeMillis() - value.getTime();
						if (time_between_changes >= condition.duration * 1000) {
							UtilsRegistry.setValue(condition.key_to_update, condition.value_to_update);
						}
					}
				}
			}

			try {
				Thread.sleep(1000);
			} catch (final InterruptedException ignored) {
				return;
			}
		}
	});
}
