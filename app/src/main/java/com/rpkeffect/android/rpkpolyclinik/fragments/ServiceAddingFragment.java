package com.rpkeffect.android.rpkpolyclinik.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

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
import com.google.firebase.storage.UploadTask;
import com.rpkeffect.android.rpkpolyclinik.R;
import com.rpkeffect.android.rpkpolyclinik.classes.ServiceDoctor;
import com.rpkeffect.android.rpkpolyclinik.interfaces.DoctorServiceListener;

import java.util.UUID;

import static android.app.Activity.RESULT_OK;

public class ServiceAddingFragment extends Fragment {
    private static final String ARG_DOCTOR_ID = "doctorId";
    private static final String ARG_SERVICE_ID = "serviceId";
    private static final String ARG_POSITION_FULL_NAME_FRAGMENT_ADD = "positionFullNameFragmentAdd";

    FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    DatabaseReference mReference = mDatabase.getReference();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseStorage mStorage = FirebaseStorage.getInstance();
    StorageReference mPhotoStorageReference;

    TextView mPositionFullNameTextView;
    EditText mName, mPrice, mDescription;
    Button mRecord, mCancel, mPhotoSelect;
    ImageView mImageView;

    String mDoctorId, mServiceId, mPositionFullName;
    Uri mImageUri;
    boolean mHasNoImage = false, mHasServiceId = false;

    DoctorServiceListener mListener;

    public void setListener(DoctorServiceListener listener) {
        mListener = listener;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            mImageUri = data.getData();
            mImageView.setImageURI(mImageUri);
            uploadUserPhoto();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_doctor_service_adding, container, false);

        Bundle args = getArguments();
        mDoctorId = args.getString(ARG_DOCTOR_ID);
        mServiceId = args.getString(ARG_SERVICE_ID);
        mPositionFullName = args.getString(ARG_POSITION_FULL_NAME_FRAGMENT_ADD);

        mPositionFullNameTextView = v.findViewById(R.id.adding_position_full_name_tv);
        mPositionFullNameTextView.setText(mPositionFullName);

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (!mHasServiceId)
            activity.getSupportActionBar().setTitle(R.string.adding_service);
        else
            activity.getSupportActionBar().setTitle(R.string.updating_service);

        mName = v.findViewById(R.id.service_name_edit_text);
        mPrice = v.findViewById(R.id.service_price_edit_text);
        mDescription = v.findViewById(R.id.service_description_edit_text);

        if (mServiceId == null) {
            mServiceId = UUID.randomUUID().toString();
        } else {
            mHasServiceId = true;
            mReference.child("service_doctor").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        ServiceDoctor serviceDoctor = dataSnapshot.getValue(ServiceDoctor.class);
                        if (serviceDoctor.getId().equals(mServiceId)) {
                            mName.setText(serviceDoctor.getName());
                            mPrice.setText(String.valueOf(serviceDoctor.getPrice()));
                            mDescription.setText(serviceDoctor.getDescription());
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        mPhotoStorageReference = mStorage.getReference(getString(R.string.service_photo_reference, mServiceId));

        mImageView = v.findViewById(R.id.service_photo_iv);

        mPhotoSelect = v.findViewById(R.id.choose_photo_button);
        mPhotoSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseUserPhoto();
            }
        });

        mCancel = v.findViewById(R.id.cancel_button);
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onServiceAdded();
            }
        });

        mRecord = v.findViewById(R.id.record_button);
        mRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mName.getText().toString().trim().isEmpty()
                        && !mPrice.getText().toString().trim().isEmpty()
                        && !mDescription.getText().toString().trim().isEmpty()
                        && !mHasServiceId) {
                    //Recording data
                    mReference.child("service_doctor").child(mServiceId)
                            .setValue(new ServiceDoctor(mServiceId,
                                    mName.getText().toString().trim(), mAuth.getUid(), mDoctorId,
                                    mDescription.getText().toString().trim(),
                                    Float.valueOf(mPrice.getText().toString())));
                    mListener.onServiceAdded();
                } else if (!mName.getText().toString().trim().isEmpty()
                        && !mPrice.getText().toString().trim().isEmpty()
                        && !mDescription.getText().toString().trim().isEmpty()
                        && mHasServiceId) {
                    //Updating data
                    mReference.child("service_doctor").child(mServiceId).setValue(
                            new ServiceDoctor(mServiceId, mName.getText().toString().trim(),
                                    mAuth.getUid(), mDoctorId, mDescription.getText().toString().trim(),
                                    Float.valueOf(mPrice.getText().toString())));
                    mListener.onServiceAdded();
                }
            }
        });

        setImage();

        return v;
    }

    private void uploadUserPhoto() {
        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Загрузка фотографии");
        progressDialog.show();

        mPhotoStorageReference.putFile(mImageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.dismiss();
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setCancelable(false);
                        builder.setTitle("Фото загружено!");
                        builder.setPositiveButton("Добро", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        builder.show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setCancelable(false);
                        builder.setMessage("Возникла ошибка при загрузке фото(");
                        builder.setPositiveButton("Ладно(", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        builder.show();
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
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mHasNoImage = true;
                        mImageView.setImageResource(R.drawable.service_green);
                    }
                });
    }

    private void chooseUserPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }

    public static ServiceAddingFragment newInstance(String doctorId, String serviceId,
                                                    String positionFullName, DoctorServiceListener listener) {
        Bundle args = new Bundle();
        args.putString(ARG_DOCTOR_ID, doctorId);
        args.putString(ARG_SERVICE_ID, serviceId);
        args.putString(ARG_POSITION_FULL_NAME_FRAGMENT_ADD, positionFullName);
        ServiceAddingFragment fragment = new ServiceAddingFragment();
        fragment.setArguments(args);
        fragment.setListener(listener);
        return fragment;
    }
}
