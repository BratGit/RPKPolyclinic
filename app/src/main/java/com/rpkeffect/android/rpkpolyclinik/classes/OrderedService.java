package com.rpkeffect.android.rpkpolyclinik.classes;

public class OrderedService {
    int id;
    int serviceId;
    String userId;

    public OrderedService() {
    }

    public OrderedService(int serviceId, String userId) {
        this.serviceId = serviceId;
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public int getId() {
        return id;
    }

    public int getServiceId() {
        return serviceId;
    }
}
