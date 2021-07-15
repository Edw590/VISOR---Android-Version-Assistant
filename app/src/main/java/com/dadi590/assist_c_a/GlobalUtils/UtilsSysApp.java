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

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * <p>Checks if an app is a system app. Also checks in case it's a system app, if it's an updated app or not.</p>
 */
public final class UtilsSysApp {

    /**
     * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
     */
    private UtilsSysApp() {
    }

    /**
     * Checks if an app is a system app.
     *
     * @return an OR operation on {@link #isSystemAppByFLAG()}, {@link #isSystemAppByPM()},
     * {@link #isSystemPreloaded()} and {@link #isSystemSigned()}
     */
    public static boolean isSystemApp() {
        return UtilsSysApp.isSystemAppByFLAG() || UtilsSysApp.isSystemAppByPM() ||
                UtilsSysApp.isSystemPreloaded() || UtilsSysApp.isSystemSigned();
    }

    /**
     * Checks if a system app has been updated or not.
     *
     * @return {@code true} if the app is a system app and has been updated.
     */
    public static boolean isSystemUpdatedAppByFlag() {
        try {
            final Context context = UtilsGeneral.getContext();
            final ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
            if (ai != null && (ai.flags & (ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0) {
                // Conjunction of FLAG_UPDATED_SYSTEM_APP and isSystemAppByFLAG()
                return isSystemAppByFLAG();
            }
        } catch (final PackageManager.NameNotFoundException ignored) {
        }
        return false;
    }

    /**
     * Check if system app by 'pm' command-line program.
     *
     * @return {@code true} if package is a system app.
     */
    private static boolean isSystemAppByPM() {
        final ProcessBuilder builder = new ProcessBuilder("pm", "list", "packages", "-s");
        final Process process;
        try {
            process = builder.start();
        } catch (final IOException | SecurityException e) {
            return false;
        }

        final InputStream in = process.getInputStream();
        final Scanner scanner = new Scanner(in);
        final Pattern pattern = Pattern.compile("^package:.+");
        final int skip = "package:".length();

        final Collection<String> systemApps = new HashSet<>(100);
        while (true) {
            if (!scanner.hasNext(pattern)) {
                break;
            }
            final String pckg = scanner.next().substring(skip);
            systemApps.add(pckg);
        }

        scanner.close();
        process.destroy();
        // An exception appears on API 15 on the emulator with process.destroy(): libcore.io.ErrnoException, but there's
        // no problem with it, since the program continues the execution to the return statement below.

        return systemApps.contains(UtilsGeneral.getContext().getPackageName());
    }

    /**
     * Check if application is preloaded.
     *
     * @return {@code true} if package is preloaded.
     */
    private static boolean isSystemPreloaded() {
        try {
            final Context context = UtilsGeneral.getContext();
            final ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
            if (ai.sourceDir.startsWith("/system/app/") || ai.sourceDir.startsWith("/system/priv-app/")) {
                return true;
            }
        } catch (final PackageManager.NameNotFoundException ignored) {
        }
        return false;
    }

    /**
     * Check if the app is system signed or not.
     *
     * @return {@code true} if application is signed by system certificate,
     *         otherwise {@code false}
     */
    private static boolean isSystemSigned() {
        try {
            final Context context = UtilsGeneral.getContext();

            // Note: GET_SIGNATURES is a problem below Android 4.4 KitKat only (no problem, I guess - not many will be
            // using below KitKat anyways).

            // Get package info for target application
            final PackageInfo targetPkgInfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_SIGNATURES);
            // Get package info for system package
            final PackageInfo sys = context.getPackageManager().getPackageInfo(
                    "android", PackageManager.GET_SIGNATURES);
            // Match both package info for their signatures
            return (targetPkgInfo != null && targetPkgInfo.signatures != null && sys.signatures[0]
                    .equals(targetPkgInfo.signatures[0]));
        } catch (final PackageManager.NameNotFoundException ignored) {
        }

        return false;
    }

    /**
     * Check if application is installed in the device's system image.
     *
     * @return {@code true} if package is a system app.
     */
    private static boolean isSystemAppByFLAG() {
        try {
            final Context context = UtilsGeneral.getContext();
            final ApplicationInfo ai = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), 0);
            // Check if FLAG_SYSTEM or FLAG_UPDATED_SYSTEM_APP are set.
            if (ai != null) {
                if ((ai.flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0) {
                    return true;
                }
            }
        } catch (final PackageManager.NameNotFoundException ignored) {
        }

        return false;
    }
}
