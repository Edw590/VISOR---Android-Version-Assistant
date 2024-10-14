/*
 * Copyright 2021-2024 Edw590
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

import android.app.ActivityManager;
import android.content.Context;

import androidx.annotation.NonNull;

/**
 * <p>Global processes-related utilities.</p>
 */
public final class UtilsProcesses {

    /**
     * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
     */
    private UtilsProcesses() {
    }

    /**
     * <p>Gets the PID of the service of the given class.</p>
     *
     * @param serviceClass the service class
     *
     * @return the PID of the service of the given class or -1 if not found
     */
    public static int getRunningServicePID(@NonNull final Class<?> serviceClass) {
        final ActivityManager manager = (ActivityManager) UtilsContext.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager == null) {
            return -1;
        }

        for (final ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return service.pid;
            }
        }

        return -1;
    }

    /**
     * <p>Terminates a PID.</p>
     * <p>If -1 is provided, nothing is done.</p>
     *
     * @param pid PID to terminate
     */
    public static void terminatePID(final int pid) {
        if (pid != -1) {
            android.os.Process.killProcess(pid);
        }

        long curr_millis = System.currentTimeMillis();
        long max_millis = curr_millis + 5*1000;
        while (System.currentTimeMillis() < max_millis) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
        }

    }

    /**
     * <p>Gets the PID of the current process.</p>
     *
     * @return the PID of the current process
     */
    public static int getCurrentPID() {
        return android.os.Process.myPid();
    }
}
