package com.example.detectapplication2;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PothethonListActivity extends AppCompatActivity {
    PieChart mypiechart;
    ArrayList<PieEntry> piedata;
    private FirebaseAuth mAuth;
    private TextView UserName;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pothethonlist);

        UserName = findViewById(R.id.user_name);
        mypiechart = findViewById(R.id.pie_chart);
        piedata = new ArrayList<>();

        // Hiển thị PieChart ban đầu (rỗng)
        updatePieChart(0, 0, 0);

        // Lấy dữ liệu người dùng
        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String usernameFromDB = snapshot.child("name").getValue(String.class);
                    UserName.setText(usernameFromDB);
                } else {
                    Toast.makeText(PothethonListActivity.this, "Không tìm thấy thông tin người dùng.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        // Lấy dữ liệu pothole từ Firebase
        fetchPotholeData();
    }

    private void fetchPotholeData() {
        DatabaseReference potholeRef = FirebaseDatabase.getInstance().getReference("potholes");
        potholeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int heavyCount = 0;
                int mediumCount = 0;
                int lightCount = 0;

                for (DataSnapshot data : snapshot.getChildren()) {
                    String level = data.child("level").getValue(String.class);
                    if (level != null) {
                        switch (level) {
                            case "heavy":
                                heavyCount++;
                                break;
                            case "medium":
                                mediumCount++;
                                break;
                            case "light":
                                lightCount++;
                                break;
                        }
                    }
                }

                updatePieChart(heavyCount, mediumCount, lightCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PothethonListActivity.this, "Error fetching pothole data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePieChart(int heavy, int medium, int light) {
        piedata.clear();

        // Thêm dữ liệu vào PieChart
        piedata.add(new PieEntry(heavy, "Heavy"));
        piedata.add(new PieEntry(medium, "Medium"));
        piedata.add(new PieEntry(light, "Light"));

        PieDataSet mypieDataSet = new PieDataSet(piedata, "Pothole Severity Levels");
        mypieDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        mypieDataSet.setValueTextColor(Color.BLACK);
        mypieDataSet.setValueTextSize(16f);

        PieData mypieData = new PieData(mypieDataSet);

        mypiechart.setData(mypieData);
        mypiechart.getDescription().setEnabled(false);
        mypiechart.setCenterText("Pothole Severity");
        mypiechart.animate();
    }

}
