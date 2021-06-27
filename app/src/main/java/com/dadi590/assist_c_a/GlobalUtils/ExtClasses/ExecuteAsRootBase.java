package com.dadi590.assist_c_a.GlobalUtils.ExtClasses;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * <p>Checks if the device can execute root commands or not, and in case it can, executes root commands.</p>
 * <br>
 * <p>Class gotten from http://muzikant-android.blogspot.com/2011/02/how-to-get-root-access-and-execute.html (now
 * adapted to this app).</p>
 *
 * @author Muzikant
 */
public final class ExecuteAsRootBase {

    /**
     * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
     */
    private ExecuteAsRootBase() {
    }

    public static final int ROOT_AVAILABLE = 0;
    public static final int ROOT_DENIED = 1;
    //public static final int ROOT_UNAVAILABLE_DENIED = 2;
    public static final int ROOT_UNAVAILABLE = 3;
    /**
     * Checks if the device can run root commands.
     * <br>
     * <p><u>---CONSTANTS---</u></p>
     * <p>- {@link #ROOT_AVAILABLE} --> returned if root access is available</p>
     * <p>- {@link #ROOT_DENIED} --> returned if the user denied root access</p>
     * <p>- {@link #ROOT_UNAVAILABLE} --> returned if the device is not rooted</p>
     * <p><u>---CONSTANTS---</u></p>
     *
     * @return one of the constants
     */
    public static int rootCommandsAvailability() {
        int retval;
        final Process suProcess;

        try {
            suProcess = Runtime.getRuntime().exec("su");

            final DataOutputStream os = new DataOutputStream(suProcess.getOutputStream());
            final BufferedReader osRes;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                osRes = new BufferedReader(new InputStreamReader(suProcess.getInputStream(),
                        StandardCharsets.UTF_8));
            } else {
                osRes = new BufferedReader(new InputStreamReader(suProcess.getInputStream()));
            }

            // Getting the id of the current user to check if this is root
            os.writeBytes("id\n");
            os.flush();

            final String currUid = osRes.readLine();
            final boolean exitSu;
            if (currUid == null) {
                retval = ROOT_DENIED;
                //retval = ROOT_UNAVAILABLE;
                exitSu = false;
                //Log.d("ROOT", "Can't get root access or denied by user");
            } else if (currUid.contains("uid=0")) {
                retval = ROOT_AVAILABLE;
                exitSu = true;
                //Log.d("ROOT", "Root access granted");
            } else {
                retval = ROOT_DENIED;
                exitSu = true;
                //Log.d("ROOT", "Root access rejected: " + currUid);
            }

            if (exitSu) {
                os.writeBytes("exit\n");
                os.flush();
            }
            suProcess.waitFor();
            suProcess.destroy();
        } catch (final IOException | InterruptedException ignored) {
            // Can't get root !
            // Probably broken pipe exception on trying to write to output stream (os) after su failed, meaning that the
            // device is not rooted

            retval = ROOT_UNAVAILABLE;
            //Log.d("ROOT", "Root access rejected [" + e.getClass().getName() + "] : " + e.getMessage());
        }

        return retval;
    }

    /*public final boolean execute()
    {
        boolean retval = false;

        try
        {
            ArrayList<String> commands = getCommandsToExecute();
            if (null != commands && commands.size() > 0)
            {
                Process suProcess = Runtime.getRuntime().exec("su");

                DataOutputStream os = new DataOutputStream(suProcess.getOutputStream());

                // Execute commands that require root access
                for (String currCommand : commands)
                {
                    os.writeBytes(currCommand + "\n");
                    os.flush();
                }

                os.writeBytes("exit\n");
                os.flush();

                try
                {
                    int suProcessRetval = suProcess.waitFor();
                    if (255 != suProcessRetval)
                    {
                        // Root access granted
                        retval = true;
                    }
                    else
                    {
                        // Root access denied
                        retval = false;
                    }
                }
                catch (Exception ex)
                {
                    Log.e("ROOT", "Error executing root action", ex);
                }
            }
        }
        catch (IOException ex)
        {
            Log.w("ROOT", "Can't get root access", ex);
        }
        catch (SecurityException ex)
        {
            Log.w("ROOT", "Can't get root access", ex);
        }
        catch (Exception ex)
        {
            Log.w("ROOT", "Error executing internal operation", ex);
        }

        return retval;
    }
    protected abstract ArrayList<String> getCommandsToExecute();*/
}
