package com.example.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.regex.Pattern;

/**
 * SignUpActivity handles user registration validation.
 *
 * <p>Live validation while typing:
 * <ul>
 *   <li>Full name: required, min 2 chars, Unicode letters + spaces + hyphen + apostrophe</li>
 *   <li>Email: required + valid format</li>
 *   <li>Password: regex strong rules</li>
 *   <li>Confirm password: must match password</li>
 * </ul>
 */
public class SignUpActivity extends AppCompatActivity {

    // Containers for helper text
    private TextInputLayout nameContainer, emailContainer, passwordContainer, confirmContainer;

    // Inputs
    private TextInputEditText editFullName, editEmail, editPassword, editConfirmPassword;

    // Controls
    private Button btnCreateAccount;
    private TextView txtLoginLink;

    /**
     * Password regex (strong rules):
     * - At least 8 characters
     * - At least 1 uppercase
     * - At least 1 lowercase
     * - At least 1 digit
     * - At least 1 special char from @$!%*#?&
     */
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*#?&]).{8,}$"
    );

    /**
     * Full name regex (Unicode-safe):
     * - Allows letters like ñ, é (\\p{L})
     * - Allows spaces, dot, hyphen, apostrophe
     */
    private static final Pattern NAME_PATTERN = Pattern.compile("^[\\p{L} .'-]+$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        bindViews();
        setupLoginClickableText();
        setupLiveValidation();
        setupCreateAccount();
    }

    /**
     * Binds all views from XML.
     */
    private void bindViews() {
        nameContainer = findViewById(R.id.nameContainer);
        emailContainer = findViewById(R.id.emailContainer);
        passwordContainer = findViewById(R.id.passwordContainer);
        confirmContainer = findViewById(R.id.confirmContainer);

        editFullName = findViewById(R.id.editFullName);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        editConfirmPassword = findViewById(R.id.editConfirmPassword);

        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        txtLoginLink = findViewById(R.id.txtLoginLink);
    }

    /**
     * Makes "Login" clickable and routes back to Login screen explicitly.
     */
    private void setupLoginClickableText() {
        String fullText = "Already have an account? Login";
        SpannableString spannable = new SpannableString(fullText);

        String clickablePart = "Login";
        int start = fullText.indexOf(clickablePart);
        int end = start + clickablePart.length();

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setColor(getResources().getColor(R.color.button_green));
                ds.setUnderlineText(true);
                ds.setFakeBoldText(true);
            }
        };

        spannable.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        txtLoginLink.setText(spannable);
        txtLoginLink.setMovementMethod(LinkMovementMethod.getInstance());
        txtLoginLink.setHighlightColor(Color.TRANSPARENT);
    }

    /**
     * Live helper text validation while user types.
     * This is similar to what you did in Login page.
     */
    private void setupLiveValidation() {

        editFullName.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                nameContainer.setHelperText(getFullNameError(getText(editFullName)));
            }
        });

        editEmail.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                emailContainer.setHelperText(getEmailError(getText(editEmail)));
            }
        });

        editPassword.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String pass = getText(editPassword);

                // Progressive helper while typing
                passwordContainer.setHelperText(getPasswordLiveHelper(pass));

                // Also re-check confirm password live
                confirmContainer.setHelperText(getConfirmPasswordError(
                        getText(editPassword), getText(editConfirmPassword)
                ));
            }
        });

        editConfirmPassword.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                confirmContainer.setHelperText(getConfirmPasswordError(
                        getText(editPassword), getText(editConfirmPassword)
                ));
            }
        });
    }

    /**
     * Validates all fields again when "Sign up" is clicked.
     * Live validation is great, but button validation is still required.
     */
    private void setupCreateAccount() {
        btnCreateAccount.setOnClickListener(v -> {

            String name = getText(editFullName);
            String email = getText(editEmail);
            String pass = getText(editPassword);
            String confirm = getText(editConfirmPassword);

            String nameError = getFullNameError(name);
            String emailError = getEmailError(email);
            String passError = getPasswordError(pass);
            String confirmError = getConfirmPasswordError(pass, confirm);

            nameContainer.setHelperText(nameError);
            emailContainer.setHelperText(emailError);
            passwordContainer.setHelperText(passError);
            confirmContainer.setHelperText(confirmError);

            boolean hasError = (nameError != null || emailError != null || passError != null || confirmError != null);

            if (hasError) {
                Toast.makeText(this, "Please fix the highlighted fields.", Toast.LENGTH_LONG).show();
                return;
            }

            Toast.makeText(this, "Account created successfully!", Toast.LENGTH_LONG).show();

            // After sign up, go back to login explicitly
            startActivity(new Intent(SignUpActivity.this, MainActivity.class));
            finish();
        });
    }

    // ---------------- Validation Methods ----------------

    /**
     * Full name validation:
     * - Required
     * - Min 2 characters
     * - Unicode letters + spaces + hyphen + apostrophe
     */
    private String getFullNameError(String name) {
        if (name.isEmpty()) return "Full name is required";
        if (name.length() < 2) return "Name must be at least 2 characters";
        if (!NAME_PATTERN.matcher(name).matches()) return "Name can only contain letters and spaces";
        return null;
    }

    /**
     * Email validation:
     * - Required
     * - Valid email format
     */
    private String getEmailError(String email) {
        if (email.isEmpty()) return "Email is required";
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) return "Invalid email address";
        return null;
    }

    /**
     * Password validation (regex):
     * - Required
     * - Must match strong password pattern
     */
    private String getPasswordError(String password) {
        if (password.isEmpty()) return "Password is required";
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            return "Password must be 8+ chars and include uppercase, lowercase, number, and symbol";
        }
        return null;
    }

    /**
     * Confirm password validation:
     * - Required
     * - Must match password
     *
     * Note: If password is empty, we usually don't show "does not match" yet.
     */
    private String getConfirmPasswordError(String password, String confirm) {
        if (confirm.isEmpty()) return "Confirm password is required";
        if (!password.isEmpty() && !confirm.equals(password)) return "Passwords do not match";
        return null;
    }

    // ---------------- Utility Methods ----------------

    /**
     * Safely gets trimmed text from a TextInputEditText.
     */
    private String getText(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    /**
     * SimpleTextWatcher: override only what you need.
     */
    public abstract static class SimpleTextWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        @Override public void afterTextChanged(Editable s) { }

    }

    /**
     * Returns a progressive helper message for password while typing.
     * Shows only what is still missing (better UX).
     *
     * @return helper message or null if the password is strong
     */
    private String getPasswordLiveHelper(String password) {
        String p = password == null ? "" : password;

        // If empty, you can either show "Password is required" or nothing.
        // I recommend showing nothing until they type something:
        if (p.isEmpty()) return null;

        if (p.length() < 8) return "Add at least 8 characters";
        if (!containsUppercase(p)) return "Add 1 uppercase letter";
        if (!containsLowercase(p)) return "Add 1 lowercase letter";
        if (!containsDigit(p)) return "Add 1 number";
        if (!containsSpecial(p)) return "Add 1 special character (@$!%*#?&)";

        return null; // strong
    }

    private boolean containsUppercase(String s) {
        return s.matches(".*[A-Z].*");
    }

    private boolean containsLowercase(String s) {
        return s.matches(".*[a-z].*");
    }

    private boolean containsDigit(String s) {
        return s.matches(".*\\d.*");
    }

    private boolean containsSpecial(String s) {
        // Match the same special set you use in your regex
        return s.matches(".*[@$!%*#?&].*");
    }

}
