package com.rpkeffect.android.rpkpolyclinik.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.rpkeffect.android.rpkpolyclinik.classes.Doctor;
import com.rpkeffect.android.rpkpolyclinik.classes.User;
import com.rpkeffect.android.rpkpolyclinik.dialogs.ThemeSwitcherDialog;

public class AuthorizationActivity extends AppCompatActivity {
    private static final String DIALOG_THEME_SWITCHER = "ThemeSwitcherDialog";
    private static final String ARG_ID = "id";

    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    DatabaseReference mReference;

    ImageView mThemeSwitcherImageView;
    ProgressBar mProgressBar;
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

        mThemeSwitcherImageView = findViewById(R.id.theme_switcher_iv);
        mThemeSwitcherImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getSupportFragmentManager();
                ThemeSwitcherDialog themeSwitcherDialog = new ThemeSwitcherDialog();

                themeSwitcherDialog.show(manager, DIALOG_THEME_SWITCHER);
            }
        });

        mIncorrectAuth = (TextView) findViewById(R.id.incorrect_auth_text_view);
        mEmailEditText = (EditText) findViewById(R.id.login);
        mPasswordEditText = (EditText) findViewById(R.id.password);
        mProgressBar = (ProgressBar) findViewById(R.id.auth_progress_bar);

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
                    auth(snapshot);
                    mProgressBar.setVisibility(View.GONE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else
            mProgressBar.setVisibility(View.GONE);
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
                                            auth(snapshot);
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

    private void auth(DataSnapshot snapshot) {
        for (DataSnapshot userSnapshot : snapshot.child("users").getChildren()) {
            User user = userSnapshot.getValue(User.class);
            if (user.getUID().equals(mAuth.getUid())) {
                authenticateClient();
            }
        }
        for (DataSnapshot userSnapshot : snapshot.child("clinics_new").getChildren()) {
            Clinic clinic = userSnapshot.getValue(Clinic.class);
            if (clinic.getId().equals(mAuth.getUid())) {
                authenticateClinic();
            }
        }
        for (DataSnapshot dataSnapshot : snapshot.child("employees").getChildren()) {
            Doctor doctor = dataSnapshot.getValue(Doctor.class);
            if (doctor.getUID().equals(mAuth.getUid())) {
                authenticateDoctor();
            }
        }
    }

    private void incorrectAuthAnimation() {
        Animation anim = AnimationUtils.loadAnimation(
                AuthorizationActivity.this,
                R.anim.error_animation);
        mIncorrectAuth.setVisibility(View.VISIBLE);
        mIncorrectAuth.startAnimation(anim);
    }

    private void authenticateClinic() {
        Intent intent = new Intent(AuthorizationActivity.this, ClinicActivity.class);
        startActivity(intent);

    }

    private void authenticateDoctor() {
        Intent intent = new Intent(AuthorizationActivity.this, DoctorActivity.class);
        startActivity(intent);

    }

    private void authenticateClient() {
        Intent intent = new Intent(AuthorizationActivity.this,
                MainActivity.class);
        intent.putExtra(ARG_ID, mId);
        startActivity(intent);
    }
}