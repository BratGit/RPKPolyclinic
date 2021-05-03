package com.rpkeffect.android.rpkpolyclinik.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rpkeffect.android.rpkpolyclinik.classes.Clinic;
import com.rpkeffect.android.rpkpolyclinik.classes.Doctor;
import com.rpkeffect.android.rpkpolyclinik.classes.Hospital;
import com.rpkeffect.android.rpkpolyclinik.R;
import com.rpkeffect.android.rpkpolyclinik.classes.Service;
import com.rpkeffect.android.rpkpolyclinik.activities.SelectedHospitalActivity;
import com.rpkeffect.android.rpkpolyclinik.adapters.ServiceAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ServicesFragment extends Fragment {
    RecyclerView mServicesRecyclerView, mHospitalRecyclerView;
    TextInputLayout mTextInputLayout;

    FirebaseDatabase database = FirebaseDatabase.getInstance();

    DatabaseReference mServices = database.getReference();

    EditText mEditText;
    Toolbar mToolbar;
    TextView mClinicsTextView;
    ProgressBar mClinicsProgressBar, mServicesProgressBar;

    ServiceAdapter mServiceAdapter;
    HospitalAdapter mHospitalAdapter;
    ClinicAdapter mClinicAdapter;

    List<Service> mServiceList;
    List<Hospital> mHospitalList;
    List<Clinic> mClinicList;

    boolean mIsSearching = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.services_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.service_search:
                mIsSearching = !mIsSearching;
                if (!mIsSearching) {
                    mTextInputLayout.setVisibility(View.GONE);
                    mServiceAdapter = new ServiceAdapter(mServiceList, getActivity());
                    mServicesRecyclerView.setAdapter(mServiceAdapter);

                    mClinicsTextView.setVisibility(View.VISIBLE);
                    mHospitalRecyclerView.setVisibility(View.VISIBLE);
                } else {
                    mClinicsTextView.setVisibility(View.GONE);
                    mHospitalRecyclerView.setVisibility(View.GONE);
                    mTextInputLayout.setVisibility(View.VISIBLE);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_services, container, false);

        mToolbar = v.findViewById(R.id.services_tool_bar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(mToolbar);
        activity.getSupportActionBar();

        mClinicsTextView = v.findViewById(R.id.clinics_text_view);

        mClinicsProgressBar = v.findViewById(R.id.clinics_loading_pb);
        mServicesProgressBar = v.findViewById(R.id.services_loading_pb);

        mHospitalRecyclerView = (RecyclerView) v.findViewById(R.id.hospital_recycler_view);
        mHospitalRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(),
                RecyclerView.HORIZONTAL, false));

        mServicesRecyclerView = (RecyclerView) v.findViewById(R.id.services_recycler_view);
        mServicesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mTextInputLayout = v.findViewById(R.id.search_layout);
        mEditText = v.findViewById(R.id.search_et);

        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                List<Service> searchedServices = new ArrayList<>();
                for (Service service : mServiceList) {
                    if (service.getName().toLowerCase()
                            .contains(mEditText.getText().toString().toLowerCase().trim())) {
                        searchedServices.add(service);
                    }
                }
                mServiceAdapter = new ServiceAdapter(searchedServices, getActivity());
                mServicesRecyclerView.setAdapter(mServiceAdapter);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mServiceList = new ArrayList<>();
        mServiceAdapter = new ServiceAdapter(mServiceList, getActivity());
        mServicesRecyclerView.setAdapter(mServiceAdapter);

        mClinicList = new ArrayList<>();
        mClinicAdapter = new ClinicAdapter(mClinicList);
        mHospitalRecyclerView.setAdapter(mClinicAdapter);

        mHospitalList = new ArrayList<>();
        mHospitalAdapter = new HospitalAdapter(mHospitalList);
