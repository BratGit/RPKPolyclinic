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
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jgabrielfreitas.core.BlurImageView;
import com.rpkeffect.android.rpkpolyclinik.R;
import com.rpkeffect.android.rpkpolyclinik.activities.AuthorizationActivity;
import com.rpkeffect.android.rpkpolyclinik.classes.Clinic;
import com.rpkeffect.android.rpkpolyclinik.classes.Doctor;
import com.rpkeffect.android.rpkpolyclinik.dialogs.ImageDisplayFragment;

import java.text.SimpleDateFormat;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class DoctorInfoFragment extends Fragment {
    private static final String DIALOG_IMAGE = "ImageDisplayDialog";

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    DatabaseReference mReference = mDatabase.getReference();
    FirebaseStorage mStorage = FirebaseStorage.getInstance();
    StorageReference mStorageReference = mStorage.getReference();

    TextView mNameTextView, mEmailTextView, mBirthDateTextView, mPositionTextView, mAddressTextView,
            mClinicNameTextView, mClinicMailTextView;
    CircleImageView mDoctorPhotoImageView;
    BlurImageView mBlurImageView;
    ProgressBar mPreloadProgressBar;
    FloatingActionButton mExitFAB;

    boolean mHasNoImage = false;
    Uri mImageUri;

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            mImageUri = data.getData();
            mDoctorPhotoImageView.setImageURI(mImageUri);
            uploadUserPhoto();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_doctor, container, false);

        mEmailTextView = v.findViewById(R.id.email_text_view);
        mBirthDateTextView = v.findViewById(R.id.birth_date_text_view);
        mPositionTextView = v.findViewById(R.id.position_text_view);
        mNameTextView = v.findViewById(R.id.doctor_name_text_view);
        mAddressTextView = v.findViewById(R.id.address_text_view);
        mClinicNameTextView = v.findViewById(R.id.clinic_name_text_view);
        mClinicMailTextView = v.findViewById(R.id.clinic_mail_text_view);

        mDoctorPhotoImageView = v.findViewById(R.id.doctor_photo_iv);
        mDoctorPhotoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] choosePhotoDialogItems = {"Загрузить изображение", "Открыть изображение",
                        "Удалить изображение"};
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder
                        .setCancelable(true)
                        .setItems(choosePhotoDialogItems, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0://Сделать фотографию
                                        chooseUserPhoto();
                                        break;
                                    case 1:
                                        if (!mHasNoImage) {
                                            FragmentManager manager = getFragmentManager();
                                            ImageDisplayFragment imageDisplayFragment = new ImageDisplayFragment();
                                            imageDisplayFragment.setReferences(mStorageReference);
                                            imageDisplayFragment.show(manager, DIALOG_IMAGE);
                                        } else
                                            Snackbar.make(v, "Изображение пользователя отсутстует",
                                                    BaseTransientBottomBar.LENGTH_SHORT).show();
                                        break;
                                    case 2:
                                        deletePhoto();
                                        break;
                                }
                            }
                        })
                        .create()
                        .show();
            }
        });

        mBlurImageView = v.findViewById(R.id.blur_iv);
        mPreloadProgressBar = v.findViewById(R.id.preload_pb);

        mStorageReference = mStorage.getReference(getString(R.string.doctor_photo_reference, mAuth.getUid()));

        mReference.child("employees").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Doctor doctor = dataSnapshot.getValue(Doctor.class);
                    if (doctor.getUID().equals(mAuth.getUid())) {
                        fillInDoctorData(doctor);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mExitFAB = v.findViewById(R.id.exit_fab);
        mExitFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
//                getActivity().finish();
                Intent i = new Intent(getActivity(), AuthorizationActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.putExtra("EXIT", true);
                startActivity(i);
            }
        });

        return v;
    }

    private void deletePhoto() {
        mStorageReference = mStorage.getReference()
                .child(getString(R.string.user_photo_reference, mAuth.getUid()));
        mStorageReference.delete();
        setImage();
    }

    private void fillInDoctorData(Doctor doctor) {
        mPositionTextView.setText(doctor.getPosition());
        mNameTextView.setText(getString(R.string.full_name, doctor.getSurname(), doctor.getName(),
                doctor.getPatronymic()));
        mEmailTextView.setText(doctor.getEmail());

        SimpleDateFormat format = new SimpleDateFormat("d MMMM yyyy");
        mBirthDateTextView.setText(format.format(doctor.getBirthDate()));

        mReference.child("clinics_new").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Clinic clinic = dataSnapshot.getValue(Clinic.class);
                    if (clinic.getId().equals(doctor.getClinicId())){
                        mAddressTextView.setText(clinic.getAddress());
                        mClinicNameTextView.setText(clinic.getName());
                        mClinicMailTextView.setText(clinic.getEmail());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        setImage();
    }

    private void setImage() {
        mStorageReference.getBytes(2048 * 2048)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        mHasNoImage = false;
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        mDoctorPhotoImageView.setImageBitmap(bitmap);
                        mBlurImageView.setImageBitmap(bitmap);
                        mBlurImageView.setBlur(24);
                        mDoctorPhotoImageView.setVisibility(View.VISIBLE);
                        mPreloadProgressBar.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mHasNoImage = true;
                        mDoctorPhotoImageView.setVisibility(View.VISIBLE);
                        mDoctorPhotoImageView.setImageResource(R.drawable.user);
                        mPreloadProgressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void uploadUserPhoto() {
        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Загрузка фотографии");
        progressDialog.show();

        mStorageReference.putFile(mImageUri)
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

    private void chooseUserPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }
}
