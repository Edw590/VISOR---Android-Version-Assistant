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
import com.dadi590.assist_c_a.Modules.CameraManager.CameraManagement;
import com.dadi590.assist_c_a.Modules.CmdsExecutor.CmdsExecutor;
import com.dadi590.assist_c_a.Modules.DeviceLocator.DeviceLocator;
import com.dadi590.assist_c_a.Modules.ModulesManager.ModulesManager;
import com.dadi590.assist_c_a.Modules.PowerProcessor.PowerProcessor;
import com.dadi590.assist_c_a.Modules.ProtectedLockScr.ProtectedLockScrSrv;
import com.dadi590.assist_c_a.Modules.Speech.Speech2;
import com.dadi590.assist_c_a.Modules.SpeechRecognitionCtrl.GoogleRecognition;
import com.dadi590.assist_c_a.Modules.SpeechRecognitionCtrl.PocketSphinxRecognition;
import com.dadi590.assist_c_a.Modules.SpeechRecognitionCtrl.SpeechRecognitionCtrl;
import com.dadi590.assist_c_a.Modules.TelephonyManagement.PhoneCallsProcessor.PhoneCallsProcessor;
import com.dadi590.assist_c_a.Modules.TelephonyManagement.SmsMsgsProcessor.SmsMsgsProcessor;
import com.dadi590.assist_c_a.Modules.TelephonyManagement.TelephonyManagement;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * <p>The list of modules and submodules ("elements") of the assistant plus information about their status.</p>
 * <p>Various elements just need to be instantiated to start working. The references to those instances are stored here
 * too, statically, and there's no problem with that because all those the elements are instantiated inside the same
 * process: the Main Service process.</p>
 * <p>Check the element index before sending it into any of the functions of this class. The index is not checked inside
 * them! They will throw exceptions when they try to use the invalid indexes!</p>
 */
public final class ModulesList {


	// todo The 'disable_mod' parameter is not being used at all yet
	//  Can all modules be disabled...? (PLS, Speech...?) can_be_disabled?

	// There are only 2 TYPE1s of modules: INSTANCE, which means it is a simple instantiation of its class on some
	// thread, or SERVICE_SEP, which means it's a service ---in a separate process---, which also means there are no
	// services running in the Main Service process other than the Main Service. That's to simplify stuff. Why have a
	// service if one can have a simple instance with which we can communicate directly and easily through IModule?
	//
	// Also, positive values are for the Modules Manager to check and restart the module if they're not working properly
	// or at all always, and negative values are for Manager to only check things on that module and do nothing about
	// it, like the Protected Lock Screen - these negative values MUST be symmetrical of their counterparts (if it's a
	// TYPE1_SERVICE_SEP but not to be restarted (though, still behave like it's a service module), then
	// -TYPE1_SERVICE_SEP, for example).
	// The modules with negative values can still be acted on by modules other than the Manager though, for example
	// submodules with negative values are fully managed by its main module, which will restart them if needed - but
	// the Modules Manager will only *check* if they're working and are supported or not and that stuff (not do anything
	// about malfunction or support or whatever else).
	//
	// NO 0 VALUES HERE!!!! (Reason just above - nothing is symmetrical of 0)
	//
	/** It's a module that runs on a simple instance of its class on the Main Service process. */
	public static final int TYPE1_INSTANCE = 1;
	/** It's a service running on an separate process. */
	public static final int TYPE1_SERVICE_SEP = 2;
	/** If it's of {@link #TYPE1_SERVICE_SEP} but it's to be only checked (on the Manager) if it's working, like with
	 * the Protected Lock Screen - it's not supposed to be always running... xD - it must be able of restarting itself
	 * then. */
	public static final int TYPE1_SERVICE_SEP_CHK_ONLY = -TYPE1_SERVICE_SEP;
	/** Same as {@link #TYPE1_SERVICE_SEP_CHK_ONLY} but for instance type - only check (on the Manager) if it's working
	 * or not. */
	public static final int TYPE1_INSTANCE_CHK_ONLY = -TYPE1_INSTANCE;
	public static final int TYPE1_LIBRARY = -3;

