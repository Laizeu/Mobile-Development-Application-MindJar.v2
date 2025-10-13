package com.example.myapplication;

import android.accounts.Account;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    Button btnLogin;
    EditText userName, userPassword;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        btnLogin = findViewById(R.id.button);
        userName = findViewById(R.id.editUserName);
        userPassword = findViewById(R.id.editPassword);

        btnLogin.setOnClickListener((View.OnClickListener) this);
    }

    public void onClick(View v){
        if(v.getId() == R.id.button){
            toLogin(v);
        }
    }

    public void toLogin(View v){
        String email = String.valueOf(userName.getText());
        String password = String.valueOf(userPassword.getText());

        Intent toDashboardPage = new Intent(MainActivity.this, Dashboard.class);
        startActivity(toDashboardPage);

        /* ---------------- Do not remove ------------------ */
//        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
//            Toast.makeText(MainActivity.this, "Email and/or Password is empty!", Toast.LENGTH_LONG).show();
//        }else if(email.equals("user") && password.equals("admin")){
//            Toast.makeText(MainActivity.this, "You are login!", Toast.LENGTH_LONG).show();
//        }else{
//            Toast.makeText(MainActivity.this, "Incorrect Credentials!", Toast.LENGTH_LONG).show();
//        }
    }




}