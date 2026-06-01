package com.example.smartflashcard.data;

import java.io.Serializable;

public class DeckModel implements Serializable {
    private String id;          // Mã định danh duy nhất của bộ thẻ trên Firebase
    private String name;        // Tên của bộ thẻ (Ví dụ: Từ vựng IT năm 4)
    private String description; // Mô tả ngắn gọn về bộ thẻ này
    private String createdBy;   // Người tạo bộ thẻ ("ADMIN" hoặc ID của User)
    private long createdAt;     // Thời gian tạo bộ thẻ (Dùng kiểu long để lưu Timestamp)

    // Constructor trống: BẮT BUỘC phải có để Firebase tự động parse dữ liệu từ Cloud về Object
    public DeckModel() {
    }

    // Constructor đầy đủ: Dùng để khởi tạo nhanh một Bộ thẻ mới khi Admin/User bấm nút "Tạo"
    public DeckModel(String id, String name, String description, String createdBy) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
        this.createdAt = System.currentTimeMillis(); // Tự động lấy thời gian hiện tại của hệ thống
    }

    // --- HỆ THỐNG CÁC HÀM GETTER VÀ SETTER TIÊU CHUẨN ---
    // Giúp các màn hình giao diện hoặc hàm chức năng khác đọc và sửa dữ liệu một cách an toàn

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}