package com.rpkeffect.android.rpkpolyclinik.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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
import com.rpkeffect.android.rpkpolyclinik.R;
import com.rpkeffect.android.rpkpolyclinik.classes.Clinic;
import com.rpkeffect.android.rpkpolyclinik.classes.Doctor;
import com.rpkeffect.android.rpkpolyclinik.classes.ServiceDoctor;
import com.rpkeffect.android.rpkpolyclinik.classes.UserService;

import java.text.SimpleDateFormat;

public class SelectedServiceActivity extends AppCompatActivity {
    public static final String ARG_SERVICE_ID = "serviceId";


    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    DatabaseReference mReference = mDatabase.getReference();
    FirebaseStorage mStorage = FirebaseStorage.getInstance();
    StorageReference mStorageReference = mStorage.getReference();


    Toolbar mToolbar;
    TextView mAddressTextView, mServiceNameTextView, mDoctorFullNameTextView, mServicePriceTextView,
            mDescriptionTextView, mDoctorPositionTextView;
    ImageView mImageView;

    String mServiceId, mClinicId;
    ServiceDoctor mServiceDoctor;
    Doctor mDoctor;
    Clinic mClinic;
    boolean mHasNoImage = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_info);

        Bundle bundle = getIntent().getExtras();
        mServiceId = bundle.getString(ARG_SERVICE_ID);

        mImageView = findViewById(R.id.service_iv);
        mStorageReference = mStorage.getInstance()
                .getReference(getString(R.string.service_photo_reference, mServiceId));
        setImage();

        mToolbar = findViewById(R.id.toolbar_clinic_name);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAddressTextView = findViewById(R.id.clinic_address_text_view);
        mServiceNameTextView = findViewById(R.id.service_name_text_view);
        mDoctorFullNameTextView = findViewById(R.id.service_doctor_text_view);
        mServicePriceTextView = findViewById(R.id.service_price_text_view);
        mDescriptionTextView = findViewById(R.id.service_description_text_view);
        mDoctorPositionTextView = findViewById(R.id.service_doctor_position_text_view);

        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.child("service_doctor").getChildren()) {
                    ServiceDoctor serviceDoctor = dataSnapshot.getValue(ServiceDoctor.class);
                    if (serviceDoctor.getId().equals(mServiceId)) {
                        mServiceDoctor = serviceDoctor;
                        mClinicId = serviceDoctor.getClinicId();
                    }
                }
                for (DataSnapshot dataSnapshot : snapshot.child("employees").getChildren()) {
                    Doctor doctor = dataSnapshot.getValue(Doctor.class);
                    if (mServiceDoctor.getDoctorId().equals(doctor.getUID())) {
                        mDoctor = doctor;
                    }
                }
                for (DataSnapshot dataSnapshot : snapshot.child("clinics_new").getChildren()) {
                    Clinic clinic = dataSnapshot.getValue(Clinic.class);
                    if (clinic.getId().equals(mClinicId))
                        mClinic = clinic;
                }

                fillInServiceData(mServiceDoctor, mDoctor, mClinic);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setImage() {
        mStorageReference.getBytes(2048 * 2048)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        mHasNoImage = false;
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        mImageView.setImageBitmap(bitmap);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mHasNoImage = true;
                        mImageView.setImageResource(R.drawable.ic_service);
                    }
                });
    }

    private void fillInServiceData(ServiceDoctor serviceDoctor, Doctor doctor, Clinic clinic) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mToolbar.setTitle(clinic.getName());
        }
        mAddressTextView.setText(clinic.getAddress());
        mServiceNameTextView.setText(serviceDoctor.getName());
        mDoctorFullNameTextView.setText(getString(R.string.full_name, doctor.getSurname(),
                doctor.getName(), doctor.getPatronymic()));
        mDoctorPositionTextView.setText(doctor.getPosition());
        mServicePriceTextView.setText(String.valueOf(serviceDoctor.getPrice()));
        mDescriptionTextView.setText(serviceDoctor.getDescription());
    }

    public static Intent newIntent(Context context, String serviceId) {
        Intent intent = new Intent(context, SelectedServiceActivity.class);
        intent.putExtra(ARG_SERVICE_ID, serviceId);
        return intent;
    }
}
