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

import com.dadi590.assist_c_a.GlobalInterfaces.IModule;
import com.dadi590.assist_c_a.GlobalUtils.UtilsProcesses;
import com.dadi590.assist_c_a.GlobalUtils.UtilsServices;
import com.dadi590.assist_c_a.Modules.AudioRecorder.AudioRecorder;
import com.dadi590.assist_c_a.Modules.BatteryProcessor.BatteryProcessor;
import com.dadi590.assist_c_a.Modules.CmdsExecutor.CmdsExecutor;
import com.dadi590.assist_c_a.Modules.ModulesManager.ModulesManager;
import com.dadi590.assist_c_a.Modules.ProtectedLockScr.ProtectedLockScrSrv;
import com.dadi590.assist_c_a.Modules.Speech.Speech2;
import com.dadi590.assist_c_a.Modules.SpeechRecognition.CONSTS;
import com.dadi590.assist_c_a.Modules.SpeechRecognition.SpeechRecognitionCtrl;
import com.dadi590.assist_c_a.Modules.Telephony.PhoneCallsProcessor.PhoneCallsProcessor;
import com.dadi590.assist_c_a.Modules.Telephony.SmsMsgsProcessor.SmsMsgsProcessor;
import com.dadi590.assist_c_a.Modules.SomeValuesUpdater.SomeValuesUpdater;

import java.lang.reflect.InvocationTargetException;

/**
 * <p>The list of modules of the assistant plus information about their status.</p>
 * <p>Various modules just need to be instantiated to start working. The references to those instances are stored here
 * too statically, and there's no problem with that because all those the modules are instantiated inside the main app
 * process.</p>
 */
public final class ModulesList {

	private static final int MODULE_TYPE_STATIC = -2;
	// In case it's not a module and it's just wanted to check if it's running or not. Could be a submodule. For example
	// the Google and PocketSphinx speech recognizers. That's why the constant is private. It's not to be used externally.
	private static final int MODULE_TYPE_SERVICE_CHK_ONLY = -1;
	public static final int MODULE_TYPE_SERVICE = 0;
	public static final int MODULE_TYPE_INSTANCE = 1;

