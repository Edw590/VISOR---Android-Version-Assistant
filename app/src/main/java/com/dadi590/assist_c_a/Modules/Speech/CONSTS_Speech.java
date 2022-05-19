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

package com.dadi590.assist_c_a.Modules.Speech;

/**
 * <p>Constants related to the Speech APIs.</p>
 */
public final class CONSTS_Speech {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private CONSTS_Speech() {
	}

	//////////////////////////////////////
	// Added and used in the Speech API v1
	// Emergency utterance ID prefix with characters not permitted in the utterance ID generator used.
	static final String EMERGENCY_ID_PREFIX = "3234_EMERGENCY-";

	static final int LENGTH_UTTERANCE_ID = 2048; // Used in both v1 and v2

	//////////////////////////////////////
	// Added and used in the Speech API v2
	static final int NUMBER_OF_PRIORITIES = 5;

	static final char UTTERANCE_ID_PREFIX_UNIQUE_CHAR = '-';
	static final String UTTERANCE_ID_PREFIX = "PRIORITY_X" + UTTERANCE_ID_PREFIX_UNIQUE_CHAR;

	static final String WAS_SAYING_PREFIX_1 = "As I was saying, "; // Used in both v1 and v2
	static final String WAS_SAYING_PREFIX_2 = "Once again, as I was saying, ";
	static final String WAS_SAYING_PREFIX_3 = "And again, as I was saying, ";
}
