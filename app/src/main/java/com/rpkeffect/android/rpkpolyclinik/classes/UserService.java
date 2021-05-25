package com.rpkeffect.android.rpkpolyclinik.classes;

import java.util.Date;

public class UserService {
    private String id;
    private String userId;
    private String serviceId;
    private Date visitDate;

    public UserService() {
    }

    public UserService(String id, String userId, String serviceId, Date visitDate) {
        this.id = id;
        this.userId = userId;
        this.serviceId = serviceId;
        this.visitDate = visitDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public Date getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(Date visitDate) {
        this.visitDate = visitDate;
    }
}
