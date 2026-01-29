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

/**
 * Dashboard is the main activity that hosts the app's bottom navigation.
 *
 * This activity uses a "show/hide" fragment approach for tab navigation.
 * Each tab fragment is created once, added to the FragmentManager, and then
 * shown/hidden when the user switches tabs. This avoids re-creating fragments
 * repeatedly and keeps switching fast.</p>
 *
 * For deeper screens (not part of bottom navigation), the activity uses
 * replace() with addToBackStack() so the system back button works correctly.</p>
 */
public class Dashboard extends AppCompatActivity implements View.OnClickListener {

    /**
     * Bundle key used to save and restore which bottom navigation icon was selected.
     */
    private static final String KEY_SELECTED_ICON_ID = "selected_icon_id";

    // Tab fragments are created once and reused to preserve UI state per tab.
    private Fragment homeFragment;
    private Fragment realizationFragment;
    private Fragment hopeFragment;
    private Fragment videosFragment;
    private Fragment hotlineFragment;

    // This tracks the fragment that is currently visible to the user.
    private Fragment activeFragment;

    // Bottom navigation icons.
    private ImageView homeIcon;
    private ImageView realizationIcon;
    private ImageView hopeIcon;
    private ImageView videosIcon;
    private ImageView hotlineIcon;

    // This stores the resource ID of the currently selected icon.
    private int selectedIconId = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Edge-to-edge ensures content can draw behind system bars when needed.
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        // This method keeps view initialization organized and easy to read.
        bindViewsAndListeners();

        FragmentManager fm = getSupportFragmentManager();

