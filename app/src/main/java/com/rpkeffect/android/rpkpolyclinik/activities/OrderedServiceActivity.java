package com.rpkeffect.android.rpkpolyclinik.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.rpkeffect.android.rpkpolyclinik.R;
import com.rpkeffect.android.rpkpolyclinik.classes.ServiceDoctor;
import com.rpkeffect.android.rpkpolyclinik.classes.User;
import com.rpkeffect.android.rpkpolyclinik.classes.UserService;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class OrderedServiceActivity extends AppCompatActivity {
    private static final String ARG_USER_ID = "userId";
    private static final String ARG_SERVICE_ID = "serviceId";
    private static final String ARG_VISIT_DATE = "visitDate";

    FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    DatabaseReference mReference = mDatabase.getReference();
    FirebaseStorage mStorage = FirebaseStorage.getInstance();
    StorageReference mUserPhotoStorageReference = mStorage.getReference();
    StorageReference mServicePhotoStorageReference = mStorage.getReference();

    TextView mClientFullNameTextView, mServiceNameTextView, mVisitDateTextView, mServiceDescriptionTextView;
    CircleImageView mUserPhotoCircleImageView;
    ImageView mServicePhotoImageView;
    Button mBackButton, mServiceProvidedButton;

    String mUserId, mServiceId;
    Date mVisitDate;
    UserService mUserService;

    boolean mUserHasNoPhoto = false, mServiceHasNoPhoto = false;

    SimpleDateFormat mFormatter = new SimpleDateFormat("dd MMMM yyyy HH:mm");

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ordered_service);

        Bundle bundle = getIntent().getExtras();
        mUserId = bundle.getString(ARG_USER_ID);
        mServiceId = bundle.getString(ARG_SERVICE_ID);
        mVisitDate = (Date) bundle.getSerializable(ARG_VISIT_DATE);

        mUserPhotoStorageReference = mStorage.getReference(getString(R.string.user_photo_reference, mUserId));
        mServicePhotoStorageReference = mStorage.getReference(getString(R.string.service_photo_reference, mServiceId));

        mClientFullNameTextView = findViewById(R.id.client_full_name_tv);
        mServiceNameTextView = findViewById(R.id.service_name_tv);

        mVisitDateTextView = findViewById(R.id.service_datetime_tv);
        mVisitDateTextView.setText(mFormatter.format(mVisitDate));

        mServiceDescriptionTextView = findViewById(R.id.service_description_tv);

        mUserPhotoCircleImageView = findViewById(R.id.user_photo_iv);
        mServicePhotoImageView = findViewById(R.id.service_photo_iv);

        mServiceProvidedButton = findViewById(R.id.service_provided_button);
        mServiceProvidedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUserService != null){
                    mReference.child("user_service").child(mUserService.getId()).removeValue();
                    finish();
                }
            }
        });

        setServiceImage();
        setUserImage();

        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.child("users").getChildren()){
                    User user = dataSnapshot.getValue(User.class);
                    if (user.getUID().equals(mUserId)){
                        mClientFullNameTextView.setText(getString(R.string.full_name, user.getSurname(),
                                user.getName(), user.getPatronymic()));
                    }
                }
                for (DataSnapshot dataSnapshot : snapshot.child("service_doctor").getChildren()){
                    ServiceDoctor serviceDoctor = dataSnapshot.getValue(ServiceDoctor.class);
                    if (serviceDoctor.getId().equals(mServiceId)){
                        mServiceNameTextView.setText(serviceDoctor.getName());
                        mServiceDescriptionTextView.setText(serviceDoctor.getDescription());
                    }
                }
                for (DataSnapshot dataSnapshot : snapshot.child("user_service").getChildren()){
                    UserService userService = dataSnapshot.getValue(UserService.class);
                    if (userService.getServiceId().equals(mServiceId) && userService.getUserId().equals(mUserId)){
                        mUserService = userService;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        
        mBackButton = findViewById(R.id.back_button);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setServiceImage() {
        mServicePhotoStorageReference.getBytes(2048 * 2048)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        mServiceHasNoPhoto = false;
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                        Log.d("myLog", "onSuccess: bitmap " + bitmap.getByteCount());
                        mServicePhotoImageView.setImageBitmap(bitmap);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mServiceHasNoPhoto = true;
                        mServicePhotoImageView.setImageResource(R.drawable.ic_service);
                    }
                });
    }

    private void setUserImage() {
        mUserPhotoStorageReference.getBytes(2048 * 2048)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        mUserHasNoPhoto = false;
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        mUserPhotoCircleImageView.setImageBitmap(bitmap);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mUserHasNoPhoto = true;
                        mUserPhotoCircleImageView.setImageResource(R.drawable.user);
                    }
                });
    }
    
    public static Intent newIntent(Context context, String userId, String serviceId, Date visitDate){
        Intent intent = new Intent(context, OrderedServiceActivity.class);
        intent.putExtra(ARG_USER_ID, userId);
        intent.putExtra(ARG_SERVICE_ID, serviceId);
        intent.putExtra(ARG_VISIT_DATE, visitDate);
        return intent;
    }
}
