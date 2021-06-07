package com.rpkeffect.android.rpkpolyclinik.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rpkeffect.android.rpkpolyclinik.classes.OrderedService;
import com.rpkeffect.android.rpkpolyclinik.R;
import com.rpkeffect.android.rpkpolyclinik.classes.Service;

public class ServiceInfoActivity extends AppCompatActivity {
    private final static String EXTRA_SERVICE_ID = "service_id";

    FirebaseAuth mAuth;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference mDatabaseReference = database.getReference();
    DatabaseReference mOrderedServices = database.getReference("ordered_services");

    Toolbar mToolbar;
    TextView mClinicAddressTextView,
            mServiceNameTextView, mServicePriceTextView;
    Button mOrderServiceButton;
    int mServiceId;
    int mClinicId;

    public static Intent newIntent(Context packageContext, int serviceId) {
        Intent intent = new Intent(packageContext, ServiceInfoActivity.class);
        intent.putExtra(EXTRA_SERVICE_ID, serviceId);
        return intent;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_service_info);

        mAuth = FirebaseAuth.getInstance();

        mServiceId = getIntent().getIntExtra(EXTRA_SERVICE_ID, -1);

        mToolbar = findViewById(R.id.toolbar_clinic_name);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mClinicAddressTextView = (TextView) findViewById(R.id.clinic_address_text_view);
        mServiceNameTextView = (TextView) findViewById(R.id.service_name_text_view);
        mServicePriceTextView = (TextView) findViewById(R.id.service_price_text_view);
        mOrderServiceButton = (Button) findViewById(R.id.order_service_button);
        mOrderServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOrderedServices.push().setValue(new OrderedService(mServiceId,
                        mAuth.getUid()));
                serviceOrdered();
            }
        });

        getCurrentServiceData();
    }

    private void serviceOrdered() {
        mOrderServiceButton.setText("Услуга заказана");
        mOrderServiceButton.setEnabled(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mOrderServiceButton.setBackgroundColor(getColor(R.color.light_green));
            mOrderServiceButton.setTextColor(getColor(R.color.text_green));
        }
    }

    private void getCurrentServiceData() {
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.child("ordered_services").getChildren()) {
                    OrderedService orderedService = userSnapshot.getValue(OrderedService.class);
                    if (orderedService.getServiceId() == mServiceId
                            && orderedService.getUserId().equals(mAuth.getUid())) {
                        serviceOrdered();
                        Log.d("myLog", "onDataChange: service ordered");
                    }
                }
                for (DataSnapshot userSnapshot : snapshot.child("services").getChildren()) {
                    Service service = userSnapshot.getValue(Service.class);
                    if (service.getId() == mServiceId) {
                        mClinicId = service.getClinicId();
                        mServiceNameTextView.setText(service.getName());
                        mServicePriceTextView.setText(String.valueOf(service.getPrice()) + "₽");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
