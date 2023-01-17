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

package com.dadi590.assist_c_a;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dadi590.assist_c_a.GlobalInterfaces.IModuleInst;
import com.dadi590.assist_c_a.GlobalUtils.UtilsProcesses;
import com.dadi590.assist_c_a.GlobalUtils.UtilsReflection;
import com.dadi590.assist_c_a.GlobalUtils.UtilsServices;
import com.dadi590.assist_c_a.Modules.AudioRecorder.AudioRecorder;
import com.dadi590.assist_c_a.Modules.BatteryProcessor.BatteryProcessor;
import com.dadi590.assist_c_a.Modules.CameraManager.CameraManagement;
import com.dadi590.assist_c_a.Modules.CmdsExecutor.CmdsExecutor;
import com.dadi590.assist_c_a.Modules.ModulesManager.ModulesManager;
import com.dadi590.assist_c_a.Modules.ProtectedLockScr.ProtectedLockScrSrv;
import com.dadi590.assist_c_a.Modules.Speech.Speech2;
import com.dadi590.assist_c_a.Modules.SpeechRecognition.CONSTS_SpeechRecog;
import com.dadi590.assist_c_a.Modules.SpeechRecognition.SpeechRecognitionCtrl;
import com.dadi590.assist_c_a.Modules.Telephony.PhoneCallsProcessor.PhoneCallsProcessor;
import com.dadi590.assist_c_a.Modules.Telephony.SmsMsgsProcessor.SmsMsgsProcessor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * <p>The list of modules and submodules of the assistant plus information about their status.</p>
 * <p>Various modules just need to be instantiated to start working. The references to those instances are stored here
 * too statically, and there's no problem with that because all those the modules are instantiated inside the same
 * process: the Main Service process.</p>
 * <p>Check the module index before sending it into any of the functions of this class. The index is not checked inside
 * them! They will throw exceptions when they try to use the invalid indexes!</p>
 * <p>In this class there are modules, submodules, and "elements", which is referring any of the 2 others. If a function
 * says it's about a module, check first if you're giving it a module with {@link #ELEMENT_IS_MODULE}. If it's
 * about an element, no need to check anything.</p>
 */
public final class ModulesList {


	// todo The 'disable_mod' parameter is not being used at all yet
	//  Can all modules be disabled...? (PLS, Speech...?) can_be_disabled?

	// There are only 2 TYPE1s of modules: INSTANCE, which means it is a simple instantiation of its class on some
	// thread, or SERVICE_SEP, which means it's a service ---in a separate process---, which also means there are no
	// services running in the Main Service process other than the Main Service. That's to simplify stuff. Why have a
	// service if one can have a simple instance with which we can communicate directly and easily through IModule?
	//
	// Also, positive values are for modules to check and restart if they're not working properly or at all always, and
	// negative values are for modules to check only, like the Protected Lock Screen - these negative values MUST be
	// symmetrical of their counterparts (if it's a TYPE1_SERVICE_SEP but not to be restarted, then -TYPE1_SERVICE_SEP).
	//
	// NO 0 VALUES HERE!!!! (Reason just above)
	/** It's a module that runs on a simple instance of its class on the Main Service process. */
	public static final int TYPE1_INSTANCE = 1;
	/** It's a service running on an separate process. */
	public static final int TYPE1_SERVICE_SEP = 2;
	/** If it's of {@link #TYPE1_SERVICE_SEP} but it's to be only checked if it's working, like with the Protected
	 * Lock Screen - it's not supposed to be always running... xD - it must be able of restarting itself then. */
	public static final int TYPE1_SERVICE_SEP_CHK_ONLY = -TYPE1_SERVICE_SEP;
	public static final int TYPE1_LIBRARY = -3;

	// To disable a module, just comment its line here and be sure you disable its usages everywhere else or pray the
	// app won't crash because of negative index from getModuleIndex() in case it's used for the disabled module.
	/** List of all modules of the app, and also the wanted submodules to be shown on the Modules Status - check which
	 * is what with {@link #ELEMENT_IS_MODULE}.*/
	private static final ElementObj[] sub_and_modules_list = {
			new ModuleObj(ModulesManager.class, "Modules Manager", TYPE1_INSTANCE),
			//new ModuleObj(SomeValuesUpdater.class, "Some Values Updater", TYPE1_INSTANCE),
			new ModuleObj(Speech2.class, "Speech", TYPE1_INSTANCE), // (TYPE2_NOT_SPECIAL because internally it checks if there's audio/TTS support or not to use TTS or notifications)
			//new ModuleObj(DeviceLocator.class, "Device Locator", TYPE1_INSTANCE),
			new ModuleObj(BatteryProcessor.class, "Battery Processor", TYPE1_INSTANCE),
			new ModuleObj(PhoneCallsProcessor.class, "Phone Calls Processor", TYPE1_INSTANCE),
			new ModuleObj(SmsMsgsProcessor.class, "SMS Messages Processor", TYPE1_INSTANCE),
			new ModuleObj(AudioRecorder.class, "Audio Recorder", TYPE1_INSTANCE),
			new ModuleObj(CameraManagement.class, "Camera Manager", TYPE1_INSTANCE),
			new ModuleObj(CmdsExecutor.class, "Commands Executor", TYPE1_INSTANCE),

			// todo Make a new class for libraries? And get them to return on a standard function the supported
			//  architectures. Then check if the file is present or at least if the library has been loaded by catching
			//  a Throwable.
			//new SubmoduleObj(ACD.ACD.class, "Advanced Commands Detection", TYPE1_LIBRARY),

			new ModuleObj(SpeechRecognitionCtrl.class, "Speech Recognition Control", TYPE1_INSTANCE),
			new SubmoduleObj(CONSTS_SpeechRecog.POCKETSPHINX_RECOG_CLASS, "- Hotword recognizer", TYPE1_SERVICE_SEP),
			new SubmoduleObj(CONSTS_SpeechRecog.GOOGLE_RECOG_CLASS, "- Commands recognizer", TYPE1_SERVICE_SEP),
			new ModuleObj(ProtectedLockScrSrv.class, "Protected Lock Screen", TYPE1_SERVICE_SEP_CHK_ONLY),
	};
	public static final int sub_and_modules_list_length = sub_and_modules_list.length;
	/**
	 * <p>Class for the elements of the list - only to be instantiated through the other 2 classes.</p>
	 */
	private static class ElementObj {
		@NonNull final Class<?> elem_class;
		final int elem_type1;
		@NonNull final String elem_name;
		final boolean elem_is_module;

		/**
		 * <p>Main class constructor.</p>
		 *
		 * @param elem_class the class of the submodule
		 * @param elem_name the name of the submodule to present to users
		 * @param elem_type1 the type 1 of the submodule ({@code TYPE1_}-started constants)
		 * @param elem_is_module true if the element is a module, false otherwise (it's a submodule then)
		 */
		ElementObj(@NonNull final Class<?> elem_class, @NonNull final String elem_name, final int elem_type1,
				   final boolean elem_is_module) {
			this.elem_class = elem_class;
			this.elem_name = elem_name;
			this.elem_type1 = elem_type1;
			this.elem_is_module = elem_is_module;
		}
	}
	/**
	 * <p>Class to use to represent each module of the assistant.</p>
	 * <p>Check the constructor for more information.</p>
	 */
	private static class ModuleObj extends ElementObj {
		@Nullable Object mod_instance = null;
		boolean disable_mod = false;
		boolean mod_supported = false;

		/**
		 * <p>Main class constructor.</p>
		 * <p>It also sets the {@link #mod_supported} variable by checking directly if the module is supported or not,
		 * and if it's a submodule, the variable is automatically set to true.</p>
		 *  @param elem_class the class of the module
		 * @param elem_name the name of the module to present to users
		 * @param elem_type1 the type 1 of the module ({@code TYPE1_}-started constants)
		 */
		ModuleObj(@NonNull final Class<?> elem_class, @NonNull final String elem_name, final int elem_type1) {
			super(elem_class, elem_name, elem_type1, true);

			mod_supported = isModuleSupported(elem_class);
		}
	}
	/**
	 * <p>Class to use to represent each submodule that is wanted to be shown to the user.</p>
	 * <p>Check the constructor for more information.</p>
	 */
	private static class SubmoduleObj extends ElementObj {
		/**
		 * <p>Main class constructor.</p>
		 * <p>Wrapper for the main class of {@link ElementObj} that automatically sets the element as a submodule.</p>
		 *
		 * @param elem_class the class of the module
		 * @param elem_name the name of the module to present to users
		 * @param elem_type1 the type 1 of the module ({@code TYPE1_}-started constants)
		 */
		SubmoduleObj(@NonNull final Class<?> elem_class, @NonNull final String elem_name, final int elem_type1) {
			super(elem_class, elem_name, elem_type1, false);
		}
	}

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private ModulesList() {
	}

	public static final int ELEMENT_CLASS = 0;
	public static final int ELEMENT_NAME = 1;
	public static final int ELEMENT_TYPE1 = 2;
	public static final int ELEMENT_IS_MODULE = 3;
	public static final int MODULE_INSTANCE = 4;
	public static final int MODULE_SUPPORTED = 5;
	public static final int MODULE_DISABLE = 6;
	/**
	 * <p>Get the value on the {@link #sub_and_modules_list} associated with the given key, for a specific element.</p>
	 * <br>
	 * <p><strong>WARNING:</strong> CHECK FIRST if the value you're getting matches the indexed element!!! If you get
	 * a MODULE_ value from an element which is a submodule, bad things will happen - the function is not made to handle
	 * that.</p>
	 * <p>WARNING 2: This method does not have @Nullable on it, but it CAN return null. Though, having @Nullable would
	 * only get in the way, because it can return null only if the required value can be null. If an int is requested,
	 * it won't be null. If it's the module instance, it can be null. So it's not necessary to check every time for
	 * nullability (as would make sense).</p>
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link #ELEMENT_CLASS} --> for {@code key}: the class of the element.</p>
	 * <p>- {@link #ELEMENT_NAME} --> for {@code key}: the element name to show to the user</p>
	 * <p>- {@link #ELEMENT_TYPE1} --> for {@code key}: the TYPE1_-started constant of the element</p>
	 * <p>- {@link #ELEMENT_IS_MODULE} --> for {@code key}: if the element is a module (or else, a submodule)</p>
	 * <p>- {@link #MODULE_INSTANCE} --> for {@code key}: the instance of the module if it's of {@link #TYPE1_INSTANCE}</p>
	 * <p>- {@link #MODULE_SUPPORTED} --> for {@code key}: if the module is supported on the device or not, including
	 *      hardware support and permissions</p>
	 * <p>- {@link #MODULE_DISABLE} --> for {@code key}: if the module has been disabled by the user or not</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @param module_index the index of the element
	 * @param key the key of the value to get (one of the constants)
	 *
	 * @return the value associated with the given key, which may or may not be null, depending on type of the wanted
	 * value as listed on {@link #sub_and_modules_list}'s documentation
	 */
	public static Object getElementValue(final int module_index, final int key) {
		switch (key) {
			case ELEMENT_CLASS: {
				return sub_and_modules_list[module_index].elem_class;
			}
			case ELEMENT_NAME: {
				return sub_and_modules_list[module_index].elem_name;
			}
			case ELEMENT_TYPE1: {
				return sub_and_modules_list[module_index].elem_type1;
			}
			case ELEMENT_IS_MODULE: {
				return sub_and_modules_list[module_index].elem_is_module;
			}
			case MODULE_INSTANCE: {
				return ((ModuleObj) sub_and_modules_list[module_index]).mod_instance;
			}
			case MODULE_SUPPORTED: {
				return ((ModuleObj) sub_and_modules_list[module_index]).mod_supported;
			}
			case MODULE_DISABLE: {
				return ((ModuleObj) sub_and_modules_list[module_index]).disable_mod;
			}
		}

		// Won't happen - always implement the constants on the switch.
		// Don't put null, or Android Studio will think it's a @Nullable method, and then warn about 'boolean' null
		// values...
		return "";
	}

	/**
	 * <p>Set the value on the {@link #sub_and_modules_list} associated with the given key, for a specific module.</p>
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link #MODULE_SUPPORTED} --> for {@code key}: same as in {@link #getElementValue(int, int)}</p>
	 * <p>- {@link #MODULE_DISABLE} --> for {@code key}: same as in {@link #getElementValue(int, int)}</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @param module_index the index of the module
	 * @param key the key of the value to get (one of the constants)
	 * @param value the value associated with the given key
	 */
	public static void setModuleValue(final int module_index, final int key, @NonNull final Object value) {
		switch (key) {
			case MODULE_SUPPORTED: {
				((ModuleObj) sub_and_modules_list[module_index]).mod_supported = (boolean) value;

				break;
			}
			case MODULE_DISABLE: {
				((ModuleObj) sub_and_modules_list[module_index]).disable_mod = (boolean) value;

				break;
			}
		}
	}

	/**
	 * <p>Checks if the given list element is running or not.</p>
	 * <p>Use {@link #isModuleFullyWorking(int)} to check if it's fully working or is malfunctioning.</p>
	 *
	 * @param module_index the index of the module
	 *
	 * @return true if it's running, false otherwise
	 */
	public static boolean isElementRunning(final int module_index) {
		switch (Math.abs((int) getElementValue(module_index, ELEMENT_TYPE1))) {
			case (TYPE1_SERVICE_SEP): {
				return UtilsServices.isServiceRunning((Class<?>) getElementValue(module_index, ELEMENT_CLASS));
			}
			case (TYPE1_INSTANCE): {
				return null != getElementValue(module_index, MODULE_INSTANCE);
			}
			case (TYPE1_LIBRARY): {

			}
		}

		// Won't ever get here.
		return false;
	}

	/**
	 * <p>Checks if the given module is fully working.</p>
	 *
	 * @param module_index the index of the module
	 *
	 * @return true if it's fully working, false otherwise
	 */
	public static boolean isModuleFullyWorking(final int module_index) {
		if (TYPE1_SERVICE_SEP == Math.abs((int) getElementValue(module_index, ELEMENT_TYPE1))) {
			// Assume it's fully working if it's of TYPE1_SERVICE_SEP, which doesn't implement IModule (no way of
			// knowing yet).
			return isElementRunning(module_index);
		}

		// module will never been null because that's what's checked inside isElementRunning().
		return isElementRunning(module_index) && Objects.requireNonNull(getIModuleInstance(module_index))
				.isFullyWorking();
	}

	/**
	 * <p>The index of the element in the {@link #sub_and_modules_list}.</p>
	 *
	 * @param module_class the class of the module
	 *
	 * @return the index of the module
	 */
	public static int getElementIndex(@NonNull final Class<?> module_class) {
		for (int module_index = 0; module_index < sub_and_modules_list_length; ++module_index) {
			if ((Class<?>) getElementValue(module_index, ELEMENT_CLASS) == module_class) {
				return module_index;
			}
		}

		return -1; // Won't ever get here - just supply a valid module by not using a string and calling .class on the
		// class directly.
	}

	/**
	 * <p>Starts the specified module in case it has not been already started.</p>
	 * <p>It is first checked if the provided module is supported or not.</p>
	 * <p>In case it's a Service, it will be started in background (not foreground), as the Main Service is already in
	 * foreground - one is enough according to the Android security policies.</p>
	 * <p>In case it's just to start the module and keep a reference to it, its reference will be stored on the
	 * {@link #sub_and_modules_list}.</p>
	 *
	 * @param module_index the index of the module to start
	 */
	public static void startModule(final int module_index) {
		if (!isModuleSupported(module_index)) {
			return;
		}

		final Class<?> module_class = (Class<?>) getElementValue(module_index, ELEMENT_CLASS);
		switch (Math.abs((int) getElementValue(module_index, ELEMENT_TYPE1))) {
			case (ModulesList.TYPE1_SERVICE_SEP): {
				// startService() already checks if the service is running or not.
				UtilsServices.startService(module_class, null, false, true);

				break;
			}
			case (ModulesList.TYPE1_INSTANCE): {
				if (!ModulesList.isElementRunning(module_index)) {
					try {
						((ModuleObj) sub_and_modules_list[module_index]).mod_instance =module_class.getConstructor()
								.newInstance();
					} catch (final NoSuchMethodException ignored) {
					} catch (final IllegalAccessException ignored) {
					} catch (final InstantiationException ignored) {
					} catch (final InvocationTargetException ignored) {
					}
				}

				break;
			}
			default: {
				// Nothing
			}
		}
	}

	/**
	 * <p>Stops the specified module in case it's running.</p>
	 * <p>It is first checked if the given index is indeed of a module and not a submodule.</p>
	 * <p>In case it's of type {@link #TYPE1_SERVICE_SEP}, its PID will be terminated. If it's of type
	 * {@link #TYPE1_INSTANCE}, the {@link IModuleInst#destroy()} will be called and its reference will be set to
	 * null.</p>
	 *
	 * @param module_index the index of the module to stop
	 */
	public static void stopModule(final int module_index) {
		switch (Math.abs((int) getElementValue(module_index, ELEMENT_TYPE1))) {
			case (ModulesList.TYPE1_SERVICE_SEP): {
				if (ModulesList.isElementRunning(module_index)) {
					final Class<?> module_class = (Class<?>) getElementValue(module_index, ELEMENT_CLASS);
					UtilsProcesses.terminatePID(UtilsProcesses.getRunningServicePID(module_class));
				}

				break;
			}
			case (ModulesList.TYPE1_INSTANCE): {
				if (ModulesList.isElementRunning(module_index)) {
					((IModuleInst) getElementValue(module_index, MODULE_INSTANCE)).destroy();
					((ModuleObj) sub_and_modules_list[module_index]).mod_instance = null;
				}

				break;
			}
		}
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
	 * <p>Get the module instance cast as an {@link IModuleInst}.</p>
	 *
	 * @param module_index the index of the module to get as an {@link IModuleInst}
	 *
	 * @return the {@link IModuleInst} instance; null in case the module is not running OR is not of {@link #TYPE1_INSTANCE}
	 */
	@Nullable
	private static IModuleInst getIModuleInstance(final int module_index) {
		if (TYPE1_INSTANCE != (int) getElementValue(module_index, ELEMENT_TYPE1)) {
			return null;
		}
		return (IModuleInst) getElementValue(module_index, MODULE_INSTANCE);
	}

	/**
	 * <p>Same as {@link #isModuleSupported(Class)}.</p>
	 * <p>ONLY FOR USE IN THE {@link ModulesManager}!!!! This takes a bit to check. Use {@link #MODULE_SUPPORTED} for
	 * all other requests.</p>
	 *
	 * @param module_index the index of the module to check
	 *
	 * @return same as in {@link #isModuleSupported(Class)}
	 */
	public static boolean isModuleSupported(final int module_index) {
		return isModuleSupported((Class<?>) getElementValue(module_index, ELEMENT_CLASS));
	}

	/**
	 * <p>Checks if the device running the app supports the module.</p>
	 * <p>This method signature is useful for {@link ModuleObj}'s constructor, which can't go on the list look for what
	 * is being created at the moment.</p>
	 *
	 * @param module_class the class of the module
	 * @return true if the module is supported by the device, false otherwise
	 */
	static boolean isModuleSupported(@NonNull final Class<?> module_class) {
		// Had to use reflection. It's to behave like IModule, but with a static method that changes for every
		// module and that can be called through the class, so that it doesn't matter if the module is a separate
		// process services. Can't use an interface, use reflection and make sure all modules implement the method on
		// them.
		final Method method = UtilsReflection.getMethod(module_class, "isSupported");
		// In case this throws an error while I'm messing with the modules, so that I know in which module is the error.
		System.out.println(module_class);
		// It's never null when it gets here, unless I was dumb and forgot to put the method in some class.
		// Even with the no-modules this won't get here - they always return true on supported check.
		assert null != method;
		// No need to check for errors invoking the method - there will be none. It's always declared and
		// implemented the same for all modules, so if it works with one, it works with the rest.
		return (boolean) UtilsReflection.invokeMethod(method, null).ret_var;
	}
}
