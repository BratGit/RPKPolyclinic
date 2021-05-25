package com.rpkeffect.android.rpkpolyclinik.interfaces;

import java.util.Date;

public interface ServiceClickListener {
    void onServiceUserClick(String userId, String serviceId, Date visitDate);
}
