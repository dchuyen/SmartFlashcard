package com.example.smartflashcard.data;

import java.io.Serializable;

public class FlashcardModel implements Serializable {
    private String id;             // Mã định danh duy nhất của thẻ trên Firebase
    private String deckId;         // Mã định danh liên kết thẻ này với bộ thẻ chủ trên Firebase
    private String word;           // Từ vựng (Hiển thị mặt trước)
    private String wordType;       // Loại từ: noun, verb, adj (Hiển thị mặt trước)
    private String ipa;            // Phiên âm quốc tế (Hiển thị mặt sau)
    private String meaning;        // Nghĩa tiếng Việt (Hiển thị mặt sau)
    private String example;        // Câu ví dụ (Hiển thị mặt sau)
    private String exampleMeaning; // Nghĩa của câu ví dụ (Hiển thị mặt sau)
    private String imagePath;      // Đường dẫn ảnh lưu tại máy (Local)

    // Các trường phục vụ thuật toán Lặp lại ngắt quãng (Spaced Repetition)
    private int intervalDays;      // Khoảng thời gian lặp lại (tính theo ngày)
    private double easeFactor;     // Hệ số độ dễ của từ vựng (mặc định tiêu chuẩn là 2.5)
    private long nextReviewDate;   // Timestamp ngày tiếp theo bắt buộc phải ôn từ này

    // Constructor trống: BẮT BUỘC phải có để Firebase có thể tự động parse dữ liệu sau này
    public FlashcardModel() {
    }

    // Constructor đầy đủ dùng để khởi tạo nhanh một thẻ mới (Đã tích hợp deckId)
    public FlashcardModel(String id, String deckId, String word, String wordType, String ipa, String meaning, String example, String exampleMeaning) {
        this.id = id;
        this.deckId = deckId;
        this.word = word;
        this.wordType = wordType;
        this.ipa = ipa;
        this.meaning = meaning;
        this.example = example;
        this.exampleMeaning = exampleMeaning;
        this.imagePath = null;
        this.intervalDays = 1;       // Từ mới mặc định ngày mai phải ôn lại ngay
        this.easeFactor = 2.5;       // Hệ số tiêu chuẩn thuật toán SM-2
        this.nextReviewDate = System.currentTimeMillis(); // Mặc định hiển thị để học ngay
    }

    // Constructor có ảnh
    public FlashcardModel(String id, String deckId, String word, String wordType, String ipa, String meaning, String example, String exampleMeaning, String imagePath) {
        this(id, deckId, word, wordType, ipa, meaning, example, exampleMeaning);
        this.imagePath = imagePath;
    }

    // Các hàm Getter và Setter để đóng gói (Encapsulation) dữ liệu hợp lệ
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDeckId() { return deckId; }
    public void setDeckId(String deckId) { this.deckId = deckId; }

    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }

    public String getWordType() { return wordType; }
    public void setWordType(String wordType) { this.wordType = wordType; }

    public String getIpa() { return ipa; }
    public void setIpa(String ipa) { this.ipa = ipa; }

    public String getMeaning() { return meaning; }
    public void setMeaning(String meaning) { this.meaning = meaning; }

    public String getExample() { return example; }
    public void setExample(String example) { this.example = example; }

    public String getExampleMeaning() { return exampleMeaning; }
    public void setExampleMeaning(String exampleMeaning) { this.exampleMeaning = exampleMeaning; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public int getIntervalDays() { return intervalDays; }
    public void setIntervalDays(int intervalDays) { this.intervalDays = intervalDays; }

    public double getEaseFactor() { return easeFactor; }
    public void setEaseFactor(double easeFactor) { this.easeFactor = easeFactor; }

    public long getNextReviewDate() { return nextReviewDate; }
    public void setNextReviewDate(long nextReviewDate) { this.nextReviewDate = nextReviewDate; }
}