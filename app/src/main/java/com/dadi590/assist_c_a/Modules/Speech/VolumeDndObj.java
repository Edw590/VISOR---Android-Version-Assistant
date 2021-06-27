package com.dadi590.assist_c_a.Modules.Speech;

import android.app.NotificationManager;
import android.media.AudioManager;

/**
 * <p>Class to instantiate to keep the values of the volume and Do Not Disturb interruption filter.</p>
 */
class VolumeDndObj {

	/**
	 * <p>Default value for all the attributes of the object.</p>
	 * <br>
	 * <p>Can't be a possible value for any of:</p>
	 * <p>- Any of the {@code STREAM_} constants defined in {@link AudioManager}</p>
	 * <p>- The parameters of {@link AudioManager#setStreamVolume(int, int, int)}</p>
	 * <p>- The return of {@link AudioManager#getStreamVolume(int)}</p>
	 * <p>- The parameter of {@link NotificationManager#setInterruptionFilter(int)}</p>
	 * <p>- The return of {@link NotificationManager#getCurrentInterruptionFilter()}</p>
	 * <p>- Any possible value of {@link System#currentTimeMillis()}</p>
	 */
	static final int DEFAULT_VALUE = -3234;

	// To add a new attribute, update the DEFAULT_VALUE doc and the setDefaultValues() function.

	/** One of the {@code STREAM_} constants in {@link AudioManager}. */
	int audio_stream;
	/** The result of {@link AudioManager#getStreamVolume(int)}. */
	int old_volume;
	/** The volume to be set with {@link AudioManager#setStreamVolume(int, int, int)}. */
	int new_volume;

	/** The result of {@link NotificationManager#getCurrentInterruptionFilter()}. */
	int old_interruption_filter;
	/** The interruption filter to be set with {@link NotificationManager#setInterruptionFilter(int)}. */
	int new_interruption_filter;

	// To add a new attribute, update the DEFAULT_VALUE doc and the setDefaultValues() function.

	/**
	 * <p>Main class constructor - calls {@link #setDefaultValues()}.</p>
	 */
	VolumeDndObj() {
		setDefaultValues();
	}

	/**
	 * <p>Sets all the attributes to their default values.</p>
	 */
	final void setDefaultValues() {
		audio_stream = DEFAULT_VALUE;
		old_volume = DEFAULT_VALUE;
		new_volume = DEFAULT_VALUE;
		old_interruption_filter = DEFAULT_VALUE;
		new_interruption_filter = DEFAULT_VALUE;
	}
}
