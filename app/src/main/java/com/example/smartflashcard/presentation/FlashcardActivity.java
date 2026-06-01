package com.example.smartflashcard.presentation;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import androidx.exifinterface.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import com.example.smartflashcard.R;
import com.example.smartflashcard.data.FlashcardModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;
import com.yuyakaido.android.cardstackview.CardStackLayoutManager;
import com.yuyakaido.android.cardstackview.CardStackListener;
import com.yuyakaido.android.cardstackview.CardStackView;
import com.yuyakaido.android.cardstackview.Direction;
import com.yuyakaido.android.cardstackview.Duration;
import com.yuyakaido.android.cardstackview.SwipeAnimationSetting;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    private Uri selectedImageUri;
    private Uri cameraImageUri;
    private ImageView dialogImageView;
    private TextInputEditText etWordInDialog;
    private MaterialCheckBox cbAutoCropInDialog;
    private Rect mainDetectedRect;

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    if (dialogImageView != null) {
                        dialogImageView.setImageURI(uri);
                        dialogImageView.setVisibility(View.VISIBLE);
                    }
                    if (etWordInDialog != null) {
                        recognizeObject(uri);
                    }
                }
            }
    );

    private final ActivityResultLauncher<Uri> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            success -> {
                if (success && cameraImageUri != null) {
                    selectedImageUri = cameraImageUri;
                    if (dialogImageView != null) {
                        dialogImageView.setImageURI(selectedImageUri);
                        dialogImageView.setVisibility(View.VISIBLE);
                    }
                    if (etWordInDialog != null) {
                        recognizeObject(selectedImageUri);
                    }
                }
            }
    );

    private void recognizeObject(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            bitmap = rotateImageIfRequired(bitmap, uri);
            
            InputImage image = InputImage.fromBitmap(bitmap, 0);
            Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            Canvas canvas = new Canvas(mutableBitmap);
            
            Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(8.0f);

            ObjectDetectorOptions options = new ObjectDetectorOptions.Builder()
                    .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
                    .enableMultipleObjects()
                    .enableClassification()
                    .build();

            ObjectDetector objectDetector = ObjectDetection.getClient(options);

            objectDetector.process(image)
                    .addOnSuccessListener(objects -> {
                        if (!objects.isEmpty()) {
                            mainDetectedRect = objects.get(0).getBoundingBox();
                            if (cbAutoCropInDialog != null) {
                                cbAutoCropInDialog.setVisibility(View.VISIBLE);
                            }

                            for (com.google.mlkit.vision.objects.DetectedObject detectedObject : objects) {
                                Rect rect = detectedObject.getBoundingBox();
                                canvas.drawRect(rect, paint);
                                
                                // Nếu có nhãn, lấy nhãn đầu tiên điền vào ô văn bản
                                if (!detectedObject.getLabels().isEmpty()) {
                                    String label = detectedObject.getLabels().get(0).getText();
                                    if (etWordInDialog != null) {
                                        etWordInDialog.setText(label);
                                    }
                                }
                            }
                            if (dialogImageView != null) {
                                dialogImageView.setImageBitmap(mutableBitmap);
                            }
                            Toast.makeText(this, "Đã khoanh vùng được " + objects.size() + " vật thể", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Không thể nhận dạng vật thể", Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

        if (deckId == null || deckId.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không nhận được ID bộ thẻ!", Toast.LENGTH_LONG).show();
        }

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
        etWordInDialog = etWord; // Lưu lại để điền từ nhận dạng được

        TextInputEditText etWordType = dialogView.findViewById(R.id.etWordType);
        TextInputEditText etIpa = dialogView.findViewById(R.id.etIpa);
        TextInputEditText etMeaning = dialogView.findViewById(R.id.etMeaning);
        TextInputEditText etExample = dialogView.findViewById(R.id.etExample);
        TextInputEditText etExampleMeaning = dialogView.findViewById(R.id.etExampleMeaning);
        dialogImageView = dialogView.findViewById(R.id.ivSelectedImage);
        cbAutoCropInDialog = dialogView.findViewById(R.id.cbAutoCrop);
        MaterialButton btnPickImage = dialogView.findViewById(R.id.btnPickImage);
        MaterialButton btnCaptureImage = dialogView.findViewById(R.id.btnCaptureImage);

        selectedImageUri = null;

        btnPickImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        btnCaptureImage.setOnClickListener(v -> {
            File photoFile = new File(getFilesDir(), "camera_photo_" + System.currentTimeMillis() + ".jpg");
            cameraImageUri = FileProvider.getUriForFile(this, "com.example.smartflashcard.fileprovider", photoFile);
            cameraLauncher.launch(cameraImageUri);
        });

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

                    if (word.isEmpty() || meaning.isEmpty()) {
                        Toast.makeText(FlashcardActivity.this, "Vui lòng nhập từ và nghĩa!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (deckId == null) {
                        Toast.makeText(FlashcardActivity.this, "Lỗi: Không tìm thấy ID bộ thẻ!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String cardId = databaseReference.push().getKey();
                    if (cardId != null) {
                        String imagePath = null;
                        if (selectedImageUri != null) {
                            boolean shouldCrop = cbAutoCropInDialog != null && cbAutoCropInDialog.isChecked();
                            imagePath = saveImageToInternalStorage(selectedImageUri, shouldCrop);
                        }
                        FlashcardModel newCard = new FlashcardModel(cardId, deckId, word, wordType, ipa, meaning, example, exampleMeaning, imagePath);
                        databaseReference.child(cardId).setValue(newCard).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(FlashcardActivity.this, "Đã lưu flashcard thành công!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(FlashcardActivity.this, "Lưu thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .setOnDismissListener(dialog -> etWordInDialog = null)
                .show();
    }

    private String saveImageToInternalStorage(Uri uri, boolean shouldCrop) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            bitmap = rotateImageIfRequired(bitmap, uri);

            if (shouldCrop && mainDetectedRect != null) {
                // Đảm bảo tọa độ cắt không vượt quá kích thước ảnh
                int left = Math.max(0, mainDetectedRect.left);
                int top = Math.max(0, mainDetectedRect.top);
                int width = Math.min(bitmap.getWidth() - left, mainDetectedRect.width());
                int height = Math.min(bitmap.getHeight() - top, mainDetectedRect.height());

                if (width > 0 && height > 0) {
                    Bitmap croppedBitmap = Bitmap.createBitmap(bitmap, left, top, width, height);
                    if (croppedBitmap != bitmap) {
                        bitmap.recycle();
                        bitmap = croppedBitmap;
                    }
                }
            }

            String fileName = "IMG_" + UUID.randomUUID().toString() + ".jpg";
            File file = new File(getFilesDir(), fileName);
            FileOutputStream outputStream = new FileOutputStream(file);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);

            outputStream.flush();
            outputStream.close();
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Bitmap rotateImageIfRequired(Bitmap img, Uri selectedImage) throws Exception {
        InputStream input = getContentResolver().openInputStream(selectedImage);
        if (input == null) return img;
        
        ExifInterface ei = new ExifInterface(input);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        input.close();

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }
}