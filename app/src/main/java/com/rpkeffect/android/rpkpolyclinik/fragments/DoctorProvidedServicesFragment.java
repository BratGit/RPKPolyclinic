package com.rpkeffect.android.rpkpolyclinik.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rpkeffect.android.rpkpolyclinik.R;
import com.rpkeffect.android.rpkpolyclinik.activities.SelectedServiceActivity;
import com.rpkeffect.android.rpkpolyclinik.adapters.ServiceDoctorAdapter;
import com.rpkeffect.android.rpkpolyclinik.classes.Doctor;
import com.rpkeffect.android.rpkpolyclinik.classes.ServiceDoctor;
import com.rpkeffect.android.rpkpolyclinik.interfaces.ServiceDoctorAdapterListener;

import java.util.ArrayList;
import java.util.List;

public class DoctorProvidedServicesFragment extends Fragment implements ServiceDoctorAdapterListener {

    RecyclerView mRecyclerView;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    DatabaseReference mReference = mDatabase.getReference();

    List<Doctor> mDoctorList;
    List<ServiceDoctor> mList;
    ServiceDoctorAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_doctor_provided_services, container, false);

        mDoctorList = new ArrayList<>();
        mList = new ArrayList<>();
        mAdapter = new ServiceDoctorAdapter(mList, mDoctorList, getActivity(), this);

        mRecyclerView = v.findViewById(R.id.doctor_provided_services_rv);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
        mRecyclerView.setAdapter(mAdapter);

        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.child("employees").getChildren()){
                    Doctor doctor = dataSnapshot.getValue(Doctor.class);
                    if (doctor.getUID().equals(mAuth.getUid())) {
                        mDoctorList.add(doctor);
                        Log.d("myLog", "onDataChange: " + doctor.getEmail());
                    }
                }

                for (DataSnapshot dataSnapshot : snapshot.child("service_doctor").getChildren()){
                    ServiceDoctor serviceDoctor = dataSnapshot.getValue(ServiceDoctor.class);
                    if (serviceDoctor.getDoctorId().equals(mAuth.getUid())) {
                        mList.add(serviceDoctor);
                        Log.d("myLog", "onDataChange: " + serviceDoctor.getName());
                    }
                }
                mAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return v;
    }

    @Override
    public void onItemClicked(String serviceId) {
        startActivity(SelectedServiceActivity.newIntent(getActivity(), serviceId));
    }
}
