package com.example.detectapplication2;

import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

public class PothethonListActivity extends AppCompatActivity {
    PieChart mypiechart;
    ArrayList<PieEntry> piedata;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pothethonlist);

        mypiechart = findViewById(R.id.pie_chart);
        piedata = new ArrayList<>();

        piedata.add(new PieEntry(3, "rat nghiem trong"));
        piedata.add(new PieEntry(2, "nghiem trong"));
        piedata.add(new PieEntry(1, "khong nghiem trong"));
        piedata.add(new PieEntry(2, "nghiem trong"));

        PieDataSet mypieDataSet = new PieDataSet(piedata, "pothole severity level");
        mypieDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        mypieDataSet.setValueTextColor(Color.BLACK);
        mypieDataSet.setValueTextSize(16f);

        PieData mypieData = new PieData(mypieDataSet);

        mypiechart.setData(mypieData);
        mypiechart.getDescription().setEnabled(false);
        mypiechart.setCenterText("severity!");
        mypiechart.animate();
    }
}
