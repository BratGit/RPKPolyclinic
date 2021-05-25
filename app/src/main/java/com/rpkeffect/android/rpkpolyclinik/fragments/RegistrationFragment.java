package com.rpkeffect.android.rpkpolyclinik.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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

import com.rpkeffect.android.rpkpolyclinik.R;
import com.rpkeffect.android.rpkpolyclinik.interfaces.CodeResendListener;
import com.rpkeffect.android.rpkpolyclinik.interfaces.RegistrationCodeSentListener;
import com.rpkeffect.android.rpkpolyclinik.utils.SendMail;

import java.util.Properties;
import java.util.Random;
import java.util.regex.Pattern;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class RegistrationFragment extends Fragment implements RegistrationCodeSentListener {

    private final static int STATUS_START_SENDING = 0;
    private static final int STATUS_SUCCESS_SENDING = 1;
    private static final int STATUS_FAIL_SENDING = 2;

    ProgressDialog mProgressDialog;
    EditText mEmail, mPassword, mRepeat;
    Button register_button;
    TextView mErrorTextView;
    String mError, mCode;

    CodeResendListener mListener;
    static Handler h;
    SendMail mSendMail;

    public void setListener(CodeResendListener listener) {
        mListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_registration, container, false);


        mSendMail = new SendMail();
        mSendMail.setListener(this);

        mEmail = (EditText) v.findViewById(R.id.email);
        mPassword = (EditText) v.findViewById(R.id.password);
        mRepeat = (EditText) v.findViewById(R.id.password_repeat);
        mErrorTextView = (TextView) v.findViewById(R.id.error_text_view);

        register_button = (Button) v.findViewById(R.id.register_button);
        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validateForm(mEmail.getText().toString().replaceAll(" ", ""),
                        mPassword.getText().toString().replaceAll(" ", ""),
                        mRepeat.getText().toString().replaceAll(" ", ""))) {
                    mErrorTextView.setVisibility(View.VISIBLE);
                    mErrorTextView.setText(mError);
                    errorTextViewAnimation();
                } else {
                    mErrorTextView.setVisibility(View.GONE);

                    Random random = new Random();
                    int i = random.nextInt(8999 + 1);
                    i += 1000;
                    mCode = String.valueOf(i);
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
                                    InternetAddress.parse(mEmail.getText().toString().trim()));
                            message.setSubject(getString(R.string.registration_code));
                            message.setText(getString(R.string.your_registration_code) + mCode);

                            mSendMail.execute(message);

                        } catch (MessagingException e) {
                            e.printStackTrace();
                        }
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

                        mListener.onCodeReceived(mCode, mEmail.getText().toString().trim(),
                                mPassword.getText().toString().trim());
                        Log.d("myLog", "registration fragment: " + mEmail.getText().toString());

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

    private void errorTextViewAnimation() {
        Animation anim = AnimationUtils.loadAnimation(
                getActivity(),
                R.anim.error_animation);
        mErrorTextView.setVisibility(View.VISIBLE);
        mErrorTextView.startAnimation(anim);
    }

    private boolean validateForm(String email, String password, String repeat) {
        mError = null;
        if (isValueEmpty(mEmail) || isValueEmpty(mPassword) || isValueEmpty(mRepeat)) {
            mError = getString(R.string.empty_values);
            return false;
        }
        if (!isEmailValid(email)) {
            mError = getString(R.string.incorrect_email);
            return false;
        }
        if (containsCyrillic(email) || containsCyrillic(password) || containsCyrillic(repeat)) {
            mError = getString(R.string.contains_cyrillic);
            return false;
        }
        if (!password.equals(repeat)) {
            mError = getString(R.string.password_mismatch);
            return false;
        }
        return true;
    }


    private boolean isValueEmpty(EditText editText) {
        if (editText.getText().toString().isEmpty()) return true;
        return false;
    }

    private boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean containsCyrillic(String s) {
        return Pattern.matches(".*\\p{InCyrillic}.*", s);
    }


    public static RegistrationFragment getInstance(CodeResendListener listener) {
        RegistrationFragment fragment = new RegistrationFragment();
        fragment.setListener(listener);
        return fragment;
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
