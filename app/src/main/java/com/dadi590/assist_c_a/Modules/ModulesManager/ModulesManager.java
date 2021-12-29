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

	boolean module_startup = true;

	static final Object[][] modules_list = ModulesList.getModulesList();

	private boolean is_module_alive = true;
	@Override
	public final boolean isModuleWorkingProperly() {
		if (!is_module_alive) {
			return false;
		}

		return infinity_thread.isAlive();
	}
	@Override
	public final void destroyModule() {
		infinity_thread.interrupt();
		is_module_alive = false;
	}

	/**
	 * <p>Main class constructor.</p>
	 */
	public ModulesManager() {
		infinity_thread.start();
	}

	final Thread infinity_thread = new Thread(new Runnable() {
		@Override
		public void run() {
			while (true) {
				for (int i = 1; i < ModulesList.modules_list_length; ++i) {
					if (ModulesList.MODULE_TYPE_SERVICE != (int) modules_list[i][1] &&
							ModulesList.MODULE_TYPE_INSTANCE != (int) modules_list[i][1]) {
						continue;
					}

					if (UtilsModulesManager.checkRestartModule(i) && !module_startup) {
						// Start everything the first time. If it has to restart a module, warn about it.
						final String speak = "Attention - Module restarted: " + modules_list[i][2];
						UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_HIGH, null);
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
