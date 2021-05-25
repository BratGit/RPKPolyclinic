package com.rpkeffect.android.rpkpolyclinik.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.rpkeffect.android.rpkpolyclinik.R;
import com.rpkeffect.android.rpkpolyclinik.fragments.SelectedClinicFragment;
import com.rpkeffect.android.rpkpolyclinik.fragments.SelectedServiceFragment;
import com.rpkeffect.android.rpkpolyclinik.interfaces.SelectedClinicListener;

public class SelectedClinicActivity extends AppCompatActivity implements SelectedClinicListener {
    private static final String ARG_CLINIC_ID = "clinicId";

    FragmentManager fm = getSupportFragmentManager();

    String mClinicId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_clinic);

        Bundle args = getIntent().getExtras();
        mClinicId = args.getString(ARG_CLINIC_ID);

        fm
                .beginTransaction()
                .replace(R.id.clinic_fragment_container, SelectedClinicFragment.newInstance(mClinicId, this))
                .commit();
    }

    public static Intent newInstance(Context context, String clinicId){
        Intent intent = new Intent(context, SelectedClinicActivity.class);
        intent.putExtra(ARG_CLINIC_ID, clinicId);
        return intent;
    }

    @Override
    public void onServiceItemClick(String serviceId) {
        fm
                .beginTransaction()
                .replace(R.id.clinic_fragment_container, SelectedServiceFragment.newInstance(serviceId, this))
                .commit();
    }

    @Override
    public void onCancelClick() {
        fm
                .beginTransaction()
                .replace(R.id.clinic_fragment_container, SelectedClinicFragment.newInstance(mClinicId, this))
                .commit();
    }

    @Override
    public void onServiceOrdered() {
        fm
                .beginTransaction()
                .replace(R.id.clinic_fragment_container, SelectedClinicFragment.newInstance(mClinicId, this))
                .commit();
    }
}
