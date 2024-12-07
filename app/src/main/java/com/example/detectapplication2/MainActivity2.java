package com.example.detectapplication2;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.detectapplication2.databinding.ActivityMain2Binding;
import com.example.detectapplication2.databinding.ActivityMainBinding;

public class MainActivity2 extends AppCompatActivity {

    ActivityMain2Binding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMain2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String userName = getIntent().getStringExtra("name");
        Log.d("MainActivity2", "Logged-in User: " + userName);
        String userid = getIntent().getStringExtra("uid");
        String userEmail = getIntent().getStringExtra("email");
        String userPassword = getIntent().getStringExtra("password");
        replaceFragment(new HomeFragment(), userName);

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home) {
                replaceFragment(new HomeFragment(), userName);
            } else if (item.getItemId() == R.id.map) {
                replaceFragment(new MapFragment(), null);
            } else if (item.getItemId() == R.id.setting) {
                replaceFragment(new SettingFragment(), null);
            }

            return true;
        });
    }

    private void replaceFragment(Fragment fragment, String userName){
        if(userName != null && fragment instanceof HomeFragment){
            Bundle bundle = new Bundle();
            bundle.putString("name", userName);
            Log.d("Bundle", "name" + bundle.getString("name"));
            fragment.setArguments(bundle);
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
}