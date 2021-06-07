package com.rpkeffect.android.rpkpolyclinik.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rpkeffect.android.rpkpolyclinik.R;
import com.rpkeffect.android.rpkpolyclinik.classes.User;
import com.rpkeffect.android.rpkpolyclinik.dialogs.DatePickerFragment;
import com.rpkeffect.android.rpkpolyclinik.interfaces.UserRegisteredListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PersonalInfoFragment extends Fragment {
    private static final String ARG_MAIL = "mail";
    private static final String ARG_PASSWORD = "password";
    private static final String DIALOG_DATE = "DialogDate";
    private static final int REQUEST_DATE = 0;

    SimpleDateFormat mFormatter = new SimpleDateFormat("dd MMMM yyyy");

    FirebaseAuth mAuth;
    String mUID;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference mUsers = database.getReference("users");

    boolean isSuccessAuth;
    private ProgressDialog mProgressDialog;

    String mMail, mPassword;
    EditText mSurname, mName, mPatronymic;
    Date mBirthDate;
    Button mNext, mBirthDateButton;
    TextView mErrorTextView;

    UserRegisteredListener mListener;

    public void setListener(UserRegisteredListener listener) {
        mListener = listener;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_DATE) {
            Date date = (Date) data
                    .getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mBirthDate = date;
            mBirthDateButton.setText(mFormatter.format(date));
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mMail = args.getString(ARG_MAIL);
        mPassword = args.getString(ARG_PASSWORD);
        mAuth = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_personal_info, container, false);

        mSurname = (EditText) v.findViewById(R.id.surname_edit_text);
        mName = (EditText) v.findViewById(R.id.name_edit_text);
        mPatronymic = (EditText) v.findViewById(R.id.patronymic_edit_text);
        mBirthDateButton = (Button) v.findViewById(R.id.birth_date_button);
        mErrorTextView = (TextView) v.findViewById(R.id.personal_info_empty_text_view);

        mBirthDate = new Date(System.currentTimeMillis());
        mBirthDateButton.setText(mFormatter.format(mBirthDate));
        mBirthDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(mBirthDate);
                dialog.setTargetFragment(PersonalInfoFragment.this, REQUEST_DATE);
                dialog.show(manager, DIALOG_DATE);
            }
        });

        mNext = (Button) v.findViewById(R.id.confirm_button);
        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Pattern pattern = Pattern.compile("[" + "а-яА-ЯёЁ" + "]" + "*");
                Matcher matcherSurname = pattern.matcher(mSurname.getText().toString().trim());
                Matcher matcherName = pattern.matcher(mName.getText().toString().trim());
                Matcher matcherPatronymic = pattern.matcher(mPatronymic.getText().toString().trim());

                if (!mSurname.getText().toString().trim().isEmpty()
                        && !mName.getText().toString().trim().isEmpty()
                        && !mPatronymic.getText().toString().trim().isEmpty()
                        && matcherSurname.matches() && matcherName.matches() && matcherPatronymic.matches()) {
                    mErrorTextView.setVisibility(View.GONE);
                    registerNewUser(mMail, mPassword);
                } else if (!matcherSurname.matches() || !matcherName.matches()
                        || !matcherPatronymic.matches()) {
                    mErrorTextView.setVisibility(View.VISIBLE);
                    mErrorTextView.setText("Поля должны состоять из кириллицы!");
                    emptyValuesAnimation();
                } else {
                    mErrorTextView.setVisibility(View.VISIBLE);
                    mErrorTextView.setText("Заполните все поля!");
                    emptyValuesAnimation();
                }
            }
        });

        return v;
    }

    private void registerNewUser(String email, String password) {
        mProgressDialog = ProgressDialog.show(getActivity(), "Ожидайте",
                "Регистрация", true, false);
        mProgressDialog.show();
        mAuth
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(),
                                    "Пользователь зарегистрирован",
                                    Toast.LENGTH_LONG)
                                    .show();
                            isSuccessAuth = true;
                            mUID = task.getResult().getUser().getUid();
                            User user = new User(mUID, mMail,
                                    mSurname.getText().toString().trim(),
                                    mName.getText().toString().trim(),
                                    mPatronymic.getText().toString().trim(),
                                    mBirthDate);
                            mUsers.push().setValue(user);
                        } else {
                            isSuccessAuth = false;
                            Toast.makeText(
                                    getActivity(),
                                    "Возникла ошибка при регистрации",
                                    Toast.LENGTH_LONG)
                                    .show();
                        }

                        mProgressDialog.dismiss();
                        mListener.onUserRegistered();
                    }
                });
    }

    private void emptyValuesAnimation() {
        Animation anim = AnimationUtils.loadAnimation(
                getActivity(),
                R.anim.error_animation);
        mErrorTextView.setVisibility(View.VISIBLE);
        mErrorTextView.startAnimation(anim);
    }

    public static PersonalInfoFragment getInstance(String email, String password,
                                                   UserRegisteredListener listener) {
        PersonalInfoFragment fragment = new PersonalInfoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MAIL, email);
        args.putString(ARG_PASSWORD, password);
        fragment.setArguments(args);
        fragment.setListener(listener);
        return fragment;
    }
}
