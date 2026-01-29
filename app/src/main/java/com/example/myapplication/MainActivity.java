package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * MainActivity handles the login screen UI and validation logic.
 *
 * <p>This screen supports:
 * 1) Live validation while typing for email and password,
 * 2) Show/Hide password using a checkbox, and
 * 3) Final validation when the user taps the Login button.</p>
 *
 * <p>If the credentials match the hardcoded test account, the user is redirected
 * to the Dashboard activity.</p>
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // Containers are used to show helper/error text underneath the input fields.
    private TextInputLayout emailContainer, passwordContainer;

    // User input fields for email and password.
    private TextInputEditText editEmail, editPassword;


    /**
     * Test credentials for demonstration purposes.
     * In a real app, this should be replaced with secure authentication.
     */
    private static final String TEST_EMAIL = "test@gmail.com";
    private static final String TEST_PASSWORD = "Password1!";


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Edge-to-edge allows content to draw behind system bars if needed.
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        bindViews();
        setupListeners();
        setupLiveValidation();
    }

    // UI controls.
    private Button btnLogin;
    private CheckBox checkPassword;

    /**
     * Finds and assigns all views from the layout.
     * Keeping this in one method makes onCreate() easier to read.
     */
    private void bindViews() {
        btnLogin = findViewById(R.id.button);

        emailContainer = findViewById(R.id.emailContainer);
        passwordContainer = findViewById(R.id.passwordContainer);

        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);

        checkPassword = findViewById(R.id.checkBox);
    }


    /**
     * Sets up click listeners for the Login button and password checkbox.
     */
    private void setupListeners() {
        btnLogin.setOnClickListener(this);

        // This toggles password visibility while keeping the cursor at the end of the text.
        checkPassword.setOnCheckedChangeListener((buttonView, isChecked) -> togglePasswordVisibility(isChecked));
    }

    /**
     * Adds TextWatchers so validation feedback is shown while the user types.
     */
    private void setupLiveValidation() {
        editEmail.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateEmailLive(s.toString());
            }
        });

        editPassword.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePasswordLive(s.toString());
            }
        });
    }
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button) {
            attemptLogin();
        }
    }

    /**
     * Shows or hides the password text depending on the checkbox state.
     * This improves usability and accessibility on the login screen.
     */
    private void togglePasswordVisibility(boolean showPassword) {
        if (showPassword) {
            editPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        } else {
            editPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }

        // This ensures the cursor stays at the end after toggling visibility.
        if (editPassword.getText() != null) {
            editPassword.setSelection(editPassword.getText().length());
        }
    }

    /**
     * Validates inputs and navigates to Dashboard if credentials are correct.
     * If inputs are invalid, the method shows helper text and prevents login.
     */
    private void attemptLogin() {
        String email = getTrimmedText(editEmail);
        String password = getTrimmedText(editPassword);

        // Validate inputs again on button click to ensure final correctness.
        boolean emailOk = isEmailValid(email);
        String passwordError = getPasswordErrorMessage(password);

        // Show helper text for email field.
        if (!emailOk) {
            emailContainer.setHelperText("Invalid email address");
        } else {
            emailContainer.setHelperText(null);
        }

        // Show helper text for password field.
        passwordContainer.setHelperText(passwordError);

        // Stop if any validation fails.
        if (!emailOk || passwordError != null) {
            Toast.makeText(this, "Please fix the highlighted fields.", Toast.LENGTH_LONG).show();
            return;
        }

        // If validation passes, check credentials (demo-only logic).
        if (email.equals(TEST_EMAIL) && password.equals(TEST_PASSWORD)) {
            openDashboard();
        } else {
            Toast.makeText(this, "Incorrect Credentials!", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Returns the trimmed text from a TextInputEditText safely.
     */
    private String getTrimmedText(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }

    /**
     * Opens the Dashboard screen after a successful login.
     */
    private void openDashboard() {
        Intent toDashboardPage = new Intent(MainActivity.this, Dashboard.class);
        startActivity(toDashboardPage);
    }

    /**
     * Validates the email field while typing and shows helpful feedback.
     */
    private void validateEmailLive(String email) {
        email = email.trim();

        if (TextUtils.isEmpty(email)) {
            emailContainer.setHelperText("Required");
            return;
        }

        if (!isEmailValid(email)) {
            emailContainer.setHelperText("Invalid email address");
        } else {
            emailContainer.setHelperText(null);
        }
    }

    /**
     * Checks if an email matches Android's built-in email pattern.
     */
    private boolean isEmailValid(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Validates the password field while typing and shows helper messages.
     */
    private void validatePasswordLive(String password) {
        String message = getPasswordErrorMessage(password);
        passwordContainer.setHelperText(message);
    }

    /**
     * Returns a readable password error message, or null if the password is valid.
     *
     * <p>Rules:
     * - Must not be empty
     * - Length must be 8 to 16 characters
     * - Must include: uppercase, lowercase, number, and symbol</p>
     */
    private String getPasswordErrorMessage(String password) {
        String trimmed = password.trim();

        if (trimmed.isEmpty()) return "Password cannot be empty";

        int length = password.length();
        if (length < 8) return "Must be at least 8 characters";
        if (length > 16) return "Must be at most 16 characters";

        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasDigit = false;
        boolean hasSymbol = false;

        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);

            if (Character.isUpperCase(c)) {
                hasUppercase = true;
            } else if (Character.isLowerCase(c)) {
                hasLowercase = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else if (!Character.isWhitespace(c)) {
                // Any non-whitespace, non-letter, non-digit counts as a symbol for this rule.
                hasSymbol = true;
            }
        }

        if (!hasUppercase) return "Must include at least 1 uppercase letter (A–Z)";
        if (!hasLowercase) return "Must include at least 1 lowercase letter (a–z)";
        if (!hasDigit) return "Must include at least 1 number (0–9)";
        if (!hasSymbol) return "Must include at least 1 symbol (e.g., !@#$...)";

        // Returning null means the password passed all checks.
        return null;
    }

    /**
     * SimpleTextWatcher reduces boilerplate by allowing subclasses to override only what they need.
     */
    public abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // No action needed before text changes.
        }

        @Override
        public void afterTextChanged(Editable s) {
            // No action needed after text changes.
        }
    }
}
