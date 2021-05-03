package com.rpkeffect.android.rpkpolyclinik.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rpkeffect.android.rpkpolyclinik.classes.Hospital;
import com.rpkeffect.android.rpkpolyclinik.R;
import com.rpkeffect.android.rpkpolyclinik.classes.Service;
import com.rpkeffect.android.rpkpolyclinik.adapters.ServiceAdapter;

import java.util.ArrayList;
import java.util.List;

public class SelectedHospitalActivity extends AppCompatActivity {
    private static final String ARG_HOSPITAL_ID = "ARG_HOSPITAL_ID";

    Toolbar mToolbar;
    RecyclerView mRecyclerView;
    TextView mAddressTextView;

    ServiceAdapter mServiceAdapter;
    List<Service> mServiceList;
    int mHospitalId;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference mDatabaseReference = database.getReference();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_hospital);

        Bundle bundle = getIntent().getExtras();
        mHospitalId = bundle.getInt(ARG_HOSPITAL_ID);

        mAddressTextView = findViewById(R.id.selected_hospital_address_tv);

        mToolbar = findViewById(R.id.selected_hospital_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRecyclerView = findViewById(R.id.selected_hospital_rv);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mServiceList = new ArrayList<>();
        mServiceAdapter = new ServiceAdapter(mServiceList, this);
        mRecyclerView.setAdapter(mServiceAdapter);

        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.child("clinics").getChildren()) {
                    Hospital hospital = userSnapshot.getValue(Hospital.class);
                    if (hospital.getId() == mHospitalId) {
                        mToolbar.setTitle(hospital.getName());
                        mAddressTextView.setText(hospital.getAddress());
                    }
                }
                for (DataSnapshot userSnapshot : snapshot.child("services").getChildren()) {
                    Service service = userSnapshot.getValue(Service.class);
                    if (service.getClinicId() == mHospitalId) {
                        mServiceList.add(service);
                        mServiceAdapter.setServices(mServiceList);
                        mServiceAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public static Intent newIntent(Context context, int hospitalId) {
        Intent intent = new Intent(context, SelectedHospitalActivity.class);
        intent.putExtra(ARG_HOSPITAL_ID, hospitalId);
        return intent;
    }
}
