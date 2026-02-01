package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

/**
 * RealizationFragment displays reflection cards and handles user interactions.
 *
 * The user can:
 * - Tap a card to open the entry details screen (added to the back stack).
 * - Long-press a card to show options (Edit, Pin, Close).
 * - See simple visual feedback while pressing a card.
 *
 * This fragment also provides a "My Journey" button that opens MyJourneyFragment.
 */
public class RealizationFragment extends Fragment {

    private static final String TAG = "RealizationFragment";

    /**
     * Required empty public constructor.
     * The Android system uses this constructor when recreating the fragment.
     */
    public RealizationFragment() {
        // No initialization is needed here.
    }

    @Override
    public @NonNull View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View root = inflater.inflate(R.layout.fragment_realization, container, false);

        setupMyJourneyButton(root);
        setupReflectionCards(root);

        return root;
    }

    /**
     * Configures the "My Journey" button to open MyJourneyFragment.
     */
    private void setupMyJourneyButton(@NonNull View root) {
        Button myJourneyBtn = root.findViewById(R.id.button);

        myJourneyBtn.setOnClickListener(v -> {
            Dashboard dashboard = getDashboardHost();
            if (dashboard != null) {
                dashboard.openScreen(new MyJourneyFragment());
            } else {
                showToast("Host activity is not Dashboard.");
            }
        });
    }

    /**
     * Finds reflection cards and attaches interaction behavior for each card.
     */
    private void setupReflectionCards(@NonNull View root) {
        CardView cardHappy = root.findViewById(R.id.cardContent1);
        CardView cardGrateful = root.findViewById(R.id.cardContent2);
        CardView cardWorried = root.findViewById(R.id.cardContent3);
        CardView cardLoved = root.findViewById(R.id.cardContent4);

        attachCardInteractions(cardHappy, "Happy");
        attachCardInteractions(cardGrateful, "Grateful");
        attachCardInteractions(cardWorried, "Worried");
        attachCardInteractions(cardLoved, "Loved");
    }

    /**
     * Attaches click, long-click, and touch feedback behavior to a CardView.
     *
     * Tap:
     * Opens EntryDetailsFragment using the Dashboard host activity.
     *
     * Long press:
     * Shows an options dialog and consumes the event so normal click does not fire.
     *
     * Touch feedback:
     * Slightly reduces opacity while the card is pressed.
     */
    private void attachCardInteractions(@Nullable CardView card, @NonNull String label) {
        if (card == null) {
            Log.w(TAG, "CardView is null for label: " + label);
            return;
        }

        card.setOnClickListener(v -> handleCardTap(label));

        card.setOnLongClickListener(v -> {
            handleCardLongPress(label);
            return true; // Returning true consumes the long press.
        });

        card.setOnTouchListener((v, event) -> applyTouchFeedback(v, event, label));
    }

    /**
     * Handles a normal tap on a card by opening the entry details screen.
     */
    private void handleCardTap(@NonNull String label) {
        Log.d(TAG, "Card tapped: " + label);

        Dashboard dashboard = getDashboardHost();
        if (dashboard != null) {
            dashboard.openScreen(new EntryDetailsFragment());
        } else {
            showToast("Host activity is not Dashboard.");
        }
    }

    /**
     * Handles a long-press on a card by showing an options dialog.
     */
    private void handleCardLongPress(@NonNull String label) {
        Log.d(TAG, "Card long-pressed: " + label);
        showOptionsDialog(label);
    }

    /**
     * Applies simple pressed-state feedback by adjusting the view alpha.
     * Returning false allows click and long-click to continue working normally.
     */
    private boolean applyTouchFeedback(@NonNull View v, @NonNull MotionEvent event, @NonNull String label) {
        int action = event.getAction();

        if (action == MotionEvent.ACTION_DOWN) {
            v.setAlpha(0.85f);
            Log.d(TAG, "Touch down: " + label);
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            v.setAlpha(1.0f);
            Log.d(TAG, "Touch up/cancel: " + label);
        }

        return false;
    }

    /**
     * Returns the Dashboard host activity if available; otherwise returns null.
     * This prevents unsafe casting and makes navigation safer.
     */
    @Nullable
    private Dashboard getDashboardHost() {
        if (getActivity() instanceof Dashboard) {
            return (Dashboard) getActivity();
        }
        return null;
    }

    /**
     * Displays an options dialog for the selected card.
     */
    private void showOptionsDialog(@NonNull String label) {
        if (!isAdded()) {
            return;
        }

        String[] options = {"Edit", "Pin", "Close"};

        new AlertDialog.Builder(requireContext())
                .setTitle(label + " Options")
                .setItems(options, (dialog, which) -> {
                    String choice = options[which];
                    Log.d(TAG, "Option selected for " + label + ": " + choice);
                    showToast(label + ": " + choice);
                })
                .show();
    }

    /**
     * Shows a toast message safely using the fragment's context.
     */
    private void showToast(@NonNull String message) {
        if (!isAdded()) {
            return;
        }
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}
