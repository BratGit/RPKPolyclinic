package com.rpkeffect.android.rpkpolyclinik.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.rpkeffect.android.rpkpolyclinik.utils.SaveState;

public class SplashScreen extends AppCompatActivity {
    SaveState saveState;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        saveState = new SaveState(this);
        if (saveState.getState() == saveState.DARK_MODE_YES)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        else if (saveState.getState() == saveState.DARK_MODE_NO)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        else if (saveState.getState() == saveState.DARK_MODE_USE_SYSTEM)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        else if (saveState.getState() == saveState.DARK_MODE_TIME)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_TIME);
        super.onCreate(savedInstanceState);
        startActivity(new Intent(this, AuthorizationActivity.class));
        finish();
    }
}
