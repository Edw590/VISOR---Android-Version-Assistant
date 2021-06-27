package com.dadi590.assist_c_a.GlobalUtils;

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
     * @param context a context
     * @param serviceClass the service class
     *
     * @return the PID of the service of the given class
     */
    public static int getRunningServicePID(@NonNull final Context context, @NonNull final Class<?> serviceClass) {
        final ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (final ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return service.pid;
            }
        }
        return -1;
    }

    /**
     * <p>Terminates a PID.</p>
     *
     * @param pid PID to terminate
     */
    public static void terminatePID(final int pid) {
        android.os.Process.killProcess(pid);
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
