package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import android.widget.Button;

public class RealizationFragment extends Fragment {

    private static final String TAG = "RealizationTouch";

    public RealizationFragment() { }

    @Override
    public @NonNull View onCreateView(@NonNull LayoutInflater inflater,
                                      @Nullable ViewGroup container,
                                      @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_realization, container, false);

        Button myJourneyBtn = root.findViewById(R.id.button);

        myJourneyBtn.setOnClickListener(v -> {
            if (getActivity() instanceof Dashboard) {
                ((Dashboard) getActivity()).openScreen(new MyJourneyFragment());
            }
        });


        // CardViews
        CardView card1 = root.findViewById(R.id.cardContent1);
        CardView card2 = root.findViewById(R.id.cardContent2);
        CardView card3 = root.findViewById(R.id.cardContent3);
        CardView card4 = root.findViewById(R.id.cardContent4);

        // Attach touch/click behavior
        setupCardInteractions(card1, "Happy");
        setupCardInteractions(card2, "Grateful");
        setupCardInteractions(card3, "Worried");
        setupCardInteractions(card4, "Loved");

        return root;
    }

    private void setupCardInteractions(CardView card, String label) {
        if (card == null) return;

        // TAP = go to details (back stack)
        card.setOnClickListener(v -> {
            Log.d(TAG, "TAP on card: " + label);

            if (getActivity() instanceof Dashboard) {
                ((Dashboard) getActivity()).openScreen(new EntryDetailsFragment());
            } else {
                Toast.makeText(requireContext(), "Host activity is not Dashboard", Toast.LENGTH_SHORT).show();
            }
        });


        // Long press = show options menu
        card.setOnLongClickListener(v -> {
            Log.d(TAG, "LONG PRESS on card: " + label);
            showOptionsDialog(label);
            return true; // important: consumes long press so click won’t also fire
        });

        // Simple touch feedback (pressed state)
        card.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    v.setAlpha(0.85f);
                    Log.d(TAG, "TOUCH DOWN on card: " + label);
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    v.setAlpha(1.0f);
                    Log.d(TAG, "TOUCH UP/CANCEL on card: " + label);
                    break;
            }
            return false; // return false so click/long press still work
        });
    }

    private void showInfoDialog(String label) {
        if (getContext() == null) return;

        new AlertDialog.Builder(requireContext())
                .setTitle(label)
                .setMessage("You tapped the " + label + " card.\n\n(This is where you can open details or show a reflection.)")
                .setPositiveButton("OK", (d, which) -> d.dismiss())
                .show();
    }

    private void showOptionsDialog(String label) {
        if (getContext() == null) return;

        String[] options = {"Edit", "Pin", "Close"};

        new AlertDialog.Builder(requireContext())
                .setTitle(label + " Options")
                .setItems(options, (dialog, which) -> {
                    String choice = options[which];
                    Log.d(TAG, "Option selected on " + label + ": " + choice);

                    // feedback
                    Toast.makeText(requireContext(),
                            label + ": " + choice,
                            Toast.LENGTH_SHORT).show();
                })
                .show();
    }
}
