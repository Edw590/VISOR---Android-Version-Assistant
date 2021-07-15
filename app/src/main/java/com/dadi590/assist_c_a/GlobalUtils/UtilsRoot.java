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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * <p>Executes any given command and returns the outputs.</p>
     * <br>
     * <p><u><strong>SECURITY WARNING:</strong></u></p>
     * <p>Do NOT use this to execute commands saved in some file or whatever. Execute ONLY constants or generated
     * strings on the code <em>still from constants</em> - NEVER generate from something that can be gotten outside
     * constants. NEVER something that can be saved on the device storage.</p>
     * <br>
     * <p><u>ATTENTION:</u></p>
     * <p>This function will return the output and error streams of the FIRST command on the list ONLY. In case that
     * command creates a new session, like the "su" command (or whatever it does to move to the root user and that
     * requires the "exit" command to go back to the original session(?)), in that case, this function will return
     * the streams of all the commands introduced inside that session.</p>
     * <br>
     * <p>Considerations to have in mind before calling this method:</p>
     * <p>- Please put one command per index in the commands list. Don't put multiple commands in the same line, so the
     * function can determine if there is need to check root availability or not.</p>
     * <p>- Do NOT put a new line at the end of each command since the function will automatically do that. In case for
     * some reason there's a new line character already as the last character, it won't put another one. The function
     * may check things counting that each command does NOT come with an ending new line.</p>
     * <p>- An empty command will not be recognized as a new line - it will be ignored. To enter a new line, simply
     * write yourself "\n" on the command string. As said in the above point, another new line won't be added (this is
     * the only exception to said point).</p>
     * <p>- The function will input "exit\n" as the last command in case the command to execute su was issued (resulting
     * in an infinite wait for it).</p>
     * <p>- The return values are byte arrays. To get the printable form of them, use
     * {@link UtilsGeneral#convertBytes2Printable(byte[])}.</p>
     *
     * @param commands_list list of commands to execute, each in a new index
     *
     * @return an instance of {@link CmdOuputObj}, or null in case the commands request the app to have root access and
     * it does not
     */
    @Nullable
    public static CmdOuputObj executeShellCmd(@NonNull final List<String> commands_list) {
        final List<byte[]> ret_streams = new ArrayList<>(2);
        @Nullable Integer exit_code;
        boolean su_required = false;

        for (final String command : commands_list) {
            if (!command.isEmpty()) { // Empty or null
                if ("su".equals(command) || command.startsWith("su ")) { // "su", "su ", "su    "....
                    // If one of the lines is the root request line...
                    if (UtilsRoot.rootCommandsAvailability() == UtilsRoot.ROOT_AVAILABLE) {
                        su_required = true;
                    } else {
                        // ... and the app doesn't have root access permission, return error
                        return null;
                    }
                }
            }
        }

        try {
            final Process process = Runtime.getRuntime().exec(commands_list.get(0)); // Just need that it starts the
            // process here
            final DataOutputStream dataOutputStream = new DataOutputStream(process.getOutputStream());
            final InputStream inputStream = process.getInputStream();
            final InputStream errorStream = process.getErrorStream();

            final int commands_list_size = commands_list.size();
            for (int i = 1; i < commands_list_size; i++) { // From index 0 because the 1st command was already executed
                final String command = commands_list.get(i);
                if (!command.isEmpty()) { // Empty or null
                    dataOutputStream.writeBytes(command.endsWith("\n") ? command : command + "\n");
                    dataOutputStream.flush();
                }
            }

            if (su_required) {
                // Don't remove this from here or it's an infinite wait if the command doesn't come in the list (I may
                // forget as I already did just now xD).
                dataOutputStream.writeBytes("exit\n");
                dataOutputStream.flush();
            }

            final InputStream[] streams = {inputStream, errorStream};
            int number_bytes_read;
            final ArrayList<Byte> storage_array = new ArrayList<>(64);
            final int buffer_length = 1; // Don't put higher. Try and see the Inspection error ("Large array
            // allocation with no OutOfMemoryError check") - also if you change this, look below. I have buffer[0]
            // because right now it's only one element per buffer (so no null bytes are appended and invalidate a file,
            // for example)
            final int streams_length = streams.length;
            for (final InputStream stream : streams) {
                final byte[] buffer = new byte[buffer_length];

                while (true) {
                    number_bytes_read = stream.read(buffer);

                    storage_array.add(buffer[0]);

                    if (number_bytes_read < buffer_length) {
                        // Everything was read (less than the buffer was filled)
                        break;
                    }
                }
                // Way of converting to bytes, since ArrayList won't let me convert for a primitive type
                final int storage_array_size = storage_array.size();
                final byte[] ret_array = new byte[storage_array_size];
                for (int j = 0; j < storage_array_size; j++) {
                    ret_array[j] = storage_array.get(j);
                }
                ret_streams.add(ret_array);
                storage_array.clear();
            }

            exit_code = process.waitFor();
        } catch (final IOException | SecurityException | InterruptedException ignored) {
            exit_code = null;
        }

        return new CmdOuputObj(exit_code, ret_streams.get(0), ret_streams.get(1), false);
    }

    /**
     * <p>Class to use for the returning value of {@link #executeShellCmd(List)}.</p>
     */
    public static class CmdOuputObj {
        public final Integer error_code;
        public final byte[] output_stream;
        public final byte[] error_stream;
        public final boolean error_no_root;

        /**
         * <p>Main class constructor.</p>
         *
         * @param error_code the exit code returned by the terminal, or null in case an exception was thrown while
         *                   processing the commands inside the app and the execution was aborted at some point
         * @param output_stream the output stream of the terminal
         * @param error_stream the error stream of the terminal
         */
        public CmdOuputObj(@Nullable final Integer error_code, @NonNull final byte[] output_stream,
                           @NonNull final byte[] error_stream, final boolean error_no_root) {
            this.error_code = error_code;
            this.output_stream = output_stream.clone();
            this.error_stream = error_stream.clone();
            this.error_no_root = error_no_root;
        }
    }
}
