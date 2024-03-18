package com.edw590.visor_c_a.VoiceEnrollment;

import android.content.Context;
import android.hardware.soundtrigger.KeyphraseEnrollmentInfo;
import android.hardware.soundtrigger.SoundTrigger;
import android.hardware.soundtrigger.SoundTrigger.Keyphrase;
import android.hardware.soundtrigger.SoundTrigger.KeyphraseSoundModel;
import android.os.Build;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.service.voice.AlwaysOnHotwordDetector;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.android.internal.app.IVoiceInteractionManagerService;
import com.edw590.visor_c_a.GlobalUtils.UtilsContext;
import com.edw590.visor_c_a.GlobalUtils.UtilsReflection;

import java.util.Arrays;
import java.util.UUID;

/**
 * Utility class for the enrollment operations like enroll;re-enroll & un-enroll.
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public class EnrollmentUtil {
	private static final String TAG = "TestEnrollmentUtil";

	/**
	 * Activity Action: Show activity for managing the keyphrases for hotword detection.
	 * This needs to be defined by an activity that supports enrolling users for hotword/keyphrase
	 * detection.
	 */
	public static final String ACTION_MANAGE_VOICE_KEYPHRASES =
			KeyphraseEnrollmentInfo.ACTION_MANAGE_VOICE_KEYPHRASES;

	/**
	 * Intent extra: The intent extra for the specific manage action that needs to be performed.
	 * Possible values are {@link AlwaysOnHotwordDetector#MANAGE_ACTION_ENROLL},
	 * {@link AlwaysOnHotwordDetector#MANAGE_ACTION_RE_ENROLL}
	 * or {@link AlwaysOnHotwordDetector#MANAGE_ACTION_UN_ENROLL}.
	 */
	public static final String EXTRA_VOICE_KEYPHRASE_ACTION =
			KeyphraseEnrollmentInfo.EXTRA_VOICE_KEYPHRASE_ACTION;

	/**
	 * Intent extra: The hint text to be shown on the voice keyphrase management UI.
	 */
	public static final String EXTRA_VOICE_KEYPHRASE_HINT_TEXT =
			KeyphraseEnrollmentInfo.EXTRA_VOICE_KEYPHRASE_HINT_TEXT;
	/**
	 * Intent extra: The voice locale to use while managing the keyphrase.
	 */
	public static final String EXTRA_VOICE_KEYPHRASE_LOCALE =
			KeyphraseEnrollmentInfo.EXTRA_VOICE_KEYPHRASE_LOCALE;

	/** Simple recognition of the key phrase */
	public static final int RECOGNITION_MODE_VOICE_TRIGGER =
			SoundTrigger.RECOGNITION_MODE_VOICE_TRIGGER;
	/** Trigger only if one user is identified */
	public static final int RECOGNITION_MODE_USER_IDENTIFICATION =
			SoundTrigger.RECOGNITION_MODE_USER_IDENTIFICATION;

	private final IVoiceInteractionManagerService mModelManagementService;

	public EnrollmentUtil() {
		mModelManagementService = IVoiceInteractionManagerService.Stub.asInterface(
				ServiceManager.getService(Context.VOICE_INTERACTION_MANAGER_SERVICE));
	}

	/**
	 * Adds/Updates a sound model.
	 * The sound model must contain a valid UUID,
	 * exactly 1 keyphrase,
	 * and users for which the keyphrase is valid - typically the current user.
	 *
	 * @param soundModel The sound model to add/update.
	 * @return {@code true} if the call succeeds, {@code false} otherwise.
	 */
	public boolean addOrUpdateSoundModel(KeyphraseSoundModel soundModel) {
		if (!verifyKeyphraseSoundModel(soundModel)) {
			return false;
		}

		int status = SoundTrigger.STATUS_ERROR;
		try {
			status = mModelManagementService.updateKeyphraseSoundModel(soundModel);
		} catch (RemoteException e) {
			//Log.ie(TAG, "RemoteException in updateKeyphraseSoundModel", e);
		}
		return status == SoundTrigger.STATUS_OK;
	}

	/**
	 * Gets the sound model for the given keyphrase, null if none exists.
	 * This should be used for re-enrollment purposes.
	 * If a sound model for a given keyphrase exists, and it needs to be updated,
	 * it should be obtained using this method, updated and then passed in to
	 * {@link #addOrUpdateSoundModel(KeyphraseSoundModel)} without changing the IDs.
	 *
	 * @param keyphraseId The keyphrase ID to look-up the sound model for.
	 * @param bcp47Locale The locale for with to look up the sound model for.
	 * @return The sound model if one was found, null otherwise.
	 */
	@Nullable
	public KeyphraseSoundModel getSoundModel(int keyphraseId, String bcp47Locale) {
		if (keyphraseId <= 0) {
			//Log.ie(TAG, "Keyphrase must have a valid ID");
			return null;
		}

		KeyphraseSoundModel model = null;
		try {
			model = mModelManagementService.getKeyphraseSoundModel(keyphraseId, bcp47Locale);
		} catch (RemoteException e) {
			//Log.ie(TAG, "RemoteException in updateKeyphraseSoundModel");
		}

		if (model == null) {
			//Log.iw(TAG, "No models present for the gien keyphrase ID");
			return null;
		} else {
			return model;
		}
	}

	/**
	 * Deletes the sound model for the given keyphrase id.
	 *
	 * @param keyphraseId The keyphrase ID to look-up the sound model for.
	 * @return {@code true} if the call succeeds, {@code false} otherwise.
	 */
	public boolean deleteSoundModel(int keyphraseId, String bcp47Locale) {
		if (keyphraseId <= 0) {
			//Log.ie(TAG, "Keyphrase must have a valid ID");
			return false;
		}

		int status = SoundTrigger.STATUS_ERROR;
		try {
			status = mModelManagementService.deleteKeyphraseSoundModel(keyphraseId, bcp47Locale);
		} catch (RemoteException e) {
			//Log.ie(TAG, "RemoteException in updateKeyphraseSoundModel");
		}
		return status == SoundTrigger.STATUS_OK;
	}

	private boolean verifyKeyphraseSoundModel(KeyphraseSoundModel soundModel) {
		if (soundModel == null) {
			//Log.ie(TAG, "KeyphraseSoundModel must be non-null");
			return false;
		}
		if (getUuid(soundModel) == null) {
			//Log.ie(TAG, "KeyphraseSoundModel must have a UUID");
			return false;
		}
		if (getData(soundModel) == null) {
			//Log.ie(TAG, "KeyphraseSoundModel must have data");
			return false;
		}
		Keyphrase[] keyphrases = getKeyphrases(soundModel);
		if (keyphrases == null || keyphrases.length != 1) {
			//Log.ie(TAG, "Keyphrase must be exactly 1");
			return false;
		}
		Keyphrase keyphrase = keyphrases[0];
		if (getId(keyphrase) <= 0) {
			//Log.ie(TAG, "Keyphrase must have a valid ID");
			return false;
		}
		if (getRecognitionModes(keyphrase) < 0) {
			//Log.ie(TAG, "Recognition modes must be valid");
			return false;
		}
		if (getLocale(keyphrase) == null) {
			//Log.ie(TAG, "Locale must not be null");
			return false;
		}
		if (getText(keyphrase) == null) {
			//Log.ie(TAG, "Text must not be null");
			return false;
		}
		int[] users = getUsers(keyphrase);
		if (users == null || users.length == 0) {
			//Log.ie(TAG, "Keyphrase must have valid user(s)");
			return false;
		}
		return true;
	}

	// Below, methods to return the required values before and after API 30 (before they were public attributes, now
	// they are all private and have getters).

	@Nullable
	static UUID getUuid(@NonNull final KeyphraseSoundModel soundModel) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			//return soundModel.getUuid();
			return soundModel.uuid;
		} else {
			final UUID value = (UUID) UtilsReflection.getFieldValue(soundModel, "uuid");
			assert null != value; // Will never be null

			return value;
		}
	}
	@Nullable
	static UUID getVendorUuid(@NonNull final KeyphraseSoundModel soundModel) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			//return soundModel.getVendorUuid();
			return soundModel.vendorUuid;
		} else {
			final UUID value = (UUID) UtilsReflection.getFieldValue(soundModel, "vendorUuid");
			assert null != value; // Will never be null

			return value;
		}
	}
	@Nullable
	static byte[] getData(@NonNull final KeyphraseSoundModel soundModel) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			//return soundModel.getData();
			return soundModel.data;
		} else {
			final byte[] value = (byte[]) UtilsReflection.getFieldValue(soundModel, "data");
			assert null != value; // Will never be null

			return value;
		}
	}
	@Nullable
	static Keyphrase[] getKeyphrases(@NonNull final KeyphraseSoundModel soundModel) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			//return soundModel.getKeyphrases();
			return soundModel.keyphrases;
		} else {
			final Keyphrase[] value = (Keyphrase[]) UtilsReflection.getFieldValue(soundModel, "keyphrases");
			assert null != value; // Will never be null

			return value;
		}
	}
	static int getId(@NonNull final Keyphrase keyphrase) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			//return keyphrase.getId();
			return keyphrase.id;
		} else {
			final Integer value = (Integer) UtilsReflection.getFieldValue(keyphrase, "id");
			assert null != value; // Will never be null

			return value;
		}
	}
	static int getRecognitionModes(@NonNull final Keyphrase keyphrase) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			//return keyphrase.getRecognitionModes();
			return keyphrase.recognitionModes;
		} else {
			final Integer value = (Integer) UtilsReflection.getFieldValue(keyphrase, "recognitionModes");
			assert null != value; // Will never be null

			return value;
		}
	}
	@Nullable
	static String getLocale(@NonNull final Keyphrase keyphrase) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			// It was returning a String too, aside from the commented line (when I switched from API 29 to 30)
			//return keyphrase.getLocale();
			// It was returning a Locale too, aside from the commented line (when I switched from API 32 back to 29)
			return keyphrase.locale;
		} else {
			final String value = (String) UtilsReflection.getFieldValue(keyphrase, "locale");
			assert null != value; // Will never be null

			//return Locale.forLanguageTag(value);
			return value;
		}
	}
	@Nullable
	static String getText(@NonNull final Keyphrase keyphrase) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			//return keyphrase.getText();
			return keyphrase.text;
		} else {
			final String value = (String) UtilsReflection.getFieldValue(keyphrase, "text");
			assert null != value; // Will never be null

			return value;
		}
	}
	@Nullable
	static int[] getUsers(@NonNull final Keyphrase keyphrase) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			//return keyphrase.getUsers();
			return keyphrase.users.clone();
		} else {
			final int[] value = (int[]) UtilsReflection.getFieldValue(keyphrase, "users");
			assert null != value; // Will never be null

			return value.clone();
		}
	}
}
