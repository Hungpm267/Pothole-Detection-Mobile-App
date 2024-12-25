package com.example.detectapplication2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class SettingFragment extends Fragment {
    TextView txt_logout;
    TextView tv_edit_profile, tv_policies, tv_security, tv_reporting, tv_notification, helpandsupports;
    private SharedPreferences sharedPreferences;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public SettingFragment() {
        // Required empty public constructor
    }

    public static SettingFragment newInstance(String param1, String param2) {
        SettingFragment fragment = new SettingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        txt_logout = view.findViewById(R.id.txt_logout);
        tv_edit_profile = view.findViewById(R.id.tv_edit_profile);
        tv_policies = view.findViewById(R.id.tv_policies);
        tv_security = view.findViewById(R.id.tv_security);
        tv_reporting = view.findViewById(R.id.report_a_problem);
        tv_notification = view.findViewById(R.id.tv_mynotification);
        helpandsupports = view.findViewById(R.id.help_and_support);

        txt_logout.setOnClickListener(v -> {
            if (getActivity() != null) {
                // Khởi tạo SharedPreferences
                sharedPreferences = getActivity().getSharedPreferences("LoginPrefs", getActivity().MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                // Xóa dữ liệu đã lưu
                editor.clear();
                editor.apply();

                // Chuyển về màn hình đăng nhập
                Intent myintent = new Intent(getActivity(), MainActivity.class);
                startActivity(myintent);

                // Kết thúc activity hiện tại
                getActivity().finish();

                // Thông báo thành công
                Toast.makeText(getContext(), "Đăng xuất thành công", Toast.LENGTH_SHORT).show();
            }
        });

        tv_edit_profile.setOnClickListener(v -> {
            Intent myintent = new Intent(getActivity(), Profile.class);
            startActivity(myintent);
        });

        tv_policies.setOnClickListener(v -> {
            Intent myintent = new Intent(getActivity(), Policies.class);
            startActivity(myintent);
        });

        helpandsupports.setOnClickListener(v -> {
            Intent myintent = new Intent(getActivity(), HelpAndSupport.class);
            startActivity(myintent);
        });

        tv_notification.setOnClickListener(v -> {
            Log.d("SettingFragment", "Notification TextView clicked");
            Intent myintent = new Intent(this.getActivity(), Notification.class);
            Log.d("SettingFragment", "Notification TextView da duoc an");
            startActivity(myintent);
        });

        tv_reporting.setOnClickListener(v -> {
            Intent myintent = new Intent(getActivity(), ReportProblem.class);
            startActivity(myintent);
        });

        tv_security.setOnClickListener(v -> {
            Intent myintent = new Intent(getActivity(), Security.class);
            startActivity(myintent);
        });

        return view;
    }
}
