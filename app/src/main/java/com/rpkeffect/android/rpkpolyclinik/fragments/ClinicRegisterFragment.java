package com.rpkeffect.android.rpkpolyclinik.fragments;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rpkeffect.android.rpkpolyclinik.classes.Clinic;
import com.rpkeffect.android.rpkpolyclinik.R;
import com.rpkeffect.android.rpkpolyclinik.interfaces.UserRegisteredListener;

import java.util.List;
import java.util.Locale;

public class ClinicRegisterFragment extends Fragment {
    private static final String ARG_MAIL = "mail";
    private static final String ARG_PASSWORD = "password";

    FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    DatabaseReference mReference;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    SupportMapFragment mSupportMapFragment;

    EditText mClinicNameEditText;
    Button mRegisterButton;

    UserRegisteredListener mListener;

    double mLatitude, mLongitude;
    String mAddress, mMail, mPassword;
    LatLng mLatLng;

    public void setListener(UserRegisteredListener listener) {
        mListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mMail = args.getString(ARG_MAIL);
        mPassword = args.getString(ARG_PASSWORD);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_clinic_registration, container, false);

        mReference = mDatabase.getReference("clinics_new");

        mClinicNameEditText = v.findViewById(R.id.email);

        mRegisterButton = v.findViewById(R.id.clinic_register_button);
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(mClinicNameEditText.getText().toString())
                        && mLatitude != 0.0f && mLongitude != 0.0f && mAddress != null) {
                    mAuth
                            .createUserWithEmailAndPassword(mMail, mPassword)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        mReference.push().setValue(new Clinic(task.getResult().getUser().getUid(),
                                                mClinicNameEditText.getText().toString().trim(), mAddress, mMail,
                                                mLatLng.latitude, mLatLng.longitude))
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        mListener.onUserRegistered();
                                                    }
                                                });
                                    } else {
                                        Toast.makeText(getActivity(),
                                                "Возникла ошибка при ргеистрации",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

        mSupportMapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.mapView);
        mSupportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(@NonNull LatLng latLng) {
                        mAddress = getAddress(latLng.latitude, latLng.longitude);
                        mLatLng = latLng;
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions
                                .position(latLng)
                                .title(mAddress);
                        googleMap.clear();
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                        googleMap.addMarker(markerOptions);
                        mLatitude = markerOptions.getPosition().latitude;
                        mLongitude = markerOptions.getPosition().longitude;
                    }
                });
            }
        });

        return v;
    }

    public static ClinicRegisterFragment getInstance(String email, String password,
                                                     UserRegisteredListener listener) {
        ClinicRegisterFragment fragment = new ClinicRegisterFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MAIL, email);
        args.putString(ARG_PASSWORD, password);
        fragment.setArguments(args);
        fragment.setListener(listener);
        return fragment;
    }

    private String getAddress(double latitude, double longitude) {
        String address = "";

        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());

        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (address != null) {
                Address returnAddress = addresses.get(0);
//                StringBuilder stringBuilder = new StringBuilder("");
//
//                for (int i = 0; i < returnAddress.getMaxAddressLineIndex(); i++){
//                    stringBuilder.append(returnAddress.getAddressLine(i)).append("\n");
//
//                    address = stringBuilder.toString();
//                }
                address = addresses.get(0).getAddressLine(0);
            } else {
                Toast.makeText(getActivity(), "Адрес не найден", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        Toast.makeText(getActivity(), address, Toast.LENGTH_SHORT).show();
        return address;
    }
}