	/**
	 * <p>List of the modules to start <strong>in order!</strong></p>
	 * <p>Each sub-array has 4 elements described below:</p>
	 * <p>- 1st index: class of the module</p>
	 * <p>- 2nd index: type of the module</p>
	 * <p>- 3rd index: name of the module</p>
	 * <p>- 4th index: instance of the module, in case it applies. If it does not apply, keep null</p>
	 * <p>To check if the module is running, check its type and check if the instance is not null or the service is
	 * running.</p>
	 * <p>Types of modules:</p>
	 * <p>- {@link #MODULE_TYPE_SERVICE}: the module is a {@link android.app.Service} running on a separate process</p>
	 * <p>- {@link #MODULE_TYPE_INSTANCE}: the module is a normal class that must only be instantiated</p>
	 * <p>- {@link #MODULE_TYPE_STATIC}: the module is a class with only static methods, so it's always ready to work</p>
	 * <p>- {@link #MODULE_TYPE_SERVICE_CHK_ONLY}: it's a submodule that run as a {@link android.app.Service} on a
	 * separate process and it's here just to appear on the modules list - not to be used for anything</p>
	 */
	private static final Object[][] modules_list = {
			{ModulesManager.class, MODULE_TYPE_INSTANCE, "Modules Manager", null},
			{SomeValuesUpdater.class, MODULE_TYPE_INSTANCE, "Some Values Updater", null},
			{Speech2.class, MODULE_TYPE_INSTANCE, "Speech", null},
			//{DeviceLocator.class, MODULE_TYPE_INSTANCE, "Device Locator", null},
			{BatteryProcessor.class, MODULE_TYPE_INSTANCE, "Battery Processor", null},
			{PhoneCallsProcessor.class, MODULE_TYPE_INSTANCE, "Phone Calls Processor", null},
			{SmsMsgsProcessor.class, MODULE_TYPE_STATIC, "SMS Messages Processor", null},
			{AudioRecorder.class, MODULE_TYPE_INSTANCE, "Audio Recorder", null},
			//{CameraManagement.class, MODULE_TYPE_INSTANCE, "Camera Manager", null},
			{CmdsExecutor.class, MODULE_TYPE_INSTANCE, "Commands Executor", null},
			{SpeechRecognitionCtrl.class, MODULE_TYPE_INSTANCE, "Speech Recognition Control", null},
			{CONSTS.POCKETSPHINX_RECOG_CLASS, MODULE_TYPE_SERVICE_CHK_ONLY, "- Hotword recognizer", null},
			{CONSTS.GOOGLE_RECOG_CLASS, MODULE_TYPE_SERVICE_CHK_ONLY, "- Commands recognizer", null},
			{ProtectedLockScrSrv.class, MODULE_TYPE_SERVICE_CHK_ONLY, "Protected Lock Screen", null},
	};
	public static final int modules_list_length = modules_list.length;

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
			case (MODULE_TYPE_INSTANCE): {
				return null != modules_list[module_index][3];

			}
			case (MODULE_TYPE_STATIC): {
				return true;
			}
		}

		// Won't ever get here.
		return false;
	}

	/**
	 * <p>The index of the module in the {@link #modules_list}.</p>
	 *
	 * @param module_class the class of the module
	 *
	 * @return the index of the module
	 */
	public static int getModuleIndex(@NonNull final Class<?> module_class) {
		for (int i = 0; i < modules_list_length; ++i) {
			if ((Class<?>) modules_list[i][0] == module_class) {
				return i;
			}
		}

		return -1; // Won't ever get here - just supply a valid module by not using a string and calling .class on the
		// class directly.
	}

	/**
	 * <p>Starts the specified module in case it has not been already started.</p>
	 * <p>In case it's a Service, it will be started in background (not foreground), as the Main Service is already in
	 * foreground - one is enough according to the Android policies.</p>
	 * <p>In case it's just to start the module and keep a reference to it, its reference will be stored on the
	 * {@link #modules_list}.</p>
	 *
	 * @param module_index the index of the module to start
	 *
	 * @return true if the module was started, false if it was already running
	 */
	public static boolean startModule(final int module_index) {
		final Object[] module = modules_list[module_index];
		switch ((int) module[1]) {
			case (ModulesList.MODULE_TYPE_SERVICE): {

				// The call to startService() will already check if the service is running or not.
				return UtilsServices.startService((Class<?>) module[0], null, false);
			}
			case (ModulesList.MODULE_TYPE_INSTANCE): {
				if (!ModulesList.isModuleRunningByIndex(module_index)) {
					try {
						modules_list[module_index][3] = ((Class<?>) module[0]).getConstructor().newInstance();

						return true;
					} catch (final NoSuchMethodException ignored) {
					} catch (final IllegalAccessException ignored) {
					} catch (final InstantiationException ignored) {
					} catch (final InvocationTargetException ignored) {
					}
				}

				return false;
			}

			default: {
				return false;
			}
		}
	}

	/**
	 * <p>Stops the specified module in case it's running.</p>
	 * <p>In case it's of type {@link #MODULE_TYPE_SERVICE}, its PID will be terminated. If it's of type
	 * {@link #MODULE_TYPE_INSTANCE}, {@link IModule#destroyModule()} will be called on it and its reference will be set
	 * to null.</p>
	 *
	 * @param module_index the index of the module to stop
	 *
	 * @return true if the module was stopped, false if it was already stopped
	 */
	public static boolean stopModule(final int module_index) {
		final Object[] module = modules_list[module_index];
		switch ((int) module[1]) {
			case (ModulesList.MODULE_TYPE_SERVICE): {

				// The call to startService() will already check if the service is running or not.
				if (UtilsServices.isServiceRunning((Class<?>) module[0])) {
					UtilsProcesses.terminatePID(UtilsProcesses.getRunningServicePID((Class<?>) module[0]));

					return true;
				} else {
					return false;
				}
			}
			case (ModulesList.MODULE_TYPE_INSTANCE): {
				if (ModulesList.isModuleRunningByIndex(module_index)) {
					((IModule) modules_list[module_index][3]).destroyModule();
					modules_list[module_index][3] = null;

					return true;
				} else {
					return false;
				}
			}
		}

		// Won't ever get here.
		return false;
	}

	/**
	 * <p>Restarts the specified module, whether it was running or not.</p>
	 * <p>Only 2 functions are called here: first {@link #stopModule(int)} and then {@link #startModule(int)}.</p>
	 *
	 * @param module_index the index of the module to restart
	 */
	public static void restartModule(final int module_index) {
		stopModule(module_index);
		startModule(module_index);
	}

	/**
	 * <p>Get the module instance masked as an {@link IModule} in case the module implements it.</p>
	 *
	 * @param module_index the index of the module to get as an {@link IModule}
	 *
	 * @return the {@link IModule} type instance, or null in case the module does not implement said interface
	 */
	@Nullable
	public static IModule getIModule(final int module_index) {
		final Object[] module = modules_list[module_index];
		if (!((Class<?>) module[0]).isAssignableFrom(IModule.class)) {
			return null;
		}

		return (IModule) modules_list[module_index][3];
	}
}
