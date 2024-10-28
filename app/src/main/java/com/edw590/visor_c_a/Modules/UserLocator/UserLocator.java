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

package com.edw590.visor_c_a.Modules.UserLocator;

import com.edw590.visor_c_a.GlobalInterfaces.IModuleInst;
import com.edw590.visor_c_a.GlobalUtils.UtilsGeneral;

import TEHelper.TEHelper;
import ULHelper.ULHelper;

public class UserLocator implements IModuleInst {

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
	public UserLocator() {
		infinity_thread.start();
	}

	final Thread infinity_thread = new Thread(new Runnable() {
		@Override
		public void run() {
			ULHelper.updateUserLocation();
		}
	});
}
