package com.rpkeffect.android.rpkpolyclinik.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;
import com.rpkeffect.android.rpkpolyclinik.R;
import com.rpkeffect.android.rpkpolyclinik.classes.Clinic;
import com.rpkeffect.android.rpkpolyclinik.classes.UserClinic;

import java.util.UUID;

public class SelectedClinicActivity extends AppCompatActivity {
    private static final String ARG_CLINIC_ID = "clinicId";

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    DatabaseReference mReference = mDatabase.getReference();
    FirebaseStorage mStorage;
    StorageReference mPhotoStorageReference;

    SupportMapFragment mSupportMapFragment;

    String mClinicId, mUserClinicId;
    SpeedDialView mSpeedDialView;
    TextView mNameTextView, mAddressTextView, mEmailTextView;
    ProgressBar mProgressBar;
    ImageView mImageView;

    LatLng mLatLng;
    GoogleMap mGoogleMap;

    boolean mHasNoImage = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_clinic);

        Bundle bundle = getIntent().getExtras();
        mClinicId = bundle.getString(ARG_CLINIC_ID);

        mUserClinicId = UUID.randomUUID().toString();

        mStorage = FirebaseStorage.getInstance();
        mPhotoStorageReference = mStorage.getReference()
                .child(getString(R.string.clinic_photo_reference, mClinicId));

        mSpeedDialView = findViewById(R.id.clinic_speed_dial);
        mSpeedDialView.inflate(R.menu.selected_clinic_speed_dial_menu);
        mSpeedDialView.setOnActionSelectedListener(new SpeedDialView.OnActionSelectedListener() {
            @Override
            public boolean onActionSelected(SpeedDialActionItem actionItem) {
                switch (actionItem.getId()){
                    case R.id.record:
                        mReference.child("user_clinic").child(mUserClinicId).setValue(new UserClinic(
                                mUserClinicId, mAuth.getUid(), mClinicId, UserClinic.STATUS_SEND,
                                null)).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(SelectedClinicActivity.this,
                                        "Заявка на прикрепление отправлена",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                        return false;
                    default:
                        return false;
                }
            }
        });

        mNameTextView = findViewById(R.id.clinic_name_tv);
        mAddressTextView = findViewById(R.id.clinic_address_tv);
        mEmailTextView = findViewById(R.id.clinic_mail_tv);

        mImageView = findViewById(R.id.clinic_iv);
        mProgressBar = findViewById(R.id.clinic_preload_pb);

        setImage();

        mReference.child("clinics_new").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Clinic clinic = dataSnapshot.getValue(Clinic.class);
                    if (clinic.getId().equals(mClinicId)){
                        mNameTextView.setText(clinic.getName());
                        mEmailTextView.setText(clinic.getEmail());
                        mAddressTextView.setText(clinic.getAddress());
                        mLatLng = new LatLng(clinic.getLatitude(),
                                clinic.getLongitude());
                        if (mGoogleMap != null){
                            setMarker(mLatLng, mGoogleMap);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mSupportMapFragment = (SupportMapFragment)  getSupportFragmentManager()
                .findFragmentById(R.id.mapView);
        mSupportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                mGoogleMap = googleMap;
                if (mLatLng != null){
                    setMarker(mLatLng, googleMap);
                }
            }
        });
    }

    private void setImage() {
        mPhotoStorageReference.getBytes(2048 * 2048)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        mHasNoImage = false;
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        mImageView.setImageBitmap(bitmap);
                        mProgressBar.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mHasNoImage = true;
                        mImageView.setImageResource(R.drawable.ic_clinic);
                        mProgressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void setMarker(LatLng latLng, GoogleMap googleMap){
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions
                .position(mLatLng)
                .title(mAddressTextView.getText().toString().trim());
        googleMap.clear();
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        googleMap.addMarker(markerOptions);
    }

    public static Intent newInstance(Context context, String clinicId){
        Intent intent = new Intent(context, SelectedClinicActivity.class);
        intent.putExtra(ARG_CLINIC_ID, clinicId);
        return intent;
    }
}
