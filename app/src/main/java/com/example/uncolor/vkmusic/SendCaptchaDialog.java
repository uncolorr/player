package com.example.uncolor.vkmusic;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;

/**
 * Created by Uncolor on 04.09.2018.
 */
public class SendCaptchaDialog extends DialogFragment {

    public static SendCaptchaDialog newInstance() {
        Bundle args = new Bundle();
        SendCaptchaDialog fragment = new SendCaptchaDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_send_captcha, null);
        return new AlertDialog.Builder(getActivity())
                .setPositiveButton(android.R.string.ok, null)
                .setView(view)
                .create();
    }
}
