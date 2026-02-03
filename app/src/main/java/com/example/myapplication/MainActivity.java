package com.example.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.LinkMovementMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ClickableSpan;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * MainActivity handles the Login screen UI and validation logic.
 *
 * <p>Login validation rules (best practice):
 * <ul>
 *   <li>Email: Required + valid email format</li>
 *   <li>Password: Required only</li>
 *   <li>On failed login: Show a generic "Invalid email or password"</li>
 * </ul>
 *
 * <p>This keeps login simple and avoids applying sign-up password rules on login.</p>
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // --- UI Containers (for helper text) ---
    private TextInputLayout emailContainer;
    private TextInputLayout passwordContainer;

    // --- Input Fields ---
    private TextInputEditText editEmail;
    private TextInputEditText editPassword;

    // --- Controls ---
    private Button btnLogin;
    private CheckBox checkShowPassword; // currently used as show/hide toggle

    /**
     * Demo-only test credentials.
     * In a real app, credentials are verified by a backend/auth provider.
     */
    private static final String TEST_EMAIL = "test@gmail.com";
    private static final String TEST_PASSWORD = "Password1!";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        bindViews();
        setupListeners();
        setupLiveValidation();
        setupSignUpLink();
    }

    /**
     * Binds XML views to Java fields.
     */
    private void bindViews() {
        btnLogin = findViewById(R.id.button);

        emailContainer = findViewById(R.id.emailContainer);
        passwordContainer = findViewById(R.id.passwordContainer);

        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);

        // NOTE: Your checkbox id is "checkBox" in XML.
        // If it's really "Remember Me", we should rename it in XML later.
        checkShowPassword = findViewById(R.id.checkBox);
    }

    /**
     * Sets up click listeners for Login button and password toggle checkbox.
     */
    private void setupListeners() {
        btnLogin.setOnClickListener(this);

        if (checkShowPassword != null) {
            checkShowPassword.setOnCheckedChangeListener(
                    (buttonView, isChecked) -> togglePasswordVisibility(isChecked)
            );
        }
    }

    /**
     * Shows helper validation messages while user types.
     */
    private void setupLiveValidation() {
        editEmail.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                emailContainer.setHelperText(getEmailErrorMessage(s.toString()));
            }
        });

        editPassword.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                passwordContainer.setHelperText(getPasswordErrorMessage(s.toString()));
            }
        });
    }

    /**
     * Makes "Sign Up" clickable and routes to SignUpActivity.
     */
    private void setupSignUpLink() {
        TextView txtSignUpLink = findViewById(R.id.txtSignUpLink);
        if (txtSignUpLink == null) return;

        String fullText = "Don’t have an account? Sign Up";
        SpannableString spannable = new SpannableString(fullText);

        String clickablePart = "Sign Up";
        int start = fullText.indexOf(clickablePart);
        int end = start + clickablePart.length();

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                startActivity(new Intent(MainActivity.this, SignUpActivity.class));
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setColor(getResources().getColor(R.color.button_green));
                ds.setUnderlineText(true);
                ds.setFakeBoldText(true);
            }
        };

        spannable.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        txtSignUpLink.setText(spannable);
        txtSignUpLink.setMovementMethod(LinkMovementMethod.getInstance());
        txtSignUpLink.setHighlightColor(Color.TRANSPARENT);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button) {
            attemptLogin();
        }
    }

    /**
     * Attempts login by validating inputs and then checking credentials.
     */
    private void attemptLogin() {
        String email = getTrimmedText(editEmail);
        String password = getTrimmedText(editPassword);

        // Field-level validation
        String emailError = getEmailErrorMessage(email);
        String passwordError = getPasswordErrorMessage(password);

        // Show helper texts
        emailContainer.setHelperText(emailError);
        passwordContainer.setHelperText(passwordError);

        // Stop if validation fails
        if (emailError != null || passwordError != null) {
            Toast.makeText(this, "Please fix the highlighted fields.", Toast.LENGTH_LONG).show();
            return;
        }

        // Demo-only credential check
        if (email.equals(TEST_EMAIL) && password.equals(TEST_PASSWORD)) {
            openDashboard();
        } else {
            // Generic message: do not reveal which is wrong (security best practice)
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Email validation for login:
     * - Required
     * - Valid email format
     *
     * @return error message or null if valid
     */
    private String getEmailErrorMessage(String email) {
        String value = email == null ? "" : email.trim();

        if (value.isEmpty()) {
            return "Required";
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(value).matches()) {
            return "Invalid email";
        }

        return null;
    }

    /**
     * Password validation for login (minimal):
     * - Required only
     *
     * @return error message or null if valid
     */
    private String getPasswordErrorMessage(String password) {
        String value = password == null ? "" : password.trim();

        if (value.isEmpty()) {
            return "Password required";
        }

        return null;
    }

    /**
     * Toggles password visibility (used by checkbox).
     */
    private void togglePasswordVisibility(boolean showPassword) {
        if (showPassword) {
            editPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        } else {
            editPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }

        // Keep cursor at end
        if (editPassword.getText() != null) {
            editPassword.setSelection(editPassword.getText().length());
        }
    }

    /**
     * Opens the Dashboard screen after successful login.
     */
    private void openDashboard() {
        startActivity(new Intent(MainActivity.this, Dashboard.class));
    }

    /**
     * Returns trimmed text safely.
     */
    private String getTrimmedText(TextInputEditText input) {
        return (input.getText() == null) ? "" : input.getText().toString().trim();
    }

    /**
     * SimpleTextWatcher reduces boilerplate by letting you override only needed methods.
     */
    public abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        @Override
        public void afterTextChanged(Editable s) { }
    }
}
