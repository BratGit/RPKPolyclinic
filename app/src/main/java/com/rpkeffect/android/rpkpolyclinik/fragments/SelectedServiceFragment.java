package com.rpkeffect.android.rpkpolyclinik.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.rpkeffect.android.rpkpolyclinik.R;
import com.rpkeffect.android.rpkpolyclinik.activities.AuthorizationActivity;
import com.rpkeffect.android.rpkpolyclinik.classes.Clinic;
import com.rpkeffect.android.rpkpolyclinik.classes.Doctor;
import com.rpkeffect.android.rpkpolyclinik.classes.ServiceDoctor;
import com.rpkeffect.android.rpkpolyclinik.classes.UserClinic;
import com.rpkeffect.android.rpkpolyclinik.classes.UserService;
import com.rpkeffect.android.rpkpolyclinik.interfaces.SelectedClinicListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class SelectedServiceFragment extends Fragment {
    private static final String ARG_SERVICE_ID_ORDER = "serviceIdOrder";

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    DatabaseReference mReference = mDatabase.getReference();
    FirebaseStorage mStorage = FirebaseStorage.getInstance();
    StorageReference mStorageReference = mStorage.getReference();

    Toolbar mToolbar;
    TextView mAddressTextView, mServiceNameTextView, mDoctorFullNameTextView, mServicePriceTextView,
            mDescriptionTextView;
    TextView mSetDateTextView, mVisitDateTextView, mIncorrectOrderTextView, mDoctorPositionTextView;
    ImageView mImageView;
    Button mOrderButton, mCancelButton;

    String mServiceId, mClinicId;
    ServiceDoctor mServiceDoctor;
    UserService mUserService;
    Doctor mDoctor;
    Clinic mClinic;
    Date mDate;
    boolean mHasNoImage = false;

    SelectedClinicListener mListener;

    public void setListener(SelectedClinicListener listener) {
        mListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_service_info, container, false);

        Bundle bundle = getArguments();
        mServiceId = bundle.getString(ARG_SERVICE_ID_ORDER);

        mToolbar = v.findViewById(R.id.toolbar_clinic_name);


        mAddressTextView = v.findViewById(R.id.clinic_address_text_view);
        mServiceNameTextView = v.findViewById(R.id.service_name_text_view);
        mDoctorFullNameTextView = v.findViewById(R.id.service_doctor_text_view);
        mServicePriceTextView = v.findViewById(R.id.service_price_text_view);
        mDescriptionTextView = v.findViewById(R.id.service_description_text_view);
        mIncorrectOrderTextView = v.findViewById(R.id.incorrect_order_tv);
        mDoctorPositionTextView = v.findViewById(R.id.service_doctor_position_text_view);

        mVisitDateTextView = v.findViewById(R.id.visit_date_tv);

        mSetDateTextView = v.findViewById(R.id.select_date_tv);
        mSetDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateTimeDialog(mVisitDateTextView);
            }
        });

        mCancelButton = v.findViewById(R.id.cancel_service_button);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onCancelClick();
            }
        });

        mOrderButton = v.findViewById(R.id.order_service_button);
        mOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDate != null) {
                    mIncorrectOrderTextView.setVisibility(View.GONE);
                    String id = UUID.randomUUID().toString();
                    mReference.child("user_service").child(id)
                            .setValue(new UserService(id, mAuth.getUid(),
                                    mServiceId, mDate))
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(getActivity(), getString(R.string.service_ordered_info,
                                            mServiceNameTextView.getText(), mVisitDateTextView.getText()),
                                            Toast.LENGTH_LONG).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            })
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    mListener.onServiceOrdered();
                                }
                            });
                } else {
                    incorrectOrderAnimation();
                }
            }
        });


        mImageView = v.findViewById(R.id.service_iv);
        mOrderButton = v.findViewById(R.id.order_service_button);

        mStorageReference = mStorage.getInstance()
                .getReference(getString(R.string.service_photo_reference, mServiceId));
        setImage();

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
                    if (clinic.getId().equals(mClinicId)) {
                        mClinic = clinic;
                        for (DataSnapshot clinicSnapshot : snapshot.child("user_clinic").getChildren()){
                            UserClinic userClinic = clinicSnapshot.getValue(UserClinic.class);
                            if (userClinic.getClinicId().equals(mClinicId)
                                    && userClinic.getUserId().equals(mAuth.getUid())
                                    && userClinic.getStatus() == UserClinic.STATUS_ACCEPT
                                    && getActivity() != null){
                                mOrderButton.setEnabled(true);
                                mOrderButton.setText(getString(R.string.order_service));
                            } else if (userClinic.getClinicId().equals(mClinicId)
                                    && userClinic.getUserId().equals(mAuth.getUid())
                                    && userClinic.getStatus() == UserClinic.STATUS_DECLINE){
                                mOrderButton.setEnabled(false);
                                mOrderButton.setText("Ваша заявка на прикрепление отклонена");
                            } else if (userClinic.getClinicId().equals(mClinicId)){
                                mOrderButton.setEnabled(false);
                                mOrderButton.setText("Вы не прикреплены к данному учреждению");
                            }
                        }
                    }
                }
                for (DataSnapshot dataSnapshot : snapshot.child("user_service").getChildren()){
                    UserService userService = dataSnapshot.getValue(UserService.class);
                    if (userService.getUserId().equals(mAuth.getUid())
                            && userService.getServiceId().equals(mServiceId)){
                        Activity activity = getActivity();
                        if (activity != null) {
                            mOrderButton.setBackgroundColor(getActivity().getColor(R.color.error));
                            mOrderButton.setTextColor(Color.WHITE);
                            mOrderButton.setText("Отменить запись");
                            mOrderButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                    builder
                                            .setTitle("Вы действительно хотите отменить запись")
                                            .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    mReference.child("user_service").child(mUserService.getId()).removeValue();
                                                    mListener.onCancelClick();
                                                }
                                            })
                                            .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            })
                                            .show();
                                }
                            });

                            mUserService = userService;
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy HH:mm");
                            mIncorrectOrderTextView.setText(getString(R.string.service_ordered_datetime,
                                    simpleDateFormat.format(userService.getVisitDate())));
                            mIncorrectOrderTextView.setVisibility(View.VISIBLE);
                            mIncorrectOrderTextView.setTextColor(Color.GREEN);
                        }
                    }
                }
                fillInServiceData(mServiceDoctor, mDoctor, mClinic);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return v;
    }

    private void showDateTimeDialog(TextView visitDateTextView) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);

                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy HH:mm");
                        visitDateTextView.setText(getString(R.string.visit_datetime,
                                simpleDateFormat.format(calendar.getTime())));
                        mDate = calendar.getTime();
                    }
                };

                new TimePickerDialog(getActivity(), timeSetListener,
                        calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE),
                        false).show();
            }
        };

        new DatePickerDialog(getActivity(), dateSetListener, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();

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

    private void incorrectOrderAnimation() {
        Animation anim = AnimationUtils.loadAnimation(
                getActivity(), R.anim.error_animation);
        mIncorrectOrderTextView.setVisibility(View.VISIBLE);
        mIncorrectOrderTextView.setText(getString(R.string.select_visit_date));
        mIncorrectOrderTextView.startAnimation(anim);
    }

    private void fillInServiceData(ServiceDoctor serviceDoctor, Doctor doctor, Clinic clinic) {
        if (getActivity() != null) {
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
    }

    public static SelectedServiceFragment newInstance(String serviceId, SelectedClinicListener listener) {
        SelectedServiceFragment fragment = new SelectedServiceFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SERVICE_ID_ORDER, serviceId);
        fragment.setArguments(args);
        fragment.setListener(listener);
        return fragment;
    }
}
