package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextInputLayout emailContainer, passwordContainer;
    private TextInputEditText editEmail, editPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Button btnLogin = findViewById(R.id.button);

        // Material input containers (helper text)
        emailContainer = findViewById(R.id.emailContainer);
        passwordContainer = findViewById(R.id.passwordContainer);

        // Actual text fields
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);

        // "Remember Me"
        CheckBox rememberMe = findViewById(R.id.checkBox);

        btnLogin.setOnClickListener(this);

        // Live helper text validation
        editEmail.addTextChangedListener(new SimpleTextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateEmailLive(s.toString());
            }
        });

        editPassword.addTextChangedListener(new SimpleTextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                passwordContainer.setHelperText(getPasswordErrorMessage(s.toString()));
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button) {
            toLogin();
        }
    }

    private void toLogin() {
        String email = editEmail.getText() == null ? "" : editEmail.getText().toString().trim();
        String password = editPassword.getText() == null ? "" : editPassword.getText().toString();

        // Validate once more on click
        boolean emailOk = isEmailValid(email);
        String passErr = getPasswordErrorMessage(password);

        // Email helper text
        if (TextUtils.isEmpty(email)) {
            emailContainer.setHelperText("Required");
        } else if (!emailOk) {
            emailContainer.setHelperText("Invalid email address");
        } else {
            emailContainer.setHelperText(null);
        }

        // Password helper text
        passwordContainer.setHelperText(passErr);

        if (!emailOk || passErr != null) {
            Toast.makeText(this, "Please fix the highlighted fields.", Toast.LENGTH_LONG).show();
            return;
        }

        // Credentials
        if (email.equals("test@gmail.com") && password.trim().equals("Password1!")) {
            startActivity(new Intent(MainActivity.this, Dashboard.class));
        } else {
            Toast.makeText(MainActivity.this, "Incorrect Credentials!", Toast.LENGTH_LONG).show();
        }
    }

    private void validateEmailLive(String email) {
        email = email.trim();

        if (TextUtils.isEmpty(email)) {
            emailContainer.setHelperText("Required");
            return;
        }

        emailContainer.setHelperText(isEmailValid(email) ? null : "Invalid email address");
    }

    private boolean isEmailValid(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private String getPasswordErrorMessage(String password) {
        if (password == null || password.trim().isEmpty()) return "Password cannot be empty";

        int len = password.length();
        if (len < 8) return "Must be at least 8 characters";
        if (len > 16) return "Must be at most 16 characters";

        boolean hasUpper = false, hasLower = false, hasDigit = false, hasSymbol = false;

        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);

            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else if (!Character.isWhitespace(c)) hasSymbol = true;
        }

        if (!hasUpper) return "Must include at least 1 uppercase letter (A–Z)";
        if (!hasLower) return "Must include at least 1 lowercase letter (a–z)";
        if (!hasDigit) return "Must include at least 1 number (0–9)";
        if (!hasSymbol) return "Must include at least 1 symbol (e.g., !@#$...)";

        return null; // valid
    }

    // TextWatcher helper
    public abstract static class SimpleTextWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void afterTextChanged(Editable s) {}
    }
}
