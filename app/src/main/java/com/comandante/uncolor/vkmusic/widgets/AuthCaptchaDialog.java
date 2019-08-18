package com.comandante.uncolor.vkmusic.widgets;


import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.comandante.uncolor.vkmusic.R;
import com.comandante.uncolor.vkmusic.application.App;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AuthCaptchaDialog extends DialogFragment {

    public static final String ARG_C_SID = "sid";
    public static final String ARG_C_IMG = "img";

    @BindView(R.id.imageViewCaptcha)
    ImageView imageViewCaptcha;

    @BindView(R.id.editTextCaptcha)
    EditText editTextCaptcha;

    private CaptchaListener listener;

    private String cSid;
    private String cImg;

    public static AuthCaptchaDialog newInstance(CaptchaListener listener, String cSid, String cImg) {
        Bundle args = new Bundle();
        args.putString(ARG_C_SID, cSid);
        args.putString(ARG_C_IMG, cImg);
        AuthCaptchaDialog fragment = new AuthCaptchaDialog();
        fragment.setArguments(args);
        fragment.listener = listener;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() == null) {
            throw new RuntimeException("Arguments for dialog have null pointer");
        }
        cSid = getArguments().getString(ARG_C_SID);
        cImg = getArguments().getString(ARG_C_IMG);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
        View dialogView = View.inflate(getContext(), R.layout.dialog_temp_send_captcha, null);
        ButterKnife.bind(this, dialogView);
        dialogBuilder.setView(dialogView);
        return dialogBuilder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        Glide.with(App.getContext()).load(cImg).into(imageViewCaptcha);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        App.Log("Captcha dialog onDestroy");
    }

    @OnClick(R.id.buttonSend)
    void onButtonSendCaptchaClick() {
        if (editTextCaptcha.getText().toString().isEmpty()) {
            return;
        }
        listener.onCaptchaEntered(editTextCaptcha.getText().toString(), cSid);
        dismiss();
    }

    public interface CaptchaListener {
        void onCaptchaEntered(String captcha, String cSid);
    }
}
