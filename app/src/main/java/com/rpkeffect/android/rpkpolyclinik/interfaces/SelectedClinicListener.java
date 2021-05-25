package com.rpkeffect.android.rpkpolyclinik.interfaces;

public interface SelectedClinicListener {
    void onServiceItemClick(String serviceId);
    void onCancelClick();
    void onServiceOrdered();
}
