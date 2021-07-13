package com.dadi590.assist_c_a.Modules.AudioRecorder;

import android.media.MediaRecorder;

import androidx.annotation.Nullable;

import com.dadi590.assist_c_a.MainSrv;
import com.dadi590.assist_c_a.Modules.Speech.Speech2;
import com.dadi590.assist_c_a.GlobalUtils.UtilsGeneral;
import com.dadi590.assist_c_a.GlobalUtils.UtilsMedia;

import java.io.File;
import java.io.IOException;

/**
 * <p>The audio recorder module of the assistant.</p>
 */
public class AudioRecorder {

	@Nullable private MediaRecorder recorder = null;
	private boolean recording = false;

	private static final String aud_src_tmp_file = "audioSourceCheck";

	/**
	 * <p>Method to call instead of calling directly {@link #startRecording(int, boolean)}.</p>
	 *  @param start true to start recording, false to stop recording
	 * @param audioSource same as in {@link #startRecording(int, boolean)}
	 */
	public final void record(final boolean start, final int audioSource) {
		if (start) {
			if (recording) {
				final String speak = "Already on it sir.";
				MainSrv.getSpeech2().speak(speak, Speech2.EXECUTOR_SOMETHING_SAID, Speech2.PRIORITY_USER_ACTION, null);
			} else {
				final Runnable runnable = () -> {
					startRecording(audioSource, false);
					/*if (audioSource == MediaRecorder.AudioSource.MIC && !recording) {
						// In case of an error and that the microphone is the audio source, start the background
						// recognition again.
						Utils_reconhecimentos_voz.iniciar_reconhecimento_pocketsphinx();
					}*/
				};
				final String speak = "Starting now, sir.";
				MainSrv.getSpeech2().speak(speak, Speech2.EXECUTOR_SOMETHING_SAID, Speech2.PRIORITY_USER_ACTION, runnable);
			}
		} else {
			if (recording) {
				stopRecording();
				final String speak = "Stopped, sir.";
				MainSrv.getSpeech2().speak(speak, Speech2.EXECUTOR_SOMETHING_SAID, Speech2.PRIORITY_USER_ACTION, null);
			} else {
				final String speak = "Already stopped, sir.";
				MainSrv.getSpeech2().speak(speak, Speech2.EXECUTOR_SOMETHING_SAID, Speech2.PRIORITY_USER_ACTION, null);
			}
		}
	}

	public static final int NO_ERRORS = 0;
	public static final int ERR_CREATE_FILE = 1;
	public static final int ERR_PREP_RECORDING = 2;
	public static final int ERR_PERM_CAP_AUDIO_OR_MIC_BUSY = 3;
	/**
	 * <p>Starts an audio recording, recording from the given audio source, to default media output files, given by
	 * {@link UtilsMedia#getOutputMediaFile(int)}</p>.
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link #NO_ERRORS} --> for the returning value: when the recording successfully started</p>
	 * <p>- {@link #ERR_CREATE_FILE} --> for the returning value: when there was an error creating the recording file</p>
	 * <p>- {@link #ERR_PREP_RECORDING} --> for the returning value: when there was an error preparing the recording</p>
	 * <p>- {@link #ERR_PERM_CAP_AUDIO_OR_MIC_BUSY} --> for the returning value: when the permission to record from the
	 * given audio source was not granted, or if the microphone is already in use and the recording could not start</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @param audioSource one of {@link MediaRecorder#setAudioSource(int)}'s parameters
	 * @param check_recording_possible true to check if it's possible to record from the given audio source, false
	 *                                 otherwise
	 * @return one of the constants
	 */
	private int startRecording(final int audioSource, final boolean check_recording_possible) {
		// Do NOT change the coder and format settings. I've put those because they were compatible with all devices
		// that the app supports, and still the sound is very good.
		recorder = new MediaRecorder();
		recorder.setAudioSource(audioSource);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		final File file;
		if (check_recording_possible) {
			file = new File(UtilsGeneral.getContext().getCacheDir(), aud_src_tmp_file);
		} else {
			file = UtilsMedia.getOutputMediaFile(UtilsMedia.AUDIO);
		}
		if (file == null) {
			final String speak = "Error 1 sir.";
			MainSrv.getSpeech2().speak(speak, Speech2.EXECUTOR_SOMETHING_SAID, Speech2.PRIORITY_USER_ACTION, null);

			recording = false;

			return ERR_CREATE_FILE;
		}
		final String fileName = file.getAbsolutePath();
		recorder.setOutputFile(fileName);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);

        /*if (!check_recording_possible) {
            Utils_reconhecimentos_voz.desativar_reconhecimentos_voz();
        }*/

		try {
			recorder.prepare();
		} catch (final IOException e) {
			e.printStackTrace();

			recorder.release();
			recorder = null;
			if (!check_recording_possible) {
				final String speak = "Error 2 sir.";
				MainSrv.getSpeech2().speak(speak, Speech2.EXECUTOR_SOMETHING_SAID, Speech2.PRIORITY_USER_ACTION, null);
			}
			file.delete();

			recording = false;

			return ERR_PREP_RECORDING;
		}

		try {
			recorder.start();
		} catch (final IllegalStateException e) {
			e.printStackTrace();

			recorder.release();
			recorder = null;
			if (!check_recording_possible) {
				final String speak = "Error 3 sir.";
				MainSrv.getSpeech2().speak(speak, Speech2.EXECUTOR_SOMETHING_SAID, Speech2.PRIORITY_USER_ACTION, null);
			}
			file.delete();

			recording = false;

			return ERR_PERM_CAP_AUDIO_OR_MIC_BUSY;
			/*int permission_status = ContextCompat.checkSelfPermission(context, Manifest.permission.CAPTURE_AUDIO_OUTPUT);
            if (permission_status == EPackageManager.PERMISSION_GRANTED) {
                return UtilsGeneral.FONTE_INDISPONIVEL;
            } else {
                return SEM_PERMISSAO_CAPTURE_AUDIO_OUTPUT;
            }*/
		}
		if (check_recording_possible) {
			recorder.release();
			recorder = null;
			file.delete();

			recording = false;

			return UtilsGeneral.FONTE_DISPONIVEL;
		}

		recording = true;

		return NO_ERRORS;
	}

	/**
	 * <p>Stops an ongoing audio recording.</p>
	 */
	private void stopRecording() {
		if (recorder != null) {
			try {
				recorder.stop();
			} catch (final IllegalStateException ignored) {
			}
			recorder.release();
			recorder = null;
		}
		recording = false;
	}
}
