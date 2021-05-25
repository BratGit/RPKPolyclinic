package com.rpkeffect.android.rpkpolyclinik.utils;

import android.os.AsyncTask;

import com.rpkeffect.android.rpkpolyclinik.interfaces.RegistrationCodeSentListener;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;

public class SendMail extends AsyncTask<Message, String, String> {
    RegistrationCodeSentListener mListener;

    public void setListener(RegistrationCodeSentListener listener) {
        mListener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mListener.onStartSending();
    }

    @Override
    protected String doInBackground(Message... messages) {
        try {
            Transport.send(messages[0]);
            return "Success";
        } catch (MessagingException e) {
            e.printStackTrace();
            return "Error";
        }
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if(s.equals("Success")){
            mListener.onCodeSent();
        } else {
            mListener.onSendFailed();
        }
    }
}
