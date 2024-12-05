package com.example.detectapplication2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignInActivity extends AppCompatActivity {

    private Button btnBack, btnLogin;
    private EditText edtEmail, edtPassword;
    private FirebaseAuth mAuth;

    private final String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Ánh xạ các thành phần giao diện
        initViews();

        // Khởi tạo FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Thiết lập sự kiện cho các nút
        initListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        btnLogin = findViewById(R.id.btn_login);
        edtEmail = findViewById(R.id.edt_email);
        edtPassword = findViewById(R.id.edt_password);
    }

    private void initListeners() {
        // Sự kiện nút "Back"
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(SignInActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // Sự kiện nút "Login"
        btnLogin.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            // Kiểm tra dữ liệu đầu vào
            if (validateEmail(email) && validatePassword(password)) {
                loginUser(email, password);
            }
        });
    }

    private boolean validateEmail(String email) {
        if (email.isEmpty()) {
            edtEmail.setError("Email không được để trống");
            edtEmail.requestFocus();
            return false;
        } else if (!email.matches(emailPattern)) {
            edtEmail.setError("Email không hợp lệ");
            edtEmail.requestFocus();
            return false;
        } else {
            edtEmail.setError(null);
            return true;
        }
    }

    private boolean validatePassword(String password) {
        if (password.isEmpty()) {
            edtPassword.setError("Password không được để trống");
            edtPassword.requestFocus();
            return false;
        } else if (password.length() < 6) {
            edtPassword.setError("Password phải có ít nhất 6 ký tự");
            edtPassword.requestFocus();
            return false;
        } else {
            edtPassword.setError(null);
            return true;
        }
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            // Nếu Email đã xác thực
                            Intent intent = new Intent(SignInActivity.this, MainActivity2.class);
                            intent.putExtra("email", user.getEmail());
                            startActivity(intent);
                            finish();
                        } else {
                            // Nếu Email chưa xác thực
                            Toast.makeText(this, "Vui lòng xác thực email trước khi đăng nhập", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        // Nếu đăng nhập thất bại
                        Toast.makeText(this, "Đăng nhập thất bại: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("LoginError", "Error: ", task.getException());
                    }
                });
    }
}
