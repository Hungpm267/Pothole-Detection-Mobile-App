package com.example.detectapplication2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SignInActivity extends AppCompatActivity {
    Button btnBack, btnLogin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);
        btnBack = findViewById(R.id.btn_back);
        btnLogin = findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(v -> {
            // Xử lý khi nút "Login" được nhấn
            Intent myintent = new Intent(SignInActivity.this, MainActivity2.class);
            startActivity(myintent);
        });
        btnBack.setOnClickListener(v -> {
            // Xử lý khi nút "Back" được nhấn
            finish();
        });

    }
}