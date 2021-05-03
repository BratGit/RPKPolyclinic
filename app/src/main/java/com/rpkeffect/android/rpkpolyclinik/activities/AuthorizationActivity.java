package com.rpkeffect.android.rpkpolyclinik.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rpkeffect.android.rpkpolyclinik.classes.Clinic;
import com.rpkeffect.android.rpkpolyclinik.R;
import com.rpkeffect.android.rpkpolyclinik.classes.User;

public class AuthorizationActivity extends AppCompatActivity {
    private static final String ARG_ID = "id";

    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    DatabaseReference mReference;

    ProgressDialog mProgressDialog;
    TextView register, mIncorrectAuth;
    EditText mEmailEditText, mPasswordEditText;
    Button mAuthButton;

    int mId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorization);

        mReference = mDatabase.getReference();

        mAuth = FirebaseAuth.getInstance();
        mIncorrectAuth = (TextView) findViewById(R.id.incorrect_auth_text_view);
        mEmailEditText = (EditText) findViewById(R.id.login);
        mPasswordEditText = (EditText) findViewById(R.id.password);

        checkAuthenticatedUser();

        mAuthButton = (Button) findViewById(R.id.auth_button);
        mAuthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUserAccount(mEmailEditText.getText().toString().trim(),
                        mPasswordEditText.getText().toString().trim());
            }
        });

        register = (TextView) findViewById(R.id.registration);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AuthorizationActivity.this,
                        RegistrationActivity.class));
            }
        });
    }

    private void checkAuthenticatedUser() {
        if (mAuth.getCurrentUser() != null) {
            mReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot userSnapshot : snapshot.child("users").getChildren()) {
                        User user = userSnapshot.getValue(User.class);
                        if (user.getEmail().equals(mAuth.getCurrentUser().getEmail())) {
                            authenticate();
                        }
                    }
                    for (DataSnapshot userSnapshot : snapshot.child("clinics_new").getChildren()) {
                        Clinic clinic = userSnapshot.getValue(Clinic.class);
                        if (clinic.getEmail().equals(mAuth.getCurrentUser().getEmail())) {
                            authenticateClinic();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void loginUserAccount(String email, String password) {
        mProgressDialog = ProgressDialog.show(this, null,
                "Авторизация", true, false);
        mProgressDialog.show();
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            incorrectAuthAnimation();
            mProgressDialog.dismiss();
            return;
        }
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(
                                    @NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    mIncorrectAuth.setVisibility(View.GONE);
                                    mReference.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot userSnapshot : snapshot.child("users").getChildren()) {
                                                User user = userSnapshot.getValue(User.class);
                                                if (user.getEmail().equals(mEmailEditText.getText().toString().trim())){
                                                    authenticate();
                                                }
                                            }
                                            for (DataSnapshot userSnapshot : snapshot.child("clinics_new").getChildren()) {
                                                Clinic clinic = userSnapshot.getValue(Clinic.class);
                                                if (clinic.getEmail().equals(mEmailEditText.getText().toString().trim())){
                                                    authenticateClinic();
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                } else {
                                    incorrectAuthAnimation();
                                }
                                mProgressDialog.dismiss();
                            }
                        });
    }

    private void incorrectAuthAnimation() {
        Animation anim = AnimationUtils.loadAnimation(
                AuthorizationActivity.this,
                R.anim.error_animation);
        mIncorrectAuth.setVisibility(View.VISIBLE);
        mIncorrectAuth.startAnimation(anim);
    }

    private void authenticateClinic(){
        Intent intent = new Intent(AuthorizationActivity.this, ClinicActivity.class);
        startActivity(intent);

    }

    private void authenticate() {
        Intent intent = new Intent(AuthorizationActivity.this,
                MainActivity.class);
        intent.putExtra(ARG_ID, mId);
        startActivity(intent);
    }
}