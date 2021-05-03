package com.rpkeffect.android.rpkpolyclinik.classes;

public class Hospital {
    int mId;
    String mName;
    String mAddress;

    public Hospital() {
    }

    public Hospital(int id, String name, String address) {
        mId = id;
        mName = name;
        mAddress = address;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        mAddress = address;
    }
}
