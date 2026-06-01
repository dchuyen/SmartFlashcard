package com.example.smartflashcard.data;

import java.io.Serializable;

public class UserModel implements Serializable {
    private String uid;          // ID duy nhất đồng bộ từ hệ thống bảo mật Firebase Auth
    private String displayName;  // Tên hiển thị của người dùng (Ví dụ: Nguyễn Văn A)
    private String email;        // Địa chỉ email đăng ký tài khoản
    private String avatarUrl;    // Đường dẫn ảnh đại diện (để trống nếu dùng ảnh mặc định)
    private String role;         // Phân quyền hệ thống: "admin" hoặc "user"

    // Constructor trống: BẮT BUỘC phải có để Firebase tự động parse dữ liệu Cloud về Object
    public UserModel() {
    }

    // Constructor đầy đủ: Dùng để khởi tạo khi một tài khoản mới được đăng ký thành công
    public UserModel(String uid, String displayName, String email, String role) {
        this.uid = uid;
        this.displayName = displayName;
        this.email = email;
        this.avatarUrl = "";     // Mặc định ban đầu để chuỗi rỗng, người dùng cập nhật sau
        this.role = role;        // Truyền vào "user" cho học viên, hoặc "admin" cho nick quản trị của bạn
    }

    // --- HỆ THỐNG CÁC HÀM GETTER VÀ SETTER TIÊU CHUẨN ---
    // Giúp ứng dụng kiểm tra quyền truy cập và hiển thị thông tin cá nhân lên màn hình Profile

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}