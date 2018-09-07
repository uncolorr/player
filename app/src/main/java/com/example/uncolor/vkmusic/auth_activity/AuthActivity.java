package com.example.uncolor.vkmusic.auth_activity;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.uncolor.vkmusic.R;
import com.example.uncolor.vkmusic.widgets.StaticViewPager;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_auth)
public class AuthActivity extends AppCompatActivity implements
        BottomNavigationView.OnNavigationItemSelectedListener{

    @ViewById
    StaticViewPager viewPager;

    @ViewById
    BottomNavigationView bottomNavigationView;

    private AuthFragmentPagerAdapter adapter;

    @AfterViews
    void init(){
        adapter = new AuthFragmentPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.action_music:
                viewPager.setCurrentItem(0, true);
                break;
            case R.id.action_auth:
                viewPager.setCurrentItem(1, true);
                break;
        }
        return true;
    }
}
