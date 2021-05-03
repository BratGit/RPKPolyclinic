package com.rpkeffect.android.rpkpolyclinik.fragments;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.rpkeffect.android.rpkpolyclinik.R;
import com.rpkeffect.android.rpkpolyclinik.interfaces.CorrectCodeInsertedListener;
import com.rpkeffect.android.rpkpolyclinik.interfaces.RegistrationCodeSentListener;
import com.rpkeffect.android.rpkpolyclinik.mail.SendMail;

import java.util.Properties;
import java.util.Random;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class ConfirmFragment extends Fragment implements RegistrationCodeSentListener {
    private final static int STATUS_START_SENDING = 0;
    private static final int STATUS_SUCCESS_SENDING = 1;
    private static final int STATUS_FAIL_SENDING = 2;
    private final static String ARG_CODE = "code";
    private final static String ARG_MAIL = "mail";
    private static final String ARG_IS_DOCTOR_REG = "isDoctorReg";

    private ProgressDialog mProgressDialog;

    Button mConfirmButton;
    EditText mCodeEditText;
    TextView mResendCodeTextView, mYourMailTextView, mIncorrectCodeTextView;

    Bundle args;
    String mRightCode, mEmail;
    SendMail mSendMail;
    Handler h;

    boolean mIsDoctorReg = false;

    CorrectCodeInsertedListener mListener;

    public void setListener(CorrectCodeInsertedListener listener) {
        mListener = listener;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ARG_CODE, mRightCode);
        outState.putString(ARG_MAIL, mEmail);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        args = getArguments();
        mRightCode = args.getString(ARG_CODE);
        mEmail = args.getString(ARG_MAIL);
        mIsDoctorReg = args.getBoolean(ARG_IS_DOCTOR_REG);

        if (savedInstanceState != null) {
            mRightCode = savedInstanceState.getString(ARG_CODE);
            mEmail = savedInstanceState.getString(ARG_MAIL);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_confirm, container, false);

        String[] registrationChoose = new String[]{getString(R.string.to_user_register),
                getString(R.string.to_clinic_register)};

        mSendMail = new SendMail();
        mSendMail.setListener(this);

        mIncorrectCodeTextView = (TextView)v.findViewById(R.id.incorrect_code_text_view);

        mYourMailTextView = (TextView) v.findViewById(R.id.your_email);
        mYourMailTextView.setText(mEmail);

        mCodeEditText = (EditText) v.findViewById(R.id.confirm_code);
        mConfirmButton = (Button) v.findViewById(R.id.confirm_button);
        mConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCodeEditText.getText().toString().trim().equals(mRightCode) && !mIsDoctorReg) {
                    mIncorrectCodeTextView.setVisibility(View.GONE);
                    AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                            .setItems(registrationChoose, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which){
                                        case 0://Регистрация пользователя
                                            mListener.onCorrectCodeInserted();
                                            break;
                                        case 1://Регистрация мед. учреждения
                                            mListener.onCorrectCodeInsertedRegClinic();
                                            break;
                                    }
                                }
                            })
                            .create();
                    alertDialog.show();
                } else if (mCodeEditText.getText().toString().trim().equals(mRightCode)
                        && mIsDoctorReg){
                    mListener.onCorrectCodeInserted();
                } else {
                    mIncorrectCodeTextView.setVisibility(View.VISIBLE);
                }
            }
        });

        mResendCodeTextView = (TextView) v.findViewById(R.id.resend_code_text_view);
        mResendCodeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Random random = new Random();
                int i = random.nextInt(8999 + 1);
                i += 1000;
                mRightCode = String.valueOf(i);
                {
                    Properties props = new Properties();
                    props.put("mail.smtp.host", "smtp.gmail.com");
                    props.put("mail.smtp.socketFactory.port", "465");
                    props.put("mail.smtp.socketFactory.class",
                            "javax.net.ssl.SSLSocketFactory");
                    props.put("mail.smtp.auth", "true");
                    props.put("mail.smtp.port", "465");
                    Session session = Session.getInstance(props, new Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(getString(R.string.email_login),
                                    getString(R.string.email_password));
                        }
                    });

                    try {
                        Message message = new MimeMessage(session);
                        message.setFrom(new InternetAddress(getString(R.string.email_login)));
                        message.setRecipients(Message.RecipientType.TO,
                                InternetAddress.parse(mEmail));
                        message.setSubject(getString(R.string.registration_code));
                        message.setText(getString(R.string.your_registration_code) + mRightCode);

                        mSendMail.execute(message);

                    } catch (MessagingException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        h = new Handler() {
            @Override
            public void handleMessage(@NonNull android.os.Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case STATUS_START_SENDING:
                        mProgressDialog = ProgressDialog.show(getActivity(), "Ожидайте",
                                "Отправка сообщения", true, false);
                        break;
                    case STATUS_SUCCESS_SENDING:
                        mProgressDialog.dismiss();
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setCancelable(false);
                        builder.setTitle("Успешно");
                        builder.setMessage("Сообщение отправлено");
                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        builder.show();

                        break;
                    case STATUS_FAIL_SENDING:
                        Toast.makeText(getActivity().getApplicationContext(), "Что-то пошло не так",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        return v;
    }

    public static ConfirmFragment getInstance(String code, String mail,
                                              CorrectCodeInsertedListener listener,
                                              boolean isDoctorReg) {
        ConfirmFragment instance;
        Bundle args = new Bundle();
        args.putString(ARG_CODE, code);
        args.putString(ARG_MAIL, mail);
        args.putBoolean(ARG_IS_DOCTOR_REG, isDoctorReg);
        instance = new ConfirmFragment();
        instance.setArguments(args);
        instance.setListener(listener);
        return instance;
    }

    @Override
    public void onCodeSent() {
        h.sendEmptyMessage(STATUS_SUCCESS_SENDING);
    }

    @Override
    public void onStartSending() {
        h.sendEmptyMessage(STATUS_START_SENDING);
    }

    @Override
    public void onSendFailed() {
        h.sendEmptyMessage(STATUS_FAIL_SENDING);
    }
}
