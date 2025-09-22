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

package com.edw590.visor_c_a.Modules.CmdsExecutor.CmdsList;

import androidx.annotation.NonNull;

import com.edw590.visor_c_a.Modules.TelephonyManagement.TelephonyManagement;

import java.util.Locale;

import ACD.ACD;

/**
 * <p>Utilities related to the commands list to send to the ACD module.</p>
 */
public final class UtilsCmdsList {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsCmdsList() {
	}

	/**
	 * <p>Update the contacts names list on the ACD module.</p>
	 */
	public static void updateMakeCallCmdContacts() {
		final String[][] contacts_list = TelephonyManagement.getContactsList();
		if (contacts_list.length == 0) {
			return;
		}

		final StringBuilder contacts_names_list = new StringBuilder(500*20); // To start, 500 contacts, 20 chars per each
		for (final String[] contact : contacts_list) {
			contacts_names_list.append(contact[0].toLowerCase(Locale.getDefault())).append("|");
		}

		CmdsList.AutoCmds.CMD_CALL_CONTACT[4] = contacts_names_list.substring(0, contacts_names_list.length()-1);

		ACD.addUpdateCmd(UtilsCmdsList.prepareCommandString(CmdsList.AutoCmds.CMD_CALL_CONTACT));
	}

	/**
	 * <p>Encodes a command information array into a string ready to be sent to {@link ACD#addUpdateCmd(String)}.</p>
	 *
	 * @param cmd_info the array with the command information
	 *
	 * @return the string
	 */
	@NonNull
	public static String prepareCommandString(@NonNull final String[] cmd_info) {
		return String.join("||", cmd_info);
	}

	/**
	 * <p>Encodes {@link CmdsList#CMDS_LIST} into a string ready to be sent to {@link ACD#reloadCmdsArray(String)}.</p>
	 *
	 * @return the string
	 */
	@NonNull
	public static String prepareCommandsString() {
		final String[] commands_almost_str = new String[CmdsList.CMDS_LIST.length];
		for (int i = 0; i < CmdsList.CMDS_LIST_len; ++i) {
			commands_almost_str[i] = prepareCommandString(CmdsList.CMDS_LIST[i]);
		}

		return String.join("\000", commands_almost_str);
	}
}
