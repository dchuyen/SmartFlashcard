package com.example.smartflashcard.data;

import java.io.Serializable;

public class ProgressModel implements Serializable {
    private String id;             // Mã định danh duy nhất của bản ghi tiến độ trên Firebase
    private String userId;         // ID của người học (Lấy từ Firebase Auth)
    private String cardId;         // ID của tấm thẻ từ vựng đang học (Liên kết với bảng Cards)
    private int intervalDays;      // Khoảng cách giữa các lần ôn tập (tính bằng ngày)
    private double easeFactor;     // Hệ số độ dễ của từ (Mặc định bắt đầu là 2.5 theo chuẩn SM-2)
    private long nextReviewDate;   // Ngày tiếp theo bắt buộc phải ôn từ này (Dạng Timestamp)

    // Constructor trống: BẮT BUỘC phải có để Firebase tự động parse dữ liệu Cloud về Object
    public ProgressModel() {
    }

    // Constructor đầy đủ: Dùng khi User vừa bấm nút "Học" một từ mới tinh lần đầu tiên
    public ProgressModel(String id, String userId, String cardId) {
        this.id = id;
        this.userId = userId;
        this.cardId = cardId;
        this.intervalDays = 1;      // Mặc định từ mới tinh thì ngày mai (sau 1 ngày) phải ôn lại luôn
        this.easeFactor = 2.5;      // Chỉ số tiêu chuẩn của thuật toán bộ nhớ SuperMemo
        this.nextReviewDate = System.currentTimeMillis(); // Kích hoạt thời gian hiện tại để hiện lên học ngay
    }

    // --- HỆ THỐNG CÁC HÀM GETTER VÀ SETTER TIÊU CHUẨN ---
    // Giúp module tính toán thuật toán đọc dữ liệu lên xử lý rồi lưu lại vào Firebase

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public int getIntervalDays() {
        return intervalDays;
    }

    public void setIntervalDays(int intervalDays) {
        this.intervalDays = intervalDays;
    }

    public double getEaseFactor() {
        return easeFactor;
    }

    public void setEaseFactor(double easeFactor) {
        this.easeFactor = easeFactor;
    }

    public long getNextReviewDate() {
        return nextReviewDate;
    }

    public void setNextReviewDate(long nextReviewDate) {
        this.nextReviewDate = nextReviewDate;
    }
}