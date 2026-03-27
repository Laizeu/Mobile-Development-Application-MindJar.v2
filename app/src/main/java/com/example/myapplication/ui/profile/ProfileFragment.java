package com.example.myapplication.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.myapplication.R;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private ProfileViewModel   viewModel;
    private TextInputEditText  etDisplayName;
    private TextInputEditText  etEmail;
    private TextView           tvJoinedAt;

    public ProfileFragment() {}

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Back arrow — pops fragment from the back stack
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v ->
                getParentFragmentManager().popBackStack());

        etDisplayName = view.findViewById(R.id.etDisplayName);
        etEmail       = view.findViewById(R.id.etEmail);
        tvJoinedAt    = view.findViewById(R.id.tvJoinedAt);
        Button btnSave = view.findViewById(R.id.btnSaveProfile);

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        // Populate display name field
        viewModel.getDisplayName().observe(getViewLifecycleOwner(),
                name -> etDisplayName.setText(name));

        // Populate email field (read-only)
        viewModel.getEmail().observe(getViewLifecycleOwner(),
                mail -> etEmail.setText(mail));

        // Format and display member-since date
        viewModel.getJoinedAt().observe(getViewLifecycleOwner(), epoch -> {
            if (epoch != null && epoch > 0) {
                String formatted = new SimpleDateFormat(
                        "MMMM dd, yyyy", Locale.getDefault())
                        .format(new Date(epoch));
                tvJoinedAt.setText("Member since " + formatted);
            }
        });

        // Show toast feedback — null after consuming to prevent re-fire on rotation
        viewModel.getStatusMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                viewModel.clearStatus();   // prevents re-fire on rotation
            }
        });

        // Save button
        // Save button
        btnSave.setOnClickListener(v -> {
            String newName = etDisplayName.getText() != null
                    ? etDisplayName.getText().toString() : "";
            viewModel.saveDisplayName(newName);


            etDisplayName.clearFocus();
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager)
                            requireActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        });

        // Load profile data from Firestore
        viewModel.loadProfile();
    }
}
