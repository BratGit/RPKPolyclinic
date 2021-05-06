package com.rpkeffect.android.rpkpolyclinik.classes;

import android.preference.PreferenceFragment;

public class UserClinic {
    public static final int STATUS_SEND = 0;
    public static final int STATUS_ACCEPT = 1;
    public static final int STATUS_DECLINE = 2;

    String id;
    String userId;
    String clinicId;
    int status;
    String comment;

    public UserClinic() {
    }

    public UserClinic(String id, String userId, String clinicId, int status, String comment) {
        this.id = id;
        this.userId = userId;
        this.clinicId = clinicId;
        this.status = status;
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getClinicId() {
        return clinicId;
    }

    public int getStatus() {
        return status;
    }
}
