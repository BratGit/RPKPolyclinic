package com.rpkeffect.android.rpkpolyclinik.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.rpkeffect.android.rpkpolyclinik.R;
import com.rpkeffect.android.rpkpolyclinik.classes.User;
import com.rpkeffect.android.rpkpolyclinik.fragments.ClinicRegisterFragment;
import com.rpkeffect.android.rpkpolyclinik.fragments.ConfirmFragment;
import com.rpkeffect.android.rpkpolyclinik.fragments.DoctorPersonalInfoFragment;
import com.rpkeffect.android.rpkpolyclinik.fragments.PersonalInfoFragment;
import com.rpkeffect.android.rpkpolyclinik.fragments.RegistrationFragment;
import com.rpkeffect.android.rpkpolyclinik.interfaces.CodeResendListener;
import com.rpkeffect.android.rpkpolyclinik.interfaces.CorrectCodeInsertedListener;
import com.rpkeffect.android.rpkpolyclinik.interfaces.UserRegisteredListener;

public class RegistrationActivity extends AppCompatActivity implements CodeResendListener,
        CorrectCodeInsertedListener, UserRegisteredListener {
    private static final String ARG_IS_REG_DOCTOR = "isRegDoctor";
    private static final String ARG_CLINIC_ID = "clinicId";

    FragmentManager fm = getSupportFragmentManager();
    String mPassword, mMail;
    User mUser;
    boolean mIsRegDoctor = false;
    String mClinicId;

    public static Intent newInstance(Context context, boolean isRegDoctor, String mClinicId) {
        Intent intent = new Intent(context, RegistrationActivity.class);
        intent.putExtra(ARG_IS_REG_DOCTOR, isRegDoctor);
        intent.putExtra(ARG_CLINIC_ID, mClinicId);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        mIsRegDoctor = false;

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mIsRegDoctor = bundle.getBoolean(ARG_IS_REG_DOCTOR);
            mClinicId = bundle.getString(ARG_CLINIC_ID);
            Log.d("myLog", "onCreate: mIsRegDoctor " + mIsRegDoctor);
        }

        fm
                .beginTransaction()
                .replace(R.id.fragment_container, RegistrationFragment.getInstance(this))
                .commit();

    }

    @Override
    public void onCodeReceived(String code, String mail, String password) {
        mPassword = password;
        mMail = mail;

        fm
                .beginTransaction()
                .replace(R.id.fragment_container, ConfirmFragment.getInstance(code, mail,
                        this, mIsRegDoctor))
                .commit();
    }

    @Override
    public void onCorrectCodeInserted() {
        Log.d("myLog", "onCorrectCodeInserted: mIsRegDoctor " + mIsRegDoctor);
        if (mIsRegDoctor) {
            fm
                    .beginTransaction()
                    .replace(R.id.fragment_container,
                            DoctorPersonalInfoFragment.getInstance(mMail, mPassword,
                                    this, mClinicId))
                    .commit();
        } else {
            fm
                    .beginTransaction()
                    .replace(R.id.fragment_container,
                            PersonalInfoFragment.getInstance(mMail, mPassword, this))
                    .commit();
        }
    }

    @Override
    public void onCorrectCodeInsertedRegClinic() {
        fm
                .beginTransaction()
                .replace(R.id.fragment_container,
                        ClinicRegisterFragment.getInstance(mMail, mPassword, this))
                .commit();
    }


    @Override
    public void onUserRegistered() {
//        Toast.makeText(this, "Пользователь зарегистрирован", Toast.LENGTH_SHORT).show();
        finish();
    }
}
