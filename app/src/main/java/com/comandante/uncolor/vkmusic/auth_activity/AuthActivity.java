package com.comandante.uncolor.vkmusic.auth_activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.comandante.uncolor.vkmusic.R;
import com.comandante.uncolor.vkmusic.application.AppSettings;
import com.comandante.uncolor.vkmusic.main_activity.MainActivity;
import com.comandante.uncolor.vkmusic.widgets.StaticViewPager;

import butterknife.BindView;
import butterknife.ButterKnife;


public class AuthActivity extends AppCompatActivity {

    @BindView(R.id.viewPager)
    StaticViewPager viewPager;

    private AuthFragmentPagerAdapter adapter;

    public static Intent getInstance(Context context) {
        return new Intent(context, AuthActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        if (AppSettings.isAuth()) {
            startActivity(MainActivity.getInstance(this));
            finish();
            return;
        }
        adapter = new AuthFragmentPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.setPagingEnabled(false);

    }
}
