package com.rpkeffect.android.rpkpolyclinik.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rpkeffect.android.rpkpolyclinik.R;
import com.rpkeffect.android.rpkpolyclinik.activities.SelectedUserActivity;
import com.rpkeffect.android.rpkpolyclinik.adapters.ClientAdapter;
import com.rpkeffect.android.rpkpolyclinik.classes.User;
import com.rpkeffect.android.rpkpolyclinik.classes.UserClinic;
import com.rpkeffect.android.rpkpolyclinik.interfaces.ClientsAdapterListener;

import java.util.ArrayList;
import java.util.List;

public class ClientsFragment extends Fragment implements ClientsAdapterListener {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    DatabaseReference mReference = mDatabase.getReference();

    List<UserClinic> mList;
    List<User> mUsers;

    ClientAdapter mAdapter;

    RecyclerView mRecyclerView;
    CardView mEmptyListCardView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_clients, container, false);

        mList = new ArrayList<>();
        mUsers = new ArrayList<>();

        mAdapter = new ClientAdapter(mList, mUsers, this, getActivity());
        mRecyclerView = v.findViewById(R.id.clients_rv);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(),
                RecyclerView.VERTICAL, false));
        mRecyclerView.setAdapter(mAdapter);

        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mList.clear();
                mUsers.clear();
                for (DataSnapshot dataSnapshot : snapshot.child("user_clinic").getChildren()) {
                    UserClinic userClinic = dataSnapshot.getValue(UserClinic.class);
                    if (userClinic.getClinicId().equals(mAuth.getUid())) {
                        mList.add(userClinic);
                    }
                }

                for (DataSnapshot dataSnapshot : snapshot.child("users").getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    mUsers.add(user);
                }

                if (!mUsers.isEmpty() && !mList.isEmpty()) {
                    mAdapter.notifyDataSetChanged();
                    mEmptyListCardView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mEmptyListCardView = v.findViewById(R.id.empty_list_cv);

        return v;
    }

    private void queryDecline(UserClinic userClinic) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final EditText editText = new EditText(getActivity());
        builder
                .setTitle("Причина отказа")
                .setPositiveButton("Ок", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!editText.getText().toString().trim().isEmpty()){
                            mReference.child("user_clinic").child(userClinic.getId())
                                    .setValue(new UserClinic(userClinic.getId(), userClinic.getUserId(),
                                            userClinic.getClinicId(), UserClinic.STATUS_DECLINE,
                                            editText.getText().toString().trim()));
                        } else {
                            dialog.dismiss();
                        }
                    }
                })
                .setNegativeButton("Отменить", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setView(editText)
                .show();
        builder.create();
    }

    @Override
    public void onItemClicked(UserClinic userClinic) {
        if (userClinic.getStatus() == UserClinic.STATUS_SEND
                || userClinic.getStatus() == UserClinic.STATUS_DECLINE) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder
                    .setTitle("Действия с клиентом")
                    .setPositiveButton("Принять", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mReference.child("user_clinic").child(userClinic.getId())
                                    .setValue(new UserClinic(userClinic.getId(), userClinic.getUserId(),
                                            userClinic.getClinicId(), UserClinic.STATUS_ACCEPT, null));
                        }
                    })
                    .setNegativeButton("Отклонить", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            queryDecline(userClinic);
                        }
                    })
                    .show();
            builder.create();
        } else {
            startActivity(SelectedUserActivity.newInstance(getActivity(), userClinic.getUserId()));
        }
    }
}
