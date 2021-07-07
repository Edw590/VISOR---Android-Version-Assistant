/**
 * <p>Everything directly related with the assistant's protected lock screen.</p>
 * <br>
 * <p>First, thank you ESET for the idea haha. Loved it! Until you got it out of Mobile Security above 3.3 for some
 * reason. So here it is again.</p>
 * <p>The Protected Lock Screen works as follows:</p>
 * <p>- If the device is a system app, the status bar will not be accessible at all.</p>
 * <p>- If the app is a Device Administrator, the moment the user clicks somewhere outside the activity, the app
 * will lock the device again and restart the activity in case it's not already in foreground.</p>
 * <p>- - If the app is not a Device Administrator, it will only do the latter, and the former will not be possible.</p>
 * <p>- If the app is neither a Device Administrator nor a system app, the way of stopping people from touching too
 * much on the status bar is by collapsing it every some time (very few time, like 200 ms). Will waste more battery,
 * but it's what it costs to not have the app as a Device Administrator. If the Location toggle is in the secondary
 * panel of the status bar, possibly no one will be able to get there fast enough before the status bar is closed.</p>
 * <p>- This also works with or without security code or pattern or whatever on the phone. It's independent of that.</p>
 * <p>Note: on API level 22 (Lollipop 5.1) and below, the SYSTEM_ALERT_WINDOW permission is granted normally. That means
 * the Protected Lock Screen will behave as if the app was installed as a system app (whether it is or not).</p>
 * <br>
 * <p>IMPORTANT NOTE THEN:</p>
 * <p>- Always have the most important buttons of the status bar hidden in the secondary side panels, like the Location
 * toggle button. The Mobile Data one needs the phone to be unlocked to be disabled, so yeah. Still, if no code is on
 * the phone, it REALLY should be in the secondary panels.</p>
 */
package com.dadi590.assist_c_a.Modules.ProtectedLockScr;
