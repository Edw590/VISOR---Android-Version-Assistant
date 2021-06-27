package com.dadi590.assist_c_a.GlobalUtils;

import android.app.ActivityThread;
import android.content.Context;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Build;

import androidx.annotation.NonNull;

import com.dadi590.assist_c_a.GlobalUtils.ExtClasses.ExecuteAsRootBase;
import com.dadi590.assist_c_a.MainSrv;
import com.dadi590.assist_c_a.Modules.Speech.Speech2;

import org.jetbrains.annotations.NonNls;

import java.io.File;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.Random;

/**
 * <p>Global app-related utilities.</p>
 */
public final class UtilsGeneral {

    /**
     * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
     */
    private UtilsGeneral() {
    }

    /**
     * <p>Deletes a directory (either file or folder).</p>
     *
     * @param dir the path to delete
     *
     * @return true if deletion was completely successful, including all files if a non-empty folder was selected for
     * deletion; false otherwise
     */
    public static boolean deletePath(@NonNull final File dir) {
        if (dir.isDirectory()) {
            final String[] children = dir.list();
            boolean success = true;
            if (children == null) {
                return false;
            } else {
                for (final String child : children) {
                    success = success && deletePath(new File(dir, child));
                }
            }
            return success && dir.delete();
        } else if (dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    /**
     * <p>Checks if the External Functions are available or not.</p>
     *
     * @return true if the External Functions are available, false otherwise.
     */
    public static boolean ext_funcs_available() {
        // "You see, Java's exception hierarchy is a bit unintuitive. You have two classes, Exception and Error, each of
        // which extends Throwable. Thus, if you want to catch absolutely everything you need to catch Throwable (not
        // recommended)."
        // In this case, the error for this case is UnsatisfiedLinkError, which is part of the Error class, not
        // Exception. But I want to catch ANY error to know if they're available or not, so I chose Throwable.
        try {
            //Funcoes_externas.chamar_tarefa("sgfhjvfgsbvysd");
        } catch (final Throwable ignored) {
            return false;
        }
        return true;
    }

    /**
     * <p>Generates a random string with the given length, containing only ASCII letters (upper case or lower case) and
     * numbers.</p>
     *
     * @param length length of the generating string
     *
     * @return the generated string
     */
    @NonNull
    public static String generateRandomString(final int length) {
        final String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        final String lower = upper.toLowerCase(Locale.ROOT);
        final String digits = "0123456789";
        @NonNls final String alphanum = upper + lower + digits;

        final Random random = new SecureRandom();
        final char[] symbols = alphanum.toCharArray();
        final char[] buf;

        if (length < 1) throw new IllegalArgumentException("Length 0 string requested");
        buf = new char[length];

        final int buf_length = buf.length;
        for (int idx = 0; idx < buf_length; idx++) {
            buf[idx] = symbols[random.nextInt(symbols.length)];
        }
        return new String(buf);
    }

    /**
     * <p>Checks and warns about root access availability for the app.</p>
     *
     * @param warn_root_available true to warn if root access is available, false to only warn when there's no access
     */
    public static void checkWarnRootAccess(final boolean warn_root_available) {
        // todo See if you can delete this... It's not supposed for the app to execute any root commands. Only system
        //  hidden/internal methods.

        switch (ExecuteAsRootBase.rootCommandsAvailability()) {
            case (ExecuteAsRootBase.ROOT_AVAILABLE): {
                if (warn_root_available) {
                    final String speak = "Root access available on the device.";
                    if (MainSrv.getSpeech2() != null) {
                        MainSrv.getSpeech2().speak(speak, Speech2.NO_ADDITIONAL_COMMANDS, Speech2.PRIORITY_USER_ACTION,
                                null);
                    }
                }

                break;
            }
            case (ExecuteAsRootBase.ROOT_DENIED): {
                @NonNls final String speak = "WARNING! Root access was denied on this device! Some features may not " +
                        "be available!";
                if (MainSrv.getSpeech2() != null) {
                    MainSrv.getSpeech2().speak(speak, Speech2.NO_ADDITIONAL_COMMANDS, Speech2.PRIORITY_HIGH, null);
                }

                break;
            }
            case (ExecuteAsRootBase.ROOT_UNAVAILABLE): {
                @NonNls final String speak = "Attention! The device is not rooted! Some features may not be available!";
                if (MainSrv.getSpeech2() != null) {
                    MainSrv.getSpeech2().speak(speak, Speech2.NO_ADDITIONAL_COMMANDS, Speech2.PRIORITY_MEDIUM, null);
                }

                break;
            }
        }
    }

    /**
     * <p>Check if an accessory with speakers (like earphones, headphones, headsets...) are connected.</p>
     *
     * @return true if an accessory with speakers is connected, false otherwise
     */
    public static boolean areExtSpeakersOn() {
        final AudioManager audioManager = (AudioManager) UtilsGeneral.getMainAppContext()
                .getSystemService(Context.AUDIO_SERVICE);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return audioManager.isWiredHeadsetOn() || audioManager.isBluetoothScoOn()
                    || audioManager.isBluetoothA2dpOn();
        } else {
            final AudioDeviceInfo[] devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS);

            for (final AudioDeviceInfo device : devices) {
                if (device.getType() == AudioDeviceInfo.TYPE_WIRED_HEADSET
                        || device.getType() == AudioDeviceInfo.TYPE_WIRED_HEADPHONES
                        || device.getType() == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP
                        || device.getType() == AudioDeviceInfo.TYPE_BLUETOOTH_SCO
                        //|| device.getType() == AudioDeviceInfo.TYPE_BLE_HEADSET - added in Android S
                        // todo Remove the // when it's on the SDK
                        || device.getType() == AudioDeviceInfo.TYPE_USB_HEADSET) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * <p>Calls {@link Context#getApplicationContext()} on {@link ActivityThread#currentApplication()}.</p>
     *
     * @return .
     */
    @NonNull
    public static Context getMainAppContext() {
        return ActivityThread.currentApplication().getApplicationContext();
    }

    public static final int FONTE_DISPONIVEL = 0;
    public static final int FONTE_INDISPONIVEL = 1;
    public static final int ERRO_NA_DETECAO = 2;
    // todo Falta a função "fonte_audio_grav_disp" aqui abaixo
}
