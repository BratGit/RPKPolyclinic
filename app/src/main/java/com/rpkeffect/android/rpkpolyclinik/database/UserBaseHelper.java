package com.rpkeffect.android.rpkpolyclinik.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.rpkeffect.android.rpkpolyclinik.database.UserDBSchema.OrderedServicesTable;
import com.rpkeffect.android.rpkpolyclinik.database.UserDBSchema.UserTable;

public class UserBaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "userBase.db";

    public UserBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + UserTable.NAME + "(" +
                UserTable.Cols.ID + " integer primary key autoincrement, " +
                UserTable.Cols.EMAIL + " text, " +
                UserTable.Cols.SURNAME + " text, " +
                UserTable.Cols.NAME + " text, " +
                UserTable.Cols.PATRONYMIC + " text, " +
                UserTable.Cols.BIRTHDATE +
                ")"
        );
        db.execSQL("create table " + OrderedServicesTable.NAME + "(" +
                OrderedServicesTable.Cols.ID + " integer primary key autoincrement, " +
                OrderedServicesTable.Cols.SERVICE_ID + " integer, " +
                OrderedServicesTable.Cols.USER_ID + " integer)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
