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

package com.edw590.visor_c_a.Modules.TelephonyManagement;

import android.Manifest;

import androidx.annotation.NonNull;

import com.edw590.visor_c_a.GlobalInterfaces.IModuleInst;
import com.edw590.visor_c_a.GlobalUtils.UtilsCheckHardwareFeatures;
import com.edw590.visor_c_a.GlobalUtils.UtilsGeneral;
import com.edw590.visor_c_a.GlobalUtils.UtilsPermsAuths;
import com.edw590.visor_c_a.Modules.CmdsExecutor.CmdsList.UtilsCmdsList;
import com.edw590.visor_c_a.Modules.ModulesManager.ModulesManager;
import com.edw590.visor_c_a.Registry.SettingsRegistry;
import com.edw590.visor_c_a.Registry.UtilsRegistry;
import com.edw590.visor_c_a.Modules.TelephonyManagement.PhoneCallsProcessor.PhoneCallsProcessor;
import com.edw590.visor_c_a.Modules.TelephonyManagement.SmsMsgsProcessor.SmsMsgsProcessor;
import com.edw590.visor_c_a.ModulesList;

/**
 * <p>The module that manages all telephony-related things, including the Phone Calls Processor and the SMS Messages
 * Processor submodules.</p>
 */
public final class TelephonyManagement implements IModuleInst {

	@NonNull private static String[][] contacts_list = {};
	private static final Object lock = new Object();

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
		ModulesList.stopElement(ModulesList.getElementIndex(PhoneCallsProcessor.class));
		ModulesList.stopElement(ModulesList.getElementIndex(SmsMsgsProcessor.class));

		is_module_destroyed = true;
	}
	@Override
	public int wrongIsSupported() {return 0;}
	/**.
	 * @return read all here {@link IModuleInst#wrongIsSupported()} */

	public static boolean isSupported() {
		return UtilsCheckHardwareFeatures.isTelephonySupported(true);
	}
	// IModuleInst stuff
	///////////////////////////////////////////////////////////////

	/**
	 * <p>Main class constructor.</p>
	 */
	public TelephonyManagement() {
		infinity_thread.start();
	}

	final Thread infinity_thread = new Thread(new Runnable() {
		@Override
		public void run() {
			while (true) {
				// Update the contacts list
				if (UtilsPermsAuths.checkSelfPermission(Manifest.permission.READ_CONTACTS)) {
					// Nested synchronization as said here: https://stackoverflow.com/a/21462631/8228163.
					synchronized (lock) {
						synchronized (contacts_list) {
							// Every CHECK_INTERNAL seconds, update the contacts list for commands to be available for new
							// contacts or to remove from it removed contacts, or to update updated contacts (like number or
							// name or whatever). Also if the READ_CONTACTS permissions was just granted, add the contacts from
							// scratch.
							final boolean only_sim = (boolean) UtilsRegistry.
									getData(SettingsRegistry.Keys.K_CONTACTS_SIM_ONLY, true);
							contacts_list = UtilsTelephony.getAllContacts(only_sim ?
									UtilsTelephony.CONTACTS_SIM : UtilsTelephony.ALL_CONTACTS);
							UtilsCmdsList.updateMakeCallCmdContacts();

							System.out.println("~~~~~~~~~~~~~~~~~~");
							System.out.println(contacts_list.length);
						}
					}
				}

				try {
					Thread.sleep(ModulesManager.CHECK_INTERVAL);
				} catch (final InterruptedException ignored) {
					return;
				}
			}
		}
	});

	/**
	 * <p>Get a clone of the {@link #contacts_list}.</p>
	 *
	 * @return the close
	 */
	@NonNull
	public static String[][] getContactsList() {
		synchronized (lock) {
			synchronized (contacts_list) {
				// Do NOT remove the clone() from here - not sure what happens to synchronization!
				return contacts_list.clone();
			}
		}
	}
}
