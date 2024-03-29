package com.rpkeffect.android.rpkpolyclinik.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rpkeffect.android.rpkpolyclinik.activities.SelectedClinicActivity;
import com.rpkeffect.android.rpkpolyclinik.adapters.ClinicAdapter;
import com.rpkeffect.android.rpkpolyclinik.adapters.ServiceDoctorAdapter;
import com.rpkeffect.android.rpkpolyclinik.classes.Clinic;
import com.rpkeffect.android.rpkpolyclinik.classes.Doctor;
import com.rpkeffect.android.rpkpolyclinik.R;
import com.rpkeffect.android.rpkpolyclinik.classes.ServiceDoctor;
import com.rpkeffect.android.rpkpolyclinik.classes.UserClinic;
import com.rpkeffect.android.rpkpolyclinik.interfaces.ClinicAdapterListener;
import com.rpkeffect.android.rpkpolyclinik.interfaces.SelectedClinicListener;
import com.rpkeffect.android.rpkpolyclinik.interfaces.ServiceDoctorAdapterListener;

import java.util.ArrayList;
import java.util.List;

public class ServicesFragment extends Fragment implements ServiceDoctorAdapterListener,
        ClinicAdapterListener, SelectedClinicListener {
    RecyclerView mServicesRecyclerView, mHospitalRecyclerView;
    TextInputLayout mTextInputLayout;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference mReference = database.getReference();

    EditText mEditText;
    Toolbar mToolbar;
    TextView mClinicsTextView;
    ProgressBar mClinicsProgressBar, mServicesProgressBar;

//    ServiceAdapter mServiceAdapter;
    ClinicAdapter mClinicAdapter;
    ServiceDoctorAdapter mServiceDoctorAdapter;

    List<ServiceDoctor> mServiceDoctors;
    List<Doctor> mDoctorList;
    List<Clinic> mClinicList;
    List<UserClinic> mUserClinics;

    SelectedServiceFragment mFragment;
    FragmentManager fm;

    boolean mIsSearching = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.services_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.service_search:
                mIsSearching = !mIsSearching;
                if (!mIsSearching) {
                    mTextInputLayout.setVisibility(View.GONE);

                    mClinicsTextView.setVisibility(View.VISIBLE);
                    mHospitalRecyclerView.setVisibility(View.VISIBLE);

                    mServiceDoctorAdapter = new ServiceDoctorAdapter(mServiceDoctors, mDoctorList,
                            getActivity(), ServicesFragment.this);
                    mServicesRecyclerView.setAdapter(mServiceDoctorAdapter);
                } else {
                    mClinicsTextView.setVisibility(View.GONE);
                    mHospitalRecyclerView.setVisibility(View.GONE);
                    mTextInputLayout.setVisibility(View.VISIBLE);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_services, container, false);

        fm = getActivity().getSupportFragmentManager();

        mToolbar = v.findViewById(R.id.services_tool_bar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(mToolbar);
        activity.getSupportActionBar();

        mClinicsTextView = v.findViewById(R.id.clinics_text_view);

        mClinicsProgressBar = v.findViewById(R.id.clinics_loading_pb);
        mServicesProgressBar = v.findViewById(R.id.services_loading_pb);

        mHospitalRecyclerView = (RecyclerView) v.findViewById(R.id.hospital_recycler_view);
        mHospitalRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(),
                RecyclerView.HORIZONTAL, false));

        mServicesRecyclerView = (RecyclerView) v.findViewById(R.id.services_recycler_view);
        mServicesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mTextInputLayout = v.findViewById(R.id.search_layout);
        mEditText = v.findViewById(R.id.search_et);

        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                List<ServiceDoctor> searchedServices = new ArrayList<>();
                for (ServiceDoctor service : mServiceDoctors) {
                    if (service.getName().toLowerCase()
                            .contains(mEditText.getText().toString().toLowerCase().trim())) {
                        searchedServices.add(service);
                    }
                }
                mServiceDoctorAdapter = new ServiceDoctorAdapter(searchedServices, mDoctorList,
                        getActivity(), ServicesFragment.this);
                mServicesRecyclerView.setAdapter(mServiceDoctorAdapter);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mDoctorList = new ArrayList<>();
        mServiceDoctors = new ArrayList<>();
        mServiceDoctorAdapter = new ServiceDoctorAdapter(mServiceDoctors, mDoctorList,
                getActivity(), this);
        mServicesRecyclerView.setAdapter(mServiceDoctorAdapter);

        mUserClinics = new ArrayList<>();
        mClinicList = new ArrayList<>();
        mClinicAdapter = new ClinicAdapter(mClinicList, mUserClinics, getActivity(), this);
        mHospitalRecyclerView.setAdapter(mClinicAdapter);


        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mUserClinics.clear();
                mServiceDoctors.clear();
                mDoctorList.clear();

                for (DataSnapshot dataSnapshot : snapshot.child("user_clinic").getChildren()){
                    UserClinic userClinic = dataSnapshot.getValue(UserClinic.class);
                    if (userClinic.getUserId().equals(mAuth.getUid()))
                        mUserClinics.add(userClinic);
                }
                for (DataSnapshot userSnapshot : snapshot.child("clinics_new").getChildren()) {
                    Clinic hospital = userSnapshot.getValue(Clinic.class);
                    mClinicList.add(hospital);
                }
                mClinicsProgressBar.setVisibility(View.GONE);
                mClinicAdapter.setServices(mClinicList);
                mClinicAdapter.notifyDataSetChanged();

                for (DataSnapshot dataSnapshot : snapshot.child("service_doctor").getChildren()){
                    ServiceDoctor serviceDoctor = dataSnapshot.getValue(ServiceDoctor.class);
                    mServiceDoctors.add(serviceDoctor);
                }

                for (DataSnapshot dataSnapshot : snapshot.child("employees").getChildren()){
                    Doctor doctor = dataSnapshot.getValue(Doctor.class);
                    mDoctorList.add(doctor);
                }
                mServiceDoctorAdapter.notifyDataSetChanged();
                mServicesProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return v;
    }

    //Service item click
    @Override
    public void onItemClicked(String serviceId) {
        mFragment = SelectedServiceFragment.newInstance(serviceId, this);
        fm
                .beginTransaction()
                .replace(R.id.fragment, mFragment)
                .commit();
    }

    //Clinic item click
    @Override
    public void onItemClick(String clinicId) {
        startActivity(SelectedClinicActivity.newInstance(getActivity(), clinicId));
    }

    @Override
    public void onServiceItemClick(String serviceId) {

    }

    @Override
    public void onCancelClick() {
        fm
                .beginTransaction()
                .remove(mFragment)
                .commit();
    }

    @Override
    public void onServiceOrdered() {
        fm
                .beginTransaction()
                .remove(mFragment)
                .commit();
    }
}
