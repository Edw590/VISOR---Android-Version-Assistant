package com.edw590.visor_c_a.VoiceInteraction;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;

import com.edw590.visor_c_a.R;

/**
 * Stub activity to test out settings selection for voice interactor.
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public class SettingsActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aohd_settings);
    }
}
