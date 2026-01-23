package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

public class RealizationFragment extends Fragment {

    private static final String TAG = "RealizationTouch";

    public RealizationFragment() { }

    @Override
    public @NonNull View onCreateView(@NonNull LayoutInflater inflater,
                                      @Nullable ViewGroup container,
                                      @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_realization, container, false);

        // CardViews
        CardView card1 = root.findViewById(R.id.cardContent1);
        CardView card2 = root.findViewById(R.id.cardContent2);
        CardView card3 = root.findViewById(R.id.cardContent3);
        CardView card4 = root.findViewById(R.id.cardContent4);

        // Attach interactions
        setupCardInteractions(card1, "Happy");
        setupCardInteractions(card2, "Grateful");
        setupCardInteractions(card3, "Worried");
        setupCardInteractions(card4, "Loved");

        return root;
    }

    private void setupCardInteractions(CardView card, String label) {
        if (card == null) return;

        // TAP: feedback only (no navigation yet)
        card.setOnClickListener(v -> {
            Log.d(TAG, "TAP on card: " + label);
            Toast.makeText(requireContext(),
                    "Tapped: " + label,
                    Toast.LENGTH_SHORT).show();
        });

        // LONG PRESS: show options
        card.setOnLongClickListener(v -> {
            Log.d(TAG, "LONG PRESS on card: " + label);
            showOptionsDialog(label);
            return true; // consume long press so click won’t also fire
        });

        // TOUCH FEEDBACK: pressed effect
        card.setOnTouchListener((v, event) -> {
            int action = event.getAction();

            if (action == MotionEvent.ACTION_DOWN) {
                v.setAlpha(0.85f);
                Log.d(TAG, "TOUCH DOWN on card: " + label);
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                v.setAlpha(1.0f);
                Log.d(TAG, "TOUCH UP/CANCEL on card: " + label);
            }

            return false; // keep click + long press working
        });
    }

    private void showOptionsDialog(String label) {
        if (getContext() == null) return;

        String[] options = {"Edit", "Pin", "Close"};

        new AlertDialog.Builder(requireContext())
                .setTitle(label + " Options")
                .setItems(options, (dialog, which) -> {
                    String choice = options[which];
                    Log.d(TAG, "Option selected on " + label + ": " + choice);

                    Toast.makeText(requireContext(),
                            label + ": " + choice,
                            Toast.LENGTH_SHORT).show();
                })
                .show();
    }
}
