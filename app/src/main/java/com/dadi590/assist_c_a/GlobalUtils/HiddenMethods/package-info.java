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

/**
 * <p>Hidden methods made public and static (utilities).</p>
 * <br>
 * <p>Package with classes used to:</p>
 * <p>- Statically publish methods inside classes that supposedly need Context (or some other instantiated attribute),
 * but the methods, internally, don't actually need it, but still the method is not declared as static (security
 * measure, maybe).
 * <p>- a modified implementation of such methods to remove the need for the instantiated attributes instance.</p>
 * <br>
 * <p>Each class name begins with an E for the name to be different from the original class. That E means External, as
 * opposite to I of Internal of Android's internal classes, like {@link android.media.IAudioService} (that's not
 * actually true - the I is from Interface, but it works).</p>
 * <br>
 * <p>No idea why I wanted to know this, but I wrote down to document it here so here it goes: getOpPackageName()
 * returns "com.dadi590.assist_c_a" on Lollipop.</p>
 * <br>
 * <p><strong>NOT IN USE while {@link android.app.AppGlobals#getInitialApplication()}.getApplicationContext() works!!!
 * </strong></p>
 */
package com.dadi590.assist_c_a.GlobalUtils.HiddenMethods;
