package com.rpkeffect.android.rpkpolyclinik.activities;

import android.os.Bundle;
import android.os.PersistableBundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.rpkeffect.android.rpkpolyclinik.R;

public class ClinicActivity extends AppCompatActivity {
    BottomNavigationView mBottomNavigation;
    NavController mNavController;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clinic);

        mBottomNavigation = findViewById(R.id.bottom_navigation);

        mNavController = Navigation.findNavController(this, R.id.fragment);

        NavigationUI.setupWithNavController(mBottomNavigation, mNavController);
    }
}
