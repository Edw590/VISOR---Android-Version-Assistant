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

package com.dadi590.assist_c_a.GlobalUtils;

import com.dadi590.assist_c_a.Modules.Speech.Speech2;
import com.dadi590.assist_c_a.Modules.Speech.UtilsSpeech2BC;

import java.util.ArrayList;
import java.util.List;

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
        switch (rootCommandsAvailability()) {
            case (ROOT_AVAILABLE): {
                if (warn_root_available) {
                    final String speak = "Root access available on the device.";
                    UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_USER_ACTION, null);
                }

                break;
            }
            case (ROOT_DENIED): {
                final String speak = "WARNING! Root access was denied on this device! Some features may not " +
                        "be available!";
                UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_HIGH, null);

                break;
            }
            case (ROOT_UNAVAILABLE): {
                final String speak = "Attention! The device is not rooted! Some features may not be available!";
                UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_MEDIUM, null);

                break;
            }
        }
    }

    public static final int ROOT_AVAILABLE = 0;
    public static final int ROOT_DENIED = 1;
    public static final int ROOT_UNAVAILABLE = 2;
    /**
     * Checks if the device can run root commands.
     * <br>
     * <p><u>---CONSTANTS---</u></p>
     * <p>- {@link #ROOT_AVAILABLE} --> for the returning value: if root access is available</p>
     * <p>- {@link #ROOT_DENIED} --> for the returning value: if the user denied root access</p>
     * <p>- {@link #ROOT_UNAVAILABLE} --> for the returning value: if the device is not rooted</p>
     * <p><u>---CONSTANTS---</u></p>
     *
     * @return one of the constants
     */
    public static int rootCommandsAvailability() {
        // The original implementation was gotten from
        // http://muzikant-android.blogspot.com/2011/02/how-to-get-root-access-and-execute.html, was then adapted to the
        // app and finally changed to call the executeShellCmd function.
        // So the way it checks root is supposed to be the same (hopefully it's the same and I didn't mess up). Only the
        // shell part is now removed because it is on the mentioned function.

        final List<String> commands = new ArrayList<>(3);
        commands.add("su");
        commands.add("id");
        commands.add("exit");
        final UtilsShell.CmdOutputObj cmdOutputObj = UtilsShell.executeShellCmd(commands, true);
        if (cmdOutputObj.error_code == null) {
            System.out.println("ROOT_UNAVAILABLE");
            return ROOT_UNAVAILABLE;
        } else if (UtilsGeneral.bytesToPrintableChars(cmdOutputObj.output_stream, false).contains("uid=0")) {
            System.out.println("ROOT_AVAILABLE");
            return ROOT_AVAILABLE;
        } else {
            System.out.println("ROOT_DENIED");
            return ROOT_DENIED;
        }
    }
}
