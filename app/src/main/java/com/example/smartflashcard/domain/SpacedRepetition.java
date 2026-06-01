package com.example.smartflashcard.domain;

import com.example.smartflashcard.data.ProgressModel;

public class SpacedRepetition {

    /**
     * Hàm cốt lõi tính toán lịch ôn tập tiếp theo dựa trên thuật toán SM-2 chuẩn quốc tế.
     * @param progress Bản ghi tiến độ hiện tại của từ vựng
     * @param quality Điểm chất lượng do User đánh giá khi lật thẻ (từ 0 đến 5)
     * 0: Quên sạch sành sanh
     * 1: Nhìn quen quen nhưng chịu không nhớ nghĩa
     * 2: Suy nghĩ hồi lâu mới nhớ ra và bị sai một chút
     * 3: Nhớ nghĩa nhưng mất thời gian suy nghĩ
     * 4: Nhớ chính xác và chỉ mất một chút ngập ngừng
     * 5: Nhớ hoàn hảo, bật ra ngay lập tức
     * @return Bản ghi progress mới đã được cập nhật số ngày chờ và ngày ôn tập tiếp theo
     */
    public static ProgressModel calculateNextReview(ProgressModel progress, int quality) {
        // Nếu người học đánh giá từ này quá khó (quality < 3), bắt học lại từ đầu
        if (quality < 3) {
            progress.setIntervalDays(1); // Ngày mai phải ôn lại ngay
            // Giữ nguyên hệ số độ dễ (easeFactor) nhưng giảm nhẹ để thuật toán lặp lại dày hơn
            double newEF = progress.getEaseFactor() - 0.2;
            if (newEF < 1.3) newEF = 1.3; // Hệ số độ dễ tối thiểu theo chuẩn SM-2 là 1.3
            progress.setEaseFactor(newEF);
        } else {
            // Trường hợp người học nhớ được từ (quality >= 3)
            int currentInterval = progress.getIntervalDays();
            int nextInterval;

            if (currentInterval == 1) {
                nextInterval = 6; // Nếu là lần đầu tiên nhớ được, khoảng cách lần sau là 6 ngày
            } else {
                // Các lần tiếp theo: Khoảng cách mới = Khoảng cách cũ * Hệ số độ dễ (Ease Factor)
                nextInterval = (int) Math.round(currentInterval * progress.getEaseFactor());
            }

            // Cập nhật hệ số độ dễ mới dựa trên công thức toán học SM-2
            double currentEF = progress.getEaseFactor();
            double newEF = currentEF + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02));
            if (newEF < 1.3) newEF = 1.3;

            progress.setIntervalDays(nextInterval);
            progress.setEaseFactor(newEF);
        }

        // Tính toán chính xác Timestamp của ngày ôn tập tiếp theo
        // Công thức: Ngày tiếp theo = Thời gian hiện tại + (Số ngày chờ * 24 giờ * 60 phút * 60 giây * 1000 mili giây)
        long millisecondsInDay = progress.getIntervalDays() * 24L * 60L * 60L * 1000L;
        long nextReviewTimestamp = System.currentTimeMillis() + millisecondsInDay;

        progress.setNextReviewDate(nextReviewTimestamp);

        return progress;
    }
}