        if (savedInstanceState == null) {
            // This is the first time the activity is created, so we create and add fragments.
            initializeTabFragments(fm);

            // HOME is the default tab when opening the dashboard.
            activeFragment = homeFragment;
            selectedIconId = R.id.homeIcon;
            tintSelection(homeIcon);

        } else {
            // The activity was recreated (example: rotation), so we reconnect to existing fragments.
            reconnectFragmentsAfterRecreate(fm, savedInstanceState);
        }
    }

    /**
     * Finds the bottom navigation views and assigns click listeners.
     */
    private void bindViewsAndListeners() {
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
    }

    /**
     * Creates the tab fragments once, adds them to the FragmentManager, and hides all tabs except HOME.
     */
    private void initializeTabFragments(FragmentManager fm) {
        homeFragment = new HomeFragment();
        realizationFragment = new RealizationFragment();
        hopeFragment = new HopeFragment();
        videosFragment = new VideosFragment();
        hotlineFragment = new HotlineFragment();

        // We add all tab fragments once and use hide/show to switch tabs.
        fm.beginTransaction()
                .add(R.id.fragmentContainerView2, homeFragment, "HOME")
                .add(R.id.fragmentContainerView2, realizationFragment, "REALIZATION").hide(realizationFragment)
                .add(R.id.fragmentContainerView2, hopeFragment, "HOPE").hide(hopeFragment)
                .add(R.id.fragmentContainerView2, videosFragment, "VIDEOS").hide(videosFragment)
                .add(R.id.fragmentContainerView2, hotlineFragment, "HOTLINE").hide(hotlineFragment)
                .commit();
    }

    /**
     * Restores fragments and the selected tab when the activity is recreated.
     */
    private void reconnectFragmentsAfterRecreate(FragmentManager fm, Bundle savedInstanceState) {
        homeFragment = fm.findFragmentByTag("HOME");
        realizationFragment = fm.findFragmentByTag("REALIZATION");
        hopeFragment = fm.findFragmentByTag("HOPE");
        videosFragment = fm.findFragmentByTag("VIDEOS");
        hotlineFragment = fm.findFragmentByTag("HOTLINE");

        // Restore which icon was selected last time.
        selectedIconId = savedInstanceState.getInt(KEY_SELECTED_ICON_ID, R.id.homeIcon);

        // Determine which fragment should be visible based on the selected icon.
        activeFragment = fragmentForIcon(selectedIconId);
        if (activeFragment == null) {
            activeFragment = homeFragment;
        }

        // Ensure only the active fragment is shown.
        fm.beginTransaction()
                .hide(homeFragment)
                .hide(realizationFragment)
                .hide(hopeFragment)
                .hide(videosFragment)
                .hide(hotlineFragment)
                .show(activeFragment)
                .commit();

        // Apply the correct tint to match the restored selection.
        View selectedView = findViewById(selectedIconId);
        if (selectedView != null) {
            handleBottomNavSelection(selectedView);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the selected icon so the UI can restore after rotation/recreate.
        outState.putInt(KEY_SELECTED_ICON_ID, selectedIconId);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        // Only handle taps from our bottom navigation icons.
        if (id == R.id.homeIcon
                || id == R.id.realizationIcon
                || id == R.id.hopeIcon
                || id == R.id.videosIcon
                || id == R.id.hotlineIcon) {
            handleBottomNavSelection(v);
        }
    }

    /**
     * Handles bottom navigation behavior by:
     * 1) resetting all icons to default color,
     * 2) switching to the selected tab fragment, and
     * 3) tinting the selected icon.
     */
    private void handleBottomNavSelection(View v) {
        resetAllIconsToDefaultColor();

        int selectedColor = Color.parseColor("#73903F");
        selectedIconId = v.getId();

        if (selectedIconId == R.id.homeIcon) {
            openTab(homeFragment);
            homeIcon.setColorFilter(selectedColor);

        } else if (selectedIconId == R.id.realizationIcon) {
            openTab(realizationFragment);
            realizationIcon.setColorFilter(selectedColor);

        } else if (selectedIconId == R.id.hopeIcon) {
            openTab(hopeFragment);
            hopeIcon.setColorFilter(selectedColor);

        } else if (selectedIconId == R.id.videosIcon) {
            openTab(videosFragment);
            videosIcon.setColorFilter(selectedColor);

        } else if (selectedIconId == R.id.hotlineIcon) {
            openTab(hotlineFragment);
            hotlineIcon.setColorFilter(selectedColor);
        }
    }

    /**
     * Resets all bottom navigation icons to the default color (black).
     */
    private void resetAllIconsToDefaultColor() {
        int black = ContextCompat.getColor(this, R.color.black);

        homeIcon.setColorFilter(black);
        realizationIcon.setColorFilter(black);
        hopeIcon.setColorFilter(black);
        videosIcon.setColorFilter(black);
        hotlineIcon.setColorFilter(black);
    }

    /**
     * Tints only the provided icon as selected and resets the others to default.
     * This is mainly used during initial setup.
     */
    private void tintSelection(ImageView selectedIcon) {
        resetAllIconsToDefaultColor();
        selectedIcon.setColorFilter(Color.parseColor("#73903F"));
    }

    /**
     * Switches between bottom navigation tabs using show/hide without back stack.
     * Bottom navigation should not add entries to the back stack because tabs are not "history."
     */
    private void openTab(Fragment target) {
        if (target == null || target == activeFragment) {
            return;
        }

        getSupportFragmentManager()
                .beginTransaction()
                .hide(activeFragment)
                .show(target)
                .commit();

        activeFragment = target;
    }

    /**
     * Opens deeper screens by replacing the container and adding the transaction to the back stack.
     * This allows the Android back button to return the user to the previous screen.
     */
    public void openScreen(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainerView2, fragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Returns the fragment associated with a selected icon ID.
     */
    private Fragment fragmentForIcon(int iconId) {
        if (iconId == R.id.homeIcon) return homeFragment;
        if (iconId == R.id.realizationIcon) return realizationFragment;
        if (iconId == R.id.hopeIcon) return hopeFragment;
        if (iconId == R.id.videosIcon) return videosFragment;
        if (iconId == R.id.hotlineIcon) return hotlineFragment;

        // HOME is a safe fallback if an unknown iconId is received.
        return homeFragment;
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();

        // If there are deeper screens open, go back within the back stack.
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
        } else {
            // If no deeper screens exist, the back button exits the dashboard.
            super.onBackPressed();
        }
    }
}
