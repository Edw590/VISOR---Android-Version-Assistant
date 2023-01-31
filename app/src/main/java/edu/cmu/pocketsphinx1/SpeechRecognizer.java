// Note: this file was modified by me, DADi590, in 2022.

package edu.cmu.pocketsphinx1;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.dadi590.assist_c_a.GlobalUtils.UtilsGeneral;
import com.dadi590.assist_c_a.GlobalUtils.UtilsSysApp;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import edu.cmu.pocketsphinx.Config;
import edu.cmu.pocketsphinx.Decoder;
import edu.cmu.pocketsphinx.FsgModel;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;

/**
 * Main class to access recognizer functions. After configuration this class
 * starts a listener thread which records the data and recognizes it using
 * Pocketsphinx engine. Recognition events are passed to a client using
 * {@link RecognitionListener}
 *
 */
public class SpeechRecognizer {

    protected static final String TAG = SpeechRecognizer.class.getSimpleName();

    private final Decoder decoder;

    private final int sampleRate;
    private final static float BUFFER_SIZE_SECONDS = 0.4f;
    private int bufferSize;
    @Nullable
    private final AudioRecord recorder;

    private final int audio_source;

    private Thread recognizerThread;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final Collection<RecognitionListener> listeners = new HashSet<RecognitionListener>();

    /**
     * Creates speech recognizer. Recognizer holds the AudioRecord object, so you
     * need to call {@link release} in order to properly finalize it.
     *
     * @param config The configuration object
     * @throws IOException thrown if audio recorder can not be created for some reason.
     */
    protected SpeechRecognizer(Config config) throws IOException {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            audio_source = MediaRecorder.AudioSource.VOICE_RECOGNITION;
        } else {
            audio_source = UtilsSysApp.mainFunction(null, UtilsSysApp.IS_PRIVILEGED_SYSTEM_APP) ?
                    MediaRecorder.AudioSource.HOTWORD : MediaRecorder.AudioSource.VOICE_RECOGNITION;
        }

        decoder = new Decoder(config);
        sampleRate = (int) decoder.getConfig().getFloat("-samprate");
        bufferSize = Math.round(sampleRate * BUFFER_SIZE_SECONDS);
        if (ContextCompat.checkSelfPermission(UtilsGeneral.getContext(), Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED) {
            recorder = new AudioRecord(audio_source, sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, bufferSize * 2);
        } else {
            recorder = null;
        }

        if (null != recorder && recorder.getState() == AudioRecord.STATE_UNINITIALIZED) {
            recorder.release();
            throw new IOException(
                    "Failed to initialize recorder. Microphone might be already in use or no RECORD_AUDIO permission granted.");
        }
    }

