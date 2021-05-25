package com.rpkeffect.android.rpkpolyclinik.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.rpkeffect.android.rpkpolyclinik.R;


public class ImageDisplayFragment extends DialogFragment {
    ImageView mImageView;
    ProgressBar mProgressBar;

    FirebaseStorage mStorage = FirebaseStorage.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    StorageReference mReference;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_display_image, null);

        mImageView = v.findViewById(R.id.display_image_iv);
        mProgressBar = v.findViewById(R.id.preload_pb_dialog);

        setupPhoto();

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setCancelable(true)
                .create();
    }

    private void setupPhoto(){
//        mReference = mStorage.getReference()
//                .child(getString(R.string.user_photo_reference, mAuth.getUid()));


        mReference.getBytes(2048 * 2048)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        mImageView.setImageBitmap(bitmap);
                        mProgressBar.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Нет фотографии пользователя",
                                Toast.LENGTH_SHORT).show();
                        mImageView.setImageResource(R.drawable.user);
                        mProgressBar.setVisibility(View.GONE);

                    }
                });
    }

    public void setReferences(StorageReference references){
        mReference = references;
    }
}
