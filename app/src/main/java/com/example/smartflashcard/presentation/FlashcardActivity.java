package com.example.smartflashcard.presentation;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.smartflashcard.R;
import com.example.smartflashcard.data.FlashcardModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yuyakaido.android.cardstackview.CardStackLayoutManager;
import com.yuyakaido.android.cardstackview.CardStackListener;
import com.yuyakaido.android.cardstackview.CardStackView;
import com.yuyakaido.android.cardstackview.Direction;
import com.yuyakaido.android.cardstackview.Duration;
import com.yuyakaido.android.cardstackview.SwipeAnimationSetting;
import java.util.ArrayList;
import java.util.List;

public class FlashcardActivity extends AppCompatActivity implements CardStackListener {

    private MaterialToolbar toolbar;
    private CardStackView cardStackView;
    private TextView tvCardIndicator, tvStillLearningCount, tvKnowCount;
    private MaterialButton btnPrevious, btnNext;

    private DatabaseReference databaseReference;
    private List<FlashcardModel> flashcardList;
    private FlashcardSwipeAdapter adapter;
    private CardStackLayoutManager manager;

    private String deckId;
    private int stillLearningCount = 0;
    private int knowCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard);

        // Ánh xạ linh kiện
        toolbar = findViewById(R.id.toolbarFlashcard);
        cardStackView = findViewById(R.id.cardStackView);
        tvCardIndicator = findViewById(R.id.tvCardIndicator);
        tvStillLearningCount = findViewById(R.id.tvStillLearningCount);
        tvKnowCount = findViewById(R.id.tvKnowCount);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);

        deckId = getIntent().getStringExtra("DECK_ID");
        String deckName = getIntent().getStringExtra("DECK_NAME");
        if (deckName != null) {
            toolbar.setTitle(deckName);
        }

        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.inflateMenu(R.menu.menu_flashcard);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_add_card) {
                showAddCardDialog();
                return true;
            }
            return false;
        });

        // Khởi tạo bộ khung quản lý vuốt card
        manager = new CardStackLayoutManager(this, this);
        manager.setDirections(Direction.HORIZONTAL); // Chỉ cho vuốt sang Trái / Phải
        manager.setCanScrollHorizontal(true);
        manager.setCanScrollVertical(false); // Khóa vuốt lên xuống
        cardStackView.setLayoutManager(manager);

        flashcardList = new ArrayList<>();
        adapter = new FlashcardSwipeAdapter(flashcardList);
        cardStackView.setAdapter(adapter);

        databaseReference = FirebaseDatabase.getInstance("https://smart-7d648-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Flashcards");
        loadFlashcardsFromFirebase();

        // Xử lý sự kiện bấm nút Sau -> Ép tự động vuốt bay sang Phải bằng code
        btnNext.setOnClickListener(v -> {
            SwipeAnimationSetting setting = new SwipeAnimationSetting.Builder()
                    .setDirection(Direction.Right)
                    .setDuration(Duration.Normal.duration)
                    .setInterpolator(new LinearInterpolator())
                    .build();
            manager.setSwipeAnimationSetting(setting);
            cardStackView.swipe();
        });

        // Nút Trước (Tạm thời cho cuộn lại thẻ vừa vuốt)
        btnPrevious.setOnClickListener(v -> cardStackView.rewind());
    }

    private void loadFlashcardsFromFirebase() {
        databaseReference.orderByChild("deckId").equalTo(deckId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        flashcardList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            FlashcardModel card = dataSnapshot.getValue(FlashcardModel.class);
                            if (card != null) {
                                flashcardList.add(card);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        updateIndicatorUI(manager.getTopPosition());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(FlashcardActivity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateIndicatorUI(int position) {
        if (flashcardList.isEmpty()) {
            tvCardIndicator.setText("0 / 0");
            btnNext.setEnabled(false);
            btnPrevious.setEnabled(false);
            return;
        }
        tvCardIndicator.setText((Math.min(position + 1, flashcardList.size())) + " / " + flashcardList.size());
        btnPrevious.setEnabled(position > 0);
        btnNext.setEnabled(position < flashcardList.size() - 1);
    }

    // --- BẮT SỰ KIỆN VUỐT THẺ (CARD STACK LISTENER) ---
    @Override
    public void onCardSwiped(Direction direction) {
        // Vuốt sang Phải = Đã thuộc bài
        if (direction == Direction.Right) {
            knowCount++;
            tvKnowCount.setText(String.valueOf(knowCount));
            Toast.makeText(this, "Đã thuộc! 👍", Toast.LENGTH_SHORT).show();
        }
        // Vuốt sang Trái = Chưa thuộc / Cần học lại
        else if (direction == Direction.Left) {
            stillLearningCount++;
            tvStillLearningCount.setText(String.valueOf(stillLearningCount));
            Toast.makeText(this, "Cần ôn lại 📋", Toast.LENGTH_SHORT).show();
        }
        updateIndicatorUI(manager.getTopPosition());
    }

    @Override
    public void onCardAppeared(View view, int position) {
        updateIndicatorUI(position);
    }

    @Override public void onCardDisappeared(View view, int position) {}
    @Override public void onCardDragging(Direction direction, float ratio) {}
    @Override public void onCardRewound() { updateIndicatorUI(manager.getTopPosition()); }
    @Override public void onCardCanceled() {}

    private void showAddCardDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_flashcard, null);
        TextInputEditText etWord = dialogView.findViewById(R.id.etWord);
        TextInputEditText etWordType = dialogView.findViewById(R.id.etWordType);
        TextInputEditText etIpa = dialogView.findViewById(R.id.etIpa);
        TextInputEditText etMeaning = dialogView.findViewById(R.id.etMeaning);
        TextInputEditText etExample = dialogView.findViewById(R.id.etExample);
        TextInputEditText etExampleMeaning = dialogView.findViewById(R.id.etExampleMeaning);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Thêm Từ Vựng Mới")
                .setView(dialogView)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String word = etWord.getText().toString().trim();
                    String wordType = etWordType.getText().toString().trim();
                    String ipa = etIpa.getText().toString().trim();
                    String meaning = etMeaning.getText().toString().trim();
                    String example = etExample.getText().toString().trim();
                    String exampleMeaning = etExampleMeaning.getText().toString().trim();

                    if (!word.isEmpty() && !meaning.isEmpty()) {
                        String cardId = databaseReference.push().getKey();
                        if (cardId != null) {
                            FlashcardModel newCard = new FlashcardModel(cardId, deckId, word, wordType, ipa, meaning, example, exampleMeaning);
                            databaseReference.child(cardId).setValue(newCard);
                        }
                    }
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .show();
    }
}