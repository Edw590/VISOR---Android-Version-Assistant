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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
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

    /**
     * <p>Checks and warns about root access availability for the app.</p>
     *
     * @param warn_root_available true to warn if root access is available, false to only warn when there's no access
     */
    public static void checkWarnRootAccess(final boolean warn_root_available) {
        // todo See if you can delete this... It's not supposed for the app to execute any root commands. Only system
        //  hidden/internal methods.

        switch (rootCommandsAvailability()) {
            case (ROOT_AVAILABLE): {
                if (warn_root_available) {
                    final String speak = "Root access available on the device.";
                    UtilsSpeech2BC.speak(speak, null, Speech2.PRIORITY_USER_ACTION, null);
                }

                break;
            }
            case (ROOT_DENIED): {
                final String speak = "WARNING! Root access was denied on this device! Some features may not " +
                        "be available!";
                UtilsSpeech2BC.speak(speak, null, Speech2.PRIORITY_HIGH, null);

                break;
            }
            case (ROOT_UNAVAILABLE): {
                final String speak = "Attention! The device is not rooted! Some features may not be available!";
                UtilsSpeech2BC.speak(speak, null, Speech2.PRIORITY_MEDIUM, null);

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
        final int ret_val;
        final Process suProcess;

        try {
            suProcess = Runtime.getRuntime().exec("su");

            try (final DataOutputStream dataOutputStream = new DataOutputStream(suProcess.getOutputStream())) {
                final String current_UID;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    try (final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                            suProcess.getInputStream(), StandardCharsets.UTF_8))) {
                        current_UID = bufferedReader.readLine();
                    } catch (final IOException ignored) {
                        return ROOT_UNAVAILABLE;
                    }
                } else {
                    try (final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                            suProcess.getInputStream(), Charset.defaultCharset()))) {
                        current_UID = bufferedReader.readLine();
                    } catch (final IOException ignored) {
                        return ROOT_UNAVAILABLE;
                    }
                }

                // Getting the ID of the current user to check if it's root
                dataOutputStream.writeBytes("id\n");
                dataOutputStream.flush();

                final boolean exit_su;
                if (current_UID == null) {
                    ret_val = ROOT_DENIED;
                    exit_su = false;
                } else if (current_UID.contains("uid=0")) {
                    ret_val = ROOT_AVAILABLE;
                    exit_su = true;
                } else {
                    ret_val = ROOT_DENIED;
                    exit_su = true;
                }

                if (exit_su) {
                    dataOutputStream.writeBytes("exit\n");
                    dataOutputStream.flush();
                }
            } catch (final IOException ignored) {
                return ROOT_UNAVAILABLE;
            }

            suProcess.waitFor();
        } catch (final IOException | InterruptedException ignored) {
            return ROOT_UNAVAILABLE; // The only place where this was, but I added this return in the other catch
            // clauses too (what could I put there...).
        }

        return ret_val;
    }
}
