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
import java.util.regex.Pattern;

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

    /**
     * Password rules:
     * - Minimum 8 characters
     * - At least 1 uppercase letter
     * - At least 1 lowercase letter
     * - At least 1 number
     * - At least 1 special character from the allowed set
     */
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$"
    );

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

        // Build error messages for each field so UI updates are consistent.
        String emailError = getEmailErrorMessage(email);
        String passwordError = getPasswordErrorMessage(password);

        // Show helper text for both fields.
        emailContainer.setHelperText(emailError);
        passwordContainer.setHelperText(passwordError);

        // Stop if any validation fails.
        if (emailError != null || passwordError != null) {
            Toast.makeText(this, "Please fix the highlighted fields.", Toast.LENGTH_LONG).show();
            return;
        }

        // For demo-only credential check.
        if (email.equals(TEST_EMAIL) && password.equals(TEST_PASSWORD)) {
            openDashboard();
        } else {
            Toast.makeText(this, "Incorrect Credentials!", Toast.LENGTH_LONG).show();
        }
    }

    private String getEmailErrorMessage(String email) {
        String value = email == null ? "" : email.trim();

        if (value.isEmpty()) {
            return "Required";
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(value).matches()) {
            return "Invalid email address";
        }

        return null; // Valid email
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
     * EMAIL VALIDATION
     * Validates the email field while typing and shows helpful feedback.
     */
    private void validateEmailLive(String email) {
        email = email.trim();

        if (TextUtils.isEmpty(email)) {
            emailContainer.setHelperText("Required");
            return;
        }

        emailContainer.setHelperText(
                isEmailValid(email) ? null : "Invalid email address"
        );
    }

    /**
     * Checks if an email matches Android's built-in email pattern.
     */
    private boolean isEmailValid(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }



    /**
     * PASSWORD VALIDATION
     *
     * Validates the password field while typing and shows helper messages.
     */
    private void validatePasswordLive(String password) {
        passwordContainer.setHelperText(getPasswordErrorMessage(password));
    }

    /**
     * Returns a readable password error message, or null if the password is valid.
     *
     * <p>Rules:
     * - Must not be empty
     * - Length must be 8 to 16 characters
     * - Must include uppercase, lowercase, number, and special character</p>
     */
    private String getPasswordErrorMessage(String password) {
        String pwd = password == null ? "" : password.trim();


        if (pwd.isEmpty()) {
            return "Password cannot be empty";
        }

        if (pwd.length() < 8) {
            return "Password must be at least 8 characters";
        }

        if (!PASSWORD_PATTERN.matcher(pwd).matches()) {
            return "Password must include uppercase, lowercase, number, and special character";
        }

        return null; // Valid password
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
