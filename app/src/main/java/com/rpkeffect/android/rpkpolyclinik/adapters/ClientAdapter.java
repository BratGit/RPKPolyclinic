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
import com.rpkeffect.android.rpkpolyclinik.classes.User;
import com.rpkeffect.android.rpkpolyclinik.classes.UserClinic;
import com.rpkeffect.android.rpkpolyclinik.interfaces.ClientsAdapterListener;

import java.util.List;
import java.util.Random;

class ClientHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener {

    private UserClinic mUserClinic;
    private User mUser;
    private ClientsAdapterListener mListener;
    private Context mContext;

    private final TextView mDoctorNameTextView;
    private final TextView mEmailTextView;
    private final CardView mCardView;
    private final ImageView mDoctorImageView;
    private final ImageView mCheckedImageView;

    private final int[] mBackgroundColors = new int[]{R.color.light_green,
            R.color.light_pink, R.color.light_purple};
    private final int[] mTextColors = new int[]{R.color.text_green,
            R.color.text_pink, R.color.text_purple};
    private final int[] mSecondaryTextColors = new int[]{R.color.secondary_text_green,
            R.color.secondary_text_pink, R.color.secondary_text_purple};
    private final int[] mServiceImageViewColor = new int[]{R.drawable.client_green,
            R.drawable.client_pink, R.drawable.client_purple};
    private final int[] mIsCheckedImageViewColor = new int[]{R.drawable.checked_green,
            R.drawable.checked_pink, R.drawable.checked_purple};


    public ClientHolder(@NonNull View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);

        mDoctorNameTextView = (TextView) itemView.findViewById(R.id.item_doctor_name_text_view);
        mEmailTextView = (TextView) itemView.findViewById(R.id.item_position_text_view);
        mCardView = (CardView) itemView.findViewById(R.id.item_doctor_cv);
        mDoctorImageView = (ImageView) itemView.findViewById(R.id.doctor_iv);
        mCheckedImageView = (ImageView) itemView.findViewById(R.id.checked_iv);
    }

    public void bind(UserClinic userClinic, User user, ClientsAdapterListener listener, Context context) {
        mUserClinic = userClinic;
        mUser = user;
        mListener = listener;
        mContext = context;

        mDoctorNameTextView.setText(context.getString(R.string.full_name, user.getSurname(),
                user.getName(), user.getPatronymic()));
        mEmailTextView.setText(mUser.getEmail());

        Random random = new Random();
        int randColor = random.nextInt(mBackgroundColors.length);
        mCardView.setCardBackgroundColor(mContext.getResources()
                .getColor(mBackgroundColors[randColor]));
        mDoctorNameTextView.setTextColor(mContext.getResources()
                .getColor(mTextColors[randColor]));
        mEmailTextView.setTextColor(mContext.getResources()
                .getColor(mSecondaryTextColors[randColor]));
        mDoctorImageView.setImageResource(mServiceImageViewColor[randColor]);

        if (mCheckedImageView != null)
            mCheckedImageView.setImageResource(mIsCheckedImageViewColor[randColor]);
    }

    @Override
    public void onClick(View v) {
        mListener.onItemClicked(mUserClinic);
    }
}

public class ClientAdapter extends RecyclerView.Adapter<ClientHolder> {
    private static final int VIEW_TYPE_BASE = 0;
    private static final int VIEW_TYPE_CHECKED = 1;

    private List<UserClinic> mList;
    private List<User> mUsers;
    private ClientsAdapterListener mListener;
    private Context mContext;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public ClientAdapter(List<UserClinic> userClinics, List<User> users,
                         ClientsAdapterListener listener, Context context) {
        mList = userClinics;
        mUsers = users;
        mListener = listener;
        mContext = context;
    }

    @NonNull
    @Override
    public ClientHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        if (viewType == VIEW_TYPE_BASE)
            view = LayoutInflater.from(mContext).inflate(R.layout.item_person, parent, false);
        else
            view = LayoutInflater.from(mContext).inflate(R.layout.item_person_checked, parent, false);


        return new ClientHolder(view);
//        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
//        return new ServiceHolder(layoutInflater, parent);
    }

    @Override
    public void onBindViewHolder(@NonNull ClientHolder holder, int position) {
        UserClinic userClinic = mList.get(position);
        for (int i = 0; i < mUsers.size(); i++) {
            if (mUsers.get(i).getUID().equals(userClinic.getUserId())) {
                User user = mUsers.get(i);
                holder.bind(userClinic, user, mListener, mContext);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mList.get(position).getClinicId().equals(mAuth.getUid())
                && mList.get(position).getStatus() == UserClinic.STATUS_ACCEPT)
            return VIEW_TYPE_CHECKED;
        else return VIEW_TYPE_BASE;
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
}
