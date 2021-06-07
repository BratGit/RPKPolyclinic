package com.rpkeffect.android.rpkpolyclinik.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.CollapsingToolbarLayout;
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
import com.rpkeffect.android.rpkpolyclinik.activities.AuthorizationActivity;
import com.rpkeffect.android.rpkpolyclinik.adapters.ServiceDoctorAdapter;
import com.rpkeffect.android.rpkpolyclinik.classes.Doctor;
import com.rpkeffect.android.rpkpolyclinik.R;
import com.rpkeffect.android.rpkpolyclinik.classes.Service;
import com.rpkeffect.android.rpkpolyclinik.classes.ServiceDoctor;
import com.rpkeffect.android.rpkpolyclinik.classes.User;
import com.rpkeffect.android.rpkpolyclinik.classes.UserService;
import com.rpkeffect.android.rpkpolyclinik.dialogs.ImageDisplayFragment;
import com.rpkeffect.android.rpkpolyclinik.interfaces.SelectedClinicListener;
import com.rpkeffect.android.rpkpolyclinik.interfaces.ServiceDoctorAdapterListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class UserFragment extends Fragment implements ServiceDoctorAdapterListener, SelectedClinicListener {
    private static final String DIALOG_IMAGE = "ImageDisplayDialog";

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    FirebaseStorage mStorage;
    StorageReference mPhotoStorageReference;
    DatabaseReference mOrderedServices = database.getReference();

    List<ServiceDoctor> mServiceDoctors;
    List<Doctor> mDoctorList;
    List<UserService> mUserServices;

    ServiceDoctorAdapter mAdapter;

    TextView mBirthDateTextView, mEmailTextView, mChangeInfoTextView;
    CollapsingToolbarLayout mCollapsingToolbarLayout;
    FloatingActionButton mExitFAB;
    RecyclerView mOrderedServicesRecyclerView;
    CircleImageView mUserPhotoImageView;
    BlurImageView mBlurImageView;
    ProgressBar mPreloadProgressBar;

    SimpleDateFormat mFormatter = new SimpleDateFormat("dd MMMM yyyy");
    String mUid;
    Uri mImageUri;
    String mUserKey;
    User mUser;

    SelectedServiceFragment mFragment;
    FragmentManager fm;

    boolean mHasNoImage = false;

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            mImageUri = data.getData();
            mUserPhotoImageView.setImageURI(mImageUri);
            uploadUserPhoto();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStorage = FirebaseStorage.getInstance();
        mPhotoStorageReference = mStorage.getReference()
                .child(getString(R.string.user_photo_reference, mAuth.getUid()));

        fm = getActivity().getSupportFragmentManager();

        mDoctorList = new ArrayList<>();
        mServiceDoctors = new ArrayList<>();
        mUserServices = new ArrayList<>();
        mAdapter = new ServiceDoctorAdapter(mServiceDoctors, mDoctorList, getActivity(), this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mOrderedServices.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.child("users").getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user.getUID().equals(mUid)) {
                        mUserKey = userSnapshot.getKey();
                        mUser = user;
                        fillInUserData(mUser);
                    }
                }

                for (DataSnapshot dataSnapshot : snapshot.child("user_service").getChildren()) {
                    UserService userService = dataSnapshot.getValue(UserService.class);
                    if (userService.getUserId().equals(mAuth.getUid())) {
                        for (DataSnapshot serviceSnapshot : snapshot.child("service_doctor").getChildren()) {
                            ServiceDoctor serviceDoctor = serviceSnapshot.getValue(ServiceDoctor.class);
                            if (serviceDoctor.getId().equals(userService.getServiceId())) {
                                mServiceDoctors.add(serviceDoctor);
                            }
                        }
                    }
                }

                for (DataSnapshot dataSnapshot : snapshot.child("employees").getChildren()) {
                    Doctor doctor = dataSnapshot.getValue(Doctor.class);
                    mDoctorList.add(doctor);
                }
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_user, container, false);



        mUid = mAuth.getUid();

        mOrderedServicesRecyclerView = v.findViewById(R.id.ordered_services_recycler_view);
        mOrderedServicesRecyclerView.setLayoutManager(new LinearLayoutManager
                (getActivity()));
        mOrderedServicesRecyclerView.setAdapter(mAdapter);

        mBlurImageView = v.findViewById(R.id.blur_iv);
        mPreloadProgressBar = v.findViewById(R.id.preload_pb);

        mChangeInfoTextView = v.findViewById(R.id.change_info_tv);
        mChangeInfoTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View dialogView = inflater.inflate(R.layout.dialog_user_info_change, container, false);
                EditText surnameET, nameET, patronymicET;
                surnameET = dialogView.findViewById(R.id.surname_et);
                nameET = dialogView.findViewById(R.id.name_et);
                patronymicET = dialogView.findViewById(R.id.patronymic_et);

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder
                        .setView(dialogView)
                        .setTitle("Редактирование информации о пользователе")
                        .setPositiveButton("Ок", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mOrderedServices.child("users").child(mUserKey).setValue(new User(mAuth.getUid(), mUser.getEmail(),
                                        TextUtils.isEmpty(surnameET.getText().toString()) ? mUser.getSurname() : surnameET.getText().toString().trim(),
                                        TextUtils.isEmpty(nameET.getText().toString()) ? mUser.getName() : nameET.getText().toString().trim(),
                                        TextUtils.isEmpty(patronymicET.getText().toString()) ? mUser.getPatronymic() : patronymicET.getText().toString().trim(),
                                        mUser.getBirthDate()));
                            }
                        })
                        .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })

                        .show();
            }
        });

        mBirthDateTextView = v.findViewById(R.id.birth_date_text_view);
        mEmailTextView = v.findViewById(R.id.email_text_view);
        mCollapsingToolbarLayout = v.findViewById(R.id.collapsing_toolbar);

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

        mUserPhotoImageView = v.findViewById(R.id.user_photo_iv);
        mUserPhotoImageView.setOnClickListener(new View.OnClickListener() {
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
                                            imageDisplayFragment.setReferences(mPhotoStorageReference);
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

        return v;
    }

    private void deletePhoto() {
        mPhotoStorageReference = mStorage.getReference()
                .child(getString(R.string.user_photo_reference, mAuth.getUid()));
        mPhotoStorageReference.delete();
        setImage();
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

    private void fillInUserData(User user) {
        if (getActivity() != null) {
            mCollapsingToolbarLayout.setTitle(getString(R.string.full_name, user.getSurname(),
                    user.getName(), user.getPatronymic()));
            mBirthDateTextView.setText(getString(R.string.birth_date_placeholder,
                    mFormatter.format(user.getBirthDate())));
            mEmailTextView.setText(user.getEmail());

            mPhotoStorageReference = mStorage.getReference()
                    .child(getString(R.string.user_photo_reference, mAuth.getUid()));
            setImage();
        }
    }

    private void setImage() {
        mPhotoStorageReference.getBytes(2048 * 2048)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        mHasNoImage = false;
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        mUserPhotoImageView.setImageBitmap(bitmap);
                        mBlurImageView.setImageBitmap(bitmap);
                        mBlurImageView.setBlur(24);
                        mUserPhotoImageView.setVisibility(View.VISIBLE);
                        mPreloadProgressBar.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mHasNoImage = true;
                        mUserPhotoImageView.setVisibility(View.VISIBLE);
                        mUserPhotoImageView.setImageResource(R.drawable.user);
                        mPreloadProgressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void chooseUserPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }

    @Override
    public void onItemClicked(String serviceId) {
        mFragment = SelectedServiceFragment.newInstance(serviceId, this);
        fm
                .beginTransaction()
                .replace(R.id.fragment, mFragment)
                .commit();
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

    }
}
