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

import com.rpkeffect.android.rpkpolyclinik.R;
import com.rpkeffect.android.rpkpolyclinik.classes.Doctor;
import com.rpkeffect.android.rpkpolyclinik.classes.ServiceDoctor;
import com.rpkeffect.android.rpkpolyclinik.interfaces.ServiceDoctorAdapterListener;

import java.util.List;
import java.util.Random;

class ServiceDoctorHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener {

    private Doctor mDoctor;
    private ServiceDoctor mServiceDoctor;
    private Context mContext;
    private ServiceDoctorAdapterListener mListener;

    private final TextView mServiceNameTextView;
    private final TextView mServicePriceTextView;
    private final TextView mDoctorNameTextView;
    private final CardView mCardView;
    private final ImageView mServiceImageView;

    private final int[] mBackgroundColors = new int[]{R.color.light_green,
            R.color.light_pink, R.color.light_purple};
    private final int[] mTextColors = new int[]{R.color.text_green,
            R.color.text_pink, R.color.text_purple};
    private final int[] mSecondaryTextColors = new int[]{R.color.secondary_text_green,
            R.color.secondary_text_pink, R.color.secondary_text_purple};
    private final int[] mServiceImageViewColor = new int[]{R.drawable.service_green,
            R.drawable.service_pink, R.drawable.service_purple};
    private final int[] mIsCheckedImageViewColor = new int[]{R.drawable.checked_green,
            R.drawable.checked_pink, R.drawable.checked_purple};


    public ServiceDoctorHolder(@NonNull View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);

        mServiceNameTextView = (TextView) itemView.findViewById(R.id.item_service_name_text_view);
        mDoctorNameTextView = (TextView) itemView.findViewById(R.id.item_service_doctor_name_text_view);
        mServicePriceTextView = (TextView) itemView.findViewById(R.id.item_service_price_text_view);
        mCardView = (CardView) itemView.findViewById(R.id.item_service_doctor_cv);
        mServiceImageView = (ImageView) itemView.findViewById(R.id.service_doctor_iv);
    }

    public void bind(ServiceDoctor serviceDoctor, Doctor doctor, Context context,
                     ServiceDoctorAdapterListener listener) {
        mServiceDoctor = serviceDoctor;
        mContext = context;
        mDoctor = doctor;
        mListener = listener;

        mServiceNameTextView.setText(mServiceDoctor.getName());
        mDoctorNameTextView.setText(context.getString(R.string.full_name, mDoctor.getSurname(),
                mDoctor.getName(), mDoctor.getPatronymic()));
        mServicePriceTextView.setText(context.getString(R.string.price_placeholder,
                String.valueOf(mServiceDoctor.getPrice())));

        Random random = new Random();
        int randColor = random.nextInt(mBackgroundColors.length);
        mCardView.setCardBackgroundColor(mContext.getResources()
                .getColor(mBackgroundColors[randColor]));
        mServiceNameTextView.setTextColor(mContext.getResources()
                .getColor(mTextColors[randColor]));
        mDoctorNameTextView.setTextColor(mContext.getResources()
                .getColor(mTextColors[randColor]));
        mServicePriceTextView.setTextColor(mContext.getResources()
                .getColor(mSecondaryTextColors[randColor]));
        mServiceImageView.setImageResource(mServiceImageViewColor[randColor]);
    }

    @Override
    public void onClick(View v) {
        mListener.onItemClicked(mServiceDoctor.getId());
    }
}

public class ServiceDoctorAdapter extends RecyclerView.Adapter<ServiceDoctorHolder> {
    private List<Doctor> mDoctorList;
    private List<ServiceDoctor> mServiceDoctors;
    private Context mContext;
    private ServiceDoctorAdapterListener mListener;

    public ServiceDoctorAdapter(List<ServiceDoctor> serviceDoctors, List<Doctor> doctors,
                                Context context, ServiceDoctorAdapterListener listener) {
        mServiceDoctors = serviceDoctors;
        mDoctorList = doctors;
        mContext = context;
        mListener = listener;
    }

    @NonNull
    @Override
    public ServiceDoctorHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        view = LayoutInflater.from(mContext).inflate(R.layout.item_service_doctor, parent, false);

        return new ServiceDoctorHolder(view);
//        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
//        return new ServiceHolder(layoutInflater, parent);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceDoctorHolder holder, int position) {
        ServiceDoctor serviceDoctor = mServiceDoctors.get(position);
        for (int i = 0; i < mDoctorList.size(); i++){
            if (serviceDoctor.getDoctorId().equals(mDoctorList.get(i).getUID())) {
                Doctor doctor = mDoctorList.get(i);
                holder.bind(serviceDoctor, doctor, mContext, mListener);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mServiceDoctors.size();
    }

    public void setDoctors(List<ServiceDoctor> serviceDoctors) {
        mServiceDoctors = serviceDoctors;
    }
}
