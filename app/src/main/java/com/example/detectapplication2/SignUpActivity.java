package com.example.detectapplication2;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SignUpActivity extends AppCompatActivity {
    Button btn_back, btn_signup;
    EditText edt_username, edt_email, edt_password, edt_confirm_password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        btn_back = findViewById(R.id.btn_back);
        btn_signup = findViewById(R.id.btn_signup);
        edt_email = findViewById(R.id.edt_email);
        edt_username = findViewById(R.id.edt_username);
        edt_password = findViewById(R.id.edt_password);
        edt_confirm_password = findViewById(R.id.edt_confirm_password);

        btn_back.setOnClickListener(v -> {
            // Xử lý khi nút "Back" được nhấn
            finish();
        });
    }
}