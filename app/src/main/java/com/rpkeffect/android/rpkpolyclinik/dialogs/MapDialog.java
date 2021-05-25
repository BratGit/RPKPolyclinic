package com.rpkeffect.android.rpkpolyclinik.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.rpkeffect.android.rpkpolyclinik.R;

public class MapDialog extends DialogFragment {

    SupportMapFragment mSupportMapFragment;
    LatLng mLatLng;
    String mTitle;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_map, container, false);

        mSupportMapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map_dialog);
        mSupportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(mLatLng);
                markerOptions.title(mTitle);
                googleMap.addMarker(markerOptions);
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, 15));
            }
        });

        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        v.setLayoutParams(layoutParams);

        return v;
    }

    public void setMarker(LatLng latLng, String title) {
        mLatLng = latLng;
        mTitle = title;
    }
}
