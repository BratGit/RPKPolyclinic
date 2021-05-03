package com.rpkeffect.android.rpkpolyclinik.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
import com.rpkeffect.android.rpkpolyclinik.R;
import com.rpkeffect.android.rpkpolyclinik.adapters.ServiceDoctorAdapter;
import com.rpkeffect.android.rpkpolyclinik.classes.Doctor;
import com.rpkeffect.android.rpkpolyclinik.classes.ServiceDoctor;
import com.rpkeffect.android.rpkpolyclinik.interfaces.DoctorServiceListener;
import com.rpkeffect.android.rpkpolyclinik.interfaces.ServiceDoctorAdapterListener;

import java.util.ArrayList;
import java.util.List;

public class DoctorServicesFragment extends Fragment implements ServiceDoctorAdapterListener {
    private static final String ARG_DOCTOR_ID = "doctorId";
    private static final String ARG_POSITION_FULL_NAME = "ARG_POSITION_FULL_NAME_FRAGMENT";

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    DatabaseReference mReference = mDatabase.getReference();

    RecyclerView mRecyclerView;
    FloatingActionButton mFloatingActionButton;
    TextView mPositionFullNameTextView;

    List<Doctor> mDoctorList;
    List<ServiceDoctor> mList;
    ServiceDoctorAdapter mAdapter;

    String mDoctorId, mPositionFullName;

    DoctorServiceListener mListener;
    public void setListener(DoctorServiceListener listener) {
        mListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_doctor_services, container, false);

        Bundle args = getArguments();
        mDoctorId = args.getString(ARG_DOCTOR_ID);
        mPositionFullName = args.getString(ARG_POSITION_FULL_NAME);

        mList = new ArrayList<>();
        mDoctorList = new ArrayList<>();
        mAdapter = new ServiceDoctorAdapter(mList, mDoctorList, getActivity(), this);

        mPositionFullNameTextView = v.findViewById(R.id.position_full_name_tv);
        mPositionFullNameTextView.setText(mPositionFullName);

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setTitle(getString(R.string.doctor_services));

        mRecyclerView = v.findViewById(R.id.doctor_services_rv);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL,
                false));
        mRecyclerView.setAdapter(mAdapter);

        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.child("employees").getChildren()){
                    Doctor doctor = dataSnapshot.getValue(Doctor.class);
                    if (doctor.getClinicId().equals(mAuth.getUid())
                            && doctor.getUID().equals(mDoctorId))
                        mDoctorList.add(doctor);
                }

                for (DataSnapshot dataSnapshot : snapshot.child("service_doctor").getChildren()){
                    ServiceDoctor serviceDoctor = dataSnapshot.getValue(ServiceDoctor.class);
                    if (serviceDoctor.getClinicId().equals(mAuth.getUid())
                            && serviceDoctor.getDoctorId().equals(mDoctorId))
                        mList.add(serviceDoctor);
                }
                mAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mFloatingActionButton = v.findViewById(R.id.doctor_service_add_fab);
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onDoctorServicesAdd();
            }
        });

        return v;
    }

    public static DoctorServicesFragment newInstance(String doctorId, String positionFullName,
                                                     DoctorServiceListener listener) {
        Bundle args = new Bundle();
        args.putString(ARG_DOCTOR_ID, doctorId);
        args.putString(ARG_POSITION_FULL_NAME, positionFullName);
        DoctorServicesFragment fragment = new DoctorServicesFragment();
        fragment.setArguments(args);
        fragment.setListener(listener);
        return fragment;
    }

    @Override
    public void onItemClicked(String serviceId) {
        mListener.onItemClicked(serviceId);
    }
}
