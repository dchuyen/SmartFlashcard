package com.example.smartflashcard.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartflashcard.R;
import com.example.smartflashcard.adapter.DeckAdapter;
import com.example.smartflashcard.data.DeckModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvDecks;
    private DeckAdapter deckAdapter;
    private List<DeckModel> deckList;
    private ImageView ivAddDeckToolbar; // 🛠️ Thay đổi sang Icon trên Toolbar
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Ánh xạ linh kiện từ sơ đồ cấu trúc bố cục XML mới
        rvDecks = findViewById(R.id.rvDecks);
        ivAddDeckToolbar = findViewById(R.id.ivAddDeckToolbar);

        deckList = new ArrayList<>();

        // 2. Cấu hình Adapter truyền kèm Callback bẻ lái sang màn hình FlashcardActivity
        deckAdapter = new DeckAdapter(deckList, deck -> {
            Intent intent = new Intent(MainActivity.this, FlashcardActivity.class);
            intent.putExtra("DECK_ID", deck.getId());
            intent.putExtra("DECK_NAME", deck.getName());
            startActivity(intent);
        });

        rvDecks.setLayoutManager(new LinearLayoutManager(this));
        rvDecks.setAdapter(deckAdapter);

        // 3. Kết nối Realtime Database chỉ định cụm máy chủ Singapore
        databaseReference = FirebaseDatabase.getInstance("https://smart-7d648-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Decks");

        // Luồng lắng nghe dữ liệu thời gian thực từ Firebase
        loadDecksFromFirebase();

        // Đón nhận hành vi bấm vào nút dấu cộng mảnh ở góc phải Toolbar đỉnh đầu
        if (ivAddDeckToolbar != null) {
            ivAddDeckToolbar.setOnClickListener(v -> showAddDeckDialog());
        }
    }

    private void showAddDeckDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_deck, null);
        TextInputEditText etDeckName = dialogView.findViewById(R.id.etDeckName);
        TextInputEditText etDeckDesc = dialogView.findViewById(R.id.etDeckDesc);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Tạo Bộ Thẻ Mới")
                .setView(dialogView)
                .setPositiveButton("Thêm", (dialog, which) -> {
                    String name = etDeckName.getText().toString().trim();
                    String desc = etDeckDesc.getText().toString().trim();

                    if (!name.isEmpty()) {
                        String deckId = databaseReference.push().getKey();
                        if (deckId != null) {
                            DeckModel newDeck = new DeckModel(deckId, name, desc, "admin");
                            databaseReference.child(deckId).setValue(newDeck)
                                    .addOnSuccessListener(aVoid -> Toast.makeText(MainActivity.this, "Đã đồng bộ lên Firebase!", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Tên bộ thẻ trống!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void loadDecksFromFirebase() {
        // Giữ nguyên cơ chế addValueEventListener gốc để đồng bộ Realtime liên tục khi thêm bộ thẻ
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                deckList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    DeckModel deck = dataSnapshot.getValue(DeckModel.class);
                    if (deck != null) {
                        deckList.add(deck);
                    }
                }
                deckAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}