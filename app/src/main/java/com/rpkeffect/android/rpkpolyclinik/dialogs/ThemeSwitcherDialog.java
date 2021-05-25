package com.rpkeffect.android.rpkpolyclinik.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.snackbar.Snackbar;
import com.rpkeffect.android.rpkpolyclinik.R;
import com.rpkeffect.android.rpkpolyclinik.utils.SaveState;

public class ThemeSwitcherDialog extends DialogFragment {
    RadioButton time, system, light, dark;
    SaveState saveState;
    View snackView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.dialog_theme_switcher, container, false);

        saveState = new SaveState(getContext());

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        root.setLayoutParams(layoutParams);

        time = root.findViewById(R.id.rb_time);
        system = root.findViewById(R.id.rb_system);
        light = root.findViewById(R.id.rb_light);
        dark = root.findViewById(R.id.rb_dark);
        snackView = root.findViewById(R.id.fragment_switcher);

        if (saveState.getState() == saveState.DARK_MODE_YES)
            dark.setChecked(true);
        else if (saveState.getState() == saveState.DARK_MODE_NO)
            light.setChecked(true);
        else if (saveState.getState() == saveState.DARK_MODE_USE_SYSTEM)
            system.setChecked(true);
        else if (saveState.getState() == saveState.DARK_MODE_TIME)
            time.setChecked(true);

        dark.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (dark.isChecked()) {
                    saveState.setState(saveState.DARK_MODE_YES);
                    switcher();
                }
            }
        });

        light.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (light.isChecked()) {
                    saveState.setState(saveState.DARK_MODE_NO);
                    switcher();
                }
            }
        });

        system.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (system.isChecked()) {
                    saveState.setState(saveState.DARK_MODE_USE_SYSTEM);
                    switcher();
                }
            }
        });

        time.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (time.isChecked()) {
                    saveState.setState(saveState.DARK_MODE_TIME);
                    switcher();
                }
            }
        });

        return root;
    }

    private void switcher(){
        switch(saveState.getState()){
            case 0://NIGHT_MODE_NO
                ((AppCompatActivity)getActivity()).getDelegate()
                        .setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case 1://NIGHT_MODE_YES
                ((AppCompatActivity)getActivity()).getDelegate()
                        .setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case 2://NIGHT_MODE_FOLLOW_SYSTEM
                Snackbar.make(snackView, "Изменения вступят в силу после перезапускка приложения",
                        Snackbar.LENGTH_SHORT).show();
                break;
            case 3://NIGHT_MODE_AUTO_TIME
                ((AppCompatActivity)getActivity()).getDelegate()
                        .setLocalNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_TIME);
                break;
        }
    }
}
