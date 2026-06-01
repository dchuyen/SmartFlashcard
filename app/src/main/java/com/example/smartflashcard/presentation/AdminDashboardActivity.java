package com.example.smartflashcard.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class AdminDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Thiết kế nhanh giao diện Admin bằng code Java để bạn chạy thử luôn không lo lỗi XML
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(64, 64, 64, 64);
        layout.setGravity(android.view.Gravity.CENTER);
        layout.setBackgroundColor(android.graphics.Color.parseColor("#FFF5F5")); // Nền đỏ nhạt phân biệt admin

        TextView tvTitle = new TextView(this);
        tvTitle.setText("HỆ THỐNG QUẢN TRỊ ADMIN");
        tvTitle.setTextSize(24);
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        tvTitle.setTextColor(android.graphics.Color.parseColor("#C92A2A"));
        tvTitle.setPadding(0, 0, 0, 48);
        layout.addView(tvTitle);

        MaterialButton btnLogout = new MaterialButton(this);
        btnLogout.setText("Đăng Xuất Hệ Thống");
        btnLogout.setBackgroundColor(android.graphics.Color.parseColor("#C92A2A"));
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
        layout.addView(btnLogout);

        setContentView(layout);
    }
}