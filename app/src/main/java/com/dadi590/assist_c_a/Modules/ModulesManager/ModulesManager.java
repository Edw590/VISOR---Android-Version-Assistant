/*
 * Copyright 2022 DADi590
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
import com.dadi590.assist_c_a.Modules.Speech.Speech2;
import com.dadi590.assist_c_a.Modules.Speech.UtilsSpeech2BC;
import com.dadi590.assist_c_a.ModulesList;

/**
 * <p>The module which ensures all the other modules are working properly.</p>
 */
public class ModulesManager implements IModuleInst {

	///////////////////////////////////////////////////////////////
	// IModuleInst stuff
	private boolean is_module_destroyed = false;
	@Override
	public final boolean isFullyWorking() {
		if (is_module_destroyed) {
			return false;
		}

		return infinity_thread.isAlive();
	}
	@Override
	public final void destroy() {
		infinity_thread.interrupt();
		is_module_destroyed = true;
	}
	@Override
	public final int wrongIsSupported() {return 0;}
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

			// Check all modules' support and put on a list to later warn if there were changes of support or not.
			final boolean[] modules_support = new boolean[ModulesList.sub_and_modules_list_length];
			for (int module_index = 0; module_index < ModulesList.sub_and_modules_list_length; ++module_index) {
				if ((boolean) ModulesList.getElementValue(module_index, ModulesList.ELEMENT_IS_MODULE)) {
					// Must be a module.
					modules_support[module_index] = ModulesList.isModuleSupported(module_index);
				}
			}

			while (true) {
				for (int module_index = 0; module_index < ModulesList.sub_and_modules_list_length; ++module_index) {
					if (!(boolean) ModulesList.getElementValue(module_index, ModulesList.ELEMENT_IS_MODULE)) {
						// If it's a sub-module, don't do anything (all is taken care of by its main module).
						continue;
					}

					final boolean module_supported = ModulesList.isModuleSupported(module_index);
					// Keep updating if the modules are supported or not, in case the user changes the app permissions.
					ModulesList.setModuleValue(module_index, ModulesList.MODULE_SUPPORTED, module_supported);

					if (module_supported) {
						if (!modules_support[module_index]) {
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
						if (!ModulesList.isModuleFullyWorking(module_index) &&
								((int) ModulesList.getElementValue(module_index, ModulesList.ELEMENT_TYPE1) > 0)) {
							ModulesList.restartModule(module_index);
							// Start everything the first time. If it has to restart a module, warn about it.
							if (!module_startup) {
								final String speak = "Attention - Module restarted: " +
										ModulesList.getElementValue(module_index, ModulesList.ELEMENT_NAME);
								UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_HIGH, null);
							}
						}
					} else {
						if (modules_support[module_index]) {
							// If the module was supported and stopped being, warn about it.
							final String speak = "Attention - The following module stopped being supported by " +
									"hardware or application permissions changes: " +
									ModulesList.getElementValue(module_index, ModulesList.ELEMENT_NAME);
							UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_HIGH, null);
						}
						// If the user disabled some permission, or some hardware component was disconnected and Android
						// detected it, stop the module (if it was running, anyway).
						ModulesList.stopModule(module_index);
					}

					modules_support[module_index] = module_supported;
				}

				module_startup = false;

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
