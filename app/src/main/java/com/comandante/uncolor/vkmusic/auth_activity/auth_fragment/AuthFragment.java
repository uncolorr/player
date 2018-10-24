package com.comandante.uncolor.vkmusic.auth_activity.auth_fragment;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.widget.EditText;

import com.comandante.uncolor.vkmusic.R;
import com.comandante.uncolor.vkmusic.main_activity.MainActivity;
import com.comandante.uncolor.vkmusic.services.music.NewMusicService;
import com.comandante.uncolor.vkmusic.utils.LoadingDialog;
import com.comandante.uncolor.vkmusic.utils.MessageReporter;
import com.flurry.android.FlurryAgent;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Uncolor on 04.09.2018.
 */

@EFragment(R.layout.fragment_auth)
public class AuthFragment extends Fragment implements AuthFragmentContract.View{

    @ViewById
    EditText editTextLogin;

    @ViewById
    EditText editTextPassword;

    private AuthFragmentContract.Presenter presenter;

    private AlertDialog dialogProcessing;

    @AfterViews
    void init(){
        presenter = new AuthFragmentPresenter(getContext(), this);
        dialogProcessing = LoadingDialog.newInstanceWithoutCancelable(getContext(), LoadingDialog.LABEL_LOADING);
    }

    @Click(R.id.buttonSignIn)
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
    public void showProcess() {
        dialogProcessing.show();
    }

    @Override
    public void hideProcess() {
        dialogProcessing.dismiss();
    }

    @Override
    public void showErrorMessage() {
        FlurryAgent.logEvent(getContext().getString(R.string.log_auth_failed));
        MessageReporter.showMessage(getContext(), "Ошибка", "Ошибка при авторизации");
    }

    @Override
    public void login() {
        FlurryAgent.logEvent(getContext().getString(R.string.log_auth_success));
        getActivity().stopService(new Intent(getContext(), NewMusicService.class));
        getActivity().finish();
        startActivity(MainActivity.getInstance(getContext()));
    }
}
