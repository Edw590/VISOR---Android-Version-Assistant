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

package com.dadi590.assist_c_a.Modules.SomeValuesUpdater;

import com.dadi590.assist_c_a.GlobalInterfaces.IModuleInst;
import com.dadi590.assist_c_a.GlobalUtils.UtilsGeneral;

/**
 * <p>The module that periodically updates some values which no other module updates by an event.</p>
 * <p>Examples: time and weather (can't be updated by an event - there isn't one).</p>
 */
public final class SomeValuesUpdater implements IModuleInst {

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


	// Maybe delete the module? Why updated time, wtf. Or at least disable it. There's no current usage for it.
	// Done. Disabled and nothing being updated here. It's on hold until it's useful again.


	/**
	 * <p>Main class constructor.</p>
	 */
	public SomeValuesUpdater() {
		infinity_thread.start();
	}

	private final Thread infinity_thread = new Thread(new Runnable() {
		@Override
		public void run() {
			while (true) {


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
