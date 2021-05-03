package com.rpkeffect.android.rpkpolyclinik.classes;

import java.util.Date;

public class User {
    private String mUID;
    private String mEmail;
    private String mSurname;
    private String mName;
    private String mPatronymic;
    private Date mBirthDate;

    public User() {
    }

    public User(String UID, String email, String surname, String name,
                String patronymic, Date birthDate) {
        mUID = UID;
        mEmail = email;
        mSurname = surname;
        mName = name;
        mPatronymic = patronymic;
        mBirthDate = birthDate;
    }

    public String getUID() {
        return mUID;
    }

    public void setUID(String UID) {
        mUID = UID;
    }

    public Date getBirthDate() {
        return mBirthDate;
    }

    public void setBirthDate(Date birthDate) {
        mBirthDate = birthDate;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        this.mEmail = email;
    }

    public String getSurname() {
        return mSurname;
    }

    public void setSurname(String surname) {
        this.mSurname = surname;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getPatronymic() {
        return mPatronymic;
    }

    public void setPatronymic(String patronymic) {
        this.mPatronymic = patronymic;
    }
}
