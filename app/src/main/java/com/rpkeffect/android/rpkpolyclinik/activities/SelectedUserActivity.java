package com.rpkeffect.android.rpkpolyclinik.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jgabrielfreitas.core.BlurImageView;
import com.rpkeffect.android.rpkpolyclinik.R;
import com.rpkeffect.android.rpkpolyclinik.adapters.ServiceAdapter;
import com.rpkeffect.android.rpkpolyclinik.classes.Service;
import com.rpkeffect.android.rpkpolyclinik.classes.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SelectedUserActivity extends AppCompatActivity {
    private static final String ARG_USER_ID = "userId";

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    DatabaseReference mReference = mDatabase.getReference();
    FirebaseStorage mStorage = FirebaseStorage.getInstance();
    StorageReference mStorageReference = mStorage.getReference();

    CollapsingToolbarLayout mCollapsingToolbarLayout;
    CircleImageView mUserPhotoCircleImageView;
    BlurImageView mBackgroundBLutImageView;
    ProgressBar mProgressBar;
    RecyclerView mRecyclerView;
    TextView mEmailTextView, mBirthDateTextView;
    FloatingActionButton mFloatingActionButton;

    SimpleDateFormat mFormatter = new SimpleDateFormat("dd MMMM yyyy");
    ServiceAdapter mAdapter;
    List<Service> mServices;
    String mUserId;
    User mUser;
    boolean mHasNoImage = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_user);

        Bundle bundle = getIntent().getExtras();
        mUserId = bundle.getString(ARG_USER_ID);

        mServices = new ArrayList<>();
        mAdapter = new ServiceAdapter(mServices, this);
        mRecyclerView = findViewById(R.id.ordered_services_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL,
                false));
        mRecyclerView.setAdapter(mAdapter);

        mCollapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);

        mUserPhotoCircleImageView = findViewById(R.id.user_photo_iv);
        mBackgroundBLutImageView = findViewById(R.id.blur_iv);
        mProgressBar = findViewById(R.id.preload_pb);

        mEmailTextView = findViewById(R.id.email_text_view);
        mBirthDateTextView = findViewById(R.id.birth_date_text_view);

        mFloatingActionButton = findViewById(R.id.exit_fab);
        mFloatingActionButton.setVisibility(View.GONE);

        mReference.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    User user = dataSnapshot.getValue(User.class);
                    if (user.getUID().equals(mUserId))
                        mUser = user;
                }
                fillInUserData();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void fillInUserData(){
        mStorageReference = mStorage.getReference()
                .child(getString(R.string.user_photo_reference, mUser.getUID()));
        setImage();
        mEmailTextView.setText(mUser.getEmail());
        mBirthDateTextView.setText(getString(R.string.birth_date_placeholder,
                mFormatter.format(mUser.getBirthDate())));
        mCollapsingToolbarLayout.setTitle(getString(R.string.full_name, mUser.getSurname(),
                mUser.getName(), mUser.getPatronymic()));
    }

    private void setImage() {
        mStorageReference.getBytes(2048 * 2048)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        mHasNoImage = false;
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        mUserPhotoCircleImageView.setImageBitmap(bitmap);
                        mBackgroundBLutImageView.setImageBitmap(bitmap);
                        mBackgroundBLutImageView.setBlur(24);
                        mUserPhotoCircleImageView.setVisibility(View.VISIBLE);
                        mProgressBar.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mHasNoImage = true;
                        mUserPhotoCircleImageView.setVisibility(View.VISIBLE);
                        mUserPhotoCircleImageView.setImageResource(R.drawable.user);
                        mProgressBar.setVisibility(View.GONE);
                    }
                });
    }

    public static Intent newInstance(Context context, String userId){
        Intent intent = new Intent(context, SelectedUserActivity.class);
        intent.putExtra(ARG_USER_ID, userId);
        return intent;
    }
}
