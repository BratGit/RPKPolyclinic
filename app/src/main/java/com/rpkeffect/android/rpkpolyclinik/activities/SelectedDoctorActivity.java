package com.rpkeffect.android.rpkpolyclinik.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import com.rpkeffect.android.rpkpolyclinik.R;
import com.rpkeffect.android.rpkpolyclinik.fragments.DoctorServicesFragment;
import com.rpkeffect.android.rpkpolyclinik.fragments.ServiceAddingFragment;
import com.rpkeffect.android.rpkpolyclinik.interfaces.DoctorServiceListener;

public class SelectedDoctorActivity extends AppCompatActivity implements DoctorServiceListener{
    private static final String ARG_DOCTOR_ID = "DoctorId";
    private static final String ARG_POSITION_FULL_NAME = "positionFullName";

    Toolbar mToolbar;
    FragmentManager fm = getSupportFragmentManager();

    String mDoctorId, mPositionFullName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_doctor);

        Bundle args = getIntent().getExtras();
        mDoctorId = args.getString(ARG_DOCTOR_ID);
        mPositionFullName = args.getString(ARG_POSITION_FULL_NAME);

        mToolbar = findViewById(R.id.toolbar_doctor);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fm
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.doctor_fragment_container, DoctorServicesFragment
                        .newInstance(mDoctorId, mPositionFullName,this))
                .commit();

        mToolbar.setTitle(mPositionFullName);
    }



    public static Intent newIntent(Context context, String doctorId, String positionFullName){
        Intent intent = new Intent(context, SelectedDoctorActivity.class);
        intent.putExtra(ARG_DOCTOR_ID, doctorId);
        intent.putExtra(ARG_POSITION_FULL_NAME, positionFullName);
        return intent;
    }

    @Override
    public void onDoctorServicesAdd() {
        fm
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.doctor_fragment_container, ServiceAddingFragment
                        .newInstance(mDoctorId, null, mPositionFullName, this))
                .commit();
    }

    @Override
    public void onServiceAdded() {
        fm
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.doctor_fragment_container, DoctorServicesFragment
                        .newInstance(mDoctorId, mPositionFullName, this))
                .commit();
    }

    @Override
    public void onItemClicked(String serviceId) {
        fm
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.doctor_fragment_container, ServiceAddingFragment
                        .newInstance(mDoctorId, serviceId, mPositionFullName, this))
                .commit();
    }
}
