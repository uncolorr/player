package com.comandante.uncolor.vkmusic.main_activity.settings_fragment;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.comandante.uncolor.vkmusic.R;
import com.comandante.uncolor.vkmusic.application.App;
import com.comandante.uncolor.vkmusic.auth_activity.AuthActivity;
import com.comandante.uncolor.vkmusic.services.MusicService;
import com.flurry.android.FlurryAgent;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Uncolor on 25.08.2018.
 */

@EFragment(R.layout.fragment_settings)
public class SettingsFragment extends Fragment implements SettingsFragmentContract.View{

    public static final String ACTION_CLEAR_CACHE = "com.example.uncolor.action.CLEAR_CACHE";

    @ViewById
    CircleImageView imageViewAvatar;

    @ViewById
    TextView textViewName;

    private SettingsFragmentContract.Presenter presenter;

    @AfterViews
    void init(){
        presenter = new SettingsFragmentPresenter(getContext(), this);
        presenter.onLoadUserInfo();
    }

    @Click(R.id.buttonClearCache)
    void onButtonClearCacheClick(){
        presenter.showClearCacheDialog();
    }

    @Click(R.id.buttonContactWithDevelopers)
    void onButtonContactWithDevelopersClick(){

    }

    @Click(R.id.buttonWebsite)
    void onButtonWebsiteClick(){

    }

    @Click(R.id.buttonPolitics)
    void onButtonPoliticsClick(){

    }

    @Click(R.id.buttonExit)
    void onButtonExitClick(){
        presenter.showExitDialog();

    }

    @Override
    public void showUserInfo(String name, String avatarUrl) {
        textViewName.setText(name);
        Glide.with(getContext())
                .load(avatarUrl)
                .into(imageViewAvatar);
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void logOut() {
        FlurryAgent.logEvent(getContext().getString(R.string.log_logout));
        App.logOut();
        getActivity().stopService(new Intent(getContext(), MusicService.class));
        getActivity().finishAffinity();
        startActivity(AuthActivity.getInstance(getContext()));
    }


}
