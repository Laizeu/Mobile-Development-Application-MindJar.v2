package com.example.myapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class Dashboard extends AppCompatActivity implements View.OnClickListener {

    private static final String KEY_SELECTED_ICON_ID = "selected_icon_id";

    // Tab fragments (created once)
    private Fragment homeFragment, realizationFragment, hopeFragment, videosFragment, hotlineFragment;

    // Track which tab fragment is currently visible
    private Fragment activeFragment;

    private ImageView homeIcon, realizationIcon, hopeIcon, videosIcon, hotlineIcon;
    private int selectedIconId = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        // find views
        homeIcon = findViewById(R.id.homeIcon);
        realizationIcon = findViewById(R.id.realizationIcon);
        hopeIcon = findViewById(R.id.hopeIcon);
        videosIcon = findViewById(R.id.videosIcon);
        hotlineIcon = findViewById(R.id.hotlineIcon);

        homeIcon.setOnClickListener(this);
        realizationIcon.setOnClickListener(this);
        hopeIcon.setOnClickListener(this);
        videosIcon.setOnClickListener(this);
        hotlineIcon.setOnClickListener(this);

        FragmentManager fm = getSupportFragmentManager();

        if (savedInstanceState == null) {
            // Create fragment once
            homeFragment = new HomeFragment();
            realizationFragment = new RealizationFragment();
            hopeFragment = new HopeFragment();
            videosFragment = new VideosFragment();
            hotlineFragment = new HotlineFragment();

            // Add them once, hide the others
            fm.beginTransaction()
                    .add(R.id.fragmentContainerView2, homeFragment, "HOME")
                    .add(R.id.fragmentContainerView2, realizationFragment, "REALIZATION").hide(realizationFragment)
                    .add(R.id.fragmentContainerView2, hopeFragment, "HOPE").hide(hopeFragment)
                    .add(R.id.fragmentContainerView2, videosFragment, "VIDEOS").hide(videosFragment)
                    .add(R.id.fragmentContainerView2, hotlineFragment, "HOTLINE").hide(hotlineFragment)
                    .commit();

            activeFragment = homeFragment;

            selectedIconId = R.id.homeIcon;
            tintSelection(homeIcon);

        } else {
            // Reconnect to existing fragments after rotation/recreate
            homeFragment = fm.findFragmentByTag("HOME");
            realizationFragment = fm.findFragmentByTag("REALIZATION");
            hopeFragment = fm.findFragmentByTag("HOPE");
            videosFragment = fm.findFragmentByTag("VIDEOS");
            hotlineFragment = fm.findFragmentByTag("HOTLINE");

            selectedIconId = savedInstanceState.getInt(KEY_SELECTED_ICON_ID, R.id.homeIcon);

            // Set the correct active fragment based on selected tab
            activeFragment = fragmentForIcon(selectedIconId);
            if (activeFragment == null) activeFragment = homeFragment;

            // Make sure the correct fragment is shown
            fm.beginTransaction()
                    .hide(homeFragment)
                    .hide(realizationFragment)
                    .hide(hopeFragment)
                    .hide(videosFragment)
                    .hide(hotlineFragment)
                    .show(activeFragment)
                    .commit();

            // Tint correct icon
            View v = findViewById(selectedIconId);
            if (v != null) changeColorIcon(v);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SELECTED_ICON_ID, selectedIconId);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.homeIcon || id == R.id.realizationIcon || id == R.id.hopeIcon
                || id == R.id.videosIcon || id == R.id.hotlineIcon) {
            changeColorIcon(v);
        }
    }

    private void changeColorIcon(View v) {
        // reset all to black
        int black = ContextCompat.getColor(this, R.color.black);
        homeIcon.setColorFilter(black);
        realizationIcon.setColorFilter(black);
        hopeIcon.setColorFilter(black);
        videosIcon.setColorFilter(black);
        hotlineIcon.setColorFilter(black);

        int active = Color.parseColor("#73903F");
        selectedIconId = v.getId();

        if (v.getId() == R.id.homeIcon) {
            openTab(homeFragment);
            homeIcon.setColorFilter(active);
        } else if (v.getId() == R.id.realizationIcon) {
            openTab(realizationFragment);
            realizationIcon.setColorFilter(active);
        } else if (v.getId() == R.id.hopeIcon) {
            openTab(hopeFragment);
            hopeIcon.setColorFilter(active);
        } else if (v.getId() == R.id.videosIcon) {
            openTab(videosFragment);
            videosIcon.setColorFilter(active);
        } else if (v.getId() == R.id.hotlineIcon) {
            openTab(hotlineFragment);
            hotlineIcon.setColorFilter(active);
        }
    }

    private void tintSelection(ImageView iv) {
        int black = ContextCompat.getColor(this, R.color.black);
        homeIcon.setColorFilter(black);
        realizationIcon.setColorFilter(black);
        hopeIcon.setColorFilter(black);
        videosIcon.setColorFilter(black);
        hotlineIcon.setColorFilter(black);
        iv.setColorFilter(Color.parseColor("#73903F"));
    }

    // Bottom navigation for tab switching: no need for back stack
    private void openTab(Fragment target) {
        if (target == null || target == activeFragment) return;

        getSupportFragmentManager()
                .beginTransaction()
                .hide(activeFragment)
                .show(target)
                .commit();

        activeFragment = target;
    }

    // Deeper screens: replace + add to back stack
    public void openScreen(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainerView2, fragment)
                .addToBackStack(null)
                .commit();
    }

    private Fragment fragmentForIcon(int iconId) {
        if (iconId == R.id.homeIcon) return homeFragment;
        if (iconId == R.id.realizationIcon) return realizationFragment;
        if (iconId == R.id.hopeIcon) return hopeFragment;
        if (iconId == R.id.videosIcon) return videosFragment;
        if (iconId == R.id.hotlineIcon) return hotlineFragment;
        return homeFragment;
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();   // go back within deeper screens
        } else {
            super.onBackPressed(); // exit app shell
        }
    }

}