	// To disable an element, just comment its line here and be sure you disable its usages everywhere else or pray the
	// app won't crash because of negative index from getModuleIndex() in case it's used for the disabled element.
	/** List of all modules of the app, and also the wanted submodules to be shown on the Modules Status - check which
	 * is what with {@link #ELEMENT_IS_MODULE}. They must also be in the order of module startup (the Modules Manager must
	 * be the first one).*/
	private static final ElementsObj[] ELEMENTS_LIST = {
			new ElementsObj(ModulesManager.class, "Modules Manager", TYPE1_INSTANCE, true),
			//new ElementsObj(PreferencesManager.class, "Preferences Manager", TYPE1_INSTANCE, TYPE2_MODULE),
			//new ElementObj(SomeValuesUpdater.class, "Some Values Updater", TYPE1_INSTANCE, TYPE2_MODULE),
			new ElementsObj(Speech2.class, "Speech", TYPE1_INSTANCE, true),
			new ElementsObj(PowerProcessor.class, "Power Processor", TYPE1_INSTANCE, true),
			new ElementsObj(TelephonyManagement.class, "Telephony Manager", TYPE1_INSTANCE, true),
			new ElementsObj(PhoneCallsProcessor.class, "Phone Calls Processor", TYPE1_INSTANCE, false),
			new ElementsObj(SmsMsgsProcessor.class, "SMS Messages Processor", TYPE1_INSTANCE, false),
			new ElementsObj(CmdsExecutor.class, "Commands Executor", TYPE1_INSTANCE, true),
			new ElementsObj(DeviceLocator.class, "Device Locator", TYPE1_INSTANCE, true),
			new ElementsObj(AudioRecorder.class, "Audio Recorder", TYPE1_INSTANCE, true),
			new ElementsObj(CameraManagement.class, "Camera Manager", TYPE1_INSTANCE, true),

			// todo Make a new class for libraries? And get them to return on a standard function the supported
			//  architectures. Then check if the file is present or at least if the library has been loaded by catching
			//  a Throwable.
			//new ElementsObj(ACD.ACD.class, "Advanced Commands Detection", TYPE1_LIBRARY, TYPE2_MODULE),

			new ElementsObj(SpeechRecognitionCtrl.class, "Speech Recognition Control", TYPE1_INSTANCE, true),
			new ElementsObj(PocketSphinxRecognition.class, "Hotword recognizer", TYPE1_INSTANCE, false),
			new ElementsObj(GoogleRecognition.class, "Commands recognizer", TYPE1_SERVICE_SEP_CHK_ONLY, false),
			new ElementsObj(ProtectedLockScrSrv.class, "Protected Lock Screen", TYPE1_SERVICE_SEP_CHK_ONLY, true),
	};
	public static final int ELEMENTS_LIST_LENGTH = ELEMENTS_LIST.length;
	/**
	 * <p>Class for the (sub)modules of the list.</p>
	 */
	private static final class ElementsObj {
		/** The class of the element. */
		@NonNull final Class<?> cls;
		/** The type 1 of the element ({@code TYPE1_}-started constants). */
		final int type1;
		/** The name of the element to present to users. */
		@NonNull final String name;
		/** True if the element is a module, false if it's a submodule. */
		final boolean is_module;
		/** True to say that the element is supported on the device, including hardware support, permissions, etc; false
		 * otherwise. */
		boolean supported = false;
		/** The instance reference of the object - only if the element is of type |{@link #TYPE1_INSTANCE}|. */
		@Nullable Object instance = null;
		/** True if the user chose to disable the element, false otherwise. */
		boolean disable = false;

