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

package com.dadi590.assist_c_a.Modules.ModulesManager;

import com.dadi590.assist_c_a.GlobalInterfaces.IModuleInst;
import com.dadi590.assist_c_a.GlobalUtils.UtilsGeneral;
import com.dadi590.assist_c_a.Modules.Speech.Speech2;
import com.dadi590.assist_c_a.Modules.Speech.UtilsSpeech2BC;
import com.dadi590.assist_c_a.ModulesList;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>The module which ensures all the other modules are working properly.</p>
 */
public final class ModulesManager implements IModuleInst {

	public static final long CHECK_INTERVAL = 10_000L;

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
	public ModulesManager() {
		infinity_thread.start();
	}

	final Thread infinity_thread = new Thread(new Runnable() {
		@Override
		public void run() {
			boolean module_startup = true;

			final List<Class<?>> elements_classes = new ArrayList<>(ModulesList.elements_list_length);

			// Check all modules' support and put on a list to later warn if there were changes of support or not.
			final boolean[] elements_support = new boolean[ModulesList.elements_list_length];
			for (int module_index = 0; module_index < ModulesList.elements_list_length; ++module_index) {
				final Class<?> element_class = (Class<?>) ModulesList.getElementValue(module_index, ModulesList.ELEMENT_CLASS);
				elements_classes.add(element_class);

				final int elem_type2 = (int) ModulesList.getElementValue(module_index, ModulesList.ELEMENT_TYPE2);
				if (ModulesList.TYPE2_MODULE == elem_type2 || ModulesList.TYPE2_SUBMODULE == elem_type2) {
					// Must be a module or a submodule.
					elements_support[module_index] = ModulesList.isElementSupported(element_class);
				}
			}

			while (true) {
				for (int module_index = 0; module_index < ModulesList.elements_list_length; ++module_index) {
					final int elem_type2 = (int) ModulesList.getElementValue(module_index, ModulesList.ELEMENT_TYPE2);
					final boolean is_module = ModulesList.TYPE2_MODULE == elem_type2;

					final boolean element_supported;
					if (is_module || ModulesList.TYPE2_SUBMODULE == elem_type2) {
						element_supported = ModulesList.isElementSupported(elements_classes.get(module_index));
						// Keep updating if the modules are supported or not, in case the user changes the app permissions.
						ModulesList.setElementValue(module_index, ModulesList.ELEMENT_SUPPORTED, element_supported);
					} else {
						// Not module, nor submodule (no idea yet, no other types exist currently).
						continue;
					}

					// From here, only check stuff on modules or submodules, and only act (start/stop) on modules.

					if (element_supported) {
						if (!elements_support[module_index]) {
							// Also warn if a module just got support (again or not).
							final String speak = "The following module is now supported by hardware or application " +
									"permissions changes: " +
									ModulesList.getElementValue(module_index, ModulesList.ELEMENT_NAME);
							UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_MEDIUM, null);
						}
						// startModule() already checks if the module is supported or not, but the manager would still
						// call isModuleFullyWorking() for nothing, so I've put this in the if statement too.
						// Also only keep checking and restarting the module if it's a module to check and restart and
						// not to check only (in which case the TYP2 value would be negative).
						if (is_module && !ModulesList.isElementFullyWorking(module_index) &&
								((int) ModulesList.getElementValue(module_index, ModulesList.ELEMENT_TYPE1) > 0)) {
							ModulesList.restartElement(module_index);
							// Start everything the first time. If it has to restart a module, warn about it.
							if (!module_startup) {
								final String speak = "Attention - Module restarted: " +
										ModulesList.getElementValue(module_index, ModulesList.ELEMENT_NAME);
								UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_HIGH, null);
							}
						}
					} else {
						if (elements_support[module_index]) {
							// If the module was supported and stopped being, warn about it.
							final String speak = "Attention - The following module stopped being supported by " +
									"hardware or application permissions changes: " +
									ModulesList.getElementValue(module_index, ModulesList.ELEMENT_NAME);
							UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_HIGH, null);
						}
						if (is_module) {
							// If the user disabled some permission, or some hardware component was disconnected and
							// Android detected it, stop the module (if it was running, anyway).
							ModulesList.stopElement(module_index);
						}
					}

					elements_support[module_index] = element_supported;
				}

				module_startup = false;

				try {
					Thread.sleep(CHECK_INTERVAL);
				} catch (final InterruptedException ignored) {
					Thread.currentThread().interrupt();

					return;
				}
			}
		}
	});
}
