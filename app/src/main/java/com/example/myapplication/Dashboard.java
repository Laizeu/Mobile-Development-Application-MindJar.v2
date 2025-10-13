package com.example.myapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class Dashboard extends AppCompatActivity implements View.OnClickListener{
    ImageView homeIcon, realizationIcon, hopeIcon, videosIcon, hotlineIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        //Accessing for fragment home as default
        accessFragments(new HomeFragment());


        //For the icons in the menu bar Item
        homeIcon = findViewById(R.id.homeIcon);
        realizationIcon = findViewById(R.id.realizationIcon);
        hopeIcon = findViewById(R.id.hopeIcon);
        videosIcon = findViewById(R.id.videosIcon);
        hotlineIcon = findViewById(R.id.hotlineIcon);

        homeIcon.setOnClickListener((View.OnClickListener) this);
        realizationIcon.setOnClickListener((View.OnClickListener) this);
        hopeIcon.setOnClickListener((View.OnClickListener) this);
        videosIcon.setOnClickListener((View.OnClickListener) this);
        hotlineIcon.setOnClickListener((View.OnClickListener) this);
    }

    public void onClick(View v){
        if(v.getId() == R.id.homeIcon || v.getId() == R.id.realizationIcon || v.getId() == R.id.hopeIcon ||
                v.getId() == R.id.videosIcon || v.getId() == R.id.hotlineIcon){
            changeColorIcon(v);
        }
    }

    public void changeColorIcon(View v){
        // Set all Icon to black color
        homeIcon.setColorFilter(getResources().getColor(R.color.black));
        realizationIcon.setColorFilter(getResources().getColor(R.color.black));
        hopeIcon.setColorFilter(getResources().getColor(R.color.black));
        videosIcon.setColorFilter(getResources().getColor(R.color.black));
        hotlineIcon.setColorFilter(getResources().getColor(R.color.black));

        // To change color of the icons
        if(v.getId() == R.id.homeIcon){
            accessFragments(new HomeFragment());
            homeIcon.setColorFilter(Color.parseColor("#73903F"));
        }else if(v.getId() == R.id.realizationIcon){
            accessFragments(new RealizationFragment());
            realizationIcon.setColorFilter(Color.parseColor("#73903F"));
        }else if(v.getId() == R.id.hopeIcon){
            accessFragments(new HopeFragment());
            hopeIcon.setColorFilter(Color.parseColor("#73903F"));
        }else if(v.getId() == R.id.videosIcon){
            accessFragments(new VideosFragment());
            videosIcon.setColorFilter(Color.parseColor("#73903F"));
        }else if(v.getId() == R.id.hotlineIcon){
            accessFragments(new HotlineFragment());
            hotlineIcon.setColorFilter(Color.parseColor("#73903F"));
        }
    }
    private void accessFragments(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainerView2, fragment);
        fragmentTransaction.commit();
    }

}