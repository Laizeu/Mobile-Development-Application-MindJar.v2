package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

/**
 * MyJourneyFragment displays the user's personal journey screen.
 *
 * This fragment uses a toolbar with a navigation button that allows
 * the user to return to the previous screen using the fragment back stack.
 */
public class MyJourneyFragment extends Fragment {

    /**
     * Required empty public constructor.
     * The system uses this constructor when recreating the fragment.
     */
    public MyJourneyFragment() {
        // No initialization is needed here.
    }

    /**
     * Inflates the layout for the My Journey screen.
     */
    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_my_journey, container, false);
    }

    /**
     * Called after the fragment's view has been created.
     * This method is used to configure the toolbar behavior.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupToolbar(view);
    }

    /**
     * Configures the toolbar navigation action.
     * Pressing the back arrow returns the user to the previous fragment
     * without recreating it.
     */
    private void setupToolbar(@NonNull View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar);

        toolbar.setNavigationOnClickListener(v ->
                getParentFragmentManager().popBackStack()
        );
    }
}
