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

package com.dadi590.assist_c_a.GlobalInterfaces;

/**
 * <p>All modules must implement this interface.</p>
 * <p>Note: no module running on a process other than the main app process must implement this interface, so that it's
 * possible to call these methods directly on their instances. For example {@link android.app.Service}s running on
 * separate processes - in these cases no idea yet on how to implement the idea of the methods of this interface.</p>
 */
public interface IModule {

	/**
	 * <p>Checks if the module is working as it should.</p>
	 * <p>Suppose a module has 2 threads running and one of them dies: the module is no longer working properly - but
	 * it's still alive. Then the module is no longer working as it should.</p>
	 * <p>So how to know the module stopped working properly so it can be restarted? The module-specific implementation
	 * of this method says if the module has died internally or not (meaning any of the things it was supposed to be
	 * doing stopped - "any" also means all, as in the module completely stopped working).</p>
	 *
	 * @return true if the module is still working properly, false if it's malfunctioning/stopped
	 */
	boolean isModuleWorkingProperly();

	/**
	 * <p>Before restarting the module in case something happened, call this function which will stop all module
	 * functions that were not stopped, allowing the Garbage Collector to do its job.</p>
	 * <p>After calling this function, any subsequent calls to {@link #isModuleWorkingProperly()} will return false.</p>
	 */
	void destroyModule();
}
