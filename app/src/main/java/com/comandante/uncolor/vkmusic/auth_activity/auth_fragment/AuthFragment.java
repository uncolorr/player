package com.comandante.uncolor.vkmusic.auth_activity.auth_fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.comandante.uncolor.vkmusic.R;
import com.comandante.uncolor.vkmusic.main_activity.MainActivity;
import com.comandante.uncolor.vkmusic.utils.LoadingDialog;
import com.comandante.uncolor.vkmusic.utils.MessageReporter;
import com.comandante.uncolor.vkmusic.widgets.AuthCaptchaDialog;
import com.flurry.android.FlurryAgent;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Uncolor on 04.09.2018.
 */

public class AuthFragment extends Fragment implements AuthFragmentContract.View,
        AuthCaptchaDialog.CaptchaListener {

    @BindView(R.id.editTextLogin)
    EditText editTextLogin;

    @BindView(R.id.editTextPassword)
    EditText editTextPassword;

    private AuthFragmentContract.Presenter presenter;

    private AlertDialog dialogProcessing;


    public static AuthFragment newInstance() {

        Bundle args = new Bundle();

        AuthFragment fragment = new AuthFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_auth, container, false);
        ButterKnife.bind(this, view);
        init();
        return view;

    }

    private void init(){
        presenter = new AuthFragmentPresenter(getContext(), this);
        dialogProcessing = LoadingDialog.newInstanceWithoutCancelable(getContext(), LoadingDialog.LABEL_LOADING);
}

    @OnClick(R.id.buttonSignIn)
    void onSignInButtonClick(){
        if(isFieldsFilling()) {
            presenter.onSignInButtonClick(editTextLogin.getText().toString(),
                    editTextPassword.getText().toString());
        }
    }

    private boolean isFieldsFilling(){
        return !editTextLogin.getText().toString().isEmpty() &&
                !editTextPassword.getText().toString().isEmpty();
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showLoadingDialog() {
        dialogProcessing.show();
    }

    @Override
    public void hideLoadingDialog() {
        dialogProcessing.dismiss();
    }

    @Override
    public void showErrorMessage(String message) {
        FlurryAgent.logEvent(message);
        MessageReporter.showMessage(getContext(), "Ошибка", message);
    }

    @Override
    public void showCaptchaDialog(String c_sid, String c_img) {
        AuthCaptchaDialog tempCaptchaDialog = AuthCaptchaDialog.newInstance(this, c_sid, c_img);
        tempCaptchaDialog.show(Objects.requireNonNull(getActivity())
                .getSupportFragmentManager(), "CaptchaDialog");
    }

    @Override
    public void signIn() {
        FlurryAgent.logEvent(Objects.requireNonNull(getContext()).getString(R.string.log_auth_success));
        Objects.requireNonNull(getActivity()).finish();
        startActivity(MainActivity.getInstance(getContext()));
    }

    @Override
    public void onCaptchaEntered(String captcha, String cSid) {
        if(!isFieldsFilling()) {
            return;
        }
        presenter.onSignInWithCaptchaButtonClick(editTextLogin.getText().toString(),
                editTextPassword.getText().toString(), cSid, captcha);
    }
}