    /**
     * Adds listener.
     */
    public void addListener(RecognitionListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    /**
     * Removes listener.
     */
    public void removeListener(RecognitionListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    /**
     * Starts recognition. Does nothing if recognition is active.
     *
     * @return true if recognition was actually started
     */
    public boolean startListening(String searchName) {
        if (null != recognizerThread)
            return false;

        //Log.ii(TAG, String.format("Start recognition \"%s\"", searchName));
        decoder.setSearch(searchName);
        recognizerThread = new RecognizerThread();
        recognizerThread.start();
        return true;
    }

    /**
     * Starts recognition. After specified timeout listening stops and the
     * endOfSpeech signals about that. Does nothing if recognition is active.
     *
     * @timeout - timeout in milliseconds to listen.
     *
     * @return true if recognition was actually started
     */
    public boolean startListening(String searchName, int timeout) {
        if (null != recognizerThread)
            return false;

        //Log.ii(TAG, String.format("Start recognition \"%s\"", searchName));
        decoder.setSearch(searchName);
        recognizerThread = new RecognizerThread(timeout);
        recognizerThread.start();
        return true;
    }

    private boolean stopRecognizerThread() {
        if (null == recognizerThread)
            return false;

        try {
            recognizerThread.interrupt();
            recognizerThread.join();
        } catch (final InterruptedException ignored) {
            // Restore the interrupted status.
            Thread.currentThread().interrupt();
        }

        recognizerThread = null;
        return true;
    }

    /**
     * Stops recognition. All listeners should receive final result if there is
     * any. Does nothing if recognition is not active.
     *
     * @return true if recognition was actually stopped
     */
    public boolean stop() {
        boolean result = stopRecognizerThread();
        if (result) {
            //Log.ii(TAG, "Stop recognition");
            final Hypothesis hypothesis = decoder.hyp();
            mainHandler.post(new ResultEvent(hypothesis, true));
        }
        return result;
    }

    /**
     * Cancels recognition. Listeners do not receive final result. Does nothing
     * if recognition is not active.
     *
     * @return true if recognition was actually canceled
     */
    public boolean cancel() {
        boolean result = stopRecognizerThread();
        if (result) {
            //Log.ii(TAG, "Cancel recognition");
        }

        return result;
    }

    /**
     * Returns the decoder object for advanced operation (dictionary extension, utterance
     * data collection, adaptation and so on).
     *
     * @return Decoder
     */
    public Decoder getDecoder() {
        return decoder;
    }

    /**
     * Shutdown the recognizer and release the recorder
     */
    public void shutdown() {
        recorder.release();
    }

    /**
     * Gets name of the currently active search.
     *
     * @return active search name or null if no search was started
     */
    public String getSearchName() {
        return decoder.getSearch();
    }

    public void addFsgSearch(String searchName, FsgModel fsgModel) {
        decoder.setFsg(searchName, fsgModel);
    }

    /**
     * Adds searches based on JSpeech grammar file.
     *
     * @param name
     *            search name
     * @param file
     *            JSGF file
     */
    public void addGrammarSearch(String name, File file) {
        //Log.ii(TAG, String.format("Load JSGF %s", file));
        decoder.setJsgfFile(name, file.getPath());
    }

    /**
     * Adds searches based on JSpeech grammar string.
     *
     * @param name
     *            search name
     * @param file
     *            JSGF string
     */
    public void addGrammarSearch(String name, String jsgfString) {
        decoder.setJsgfString(name, jsgfString);
    }

    /**
     * Adds search based on N-gram language model.
     *
     * @param name
     *            search name
     * @param file
     *            N-gram model file
     */
    public void addNgramSearch(String name, File file) {
        //Log.ii(TAG, String.format("Load N-gram model %s", file));
        decoder.setLmFile(name, file.getPath());
    }

    /**
     * Adds search based on a single phrase.
     *
     * @param name
     *            search name
     * @param phrase
     *            search phrase
     */
    public void addKeyphraseSearch(String name, String phrase) {
        decoder.setKeyphrase(name, phrase);
    }

    /**
     * Adds search based on a keyphrase file.
     *
     * @param name
     *            search name
     * @param phrase
     *            a file with search phrases, one phrase per line with optional weight in the end, for example
     *            <br/>
     *            <code>
     *            oh mighty computer /1e-20/
     *            how do you do /1e-10/
     *            </code>
     */
    public void addKeywordSearch(String name, File file) {
        decoder.setKws(name, file.getPath());
    }

    /**
     * Adds a search to look for the phonemes
     *
     * @param name
     *          search name
     * @param phonetic bigram model
     *
     */
    public void addAllphoneSearch(String name, File file) {
        decoder.setAllphoneFile(name, file.getPath());
    }

    private final class RecognizerThread extends Thread {

        private int remainingSamples;
        private int timeoutSamples;
        private final static int NO_TIMEOUT = -1;

        public RecognizerThread(int timeout) {
            if (timeout != NO_TIMEOUT)
                this.timeoutSamples = timeout * sampleRate / 1000;
            else
                this.timeoutSamples = NO_TIMEOUT;
            this.remainingSamples = this.timeoutSamples;
        }

        public RecognizerThread() {
            this(NO_TIMEOUT);
        }

        @Override
        public void run() {

            recorder.startRecording();
            if (recorder.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED) {
                recorder.stop();
                IOException ioe = new IOException(
                        "Failed to start recording. Microphone might be already in use.");
                mainHandler.post(new OnErrorEvent(ioe));
                return;
            }

            //Log.id(TAG, "Starting decoding");

            decoder.startUtt();
            short[] buffer = new short[bufferSize];
            boolean inSpeech = decoder.getInSpeech();

            // Skip the first buffer, usually zeroes
            recorder.read(buffer, 0, buffer.length);

            while (!interrupted()
                    && ((timeoutSamples == NO_TIMEOUT) || (remainingSamples > 0))) {
                int nread = recorder.read(buffer, 0, buffer.length);

                if (nread < 0) {
                    mainHandler.post(new OnErrorEvent(new RuntimeException("error reading audio buffer, nread = " + nread)));

                    return;
                } else if (nread > 0) {
                    decoder.processRaw(buffer, nread, false, false);

                    // int max = 0;
                    // for (int i = 0; i < nread; i++) {
                    //     max = Math.max(max, Math.abs(buffer[i]));
                    // }
                    // //Log.ie("!!!!!!!!", "Level: " + max);

                    if (decoder.getInSpeech() != inSpeech) {
                        inSpeech = decoder.getInSpeech();
                        mainHandler.post(new InSpeechChangeEvent(inSpeech));
                    }

                    if (inSpeech)
                        remainingSamples = timeoutSamples;

                    final Hypothesis hypothesis = decoder.hyp();
                    mainHandler.post(new ResultEvent(hypothesis, false));
                }

                if (timeoutSamples != NO_TIMEOUT) {
                    remainingSamples = remainingSamples - nread;
                }
            }

            recorder.stop();
            decoder.endUtt();

            // Remove all pending notifications.
            mainHandler.removeCallbacksAndMessages(null);

            // If we met timeout signal that speech ended
            if (timeoutSamples != NO_TIMEOUT && remainingSamples <= 0) {
                mainHandler.post(new TimeoutEvent());
            }
        }
    }

    private abstract class RecognitionEvent implements Runnable {
        public void run() {
            RecognitionListener[] emptyArray = new RecognitionListener[0];
            for (RecognitionListener listener : listeners.toArray(emptyArray))
                execute(listener);
        }

        protected abstract void execute(RecognitionListener listener);
    }

    private class InSpeechChangeEvent extends RecognitionEvent {
        private final boolean state;

        InSpeechChangeEvent(boolean state) {
            this.state = state;
        }

        @Override
        protected void execute(RecognitionListener listener) {
            if (state)
                listener.onBeginningOfSpeech();
            else
                listener.onEndOfSpeech();
        }
    }

    private class ResultEvent extends RecognitionEvent {
        protected final Hypothesis hypothesis;
        private final boolean finalResult;

        ResultEvent(Hypothesis hypothesis, boolean finalResult) {
            this.hypothesis = hypothesis;
            this.finalResult = finalResult;
        }

        @Override
        protected void execute(RecognitionListener listener) {
            if (finalResult)
                listener.onResult(hypothesis);
            else
                listener.onPartialResult(hypothesis);
        }
    }

    private class OnErrorEvent extends RecognitionEvent {
        private final Exception exception;

        OnErrorEvent(Exception exception) {
            this.exception = exception;
        }

        @Override
        protected void execute(RecognitionListener listener) {
            listener.onError(exception);
        }
    }

    private class TimeoutEvent extends RecognitionEvent {
        @Override
        protected void execute(RecognitionListener listener) {
            listener.onTimeout();
        }
    }
}