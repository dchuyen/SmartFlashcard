package com.example.smartflashcard.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.smartflashcard.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;
    private TextView tvAvatarChar;
    private TextView tvContinueDeckName, tvProgressText;
    private LinearProgressIndicator progressLearning;
    private MaterialButton btnContinueLearning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 1. Ánh xạ toàn bộ linh kiện từ XML bao gồm cả khối Học tiếp
        bottomNavigation = findViewById(R.id.bottomNavigationHome);
        tvContinueDeckName = findViewById(R.id.tvContinueDeckName);
        tvProgressText = findViewById(R.id.tvProgressText);
        progressLearning = findViewById(R.id.progressLearning);
        btnContinueLearning = findViewById(R.id.btnContinueLearning);

        // Đảm bảo ánh xạ Avatar an toàn tránh lỗi crash NullPointer
        View cardAvatar = findViewById(R.id.cardAvatar);
        if (cardAvatar != null) {
            tvAvatarChar = cardAvatar.findViewById(R.id.tvAvatarChar);
        } else {
            tvAvatarChar = findViewById(R.id.tvAvatarChar);
        }

        // Đặt trạng thái nút Trang chủ đang được chọn active trên thanh điều hướng
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_home);
        }

        // 2. Điền chữ cái đầu của email đăng nhập thật vào avatar tròn
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getEmail() != null && tvAvatarChar != null) {
            String firstChar = user.getEmail().substring(0, 1).toUpperCase();
            tvAvatarChar.setText(firstChar);
        }

        // 3. Thiết lập trạng thái chờ sạch sẽ, dọn sạch hoàn toàn dữ liệu bịa
        if (tvContinueDeckName != null) tvContinueDeckName.setText("...");
        if (tvProgressText != null) tvProgressText.setText("Đã học .../... thẻ");
        if (progressLearning != null) progressLearning.setProgress(0);
        if (btnContinueLearning != null) btnContinueLearning.setEnabled(false);

        // 4. Bắt sự kiện chuyển Tab giả lập bằng Intent sang trang Thư viện (MainActivity)
        if (bottomNavigation != null) {
            bottomNavigation.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_library) {
                    Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0); // Hiệu ứng lướt mượt mà không giật lag
                    return true;
                }
                return true;
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Khi quay lại trang chủ, ép nút Trang chủ sáng lên đúng vị trí
        if (bottomNavigation != null) {
            bottomNavigation.setOnItemSelectedListener(null);
            bottomNavigation.setSelectedItemId(R.id.nav_home);

            // Kích hoạt lại Listener lắng nghe lượt bấm tiếp theo
            bottomNavigation.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_library) {
                    Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                }
                return true;
            });
        }
    }
}