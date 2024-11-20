package com.example.detectapplication2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ForgotPasswordActivity extends AppCompatActivity {
    EditText edt_email;
    Button btn_back, btn_continue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

        edt_email = findViewById(R.id.edt_email);
        btn_back = findViewById(R.id.btn_back);
        btn_continue = findViewById(R.id.btn_continue);

        btn_back.setOnClickListener(v -> {
            // Xử lý khi nút "Back" được nhấn
            finish();
        });
        btn_continue.setOnClickListener(v -> {
            // Xử lý khi nút "Continue" được nhấn
            String email = edt_email.getText().toString();
            // xử lý vụ otp backend


            // chuyển sang màn hình otp
            Intent myintent = new Intent(this, OtpActivity.class);
            startActivity(myintent);
        });
    }
}