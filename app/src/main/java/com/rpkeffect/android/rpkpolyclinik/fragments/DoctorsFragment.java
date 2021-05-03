package com.rpkeffect.android.rpkpolyclinik.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rpkeffect.android.rpkpolyclinik.classes.Doctor;
import com.rpkeffect.android.rpkpolyclinik.R;
import com.rpkeffect.android.rpkpolyclinik.activities.RegistrationActivity;
import com.rpkeffect.android.rpkpolyclinik.adapters.DoctorAdapter;

import java.util.ArrayList;
import java.util.List;

public class DoctorsFragment extends Fragment {
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    FloatingActionButton mFloatingActionButton;
    RecyclerView mDoctorsRecyclerView;
    TextView mEmptyListTextView;
    Toolbar mToolbar;

    List<Doctor> mDoctorList;
    DoctorAdapter mAdapter;

    String mClinicId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_doctors, container, false);

        mClinicId = mAuth.getUid();

        mDoctorList = new ArrayList<>();

        mToolbar = v.findViewById(R.id.toolbar_doctors);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(mToolbar);
        activity.getSupportActionBar();


        mDoctorsRecyclerView = v.findViewById(R.id.doctors_rv);
        mDoctorsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(),
                RecyclerView.VERTICAL, false));

        mFloatingActionButton = v.findViewById(R.id.add_doctor_fab);
        mEmptyListTextView = v.findViewById(R.id.empty_list_tv);

        mAdapter = new DoctorAdapter(mDoctorList, getActivity());
        mDoctorsRecyclerView.setAdapter(mAdapter);

        DatabaseReference mReference = FirebaseDatabase.getInstance().getReference().child("employees");
        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot doctorSnapshot : snapshot.getChildren()){
                        Doctor doctor = doctorSnapshot.getValue(Doctor.class);
                        mDoctorList.add(doctor);
                    }
                    mAdapter.setDoctors(mDoctorList);
                    mAdapter.notifyDataSetChanged();
                    if (!mDoctorList.isEmpty()){
                        mEmptyListTextView.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("myLog", "onCancelled: " + error.getMessage());
            }
        });



        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mClinicId != null) {
                    startActivity(RegistrationActivity.newInstance(getActivity(),
                            true, mClinicId));
                }
            }
        });

        return v;
    }
}
