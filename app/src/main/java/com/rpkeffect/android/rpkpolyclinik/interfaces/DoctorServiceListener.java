package com.rpkeffect.android.rpkpolyclinik.interfaces;

public interface DoctorServiceListener {
    void onDoctorServicesAdd();
    void onServiceAdded();
    void onItemClicked(String serviceId);
}
