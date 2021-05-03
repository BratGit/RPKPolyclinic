package com.rpkeffect.android.rpkpolyclinik.classes;

public class ServiceDoctor {
    String mId;
    String mName;
    String mClinicId;
    String mDoctorId;
    String mDescription;
    float mPrice;

    public ServiceDoctor() {
    }

    public ServiceDoctor(String id, String name, String clinicId, String doctorId,
                         String description, float price) {
        mId = id;
        mName = name;
        mClinicId = clinicId;
        mDoctorId = doctorId;
        mDescription = description;
        mPrice = price;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getClinicId() {
        return mClinicId;
    }

    public void setClinicId(String clinicId) {
        mClinicId = clinicId;
    }

    public String getDoctorId() {
        return mDoctorId;
    }

    public void setDoctorId(String doctorId) {
        mDoctorId = doctorId;
    }

    public float getPrice() {
        return mPrice;
    }

    public void setPrice(float price) {
        mPrice = price;
    }
}
