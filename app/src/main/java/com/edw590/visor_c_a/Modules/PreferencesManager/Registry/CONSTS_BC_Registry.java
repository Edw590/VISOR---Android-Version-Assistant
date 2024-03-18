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

package com.edw590.visor_c_a.Modules.PreferencesManager.Registry;

/**
 * <p>Actions and extras of broadcasts sent by the Static Storage classes methods.</p>
 * <br>
 * <p>Check the doc on the action string to know what to do.</p>
 */
public final class CONSTS_BC_Registry {

	/**
	 * <p>Explanation: warns when a value on the array was just updated.</p>
	 * <p>Is broadcast by the class(es): {@link UtilsRegistry}.</p>
	 * <p>To be received only by the class(es): any chosen class.</p>
	 * <p>Extras:</p>
	 * <p>- {@link #EXTRA_VALUE_UPDATED_1} ({@link String}): {@link Value#key}</p>
	 */
	public static final String ACTION_VALUE_UPDATED =  "Registry_ACTION_VALUE_UPDATED";
	public static final String EXTRA_VALUE_UPDATED_1 = "Registry_EXTRA_VALUE_UPDATED_1";

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private CONSTS_BC_Registry() {
	}
}
