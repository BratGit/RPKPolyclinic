package com.rpkeffect.android.rpkpolyclinik.classes;

import java.util.Date;

public class Doctor {
    private String mUID;
    private String mEmail;
    private String mSurname;
    private String mName;
    private String mPatronymic;
    private Date mBirthDate;
    private String mClinicId;
    private String mPosition;

    public Doctor() {
    }

    public Doctor(String UID, String email, String surname, String name, String patronymic, Date birthDate, String clinicId, String position) {
        mUID = UID;
        mEmail = email;
        mSurname = surname;
        mName = name;
        mPatronymic = patronymic;
        mBirthDate = birthDate;
        mClinicId = clinicId;
        mPosition = position;
    }

    public String getPosition() {
        return mPosition;
    }

    public String getUID() {
        return mUID;
    }

    public String getEmail() {
        return mEmail;
    }

    public String getSurname() {
        return mSurname;
    }

    public String getName() {
        return mName;
    }

    public String getPatronymic() {
        return mPatronymic;
    }

    public Date getBirthDate() {
        return mBirthDate;
    }

    public String getClinicId() {
        return mClinicId;
    }

    public void setUID(String UID) {
        mUID = UID;
    }

    public void setEmail(String email) {
        mEmail = email;
    }

    public void setSurname(String surname) {
        mSurname = surname;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setPatronymic(String patronymic) {
        mPatronymic = patronymic;
    }

    public void setBirthDate(Date birthDate) {
        mBirthDate = birthDate;
    }

    public void setClinicId(String clinicId) {
        mClinicId = clinicId;
    }

    public void setPosition(String position) {
        mPosition = position;
    }
}
