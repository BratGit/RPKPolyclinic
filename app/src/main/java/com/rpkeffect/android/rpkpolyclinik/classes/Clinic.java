package com.rpkeffect.android.rpkpolyclinik.classes;

public class Clinic {
    String id;
    String name;
    String address;
    String email;
    double latitude;
    double longitude;

    public Clinic() {
    }

    public Clinic(String id, String name, String address, String email, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.email = email;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getEmail() {
        return email;
    }
}
