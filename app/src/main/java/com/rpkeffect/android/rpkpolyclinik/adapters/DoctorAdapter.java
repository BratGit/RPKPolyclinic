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

import com.rpkeffect.android.rpkpolyclinik.activities.DoctorActivity;
import com.rpkeffect.android.rpkpolyclinik.classes.Doctor;
import com.rpkeffect.android.rpkpolyclinik.R;

import java.util.List;
import java.util.Random;

class DoctorHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener {

    private Doctor mDoctor;
    private Context mContext;

    private final TextView mDoctorNameTextView;
    private final TextView mPositionTextView;
    private final CardView mCardView;
    private final ImageView mDoctorImageView;

    private final int[] mBackgroundColors = new int[]{R.color.light_green,
            R.color.light_pink, R.color.light_purple};
    private final int[] mTextColors = new int[]{R.color.text_green,
            R.color.text_pink, R.color.text_purple};
    private final int[] mSecondaryTextColors = new int[]{R.color.secondary_text_green,
            R.color.secondary_text_pink, R.color.secondary_text_purple};
    private final int[] mServiceImageViewColor = new int[]{R.drawable.doctor_green,
            R.drawable.doctor_pink, R.drawable.doctor_purple};
    private final int[] mIsCheckedImageViewColor = new int[]{R.drawable.checked_green,
            R.drawable.checked_pink, R.drawable.checked_purple};


    public DoctorHolder(@NonNull View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);

        mDoctorNameTextView = (TextView) itemView.findViewById(R.id.item_doctor_name_text_view);
        mPositionTextView = (TextView) itemView.findViewById(R.id.item_position_text_view);
        mCardView = (CardView) itemView.findViewById(R.id.item_doctor_cv);
        mDoctorImageView = (ImageView) itemView.findViewById(R.id.doctor_iv);
    }

    public void bind(Doctor doctor, Context context) {
        mDoctor = doctor;
        mContext = context;

        mDoctorNameTextView.setText(context.getString(R.string.full_name, mDoctor.getSurname(),
                mDoctor.getName(), mDoctor.getPatronymic()));
        mPositionTextView.setText(mDoctor.getPosition());

        Random random = new Random();
        int randColor = random.nextInt(mBackgroundColors.length);
        mCardView.setCardBackgroundColor(mContext.getResources()
                .getColor(mBackgroundColors[randColor]));
        mDoctorNameTextView.setTextColor(mContext.getResources()
                .getColor(mTextColors[randColor]));
        mPositionTextView.setTextColor(mContext.getResources()
                .getColor(mSecondaryTextColors[randColor]));
        mDoctorImageView.setImageResource(mServiceImageViewColor[randColor]);
    }

    @Override
    public void onClick(View v) {
        mContext.startActivity(DoctorActivity.newIntent(mContext, mDoctor.getUID(),
                mContext.getString(R.string.position_full_name, mDoctor.getPosition(),
                        mDoctor.getSurname(), mDoctor.getName(), mDoctor.getPatronymic())));
    }
}

public class DoctorAdapter extends RecyclerView.Adapter<DoctorHolder> {
    private List<Doctor> mList;
    private Context mContext;

    public DoctorAdapter(List<Doctor> doctors, Context context) {
        mList = doctors;
        mContext = context;
    }

    @NonNull
    @Override
    public DoctorHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        view = LayoutInflater.from(mContext).inflate(R.layout.item_person, parent, false);

        return new DoctorHolder(view);
//        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
//        return new ServiceHolder(layoutInflater, parent);
    }

    @Override
    public void onBindViewHolder(@NonNull DoctorHolder holder, int position) {
        Doctor doctor = mList.get(position);
        holder.bind(doctor, mContext);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void setDoctors(List<Doctor> services) {
        mList = services;
    }
}
