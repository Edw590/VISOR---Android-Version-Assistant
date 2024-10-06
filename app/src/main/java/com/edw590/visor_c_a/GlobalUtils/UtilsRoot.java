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

package com.edw590.visor_c_a.GlobalUtils;

import com.edw590.visor_c_a.Modules.Speech.Speech2;
import com.edw590.visor_c_a.Modules.Speech.UtilsSpeech2BC;

import UtilsSWA.UtilsSWA;

/**
 * <p>Utilities related to root features, like checking if root user rights are available or not, for example.</p>
 *
 * @author Muzikant
 */
public final class UtilsRoot {

    /**
     * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
     */
    private UtilsRoot() {
    }

    /**
     * <p>Checks and warns about root access availability for the app.</p>
     *
     * @param warn_root_available true to warn if root access is available, false to only warn when there's no access
     */
    public static void checkWarnRootAccess(final boolean warn_root_available) {
        if (UtilsSWA.isRootAvailableROOT()) {
            if (warn_root_available) {
                final String speak = "Root access available on the device.";
                UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_USER_ACTION, 0, true, null);
            }
        } else {
            final String speak = "Attention! Root access was denied or is not available on this device! Some " +
                    "features may not be available!";
            UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_HIGH, 0, true, null);
        }
    }
}
