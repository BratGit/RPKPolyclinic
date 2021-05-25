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
import com.rpkeffect.android.rpkpolyclinik.activities.OrderedServiceActivity;
import com.rpkeffect.android.rpkpolyclinik.adapters.OrderedServiceAdapter;
import com.rpkeffect.android.rpkpolyclinik.classes.ServiceDoctor;
import com.rpkeffect.android.rpkpolyclinik.classes.User;
import com.rpkeffect.android.rpkpolyclinik.classes.UserService;
import com.rpkeffect.android.rpkpolyclinik.interfaces.ServiceClickListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DoctorOrderedServicesFragment extends Fragment implements ServiceClickListener {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    DatabaseReference mReference = mDatabase.getReference();

    RecyclerView mRecyclerView;

    OrderedServiceAdapter mAdapter;

    List<UserService> mUserServices;
    List<User> mUsers;
    List<ServiceDoctor> mServiceDoctors;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_doctor_ordered_services, container, false);

        mUserServices = new ArrayList<>();
        mUsers = new ArrayList<>();
        mServiceDoctors = new ArrayList<>();
        mAdapter = new OrderedServiceAdapter(mUserServices, mUsers, mServiceDoctors, getActivity(), this);

        mRecyclerView = v.findViewById(R.id.doctor_ordered_services_rv);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
        mRecyclerView.setAdapter(mAdapter);

        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.child("service_doctor").getChildren()){
                    ServiceDoctor serviceDoctor = dataSnapshot.getValue(ServiceDoctor.class);
                    if (serviceDoctor.getDoctorId().equals(mAuth.getUid()))
                        mServiceDoctors.add(serviceDoctor);
                }
                for (DataSnapshot dataSnapshot : snapshot.child("user_service").getChildren()){
                    UserService userService = dataSnapshot.getValue(UserService.class);
                    for (ServiceDoctor serviceDoctor : mServiceDoctors){
                        if (serviceDoctor.getId().equals(userService.getServiceId())){
                            mUserServices.add(userService);
                        }
                    }

                }
                for (DataSnapshot dataSnapshot : snapshot.child("users").getChildren()){
                    User user = dataSnapshot.getValue(User.class);
                    mUsers.add(user);
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
    public void onServiceUserClick(String userId, String serviceId, Date visitDate) {
        startActivity(OrderedServiceActivity.newIntent(getActivity(), userId, serviceId, visitDate));
    }
}
