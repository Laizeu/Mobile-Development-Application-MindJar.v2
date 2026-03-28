package com.example.myapplication.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import android.content.Intent;
import androidx.appcompat.app.AlertDialog;
import com.example.myapplication.data.local.AppDatabase;
import com.example.myapplication.data.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.example.myapplication.ui.MainActivity;


public class ProfileFragment extends Fragment {

    private ProfileViewModel   viewModel;
    private TextInputEditText  etDisplayName;
    private TextInputEditText  etEmail;
    private TextView           tvJoinedAt;
    private ImageView ivAvatar;
    private AlertDialog        loadingDialog;


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

        ivAvatar = view.findViewById(R.id.ivAvatar);

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        viewModel.getDisplayName().observe(getViewLifecycleOwner(),
                name -> etDisplayName.setText(name));

        viewModel.getEmail().observe(getViewLifecycleOwner(),
                mail -> etEmail.setText(mail));

        // Observe avatarId and update the ImageView
        viewModel.getAvatarId().observe(getViewLifecycleOwner(),
                id -> ivAvatar.setImageResource(avatarDrawableFor(id)));

        // Open avatar picker when user taps ivAvatar
        ivAvatar.setOnClickListener(v -> showAvatarPickerDialog());


        // Format and display member-since date
        viewModel.getJoinedAt().observe(getViewLifecycleOwner(), epoch -> {
            if (epoch != null && epoch > 0) {
                String formatted = new SimpleDateFormat(
                        "MMMM dd, yyyy", Locale.getDefault())
                        .format(new Date(epoch));
                tvJoinedAt.setText("Member since " + formatted);
            }
        });

        viewModel.getStatusMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg == null) return;

            if (msg.equals("ACCOUNT_DELETED")) {
                dismissLoadingDialog();
                // Clear SharedPreferences session
                new SessionManager(requireContext()).clearSession();

                // Navigate to MainActivity, clear back stack
                Intent intent = new Intent(requireActivity(), MainActivity.class);
                intent.setFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                requireActivity().finish();
                return;
            }
            dismissLoadingDialog();
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            viewModel.clearStatus();
        });


        // Save button
        btnSave.setOnClickListener(v -> {
            if (!isNetworkAvailable()) {
                Toast.makeText(requireContext(),
                        "No internet connection. Please connect to save.",
                        Toast.LENGTH_LONG).show();
                return;
            }

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

        // Delete button — checks internet before showing confirmation dialog
        Button btnDelete = view.findViewById(R.id.btnDeleteAccount);
        btnDelete.setOnClickListener(v -> {
            if (!isNetworkAvailable()) {
                Toast.makeText(requireContext(),
                        "No internet connection. Please connect to delete your account.",
                        Toast.LENGTH_LONG).show();
                return;
            }
            showDeleteConfirmDialog();
        });
    }

    private int avatarDrawableFor(int id) {
        switch (id) {
            case 2:
                return R.drawable.avatar_2;
            case 3:
                return R.drawable.avatar_3;
            case 4:
                return R.drawable.avatar_4;
            case 5:
                return R.drawable.avatar_5;
            default:
                return R.drawable.avatar_1;
        }
    }
    private void showAvatarPickerDialog() {
        // Build a horizontal LinearLayout with 5 avatar ImageViews
        android.widget.LinearLayout row = new android.widget.LinearLayout(requireContext());
        row.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER);
        row.setPadding(32, 32, 32, 32);

        int[] ids = {1, 2, 3, 4, 5};

        // We hold a reference to the dialog so we can dismiss it on tap
        androidx.appcompat.app.AlertDialog[] dialogRef = new androidx.appcompat.app.AlertDialog[1];

        for (int avatarId : ids) {
            ImageView img = new ImageView(requireContext());
            int size = (int) (48 * getResources().getDisplayMetrics().density);
            android.widget.LinearLayout.LayoutParams lp =
                    new android.widget.LinearLayout.LayoutParams(size, size);
            lp.setMargins(7, 0, 7, 0);
            img.setLayoutParams(lp);
            img.setImageResource(avatarDrawableFor(avatarId));
            img.setContentDescription("Avatar " + avatarId);

            // Highlight currently selected avatar with a tinted background
            Integer current = viewModel.getAvatarId().getValue();
            if (current != null && current == avatarId) {
                android.graphics.drawable.GradientDrawable circle =
                        new android.graphics.drawable.GradientDrawable();
                circle.setShape(android.graphics.drawable.GradientDrawable.OVAL);
                circle.setStroke(6, android.graphics.Color.parseColor("#4DB6AC")); // teal ring
                circle.setColor(android.graphics.Color.TRANSPARENT);
                img.setBackground(circle.mutate());
            }

            final int chosenId = avatarId;
            img.setOnClickListener(v -> {
                if (!isNetworkAvailable()) {
                    Toast.makeText(requireContext(),
                            "No internet connection. Please connect to save.",
                            Toast.LENGTH_LONG).show();
                    if (dialogRef[0] != null) dialogRef[0].dismiss();
                    return;
                }
                viewModel.saveAvatarId(chosenId);
                if (dialogRef[0] != null) dialogRef[0].dismiss();
            });

            row.addView(img);
        }

        dialogRef[0] = new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Choose your avatar")
                .setView(row)
                .setNegativeButton("Cancel", null)
                .show();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager)
                requireContext().getSystemService(android.content.Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    private void showDeleteConfirmDialog() {
        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Account")
                .setMessage("This will permanently delete your account and all journal " +
                        "entries. This action cannot be undone.")
                .setPositiveButton("Delete", null)   // null = no auto-dismiss
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();


        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
        dialog.dismiss();
        showLoadingDialog();  // ← ADD THIS
        AppDatabase roomDb = AppDatabase.getInstance(requireContext());
        viewModel.deleteAccount(requireContext(), roomDb);
        });
    }

    private void showLoadingDialog() {
        android.widget.LinearLayout layout = new android.widget.LinearLayout(requireContext());
        layout.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        layout.setPadding(60, 40, 40, 40);
        layout.setGravity(android.view.Gravity.CENTER_VERTICAL);

        android.widget.ProgressBar spinner = new android.widget.ProgressBar(requireContext());
        android.widget.LinearLayout.LayoutParams lp =
                new android.widget.LinearLayout.LayoutParams(80, 80);
        lp.setMarginEnd(40);
        spinner.setLayoutParams(lp);

        android.widget.TextView text = new android.widget.TextView(requireContext());
        text.setText("Deleting your account...");
        text.setTextSize(16);

        layout.addView(spinner);
        layout.addView(text);

        loadingDialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(layout)
                .setCancelable(false)
                .show();
    }

    private void dismissLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }
}