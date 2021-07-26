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

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dadi590.assist_c_a.BroadcastRecvs.DeviceAdmin.DeviceAdminRecv;

/**
 * <p>Global app-related utilities.</p>
 */
public final class UtilsApp {

    /**
     * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
     */
    private UtilsApp() {
    }

    /**
     * <p>Gets the app ready to be shut down. For example might save (NOT stop) some important processing as fast as
     * possible for the app to be shut down.</p>
     * <br>
     * <p>This method should be useful if a shut down or reboot is detected or if the user clicks Force Stop on the
     * Settings app (if it's available).</p>
     * <br>
     * <p>NOTE: this does NOT shut down the app. The only supposed way to stop the app is the system by shutting down
     * the phone, and nothing else.</p>
     * <br>
     * <p>This method does not stop ongoing tasks because any system app can send the shut down broadcast or something,
     * and that could be useful in a malicious way. Not stopping anything and only saving <em>in case</em> the app is
     * shut down prevents that.</p>
     * <br>
     * <p><u><i><b>CURRENTLY THIS METHOD DOES NOTHING</b></i></u></p>
     */
    public static void prepareShutdown() {
        // todo PUT THE APP RESETTING THE NORMAL CHARGING MODE ON SHUT DOWN AND UNINSTALLATION OR IT'S DEATH!!!!!!
        // (in case the stop charging is implemented)
    }

    /**
     * <p>Gets the app ready to be uninstalled. For example might reset important things it changed on the system - an
     * example could be reset the charging in case it has been disabled because the battery is at 80%.</p>
     * <br>
     * <p>This method should be useful if it's requested to the assistant that the app should be uninstalled, or in case
     * the user clicks the Uninstall button on the Settings app (if it's available).</p>
     * <br>
     * <p>NOTE: this does NOT uninstall the app. The only supposed way to uninstall the app is by telling the assistant
     * to do it, and nothing else.</p>
     * <br>
     * <p><u><i><b>CURRENTLY THIS METHOD DOES NOTHING</b></i></u></p>
     */
    public static void prepareUninstall() {
        // todo PUT THE APP RESETTING THE NORMAL CHARGING MODE ON SHUT DOWN AND UNINSTALLATION OR IT'S DEATH!!!!!!
        // (in case the stop charging is implemented)

        // todo See if the method used to detect the uninstallation is 100% secure, as opposite to detecting a shut down
        // If it's not that much secure, reset for a period of time and then put the settings/files back, or something.
        // Try to think of anything better than that, as that seems like a security hole.

        // If it's to be uninstalled, will be shut down too, so get ready for that too, but in the end.
        prepareShutdown();
    }

    /**
     * Deletes the app cache.
     */
    public static void deleteAppCache() {
        UtilsFilesDirs.deletePath(UtilsGeneral.getContext().getCacheDir());
    }

    /**
     * <p>Checks if an app is installed on the device or not.</p>
     *
     * @param packageName The name of the package of the app to be checked
     *
     * @return true if the app is installed, false otherwise
     */
    public static boolean isAnAppInstalled(@NonNull final String packageName) {
        final PackageManager packageManager = UtilsGeneral.getContext().getPackageManager();
        try {
            packageManager.getPackageInfo(packageName, 0);

            return true;
        } catch (final PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /**
     * <p>Checks if an installed app is enabled or not.</p>
     * <br>
     * <p>Note: one of the states a package may have is the DEFAULT one, which is defined on the manifest, and we can't
     * know if that means enabled or disabled. So this function supposes all packages have the default as enabled in
     * the manifest. Hence, the result is an OR operation between STATE_DEFAULT and STATE_ENABLED.</p>
     *
     * @param packageName the name of the package of the app to be checked
     *
     * @return true if the app is enabled, false otherwise; null if the package is not installed
     */
    @Nullable
    public static Boolean isAppEnabled(@NonNull final String packageName) {
        final PackageManager packageManager = UtilsGeneral.getContext().getPackageManager();
        final int app_enabled_setting;
        try {
            app_enabled_setting = packageManager.getApplicationEnabledSetting(packageName);
        } catch (final IllegalArgumentException ignored) {
            return null;
        }

        return app_enabled_setting == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT ||
                app_enabled_setting == PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
    }

    public static final int NORMAL = 0;
    public static final int SYSTEM_WITHOUT_UPDATES = 1;
    public static final int SYSTEM_WITH_UPDATES = 2;
    /**
     * <p>Checks if the app is installed as normal app (with or without updates), as system app without updates, or
     * system app with updates.</p>
     * <br>
     * <p><u>---CONSTANTS---</u></p>
     * <p>- {@link #NORMAL} --> for the returning value: the app is a normal app (with or without updates)</p>
     * <p>- {@link #SYSTEM_WITHOUT_UPDATES} --> for the returning value: the app is a system app without updates</p>
     * <p>- {@link #SYSTEM_WITH_UPDATES} --> for the returning value: the app is a system app with updates</p>
     * <p><u>---CONSTANTS---</u></p>
     *
     * @return one of the constants
     */
    public static int appInstallationType() {
        if (UtilsSysApp.isSystemApp()) {
            if (UtilsSysApp.isSystemUpdatedAppByFlag()) {
                System.out.println("---------------SYSTEM WITH UPDATES---------------");
                return SYSTEM_WITH_UPDATES;
            } else {
                System.out.println("---------------SYSTEM WITHOUT UPDATES---------------");
                return SYSTEM_WITHOUT_UPDATES;
            }
        } else {
            System.out.println("---------------NORMAL---------------");
            return NORMAL;
        }
    }

    /**
     * <p>Checks if the app is a Device Administrator.</p>
     *
     * @return true if it's a Device Administrator, false otherwise
     */
    public static boolean isDeviceAdmin() {
        final Context context = UtilsGeneral.getContext();
        final DevicePolicyManager mDPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        final ComponentName mAdminName = new ComponentName(context, DeviceAdminRecv.class);

        return mDPM.isAdminActive(mAdminName);
    }

    /**
     * <p>Sends a broadcast that can only be received by components inside this application (which means, an
     * app-internal broadcast).</p>
     * <p>To do this, this method sets {@link Intent#setPackage(String)} to this package's name automatically.</p>
     *
     *  @param intent the intent to use with the broadcast
     */
    public static void sendInternalBroadcast(@NonNull final Intent intent) {
        final Context context = UtilsGeneral.getContext();
        intent.setPackage(context.getPackageName());

        context.sendBroadcast(intent);
    }
}
