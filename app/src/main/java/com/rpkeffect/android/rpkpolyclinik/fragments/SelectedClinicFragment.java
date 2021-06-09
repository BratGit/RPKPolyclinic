package com.rpkeffect.android.rpkpolyclinik.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
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
import com.rpkeffect.android.rpkpolyclinik.adapters.ServiceDoctorAdapter;
import com.rpkeffect.android.rpkpolyclinik.classes.Clinic;
import com.rpkeffect.android.rpkpolyclinik.classes.Doctor;
import com.rpkeffect.android.rpkpolyclinik.classes.ServiceDoctor;
import com.rpkeffect.android.rpkpolyclinik.classes.UserClinic;
import com.rpkeffect.android.rpkpolyclinik.classes.UserService;
import com.rpkeffect.android.rpkpolyclinik.dialogs.MapDialog;
import com.rpkeffect.android.rpkpolyclinik.interfaces.SelectedClinicListener;
import com.rpkeffect.android.rpkpolyclinik.interfaces.ServiceDoctorAdapterListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SelectedClinicFragment extends Fragment implements ServiceDoctorAdapterListener {
    private static final String MAP_DIALOG = "mapDialog";
    private static final String ARG_CLINIC_ID = "clinicId";

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    DatabaseReference mReference = mDatabase.getReference();
    FirebaseStorage mStorage;
    StorageReference mPhotoStorageReference;

    RecyclerView mRecyclerView;
    String mClinicId, mUserClinicId;
    TextView mNameTextView, mAddressTextView, mEmailTextView, mToMapTextView, mQueryDeclinedTextView;
    ProgressBar mProgressBar;
    ImageView mImageView;
    Button mCreateQueryButton;

    List<ServiceDoctor> mServiceDoctors;
    List<Doctor> mDoctors;
    ServiceDoctorAdapter mAdapter;

    boolean mHasNoImage = false;

    SelectedClinicListener mListener;
    private LatLng mLatLng;
    private String mTitle;

    public void setListener(SelectedClinicListener listener) {
        mListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_selected_clinic, container, false);

        Bundle bundle = getArguments();
        mClinicId = bundle.getString(ARG_CLINIC_ID);

        mUserClinicId = UUID.randomUUID().toString();

        mServiceDoctors = new ArrayList<>();
        mDoctors = new ArrayList<>();

        mAdapter = new ServiceDoctorAdapter(mServiceDoctors, mDoctors, getActivity(), this);
        mRecyclerView = v.findViewById(R.id.selected_clinic_services_rv);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL,
                false));
        mRecyclerView.setAdapter(mAdapter);

        mStorage = FirebaseStorage.getInstance();
        mPhotoStorageReference = mStorage.getReference()
                .child(getString(R.string.clinic_photo_reference, mClinicId));

        mCreateQueryButton = v.findViewById(R.id.create_query_button);
        mCreateQueryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mReference.child("user_clinic").child(mUserClinicId).setValue(new UserClinic(
                        mUserClinicId, mAuth.getUid(), mClinicId, UserClinic.STATUS_SEND,
                        null)).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(getActivity(),
                                "Заявка на прикрепление отправлена",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        mQueryDeclinedTextView = v.findViewById(R.id.query_declined_tv);
        mNameTextView = v.findViewById(R.id.clinic_name_tv);
        mAddressTextView = v.findViewById(R.id.clinic_address_tv);
        mEmailTextView = v.findViewById(R.id.clinic_mail_tv);

        mImageView = v.findViewById(R.id.clinic_iv);
        mProgressBar = v.findViewById(R.id.clinic_preload_pb);

        setImage();

        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.child("clinics_new").getChildren()) {
                    Clinic clinic = dataSnapshot.getValue(Clinic.class);
                    if (clinic.getId().equals(mClinicId)) {
                        fillInClinicData(clinic);
                    }
                }
                for (DataSnapshot dataSnapshot : snapshot.child("user_clinic").getChildren()) {
                    UserClinic userClinic = dataSnapshot.getValue(UserClinic.class);
                    if (userClinic.getUserId().equals(mAuth.getUid())
                            && userClinic.getClinicId().equals(mClinicId)) {
                        fillInQueryData(userClinic);
                    }
                }

                for (DataSnapshot dataSnapshot : snapshot.child("service_doctor").getChildren()) {
                    ServiceDoctor serviceDoctor = dataSnapshot.getValue(ServiceDoctor.class);
                    if (serviceDoctor.getClinicId().equals(mClinicId)) {
                        mServiceDoctors.add(serviceDoctor);
                    }
                }
                for (DataSnapshot dataSnapshot : snapshot.child("employees").getChildren()) {
                    Doctor doctor = dataSnapshot.getValue(Doctor.class);
                    if (doctor.getClinicId().equals(mClinicId)) {
                        mDoctors.add(doctor);
                    }
                }
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mToMapTextView = v.findViewById(R.id.to_map_tv);
        mToMapTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLatLng != null && mTitle != null) {
                    MapDialog mapDialog = new MapDialog();
                    mapDialog.setMarker(mLatLng, mTitle);

                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    mapDialog.show(fm, MAP_DIALOG);
                }
            }
        });

        return v;
    }

    private void fillInClinicData(Clinic clinic) {
        mNameTextView.setText(clinic.getName());
        mEmailTextView.setText(clinic.getEmail());
        mAddressTextView.setText(clinic.getAddress());

        mLatLng = new LatLng(clinic.getLatitude(), clinic.getLongitude());
        mTitle = clinic.getAddress();
    }

    private void fillInQueryData(UserClinic userClinic) {
        if (getActivity() != null) {
            if (userClinic.getStatus() == UserClinic.STATUS_ACCEPT) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mCreateQueryButton.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.light_green));
                    mCreateQueryButton.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_green));
                }
                mQueryDeclinedTextView.setVisibility(View.GONE);
                mCreateQueryButton.setText("Вы прикреплены к данному учреждению");
                mCreateQueryButton.setEnabled(false);
            } else if (userClinic.getStatus() == UserClinic.STATUS_DECLINE) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    mCreateQueryButton.setBackgroundColor(Color.parseColor("#410625"));
//                    mCreateQueryButton.setTextColor(Color.parseColor("#B00020"));
//                }
                mQueryDeclinedTextView.setVisibility(View.VISIBLE);
                mCreateQueryButton.setText("Заявка отклонена");
                mCreateQueryButton.setEnabled(false);
                if (userClinic.getComment() != null)
                    mQueryDeclinedTextView.setText(getString(R.string.comment_decline,
                            userClinic.getComment()));
            } else if (userClinic.getStatus() == UserClinic.STATUS_SEND){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mCreateQueryButton.setBackgroundColor(Color.parseColor("#E1C741"));
                    mCreateQueryButton.setTextColor(Color.parseColor("#786712"));
                }
                mCreateQueryButton.setText("Заявка подана");
                mCreateQueryButton.setEnabled(false);
            }
        }
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

    public static SelectedClinicFragment newInstance(String clinicId, SelectedClinicListener listener) {
        SelectedClinicFragment fragment = new SelectedClinicFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_CLINIC_ID, clinicId);
        fragment.setArguments(bundle);
        fragment.setListener(listener);
        return fragment;
    }

    @Override
    public void onItemClicked(String serviceId) {
        mListener.onServiceItemClick(serviceId);
    }
}
