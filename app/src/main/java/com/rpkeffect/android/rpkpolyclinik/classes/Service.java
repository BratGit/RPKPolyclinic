package com.rpkeffect.android.rpkpolyclinik.classes;

public class Service {
    int id;
    int clinicId;
    String name;
    float price;

    public Service() {
    }

    public Service(int id, int clinicId, String name, float price) {
        this.id = id;
        this.clinicId = clinicId;
        this.name = name;
        this.price = price;
    }

    public int getId() {
        return id;
    }

    public int getClinicId() {
        return clinicId;
    }

    public String getName() {
        return name;
    }

    public float getPrice() {
        return price;
    }
}
