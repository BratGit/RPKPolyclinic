package com.rpkeffect.android.rpkpolyclinik.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SaveState {
    Context context;
    SharedPreferences sharedPreferences;

    public final int DARK_MODE_NO = 0;
    public final int DARK_MODE_YES = 1;
    public final int DARK_MODE_USE_SYSTEM = 2;
    public final int DARK_MODE_TIME = 3;

    public SaveState(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE);
    }

    public void setState(int mode){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("dark_mode", mode);
        editor.apply();
    }

    public int getState(){
        return sharedPreferences.getInt("dark_mode", DARK_MODE_USE_SYSTEM);
    }
}
