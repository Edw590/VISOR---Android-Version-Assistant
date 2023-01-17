// Note: this file was modified by me, DADi590, in 2022.

package edu.cmu.pocketsphinx1;

import java.io.File;
import java.io.IOException;

import edu.cmu.pocketsphinx.Config;
import edu.cmu.pocketsphinx.Decoder;

/**
 * Wrapper for the decoder configuration to implement builder pattern.
 * Configures most important properties of the decoder
 */
public class SpeechRecognizerSetup {

    static {
        System.loadLibrary("pocketsphinx_jni");
    }

    private final Config config;

    /**
     * Creates new speech recognizer builder with default configuration.
     */
    public static SpeechRecognizerSetup defaultSetup() {
        return new SpeechRecognizerSetup(Decoder.defaultConfig());
    }

    /**
     * Creates new speech recognizer builder from configuration file.
     * Configuration file should consist of lines containing key-value pairs.
     *
     * @param configFile
     *            configuration file
     */
    public static SpeechRecognizerSetup setupFromFile(File configFile) {
        return new SpeechRecognizerSetup(Decoder.fileConfig(configFile.getPath()));
    }

    private SpeechRecognizerSetup(Config config) {
        this.config = config;
    }

    public SpeechRecognizer getRecognizer() throws IOException {
        return new SpeechRecognizer(config);
    }

    public SpeechRecognizerSetup setAcousticModel(File model) {
        return setString("-hmm", model.getPath());
    }

    public SpeechRecognizerSetup setDictionary(File dictionary) {
        return setString("-dict", dictionary.getPath());
    }

    public SpeechRecognizerSetup setSampleRate(int rate) {
        return setFloat("-samprate", rate);
    }

    public SpeechRecognizerSetup setRawLogDir(File dir) {
        return setString("-rawlogdir", dir.getPath());
    }

    public SpeechRecognizerSetup setKeywordThreshold(float threshold) {
        return setFloat("-kws_threshold", threshold);
    }

    public SpeechRecognizerSetup setBoolean(String key, boolean value) {
        config.setBoolean(key, value);
        return this;
    }

    public SpeechRecognizerSetup setInteger(String key, int value) {
        config.setInt(key, value);
        return this;
    }

    public SpeechRecognizerSetup setFloat(String key, double value) {
        config.setFloat(key, value);
        return this;
    }

    public SpeechRecognizerSetup setString(String key, String value) {
        config.setString(key, value);
        return this;
    }
}
