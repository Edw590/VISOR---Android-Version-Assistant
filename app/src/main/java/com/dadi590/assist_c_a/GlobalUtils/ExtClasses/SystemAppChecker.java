package com.dadi590.assist_c_a.GlobalUtils.ExtClasses;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * <p>Checks if an app is a system app. Also checks in case it's a system app, if it's an updated app or not.</p>
 * <br>
 * <p>Class gotten from
 * <a href="https://stackoverflow.com/questions/8784505/how-do-i-check-if-an-app-is-a-non-system-app-in-android">this
 * question</a> on StackOverflow, more precisely <a href="https://stackoverflow.com/a/48029011/8228163">this answer</a>,
 * by <a href="https://stackoverflow.com/users/28557/vinayak-bevinakatti">this person</a> (Vinayak Bevinakatti) - now
 * adapted to this app.</p>
 * <p>Except {@link #isSystemUpdatedAppByFLAG(Context)} which I took from somewhere else, and
 * {@link #isSystemApp(Context)}, which I made.</p>
 *
 * @author Vinayak Bevinakatti and others
 */
public final class SystemAppChecker {

    /**
     * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
     */
    private SystemAppChecker() {
    }

    /**
     * Checks if an app is a system app.
     *
     * @param context a context
     *
     * @return an OR operation on {@link #isSystemAppByFLAG(Context)}, {@link #isSystemAppByPM(Context)},
     * {@link #isSystemPreloaded(Context)} and {@link #isSystemSigned(Context)}
     */
    public static boolean isSystemApp(@NonNull final Context context) {
        return SystemAppChecker.isSystemAppByFLAG(context) || SystemAppChecker.isSystemAppByPM(context) ||
                SystemAppChecker.isSystemPreloaded(context) || SystemAppChecker.isSystemSigned(context);
    }

    /**
     * Checks if a system app has been updated or not.
     *
     * @param context a context
     *
     * @return {@code true} if the app is a system app and has been updated.
     */
    public static boolean isSystemUpdatedAppByFLAG(@NonNull final Context context) {
        try {
            final ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
            if (ai != null && (ai.flags & (ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0) {
                // Conjunction of FLAG_UPDATED_SYSTEM_APP and isSystemAppByFLAG()
                return isSystemAppByFLAG(context);
            }
        } catch (final PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Check if system app by 'pm' command-line program.
     *
     * @param context a context
     *
     * @return {@code true} if package is a system app.
     */
    private static boolean isSystemAppByPM(@NonNull final Context context) {
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

        return systemApps.contains(context.getPackageName());
    }

    /**
     * Check if application is preloaded.
     *
     * @param context a context
     *
     * @return {@code true} if package is preloaded.
     */
    private static boolean isSystemPreloaded(@NonNull final Context context) {
        try {
            final ApplicationInfo ai = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), 0);
            if (ai.sourceDir.startsWith("/system/app/") || ai.sourceDir.startsWith("/system/priv-app/")) {
                return true;
            }
        } catch (final PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Check if the app is system signed or not.
     *
     * @param context a context
     *
     * @return {@code true} if application is signed by system certificate,
     *         otherwise {@code false}
     */
    private static boolean isSystemSigned(@NonNull final Context context) {
        try {
            // Note: EPackageManager.GET_SIGNATURES is a problem below Android 4.4 KitKat only (no problem, I guess).

            // Get packageinfo for target application
            final PackageInfo targetPkgInfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_SIGNATURES);
            // Get packageinfo for system package
            final PackageInfo sys = context.getPackageManager().getPackageInfo(
                    "android", PackageManager.GET_SIGNATURES);
            // Match both packageinfo for there signatures
            return (targetPkgInfo != null && targetPkgInfo.signatures != null && sys.signatures[0]
                    .equals(targetPkgInfo.signatures[0]));
        } catch (final PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Check if application is installed in the device's system image.
     *
     * @param context a context
     *
     * @return {@code true} if package is a system app.
     */
    private static boolean isSystemAppByFLAG(@NonNull final Context context) {
        try {
            final ApplicationInfo ai = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), 0);
            // Check if FLAG_SYSTEM or FLAG_UPDATED_SYSTEM_APP are set.
            if (ai != null
                    && (ai.flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0) {
                return true;
            }
        } catch (final PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }
}
