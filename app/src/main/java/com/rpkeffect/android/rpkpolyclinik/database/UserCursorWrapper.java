package com.rpkeffect.android.rpkpolyclinik.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.rpkeffect.android.rpkpolyclinik.classes.User;
import com.rpkeffect.android.rpkpolyclinik.database.UserDBSchema.UserTable;

import java.util.Date;

public class UserCursorWrapper extends CursorWrapper {
    public UserCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public User getUser() {
        String id = getString(getColumnIndex(UserTable.Cols.ID));
        String email = getString(getColumnIndex(UserTable.Cols.EMAIL));
        String surname = getString(getColumnIndex(UserTable.Cols.SURNAME));
        String name = getString(getColumnIndex(UserTable.Cols.NAME));
        String patronymic = getString(getColumnIndex(UserTable.Cols.PATRONYMIC));
        long birthDate = getLong(getColumnIndex(UserTable.Cols.BIRTHDATE));

        User user = new User();
        user.setUID(id);
        user.setEmail(email);
        user.setSurname(surname);
        user.setName(name);
        user.setPatronymic(patronymic);
        user.setBirthDate(new Date(birthDate));

        return user;
    }
}
