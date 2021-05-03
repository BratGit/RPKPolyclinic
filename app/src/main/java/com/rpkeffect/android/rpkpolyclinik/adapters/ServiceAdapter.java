package com.rpkeffect.android.rpkpolyclinik.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rpkeffect.android.rpkpolyclinik.classes.OrderedService;
import com.rpkeffect.android.rpkpolyclinik.R;
import com.rpkeffect.android.rpkpolyclinik.classes.Service;
import com.rpkeffect.android.rpkpolyclinik.activities.ServiceInfoActivity;

import java.util.List;
import java.util.Random;

class ServiceHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener {

    private Service mService;
    private Context mContext;

    private final TextView mServiceNameTextView;
    private final TextView mServicePriceTextView;
    private final CardView mCardView;
    private final ImageView mServiceImageView;

    private final ImageView mIsCheckedImageView;

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


    public ServiceHolder(@NonNull View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);

        mServiceNameTextView = (TextView) itemView.findViewById(R.id.item_service_name_text_view);
        mServicePriceTextView = (TextView) itemView.findViewById(R.id.item_service_price_text_view);
        mCardView = (CardView) itemView.findViewById(R.id.item_service_cv);
        mServiceImageView = (ImageView) itemView.findViewById(R.id.service_iv);

        mIsCheckedImageView = (ImageView) itemView.findViewById(R.id.is_checked_iv);
    }

    public void bind(Service service, Context context) {
        mService = service;
        mContext = context;

        mServiceNameTextView.setText(mService.getName());
        if (mService.getPrice() == 0.0f)
            mServicePriceTextView.setText("Бесплатно");
        else
            mServicePriceTextView.setText(String.valueOf(mService.getPrice()) + "₽");

        Random random = new Random();
        int randColor = random.nextInt(mBackgroundColors.length);
        mCardView.setCardBackgroundColor(mContext.getResources()
                .getColor(mBackgroundColors[randColor]));
        mServiceNameTextView.setTextColor(mContext.getResources()
                .getColor(mTextColors[randColor]));
        mServicePriceTextView.setTextColor(mContext.getResources()
                .getColor(mSecondaryTextColors[randColor]));
        mServiceImageView.setImageResource(mServiceImageViewColor[randColor]);

        if (mIsCheckedImageView != null)
            mIsCheckedImageView.setImageResource(mIsCheckedImageViewColor[randColor]);
    }

    @Override
    public void onClick(View v) {
        Intent intent = ServiceInfoActivity.newIntent(mContext, mService.getId());
        mContext.startActivity(intent);
    }
}

public class ServiceAdapter extends RecyclerView.Adapter<ServiceHolder> {
    public static final int SERVICE_TYPE_BASE = 0;
    public static final int SERVICE_TYPE_ORDERED = 1;

    FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    DatabaseReference mDatabaseReference = mDatabase.getReference();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    String mOrderedServices;

    private List<Service> mList;
    private Context mContext;

    public ServiceAdapter(List<Service> services, Context context) {
        mList = services;
        mContext = context;

        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.child("ordered_services").getChildren()) {
                    OrderedService orderedService = userSnapshot.getValue(OrderedService.class);
                    if (orderedService.getUserId().equals(mAuth.getUid())) {
                        mOrderedServices += " " + orderedService.getServiceId();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @NonNull
    @Override
    public ServiceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == SERVICE_TYPE_ORDERED) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_service_ordered, parent, false);
        } else {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_service, parent, false);
        }
        return new ServiceHolder(view);
//        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
//        return new ServiceHolder(layoutInflater, parent);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceHolder holder, int position) {
        Service service = mList.get(position);
        holder.bind(service, mContext);
    }

    @Override
    public int getItemViewType(int position) {
        if (mList != null && !mList.isEmpty() && mOrderedServices != null) {
            if (mOrderedServices.contains(String.valueOf(mList.get(position).getId())))
                return SERVICE_TYPE_ORDERED;
            else
                return SERVICE_TYPE_BASE;
        } else
            return SERVICE_TYPE_BASE;
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void setServices(List<Service> services) {
        mList = services;
    }
}
