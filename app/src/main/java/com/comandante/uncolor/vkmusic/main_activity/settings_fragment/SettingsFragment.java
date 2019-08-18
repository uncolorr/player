package com.comandante.uncolor.vkmusic.main_activity.settings_fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.comandante.uncolor.vkmusic.R;
import com.comandante.uncolor.vkmusic.application.App;
import com.comandante.uncolor.vkmusic.application.AppSettings;
import com.comandante.uncolor.vkmusic.auth_activity.AuthActivity;
import com.comandante.uncolor.vkmusic.services.music.NewMusicService;
import com.flurry.android.FlurryAgent;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Uncolor on 25.08.2018.
 */


public class SettingsFragment extends Fragment implements SettingsFragmentContract.View{

    public static final String ACTION_CLEAR_CACHE = "com.example.uncolor.action.CLEAR_CACHE";

    @BindView(R.id.imageViewAvatar)
    CircleImageView imageViewAvatar;

    @BindView(R.id.textViewName)
    TextView textViewName;

    private SettingsFragmentContract.Presenter presenter;

    public static SettingsFragment newInstance() {

        Bundle args = new Bundle();

        SettingsFragment fragment = new SettingsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.bind(this, view);
        init();
        return view;

    }

    private void init(){
        presenter = new SettingsFragmentPresenter(getContext(), this);
        presenter.onLoadUserInfo();
    }

    @OnClick(R.id.buttonClearCache)
    void onButtonClearCacheClick(){
        presenter.showClearCacheDialog();
    }


    @OnClick(R.id.buttonExit)
    void onButtonExitClick(){
        presenter.showExitDialog();

    }

    @OnClick(R.id.buttonPolitics)
    void onButtonPoliticsClick(){
        String url = "https://uncolorr.github.io/privacy_policy_vmusic.html";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    @Override
    public void showUserInfo(String name, String avatarUrl) {
        textViewName.setText(name);
        Glide.with(App.getContext())
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
        AppSettings.logOut();
        getActivity().stopService(new Intent(getContext(), NewMusicService.class));
        getActivity().finishAffinity();
        startActivity(AuthActivity.getInstance(getContext()));
    }


}
