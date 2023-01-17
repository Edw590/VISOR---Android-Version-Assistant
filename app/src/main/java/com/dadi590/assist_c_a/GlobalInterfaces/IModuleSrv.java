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

import android.app.Service;

import com.dadi590.assist_c_a.ModulesList;

/**
 * <p>All {@link ModulesList#TYPE1_SERVICE_SEP} modules must implement this interface.</p>
 * <p>Note: no module running on a process other than the Main Service process must implement this interface, so that
 * it's possible to call these methods directly on their instances. For example {@link Service}s running on
 * separate processes - in these cases no idea yet on how to implement the idea of the methods of this interface with
 * this ease --> todo.</p>
 */
public interface IModuleSrv extends IModule {
}
