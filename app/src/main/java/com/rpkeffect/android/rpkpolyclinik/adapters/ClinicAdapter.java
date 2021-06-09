package com.rpkeffect.android.rpkpolyclinik.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.rpkeffect.android.rpkpolyclinik.R;
import com.rpkeffect.android.rpkpolyclinik.classes.Clinic;
import com.rpkeffect.android.rpkpolyclinik.classes.UserClinic;
import com.rpkeffect.android.rpkpolyclinik.interfaces.ClinicAdapterListener;

import java.util.List;
import java.util.Random;

class ClinicHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener {

    private Context mContext;
    private ClinicAdapterListener mListener;
    private Clinic mClinic;

    private final TextView mHospitalNameTextView;
    private final TextView mHospitalAddressTextView;
    private final CardView mCardView;
    private final ImageView mHospitalImageView;
    private final ImageView mCheckedImageView;

    private final int[] mBackgroundColors = new int[]{R.color.light_green,
            R.color.light_pink, R.color.light_purple};
    private final int[] mTextColors = new int[]{R.color.text_green,
            R.color.text_pink, R.color.text_purple};
    private final int[] mSecondaryTextColors = new int[]{R.color.secondary_text_green,
            R.color.secondary_text_pink, R.color.secondary_text_purple};
    private final int[] mServiceImageViewColor = new int[]{R.drawable.hospital_green,
            R.drawable.hospital_pink, R.drawable.hospital_purple};
    private final int[] mCheckedImageViewColor = new int[]{R.drawable.checked_green,
            R.drawable.checked_pink, R.drawable.checked_purple};


    public ClinicHolder(@NonNull View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);

        mHospitalNameTextView = (TextView) itemView.findViewById(R.id.item_hospital_name_text_view);
        mHospitalAddressTextView = (TextView) itemView.findViewById(R.id.item_hospital_address_text_view);
        mCardView = (CardView) itemView.findViewById(R.id.item_hospital_cv);
        mHospitalImageView = (ImageView) itemView.findViewById(R.id.hospital_iv);
        mCheckedImageView = (ImageView) itemView.findViewById(R.id.clinic_checked_iv);
    }

    public void bind(Clinic clinic, Context context, ClinicAdapterListener listener) {
        mClinic = clinic;
        mContext = context;
        mListener = listener;
        mHospitalNameTextView.setText(mClinic.getName());
        mHospitalAddressTextView.setText(mClinic.getAddress());

        Random random = new Random();
        int randColor = random.nextInt(mBackgroundColors.length);

        mCardView.setCardBackgroundColor(mContext.getResources()
                .getColor(mBackgroundColors[randColor]));
        mHospitalNameTextView.setTextColor(mContext.getResources()
                .getColor(mTextColors[randColor]));
        mHospitalAddressTextView.setTextColor(mContext.getResources()
                .getColor(mSecondaryTextColors[randColor]));
        mHospitalImageView.setImageResource(mServiceImageViewColor[randColor]);

        if (mCheckedImageView != null)
            mCheckedImageView.setImageResource(mCheckedImageViewColor[randColor]);
    }

    @Override
    public void onClick(View v) {
        mListener.onItemClick(mClinic.getId());
    }
}

public class ClinicAdapter extends RecyclerView.Adapter<ClinicHolder> {
    private static final int ITEM_VIEW_BASE = 0;
    private static final int ITEM_VIEW_CHECKED = 1;

    private List<Clinic> mList;
    private List<UserClinic> mUserClinics;
    private Context mContext;
    private ClinicAdapterListener mListener;

    public ClinicAdapter(List<Clinic> hospitals, List<UserClinic> userClinics, Context context,
                         ClinicAdapterListener listener) {
        mList = hospitals;
        mUserClinics = userClinics;
        mContext = context;
        mListener = listener;
    }

    @NonNull
    @Override
    public ClinicHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        if (viewType == ITEM_VIEW_BASE)
            view = LayoutInflater.from(mContext).inflate(R.layout.item_hospital, parent, false);
        else
            view = LayoutInflater.from(mContext).inflate(R.layout.item_hospital_checked, parent, false);


        return new ClinicHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClinicHolder holder, int position) {
        Clinic hospital = mList.get(position);
        holder.bind(hospital, mContext, mListener);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public int getItemViewType(int position) {
        for (UserClinic userClinic : mUserClinics){
            if (mList.get(position).getId().equals(userClinic.getClinicId())
                    && userClinic.getStatus() == UserClinic.STATUS_ACCEPT)
                return ITEM_VIEW_CHECKED;
        } return ITEM_VIEW_BASE;
    }

    public void setServices(List<Clinic> hospitals) {
        mList = hospitals;
    }
}
