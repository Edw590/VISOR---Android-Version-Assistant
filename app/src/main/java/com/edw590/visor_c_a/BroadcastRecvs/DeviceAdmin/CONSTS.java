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

package com.edw590.visor_c_a.BroadcastRecvs.DeviceAdmin;

import UtilsSWA.UtilsSWA;

/**
 * <p>Constants related to the Device Administrator mode.</p>
 */
final class CONSTS {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private CONSTS() {
	}

	static final String SPEAK_ENABLED = "Administrator mode successfully enabled to VISOR.";

	// Shorter version of RET_STR_DISABLE_REQUESTED since devices may have an option to disable and force stop the app
	// all in one button, and then have a confirmation box to uninstall the app. Clicking on the option to Uninstall
	// will make the phone automatically disable the device administration mode and uninstall the app all in once.
	// Means make the speech as small as possible but still decently warning the user.
	static final String SPEAK_DISABLED = "WARNING, ADMIN MODE REVOKED FROM VISOR!!!";
	// With "WARNING, ADMIN MODE REVOKED FROM VISOR!!!", the last word said on BV9500 is "revoked". Not that bad.
	// todo In case the app gets published on some store, put a warning saying what the app will say if the Admin Mode
	//  gets revoked and explain why such an abbreviated speech.

	static final String RET_STR_DISABLE_REQUESTED =
			"SECURITY WARNING!!!" +
			"\n" +
			"\n" +
			"THE ADMINISTRATOR MODE WILL BE REVOKED FROM VISOR!!!"
			;
	static final String SPEAK_DISABLE_REQUESTED = "SECURITY WARNING!!! THE ADMINISTRATOR MODE WILL BE REVOKED FROM " +
			"VISOR!!!";
}
