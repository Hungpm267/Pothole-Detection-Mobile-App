package com.example.detectapplication2;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.detectapplication2.databinding.ActivityMain2Binding;

public class MainActivity2 extends AppCompatActivity {

    ActivityMain2Binding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMain2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Kiểm tra Intent nếu có yêu cầu hiển thị SettingFragment
        Intent intent = getIntent();
        String fragment = intent.getStringExtra("fragment");

        if (fragment != null && fragment.equals("setting")) {
            replaceFragment(new SettingFragment());
            binding.bottomNavigationView.setSelectedItemId(R.id.setting);
        } else {
            replaceFragment(new HomeFragment());  // Mặc định hiển thị HomeFragment
            binding.bottomNavigationView.setSelectedItemId(R.id.home);
        }

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home) {
                replaceFragment(new HomeFragment());
            } else if (item.getItemId() == R.id.map) {
                replaceFragment(new MapFragment());
            } else if (item.getItemId() == R.id.setting) {
                replaceFragment(new SettingFragment());
            }

            return true;
        });
    }

    // Hàm thay thế fragment trong container
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment); // Thay thế fragment vào container
        fragmentTransaction.commit();
    }
}