//        mHospitalRecyclerView.setAdapter(mHospitalAdapter);

        mServices.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.child("clinics_new").getChildren()) {
                    Clinic hospital = userSnapshot.getValue(Clinic.class);
                    mClinicList.add(hospital);
                }
                mClinicsProgressBar.setVisibility(View.GONE);
                mClinicAdapter.setServices(mClinicList);
                mClinicAdapter.notifyDataSetChanged();
                for (DataSnapshot userSnapshot : snapshot.child("services").getChildren()) {
                    Service service = userSnapshot.getValue(Service.class);
                    mServiceList.add(service);
                }
                mServicesProgressBar.setVisibility(View.GONE);
                mServiceAdapter.setServices(mServiceList);
                mServiceAdapter.notifyDataSetChanged();
                for (DataSnapshot userSnapshot : snapshot.child("employees").getChildren()){
                    Doctor doctor = userSnapshot.getValue(Doctor.class);
                    Log.d("myLog", "onDataChange: employees " + doctor.getEmail());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return v;
    }

    private void initializeServices() {
        mServices.push().setValue(new Service(0, 0, "Лечение зубов", 100.2f));
        mServices.push().setValue(new Service(1, 0, "Лор-услуга", 500.2f));
        mServices.push().setValue(new Service(2, 0, "Массаж", 150.2f));
        mServices.push().setValue(new Service(3, 0, "Лазерная коррекция зрения", 600));
        mServices.push().setValue(new Service(4, 0, "Консультация", 0.0f));

        mServices.push().setValue(new Service(5, 1, "Лечение зубов", 1000.2f));
        mServices.push().setValue(new Service(6, 1, "Дет. стоматология", 5000.2f));
        mServices.push().setValue(new Service(7, 1, "Удаление зубов", 1050.2f));
        mServices.push().setValue(new Service(8, 1, "Лечение дёсен", 6000));
        mServices.push().setValue(new Service(9, 1, "Установка коронок", 1000.0f));

        mServices.push().setValue(new Service(10, 3, "Диагностика", 2100.2f));
        mServices.push().setValue(new Service(11, 3, "Вызов врача", 4500.2f));
        mServices.push().setValue(new Service(12, 3, "Анализы", 1509.2f));
        mServices.push().setValue(new Service(13, 3, "Операционные", 6500));
        mServices.push().setValue(new Service(14, 3, "Вакцинация", 6000.0f));

        mServices.push().setValue(new Service(15, 4, "Скорая помощь", 4100.2f));
        mServices.push().setValue(new Service(16, 4, "Диспансеризация", 5100.2f));
        mServices.push().setValue(new Service(17, 4, "Экстренная хир. помощь", 3150.2f));
        mServices.push().setValue(new Service(18, 4, "Мед. услуги в кредит", 1600));
        mServices.push().setValue(new Service(19, 4, "Дисконт программа", 500.0f));

        mServices.push().setValue(new Service(20, 5, "Аллергология", 3100.2f));
        mServices.push().setValue(new Service(21, 5, "Иммунология", 100.2f));
        mServices.push().setValue(new Service(22, 5, "Андрология", 1150.2f));
        mServices.push().setValue(new Service(23, 5, "Аноскопия", 600));
        mServices.push().setValue(new Service(24, 5, "Гастроскопия", 510.0f));

        mServices.push().setValue(new Service(25, 6, "Диетология", 3100.2f));
        mServices.push().setValue(new Service(26, 6, "Гинескопия", 1100.2f));
        mServices.push().setValue(new Service(27, 6, "Дерматология", 150.2f));
        mServices.push().setValue(new Service(28, 6, "Кардиология", 100));
        mServices.push().setValue(new Service(29, 6, "Колоноскопия", 500.0f));
    }

    private class HospitalHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private final TextView mHospitalNameTextView;
        private final TextView mHospitalAddressTextView;
        private final CardView mCardView;
        private final ImageView mHospitalImageView;

        private final int[] mBackgroundColors = new int[]{R.color.light_green,
                R.color.light_pink, R.color.light_purple};
        private final int[] mTextColors = new int[]{R.color.text_green,
                R.color.text_pink, R.color.text_purple};
        private final int[] mSecondaryTextColors = new int[]{R.color.secondary_text_green,
                R.color.secondary_text_pink, R.color.secondary_text_purple};
        private final int[] mServiceImageViewColor = new int[]{R.drawable.hospital_green,
                R.drawable.hospital_pink, R.drawable.hospital_purple};

        private Hospital mHospital;

        public HospitalHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.item_hospital, parent, false));
            itemView.setOnClickListener(this);

            mHospitalNameTextView = (TextView) itemView.findViewById(R.id.item_hospital_name_text_view);
            mHospitalAddressTextView = (TextView) itemView.findViewById(R.id.item_hospital_address_text_view);
            mCardView = (CardView) itemView.findViewById(R.id.item_hospital_cv);
            mHospitalImageView = (ImageView) itemView.findViewById(R.id.hospital_iv);
        }

        public void bind(Hospital hospital) {
            mHospital = hospital;
            mHospitalNameTextView.setText(mHospital.getName());
            mHospitalAddressTextView.setText(mHospital.getAddress());

            Random random = new Random();
            int randColor = random.nextInt(mBackgroundColors.length);

            mCardView.setCardBackgroundColor(getResources()
                    .getColor(mBackgroundColors[randColor]));
            mHospitalNameTextView.setTextColor(getResources()
                    .getColor(mTextColors[randColor]));
            mHospitalAddressTextView.setTextColor(getResources()
                    .getColor(mSecondaryTextColors[randColor]));
            mHospitalImageView.setImageResource(mServiceImageViewColor[randColor]);
        }

        @Override
        public void onClick(View v) {
            Intent intent = SelectedHospitalActivity.newIntent(getActivity(), mHospital.
                    getId());
            startActivity(intent);
        }
    }

    private class HospitalAdapter extends RecyclerView.Adapter<HospitalHolder> {
        private List<Hospital> mList;

        public HospitalAdapter(List<Hospital> hospitals) {
            mList = hospitals;
        }

        @NonNull
        @Override
        public HospitalHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new HospitalHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull HospitalHolder holder, int position) {
            Hospital hospital = mList.get(position);
            holder.bind(hospital);
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }

        public void setServices(List<Hospital> hospitals) {
            mList = hospitals;
        }
    }


    private class ClinicHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private final TextView mHospitalNameTextView;
        private final TextView mHospitalAddressTextView;
        private final CardView mCardView;
        private final ImageView mHospitalImageView;

        private final int[] mBackgroundColors = new int[]{R.color.light_green,
                R.color.light_pink, R.color.light_purple};
        private final int[] mTextColors = new int[]{R.color.text_green,
                R.color.text_pink, R.color.text_purple};
        private final int[] mSecondaryTextColors = new int[]{R.color.secondary_text_green,
                R.color.secondary_text_pink, R.color.secondary_text_purple};
        private final int[] mServiceImageViewColor = new int[]{R.drawable.hospital_green,
                R.drawable.hospital_pink, R.drawable.hospital_purple};

        private Clinic mHospital;

        public ClinicHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.item_hospital, parent, false));
            itemView.setOnClickListener(this);

            mHospitalNameTextView = (TextView) itemView.findViewById(R.id.item_hospital_name_text_view);
            mHospitalAddressTextView = (TextView) itemView.findViewById(R.id.item_hospital_address_text_view);
            mCardView = (CardView) itemView.findViewById(R.id.item_hospital_cv);
            mHospitalImageView = (ImageView) itemView.findViewById(R.id.hospital_iv);
        }

        public void bind(Clinic clinic) {
            mHospital = clinic;
            mHospitalNameTextView.setText(mHospital.getName());
            mHospitalAddressTextView.setText(mHospital.getAddress());

            Random random = new Random();
            int randColor = random.nextInt(mBackgroundColors.length);

            mCardView.setCardBackgroundColor(getResources()
                    .getColor(mBackgroundColors[randColor]));
            mHospitalNameTextView.setTextColor(getResources()
                    .getColor(mTextColors[randColor]));
            mHospitalAddressTextView.setTextColor(getResources()
                    .getColor(mSecondaryTextColors[randColor]));
            mHospitalImageView.setImageResource(mServiceImageViewColor[randColor]);
        }

        @Override
        public void onClick(View v) {
//            Intent intent = SelectedHospitalActivity.newIntent(getActivity(), mHospital.
//                    getId());
//            startActivity(intent);
        }
    }

    private class ClinicAdapter extends RecyclerView.Adapter<ClinicHolder> {
        private List<Clinic> mList;

        public ClinicAdapter(List<Clinic> hospitals) {
            mList = hospitals;
        }

        @NonNull
        @Override
        public ClinicHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new ClinicHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull ClinicHolder holder, int position) {
            Clinic hospital = mList.get(position);
            holder.bind(hospital);
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }

        public void setServices(List<Clinic> hospitals) {
            mList = hospitals;
        }
    }
}
