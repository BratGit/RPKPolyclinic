package com.rpkeffect.android.rpkpolyclinik.fragments;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.CollapsingToolbarLayout;
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
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;
import com.rpkeffect.android.rpkpolyclinik.R;
import com.rpkeffect.android.rpkpolyclinik.classes.Clinic;

import static android.app.Activity.RESULT_OK;

public class ClinicFragment extends Fragment {
    private static final String DIALOG_IMAGE_CLINIC = "dialogImageClinic";

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    DatabaseReference mReference = mDatabase.getReference();
    FirebaseStorage mStorage;
    StorageReference mPhotoStorageReference;

    SupportMapFragment mSupportMapFragment;
    GoogleMap mGoogleMap;

    TextView mEmailTextView, mAddressTextView, mNameTextView;
    ImageView mClinicPhotoImageView;
    ProgressBar mProgressBar;
    SpeedDialView mSpeedDialView;

    LatLng mLatLng;
    Uri mImageUri;
    boolean mHasNoImage = false;

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            mImageUri = data.getData();
            mClinicPhotoImageView.setImageURI(mImageUri);
            uploadUserPhoto();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStorage = FirebaseStorage.getInstance();
        mPhotoStorageReference = mStorage.getReference()
                .child(getString(R.string.clinic_photo_reference, mAuth.getUid()));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_clinic, container, false);

        mAddressTextView = v.findViewById(R.id.clinic_address_tv);
        mEmailTextView = v.findViewById(R.id.clinic_mail_tv);
        mNameTextView = v.findViewById(R.id.clinic_name_tv);

        mProgressBar = v.findViewById(R.id.clinic_preload_pb);
        mClinicPhotoImageView = v.findViewById(R.id.clinic_iv);
        setImage();

        mSpeedDialView = v.findViewById(R.id.clinic_speed_dial);
        mSpeedDialView.inflate(R.menu.clinic_speed_dial_menu);
        mSpeedDialView.setOnActionSelectedListener(new SpeedDialView.OnActionSelectedListener() {
            @Override
            public boolean onActionSelected(SpeedDialActionItem actionItem) {
                switch (actionItem.getId()){
                    case R.id.photo_action_item:
                        String[] choosePhotoDialogItems = {"Загрузить изображение", "Открыть изображение",
                                "Удалить изображение"};

                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                        alertDialog
                                .setCancelable(true)
                                .setItems(choosePhotoDialogItems, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which){
                                            case 0: //Загрузить фотографию
                                                chooseClinicPhoto();
                                                break;
                                            case 1:
                                                if (!mHasNoImage) {
                                                    FragmentManager manager = getFragmentManager();
                                                    ImageDisplayFragment imageDisplayFragment = new ImageDisplayFragment();
                                                    imageDisplayFragment.setReferences(mPhotoStorageReference);
                                                    imageDisplayFragment.show(manager, DIALOG_IMAGE_CLINIC);
                                                } else
                                                    Snackbar.make(v, "Изображение пользователя отсутстует",
                                                            BaseTransientBottomBar.LENGTH_SHORT).show();
                                                break;
                                            case 2:
                                                deletePhoto();
                                                break;
                                        }
                                    }
                                });
                        alertDialog.create();
                        alertDialog.show();
                        return false;
                    case R.id.exit_item:
                        mAuth.signOut();
                        getActivity().finish();
                        return false;
                    default:
                        return false;
                }
            }
        });

        mReference.child("clinics_new").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Clinic clinic = dataSnapshot.getValue(Clinic.class);
                    if (clinic.getId().equals(mAuth.getUid())){
                        mNameTextView.setText(clinic.getName());
                        mEmailTextView.setText(clinic.getEmail());
                        mAddressTextView.setText(clinic.getAddress());
                        mLatLng = new LatLng(clinic.getLatitude(),
                                clinic.getLongitude());
                        if (mGoogleMap != null){
                            setMarker(mLatLng, mGoogleMap);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mSupportMapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.mapView);
        mSupportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                mGoogleMap = googleMap;
                if (mLatLng != null){
                    setMarker(mLatLng, googleMap);
                }
            }
        });

        return v;
    }

    private void setMarker(LatLng latLng, GoogleMap googleMap){
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions
                .position(mLatLng)
                .title(mAddressTextView.getText().toString().trim());
        googleMap.clear();
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        googleMap.addMarker(markerOptions);
    }

    private void chooseClinicPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }

    private void deletePhoto() {
        mPhotoStorageReference = mStorage.getReference()
                .child(getString(R.string.clinic_photo_reference, mAuth.getUid()));
        mPhotoStorageReference.delete();
        setImage();
    }

    private void setImage() {
        mPhotoStorageReference.getBytes(2048 * 2048)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        mHasNoImage = false;
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        mClinicPhotoImageView.setImageBitmap(bitmap);
                        mProgressBar.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mHasNoImage = true;
                        mClinicPhotoImageView.setImageResource(R.drawable.ic_clinic);
                        mProgressBar.setVisibility(View.GONE);
                    }
                });
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
                        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
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
                        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
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
}
