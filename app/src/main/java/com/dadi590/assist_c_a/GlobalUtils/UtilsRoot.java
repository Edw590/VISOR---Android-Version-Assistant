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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * <p>Checks if the device can execute root commands or not, and in case it can, executes root commands.</p>
 * <br>
 * <p>Class gotten from http://muzikant-android.blogspot.com/2011/02/how-to-get-root-access-and-execute.html (now
 * adapted to this app and with added methods).</p>
 *
 * @author Muzikant
 */
public final class UtilsRoot {

    /**
     * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
     */
    private UtilsRoot() {
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
        int retval;
        final Process suProcess;

        try {
            suProcess = Runtime.getRuntime().exec("su");

            final DataOutputStream dataOutputStream = new DataOutputStream(suProcess.getOutputStream());
            final BufferedReader bufferedReader;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                bufferedReader = new BufferedReader(new InputStreamReader(suProcess.getInputStream(),
                        StandardCharsets.UTF_8));
            } else {
                bufferedReader = new BufferedReader(new InputStreamReader(suProcess.getInputStream()));
            }

            // Getting the id of the current user to check if this is root
            dataOutputStream.writeBytes("id\n");
            dataOutputStream.flush();

            final String currUid = bufferedReader.readLine();
            final boolean exitSu;
            if (currUid == null) {
                retval = ROOT_DENIED;
                exitSu = false;
            } else if (currUid.contains("uid=0")) {
                retval = ROOT_AVAILABLE;
                exitSu = true;
            } else {
                retval = ROOT_DENIED;
                exitSu = true;
            }

            if (exitSu) {
                dataOutputStream.writeBytes("exit\n");
                dataOutputStream.flush();
            }

            suProcess.waitFor();
        } catch (final IOException | InterruptedException ignored) {
            retval = ROOT_UNAVAILABLE;
        }

        return retval;
    }
}
