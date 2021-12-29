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
import com.dadi590.assist_c_a.ModulesList;

/**
 * <p>Modules Manager module specific utilities.</p>
 */
public final class UtilsModulesManager {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsModulesManager() {
	}

	/**
	 * <p>Checks if a module is running properly or not, and if it's not, restarts it.</p>
	 * <p>Please check if the module is of valid types (public types in {@link ModulesList}) prior to calling this
	 * function.</p>
	 *
	 * @param module_index the index of the module to check
	 *
	 * @return true if the module was not working properly and had to be restarted, false if it was working normally and
	 * nothing was done
	 */
	public static boolean checkRestartModule(final int module_index) {
		boolean restart_module = false;
		final IModule module = ModulesList.getIModule(module_index);
		if (null == module) {
			// Can't check with the IModule functions, so plan B: must be a service or something. Lets just
			// check if it's running. If it's not, restart it.
			if (!ModulesList.isModuleRunningByIndex(module_index)) {
				restart_module = true;
			}
		} else {
			// Else, if it the module implements the IModule interface, check by its functions.
			if (!module.isModuleFullyWorking()) {
				restart_module = true;
			}
		}

		if (restart_module) {
			ModulesList.restartModule(module_index);

			return true;
		}

		return false;
	}
}
