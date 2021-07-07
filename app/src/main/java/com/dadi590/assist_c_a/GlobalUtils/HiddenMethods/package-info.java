/**
 * <p>Hidden methods made public and static (utilities).</p>
 * <br>
 * <p>Package with classes used to:</p>
 * <p>- Statically publish methods inside classes that supposedly need Context (or some other instantiated attribute),
 * but the methods, internally, don't actually need it, but still the method is not declared as static (security
 * measure, maybe).
 * <p>- a modified implementation of such methods to remove the need for the instantiated attributes instance.</p>
 * <br>
 * <p>Each class name begins with an E for the name to be different from the original class. That E means External, as
 * opposite to I of Internal of Android's internal classes, like {@link android.media.IAudioService} (that's not
 * actually true - the I is from Interface, but it works).</p>
 * <br>
 * <p>No idea why I wanted to know this, but I wrote down to document it here so here it goes: getOpPackageName()
 * returns "com.dadi590.assist_c_a" on Lollipop.</p>
 * <br>
 * <p><strong>NOT IN USE while {@link android.app.AppGlobals#getInitialApplication()}.getApplicationContext() works!!!
 * </strong></p>
 */
package com.dadi590.assist_c_a.GlobalUtils.HiddenMethods;
