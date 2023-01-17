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

package com.dadi590.assist_c_a.GlobalInterfaces;

interface IModule {

	/**
	 * <p><u><strong>ATTENTION: DO NOT CALL THIS METHOD!!!!!!</strong></u></p>
	 * <p>This method is only here so that I don't forget to implement the <em>static</em> one to actually be called:
	 * the one named this method's name minus the "wrong" on it. I've even set the return to a wrong type on purpose.</p>
	 * <br>
	 * <p>Checks if the module is supported by whatever is required for it to work, be it another app available on the
	 * device, the device hardware, or permissions.</p>
	 *
	 * @return true if currently supported on the device, false otherwise
	 */
	int wrongIsSupported();
}
