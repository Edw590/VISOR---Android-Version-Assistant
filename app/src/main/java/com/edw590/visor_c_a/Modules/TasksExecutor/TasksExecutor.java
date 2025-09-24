/*
 * Copyright 2021-2024 Edw590
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

package com.edw590.visor_c_a.Modules.TasksExecutor;

import com.edw590.visor_c_a.GlobalInterfaces.IModuleInst;
import com.edw590.visor_c_a.GlobalUtils.UtilsGeneral;
import com.edw590.visor_c_a.GlobalUtils.UtilsLogging;
import com.edw590.visor_c_a.Modules.CmdsExecutor.UtilsCmdsExecutorBC;
import com.edw590.visor_c_a.Modules.Speech.Speech2;
import com.edw590.visor_c_a.Modules.Speech.UtilsSpeech2BC;

import TEHelper.TEHelper;

public class TasksExecutor implements IModuleInst {

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
		TEHelper.stopChecker();

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
	public TasksExecutor() {
		infinity_thread.start();
	}

	final Thread infinity_thread = new Thread(() -> {
		while (true) {
			ModsFileInfo.Task task = TEHelper.checkDueTasks();
			if (task == null) {
				return;
			}

			UtilsLogging.logLnInfo("Task! --> " + task.getId());

			if (!task.getMessage().isEmpty()) {
				UtilsSpeech2BC.speak(task.getMessage(), Speech2.PRIORITY_MEDIUM, Speech2.MODE1_ALWAYS_NOTIFY,
						UtilsSpeech2BC.SESSION_TYPE_NONE, false, null);
			}

			if (!task.getCommand().isEmpty()) {
				UtilsCmdsExecutorBC.processTask(task.getCommand(), false, false, true);
			}
		}
	});
}
