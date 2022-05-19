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

package com.dadi590.assist_c_a.Modules.ModulesManager;

import com.dadi590.assist_c_a.GlobalInterfaces.IModule;
import com.dadi590.assist_c_a.Modules.Speech.Speech2;
import com.dadi590.assist_c_a.Modules.Speech.UtilsSpeech2BC;
import com.dadi590.assist_c_a.ModulesList;

/**
 * <p>The module which ensures all the other modules are working properly.</p>
 */
public class ModulesManager implements IModule {

	///////////////////////////////////////////////////////////////
	// IModule stuff
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
	// IModule stuff
	///////////////////////////////////////////////////////////////

	/**
	 * <p>Main class constructor.</p>
	 */
	public ModulesManager() {
		for (int i = 0; i < ModulesList.modules_list_length; ++i) {
			ModulesList.setModuleValue(i, ModulesList.MODULE_SUPPORTED, ModulesList.deviceSupportsModule(i));
		}

		infinity_thread.start();
	}

	final Thread infinity_thread = new Thread(new Runnable() {
		@Override
		public void run() {
			boolean module_startup = true;
			while (true) {
				for (int i = 0; i < ModulesList.modules_list_length; ++i) {
					final int module_type1 = (int) ModulesList.getModuleValue(i, ModulesList.MODULE_TYPE1);
					if (ModulesList.TYPE1_SERVICE != module_type1 &&
							ModulesList.TYPE1_INSTANCE != module_type1) {
						// If the module is not one of these 2 types, don't do anything.
						continue;
					}

					if ((boolean) ModulesList.getModuleValue(i, ModulesList.MODULE_SUPPORTED)) {
						if (module_startup) {
							ModulesList.startModule(i);
						} else {
							boolean module_restarted = false;
							if (ModulesList.TYPE1_SERVICE == module_type1) {
								if (!ModulesList.isModuleRunning(i)) {
									ModulesList.restartModule(i);
									module_restarted = true;
								}
							} else {
								if (!ModulesList.isModuleFullyWorking(i)) {
									ModulesList.restartModule(i);
									module_restarted = true;
								}
							}

							if (module_restarted) {
								// Start everything the first time. If it has to restart a module, warn about it.
								final String speak = "Attention - Module restarted: " +
										ModulesList.getModuleValue(i, ModulesList.MODULE_NAME);
								UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_HIGH, null);
							}
						}
					}
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
