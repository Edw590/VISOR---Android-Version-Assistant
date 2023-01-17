package com.dadi590.assist_c_a.VoiceEnrollment;

import android.app.Activity;
import android.hardware.soundtrigger.SoundTrigger;
import android.hardware.soundtrigger.SoundTrigger.Keyphrase;
import android.hardware.soundtrigger.SoundTrigger.KeyphraseSoundModel;
import android.os.Build;
import android.os.Bundle;
import android.os.UserManager;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.dadi590.assist_c_a.R;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Random;
import java.util.UUID;

@RequiresApi(Build.VERSION_CODES.L)
public class TestEnrollmentActivity extends Activity {
	private static final String TAG = "TestEnrollmentActivity";
	private static final boolean DBG = true;

	/** Keyphrase related constants, must match those defined in enrollment_application.xml */
	private static final int KEYPHRASE_ID = 101;
	private static final int RECOGNITION_MODES = SoundTrigger.RECOGNITION_MODE_VOICE_TRIGGER;
	private static final String BCP47_LOCALE = "en-US";
	private static final String TEXT = "Hello There";

	private EnrollmentUtil mEnrollmentUtil;
	private Random mRandom;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (DBG) //Log.id(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.aohd_main);
		mEnrollmentUtil = new EnrollmentUtil();
		mRandom = new Random();
	}

	/**
	 * Called when the user clicks the enroll button.
	 * Performs a fresh enrollment.
	 */
	public void onEnrollButtonClicked(View v) {
		Keyphrase kp;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			kp = new Keyphrase(KEYPHRASE_ID, RECOGNITION_MODES, BCP47_LOCALE/*Locale.forLanguageTag(BCP47_LOCALE)*/, TEXT,
					new int[] { UserManager.get(this).getUserHandle() /* current user */});
		} else {
			try {
				final Constructor<Keyphrase> constructor = Keyphrase.class.
						getConstructor(int.class, int.class, String.class, String.class, int[].class);
				constructor.setAccessible(true);
				kp = constructor.newInstance(KEYPHRASE_ID, RECOGNITION_MODES, BCP47_LOCALE, TEXT,
								new int[] { UserManager.get(this).getUserHandle() /* current user */});
			} catch (final NoSuchMethodException ignored) {
			} catch (final IllegalAccessException ignored) {
			} catch (final InvocationTargetException ignored) {
			} catch (final InstantiationException ignored) {
			}

			// Won't happen.
			return;
		}
		UUID modelUuid = UUID.randomUUID();
		// Generate a fake model to push.
		byte[] data = new byte[1024];
		mRandom.nextBytes(data);
		KeyphraseSoundModel soundModel = new KeyphraseSoundModel(modelUuid, null, data,
				new Keyphrase[] { kp });
		boolean status = mEnrollmentUtil.addOrUpdateSoundModel(soundModel);
		if (status) {
			Toast.makeText(
					this, "Successfully enrolled, model UUID=" + modelUuid, Toast.LENGTH_SHORT)
					.show();
		} else {
			Toast.makeText(this, "Failed to enroll!!!" + modelUuid, Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Called when the user clicks the un-enroll button.
	 * Clears the enrollment information for the user.
	 */
	public void onUnEnrollButtonClicked(View v) {
		KeyphraseSoundModel soundModel = mEnrollmentUtil.getSoundModel(KEYPHRASE_ID, BCP47_LOCALE);
		if (soundModel == null) {
			Toast.makeText(this, "Sound model not found!!!", Toast.LENGTH_SHORT).show();
			return;
		}
		boolean status = mEnrollmentUtil.deleteSoundModel(KEYPHRASE_ID, BCP47_LOCALE);
		if (status) {
			Toast.makeText(this, "Successfully un-enrolled, model UUID=" + EnrollmentUtil.getUuid(soundModel),
					Toast.LENGTH_SHORT)
					.show();
		} else {
			Toast.makeText(this, "Failed to un-enroll!!!", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Called when the user clicks the re-enroll button.
	 * Uses the previously enrolled sound model and makes changes to it before pushing it back.
	 */
	public void onReEnrollButtonClicked(View v) {
		KeyphraseSoundModel soundModel = mEnrollmentUtil.getSoundModel(KEYPHRASE_ID, BCP47_LOCALE);
		if (soundModel == null) {
			Toast.makeText(this, "Sound model not found!!!", Toast.LENGTH_SHORT).show();
			return;
		}
		// Generate a fake model to push.
		byte[] data = new byte[2048];
		mRandom.nextBytes(data);
		KeyphraseSoundModel updated = new KeyphraseSoundModel(EnrollmentUtil.getUuid(soundModel),
				EnrollmentUtil.getVendorUuid(soundModel), data, EnrollmentUtil.getKeyphrases(soundModel));
		boolean status = mEnrollmentUtil.addOrUpdateSoundModel(updated);
		if (status) {
			Toast.makeText(this, "Successfully re-enrolled, model UUID=" + EnrollmentUtil.getUuid(updated),
					Toast.LENGTH_SHORT)
					.show();
		} else {
			Toast.makeText(this, "Failed to re-enroll!!!", Toast.LENGTH_SHORT).show();
		}
	}
}
