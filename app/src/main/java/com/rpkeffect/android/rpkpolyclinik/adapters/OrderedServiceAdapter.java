package com.rpkeffect.android.rpkpolyclinik.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.rpkeffect.android.rpkpolyclinik.R;
import com.rpkeffect.android.rpkpolyclinik.classes.ServiceDoctor;
import com.rpkeffect.android.rpkpolyclinik.classes.User;
import com.rpkeffect.android.rpkpolyclinik.classes.UserService;
import com.rpkeffect.android.rpkpolyclinik.interfaces.ServiceClickListener;
import com.rpkeffect.android.rpkpolyclinik.interfaces.ServiceDoctorAdapterListener;

import java.util.List;
import java.util.Random;

class OrderedServiceHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener {

    private UserService mUserService;
    private User mUser;
    private ServiceDoctor mServiceDoctor;
    private Context mContext;
    private ServiceClickListener mListener;

    private final TextView mClientNameTextView;
    private final TextView mServiceTextView;
    private final CardView mCardView;
    private final ImageView mClientImageView;

    private final int[] mBackgroundColors = new int[]{R.color.light_green,
            R.color.light_pink, R.color.light_purple};
    private final int[] mTextColors = new int[]{R.color.text_green,
            R.color.text_pink, R.color.text_purple};
    private final int[] mSecondaryTextColors = new int[]{R.color.secondary_text_green,
            R.color.secondary_text_pink, R.color.secondary_text_purple};
    private final int[] mServiceImageViewColor = new int[]{R.drawable.client_green,
            R.drawable.client_pink, R.drawable.client_purple};


    public OrderedServiceHolder(@NonNull View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);

        mClientNameTextView = (TextView) itemView.findViewById(R.id.item_doctor_name_text_view);
        mServiceTextView = (TextView) itemView.findViewById(R.id.item_position_text_view);
        mCardView = (CardView) itemView.findViewById(R.id.item_doctor_cv);
        mClientImageView = (ImageView) itemView.findViewById(R.id.doctor_iv);
    }

    public void bind(UserService userService, User user, ServiceDoctor serviceDoctor, Context context, ServiceClickListener listener) {
        mUserService = userService;
        mUser = user;
        mServiceDoctor = serviceDoctor;
        mContext = context;
        mListener = listener;

        mClientNameTextView.setText(context.getString(R.string.full_name, mUser.getSurname(),
                mUser.getName(), mUser.getPatronymic()));
        mServiceTextView.setText(serviceDoctor.getName());

        Random random = new Random();
        int randColor = random.nextInt(mBackgroundColors.length);
        mCardView.setCardBackgroundColor(mContext.getResources()
                .getColor(mBackgroundColors[randColor]));
        mClientNameTextView.setTextColor(mContext.getResources()
                .getColor(mTextColors[randColor]));
        mServiceTextView.setTextColor(mContext.getResources()
                .getColor(mSecondaryTextColors[randColor]));
        mClientImageView.setImageResource(mServiceImageViewColor[randColor]);
    }

    @Override
    public void onClick(View v) {
//        if (mListener == null || mUserService == null)

        mListener.onServiceUserClick(mUser.getUID(), mServiceDoctor.getId(), mUserService.getVisitDate());
    }
}

public class OrderedServiceAdapter extends RecyclerView.Adapter<OrderedServiceHolder> {
    private List<UserService> mList;
    private List<User> mUsers;
    private List<ServiceDoctor> mServiceDoctors;
    private Context mContext;
    private ServiceClickListener mListener;

    public OrderedServiceAdapter(List<UserService> orderedServices, List<User> users,
                                 List<ServiceDoctor> serviceDoctors, Context context, ServiceClickListener listener) {
        mList = orderedServices;
        mUsers = users;
        mServiceDoctors = serviceDoctors;
        mContext = context;
        mListener = listener;
    }

    @NonNull
    @Override
    public OrderedServiceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        view = LayoutInflater.from(mContext).inflate(R.layout.item_person, parent, false);

        return new OrderedServiceHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderedServiceHolder holder, int position) {
        for (User user : mUsers) {
            if (user.getUID().equals(mList.get(position).getUserId())) {
                for (ServiceDoctor serviceDoctor : mServiceDoctors) {
                    if (serviceDoctor.getId().equals(mList.get(position).getServiceId())) {
                        holder.bind(mList.get(position), user, serviceDoctor, mContext, mListener);
                    }
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
}