		/**
		 * .
		 *
		 * @param cls {@link #cls}
		 * @param name {@link #name}
		 * @param type1 {@link #type1}
		 * @param is_module {@link #is_module}
		 */
		ElementsObj(@NonNull final Class<?> cls, @NonNull final String name, final int type1,
					final boolean is_module) {
			this.cls = cls;
			this.name = name;
			this.type1 = type1;
			this.is_module = is_module;

			supported = isElementSupported(cls);
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
	public static final int ELEMENT_AUTO_RESTART = 4;
	public static final int ELEMENT_INSTANCE = 5;
	public static final int ELEMENT_SUPPORTED = 6;
	public static final int ELEMENT_DISABLE = 7;
	/**
	 * <p>Get the value on the {@link #ELEMENTS_LIST} associated with the given key, for a specific element.</p>
	 * <br>
	 * <p><strong>WARNING:</strong> CHECK FIRST if the value you're getting matches the indexed element!!! If you get
	 * a MODULE_ value from an element which is a submodule, bad things will happen - the function is not made to handle
	 * that.</p>
	 * <p>WARNING 2: This method does not have @Nullable on it, but it CAN return null. Though, having @Nullable would
	 * only get in the way, because it can return null only if the requested value can be null. If an int is requested,
	 * it won't be null. If it's the module instance, it can be null. So it's not necessary to check every time for
	 * nullability, as it's supposed.</p>
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link #ELEMENT_CLASS} --> for {@code key}: {@link ElementsObj#cls}</p>
	 * <p>- {@link #ELEMENT_NAME} --> for {@code key}: {@link ElementsObj#name}</p>
	 * <p>- {@link #ELEMENT_TYPE1} --> for {@code key}: {@link ElementsObj#type1}</p>
	 * <p>- {@link #ELEMENT_IS_MODULE} --> for {@code key}: {@link ElementsObj#is_module}</p>
	 * <p>- {@link #ELEMENT_INSTANCE} --> for {@code key}: {@link ElementsObj#instance}</p>
	 * <p>- {@link #ELEMENT_SUPPORTED} --> for {@code key}: {@link ElementsObj#supported}</p>
	 * <p>- {@link #ELEMENT_DISABLE} --> for {@code key}: {@link ElementsObj#disable}</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @param element_index the index of the element
	 * @param key the key of the value to get (one of the constants)
	 *
	 * @return the value associated with the given key, which may or may not be null, depending on type of the wanted
	 * value as listed on {@link #ELEMENTS_LIST}'s documentation
	 */
	public static Object getElementValue(final int element_index, final int key) {
		switch (key) {
			case ELEMENT_CLASS: {
				return ELEMENTS_LIST[element_index].cls;
			}
			case ELEMENT_NAME: {
				return ELEMENTS_LIST[element_index].name;
			}
			case ELEMENT_TYPE1: {
				return ELEMENTS_LIST[element_index].type1;
			}
			case ELEMENT_IS_MODULE: {
				return ELEMENTS_LIST[element_index].is_module;
			}
			case ELEMENT_INSTANCE: {
				return ELEMENTS_LIST[element_index].instance;
			}
			case ELEMENT_SUPPORTED: {
				return ELEMENTS_LIST[element_index].supported;
			}
			case ELEMENT_DISABLE: {
				return ELEMENTS_LIST[element_index].disable;
			}
		}

		// Won't happen - always implement the constants on the switch.
		// Don't put null, or Android Studio will think it's a @Nullable method and will warn about 'boolean' null
		// values...
		return "";
	}

	/**
	 * <p>Set the value on the {@link #ELEMENTS_LIST} associated with the given key, for a specific element.</p>
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link #ELEMENT_SUPPORTED} --> for {@code key}: {@link ElementsObj#supported}</p>
	 * <p>- {@link #ELEMENT_DISABLE} --> for {@code key}: {@link ElementsObj#disable}</p>p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @param element_index the index of the element
	 * @param key the key of the value to get (one of the constants)
	 * @param value the value associated with the given key
	 */
	public static void setElementValue(final int element_index, final int key, @NonNull final Object value) {
		switch (key) {
			case ELEMENT_SUPPORTED: {
				ELEMENTS_LIST[element_index].supported = (boolean) value;

				break;
			}
			case ELEMENT_DISABLE: {
				ELEMENTS_LIST[element_index].disable = (boolean) value;

				break;
			}
		}
	}

	/**
	 * <p>Checks if the given list element is running or not.</p>
	 * <p>Use {@link #isElementFullyWorking(int)} to check if it's fully working or is malfunctioning.</p>
	 *
	 * @param element_index the index of the element
	 *
	 * @return true if it's running, false otherwise
	 */
	public static boolean isElementRunning(final int element_index) {
		switch (Math.abs((int) getElementValue(element_index, ELEMENT_TYPE1))) {
			case (TYPE1_SERVICE_SEP): {
				return UtilsServices.isServiceRunning((Class<?>) getElementValue(element_index, ELEMENT_CLASS));
			}
			case (TYPE1_INSTANCE): {
				return null != getElementValue(element_index, ELEMENT_INSTANCE);
			}
			default: {
				return false;
			}
		}
	}

	/**
	 * <p>Checks if the given element is fully working.</p>
	 *
	 * @param element_index the index of the element
	 *
	 * @return true if it's fully working, false otherwise
	 */
	public static boolean isElementFullyWorking(final int element_index) {
		switch (Math.abs((int) getElementValue(element_index, ELEMENT_TYPE1))) {
			case (TYPE1_SERVICE_SEP): {
				// Assume it's fully working if it's of TYPE1_SERVICE_SEP, which doesn't implement IModule (no way of
				// knowing yet).
				return isElementRunning(element_index);
			}
			case (TYPE1_INSTANCE): {
				return isElementRunning(element_index) &&
						((IModuleInst) getElementValue(element_index, ELEMENT_INSTANCE)).isFullyWorking();
			}
			default: {
				return false;
			}
		}
	}

	/**
	 * <p>The index of the element in the {@link #ELEMENTS_LIST}.</p>
	 *
	 * @param element_class the class of the element
	 *
	 * @return the index of the element
	 */
	public static int getElementIndex(@NonNull final Class<?> element_class) {
		for (int element_index = 0; element_index < ELEMENTS_LIST_LENGTH; ++element_index) {
			if (getElementValue(element_index, ELEMENT_CLASS) == element_class) {
				return element_index;
			}
		}

		return -1; // Won't ever get here - just supply a valid element by not using a string and calling .class on the
		// class directly.
	}

	/**
	 * <p>Starts the specified element in case it has not been already started.</p>
	 * <p>It is first checked if the provided element is supported or not.</p>
	 * <p>In case it's a Service, it will be started in background (not foreground), as the Main Service is already in
	 * foreground - one is enough according to the Android security policies.</p>
	 * <p>In case it's just to start the element and keep a reference to it, its reference will be stored on the
	 * {@link #ELEMENTS_LIST}.</p>
	 *
	 * @param element_index the index of the element to start
	 */
	public static void startElement(final int element_index) {
		final Class<?> element_class = (Class<?>) getElementValue(element_index, ELEMENT_CLASS);
		if (!isElementSupported(element_class)) {
			return;
		}

		switch (Math.abs((int) getElementValue(element_index, ELEMENT_TYPE1))) {
			case (ModulesList.TYPE1_SERVICE_SEP): {
				UtilsServices.startService(element_class, null, false, true);

				break;
			}
			case (ModulesList.TYPE1_INSTANCE): {
				if (!ModulesList.isElementRunning(element_index)) {
					try {
						ELEMENTS_LIST[element_index].instance = element_class.getConstructor().newInstance();
					} catch (final NoSuchMethodException e) {
						e.printStackTrace();
					} catch (final IllegalAccessException e) {
						e.printStackTrace();
					} catch (final InstantiationException e) {
						e.printStackTrace();
					} catch (final InvocationTargetException e) {
						e.printStackTrace();
					}
					// Keep the printStackTraces here. Useful to know if something is wrong here with modifications to
					// the way how the class works.
				}

				break;
			}
			default: {
				// Nothing
			}
		}
	}

	/**
	 * <p>Stops the specified element in case it's running.</p>
	 * <p>In case it's of type |{@link #TYPE1_SERVICE_SEP}|, its PID will be terminated. If it's of type
	 * |{@link #TYPE1_INSTANCE}|, the {@link IModuleInst#destroy()} will be called and its reference will be set to
	 * null.</p>
	 *
	 * @param element_index the index of the element to stop
	 */
	public static void stopElement(final int element_index) {
		switch (Math.abs((int) getElementValue(element_index, ELEMENT_TYPE1))) {
			case (ModulesList.TYPE1_SERVICE_SEP): {
				if (ModulesList.isElementRunning(element_index)) {
					final Class<?> element_class = (Class<?>) getElementValue(element_index, ELEMENT_CLASS);
					UtilsProcesses.terminatePID(UtilsProcesses.getRunningServicePID(element_class));
				}

				break;
			}
			case (ModulesList.TYPE1_INSTANCE): {
				if (ModulesList.isElementRunning(element_index)) {
					((IModuleInst) getElementValue(element_index, ELEMENT_INSTANCE)).destroy();
					ELEMENTS_LIST[element_index].instance = null;
				}

				break;
			}
			default: {
				// Nothing
			}
		}
	}

	/**
	 * <p>Restarts the specified element, whether it was running or not.</p>
	 * <p>Only 2 functions are called here: first {@link #stopElement(int)} and then {@link #startElement(int)}.</p>
	 *
	 * @param element_index the index of the element to restart
	 */
	public static void restartElement(final int element_index) {
		stopElement(element_index);
		startElement(element_index);
	}

	/**
	 * <p>Checks if the device running the app supports the element.</p>
	 *
	 * @param element_class the class of the element
	 * @return true if the element is supported by the device, false otherwise
	 */
	public static boolean isElementSupported(@NonNull final Class<?> element_class) {
		// Below I mention only modules, but it's exactly the same for submodules.
		// Had to use reflection. It's to behave like IModule, but with a static method that changes for every
		// module and that can be called through the class, so that it doesn't matter if the module is a separate
		// process services. Can't use an interface - use reflection and make sure all modules implement the method on
		// them.
		final Method method = UtilsReflection.getMethod(element_class, "isSupported");
		// In case this throws an error while I'm messing with the modules, so that I know in which module is the error.
		System.out.println(element_class);
		// It's never null when it gets here, unless I was dumb and forgot to put the method in some class.
		assert null != method;
		// No need to check for errors invoking the method - there will be none. It's always declared and
		// implemented the same for all modules, so if it works with one, it works with the rest.
		return (boolean) UtilsReflection.invokeMethod(method, null).ret_var;
	}
}
