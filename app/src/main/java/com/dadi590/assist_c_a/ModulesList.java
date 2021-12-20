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

package com.dadi590.assist_c_a;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dadi590.assist_c_a.GlobalUtils.UtilsServices;
import com.dadi590.assist_c_a.Modules.AudioRecorder.AudioRecorder;
import com.dadi590.assist_c_a.Modules.BatteryProcessor.BatteryProcessor;
import com.dadi590.assist_c_a.Modules.CmdsExecutor.CmdsExecutor;
import com.dadi590.assist_c_a.Modules.Speech.Speech2;
import com.dadi590.assist_c_a.Modules.SpeechRecognition.GoogleRecognition;
import com.dadi590.assist_c_a.Modules.SpeechRecognition.PocketSphinxRecognition;
import com.dadi590.assist_c_a.Modules.SpeechRecognition.SpeechRecognitionCtrl;
import com.dadi590.assist_c_a.Modules.Telephony.PhoneCallsProcessor.PhoneCallsProcessor;
import com.dadi590.assist_c_a.Modules.ValuesStorage.SomeValuesUpdater;

/**
 * <p>The list of modules of the assistant.</p>
 */
public final class ModulesList {

	// In case it's not a module and it's just wanted to check if it's running or not. Could be a submodule. For example
	// the Google and PocketSphinx speech recognizers. That's why the constant is private. It's not to be used externally.
	private static final int MODULE_TYPE_INSTANCE_CHK_ONLY = -2;
	private static final int MODULE_TYPE_SERVICE_CHK_ONLY = -1;
	public static final int MODULE_TYPE_SERVICE = 0;
	public static final int MODULE_TYPE_INSTANCE = 1;

	/**
	 * <p>List of the modules to start in order!</p>
	 * <p>Each sub-array has 4 elements described below:</p>
	 * <p>- 1st index: class of the module</p>
	 * <p>- 2nd index: type of the module</p>
	 * <p>- 3rd index: name of the module</p>
	 * <p>- 4th index: instance of the module, in case it applies. If it does not apply, keep null</p>
	 * <p>To check if the module is running, check its type and check if the instance is not null or the service is
	 * running.</p>
	 * <p>Types of modules:</p>
	 * <p>- {@link #MODULE_TYPE_SERVICE}: the module is a {@link android.app.Service}</p>
	 * <p>- {@link #MODULE_TYPE_INSTANCE}: the module is a normal class that must only be instantiated</p>
	 */
	private static final Object[][] modules_list = {
			{SomeValuesUpdater.class, MODULE_TYPE_INSTANCE, "Some Values Updater", null},
			{Speech2.class, MODULE_TYPE_SERVICE, "Speech", null},
			//{DeviceLocator.class, MODULE_TYPE_SERVICE, "Device Locator", null},
			{BatteryProcessor.class, MODULE_TYPE_INSTANCE, "Battery Processor", null},
			{PhoneCallsProcessor.class, MODULE_TYPE_INSTANCE, "Phone Calls Processor", null},
			{AudioRecorder.class, MODULE_TYPE_INSTANCE, "Audio Recorder", null},
			{CmdsExecutor.class, MODULE_TYPE_INSTANCE, "Commands Executor", null},
			{SpeechRecognitionCtrl.class, MODULE_TYPE_INSTANCE, "Speech Recognition Control", null},
			{PocketSphinxRecognition.class, MODULE_TYPE_SERVICE_CHK_ONLY, "- Hotword recognizer", null},
			{GoogleRecognition.class, MODULE_TYPE_SERVICE_CHK_ONLY, "- Commands recognizer", null},
	};

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private ModulesList() {
	}

	/**
	 * <p>Get a clone of {@link #modules_list}.</p>
	 *
	 * @return .
	 */
	@NonNull
	public static Object[][] getModulesList() {
		return modules_list.clone();
	}

	/**
	 * <p>Checks if the given module is running.</p>
	 *
	 * @param module_class the class of the module
	 *
	 * @return true if it's running, false otherwise
	 */
	public static boolean isModuleRunningByClass(@NonNull final Class<?> module_class) {
		int i = 0;
		for (final Object[] module : modules_list) {
			if ((Class<?>) module[0] == module_class) {
				return isModuleRunningByIndex(i);
			}
			++i;
		}

		// Won't ever get here as long as the class exists.
		return false;
	}

	/**
	 * <p>Checks if the given module is running.</p>
	 *
	 * @param module_index the index of the module
	 *
	 * @return true if it's running, false otherwise
	 */
	public static boolean isModuleRunningByIndex(final int module_index) {
		switch ((int) modules_list[module_index][1]) {
			case (MODULE_TYPE_SERVICE):
			case (MODULE_TYPE_SERVICE_CHK_ONLY): {
				return UtilsServices.isServiceRunning((Class<?>) modules_list[module_index][0]);
			}
			case (MODULE_TYPE_INSTANCE):
			case (MODULE_TYPE_INSTANCE_CHK_ONLY): {
					return modules_list[module_index][3] != null;
			}
		}

		// Won't ever get here.
		return false;
	}

	/**
	 * <p>Sets the module instance address to the given one on the {@link #modules_list}.</p>
	 *
	 * @param reference_instance a reference to the module instance address
	 * @param module_index the index of the module in the {@link #modules_list}
	 */
	public static void setModuleInstance(@Nullable final Object reference_instance, final int module_index) {
		modules_list[module_index][3] = reference_instance;
	}

	/**
	 * <p>Gets the reference address to the instance of the module inside {@link #modules_list}, given the module's
	 * class.</p>
	 *
	 * @param module_class the class of the module
	 *
	 * @return the instance of the module
	 */
	@NonNull
	public static Object getModuleInstance(@NonNull final Class<?> module_class) {
		for (final Object[] module : modules_list) {
			if ((Class<?>) module[0] == module_class) {
				return module[3];
			}
		}

		// Won't ever return null as long as the requested modules exist...
		// Unless the module goes down for some reason. Though I think it won't, because if modules_instances is static,
		// the instances will be tied to the process, not the service (so if the process goes down, that means the
		// entire app went too). Hopefully.
		return null;
	}
}
