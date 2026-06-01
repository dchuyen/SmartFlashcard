package com.example.smartflashcard.presentation;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartflashcard.R;
import com.example.smartflashcard.data.FlashcardModel;
import com.google.android.material.card.MaterialCardView;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlashcardSwipeAdapter extends RecyclerView.Adapter<FlashcardSwipeAdapter.ViewHolder> {

    private final List<FlashcardModel> flashcardList;
    // Lưu trạng thái xem thẻ này đang ở mặt trước hay mặt sau dựa vào ID của thẻ
    private final Map<String, Boolean> cardStateMap = new HashMap<>();

    public FlashcardSwipeAdapter(List<FlashcardModel> flashcardList) {
        this.flashcardList = flashcardList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_flashcard_swipe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FlashcardModel card = flashcardList.get(position);

        // Mặc định từ đầu nếu chưa lưu trạng thái thì là mặt trước (true)
        if (!cardStateMap.containsKey(card.getId())) {
            cardStateMap.put(card.getId(), true);
        }

        updateCardText(holder, card);

        // Click để lật 3D giả lập ngay trên chuỗi vuốt
        holder.itemView.setOnClickListener(v -> {
            boolean isFront = cardStateMap.get(card.getId());
            holder.itemCardView.animate().rotationY(isFront ? 90f : -90f).setDuration(150).withEndAction(() -> {
                cardStateMap.put(card.getId(), !isFront);
                updateCardText(holder, card);
                holder.itemCardView.setRotationY(isFront ? -90f : 90f);
                holder.itemCardView.animate().rotationY(0f).setDuration(150).start();
            }).start();
        });
    }

    private void updateCardText(ViewHolder holder, FlashcardModel card) {
        boolean isFront = cardStateMap.get(card.getId());
        if (isFront) {
            String frontText = card.getWord();
            if (card.getWordType() != null && !card.getWordType().isEmpty()) {
                frontText += "\n(" + card.getWordType() + ")";
            }
            holder.tvCardContent.setText(frontText);
            holder.tvCardContent.setTextColor(Color.BLACK);

            // Hiển thị ảnh ở mặt trước nếu có
            if (card.getImagePath() != null && new File(card.getImagePath()).exists()) {
                holder.ivCardImage.setImageBitmap(BitmapFactory.decodeFile(card.getImagePath()));
                holder.ivCardImage.setVisibility(View.VISIBLE);
            } else {
                holder.ivCardImage.setVisibility(View.GONE);
            }
        } else {
            StringBuilder backText = new StringBuilder();
            if (card.getIpa() != null && !card.getIpa().isEmpty()) {
                backText.append(card.getIpa()).append("\n\n");
            }
            backText.append(card.getMeaning());
            if (card.getExample() != null && !card.getExample().isEmpty()) {
                backText.append("\n\nEx: ").append(card.getExample());
            }
            holder.tvCardContent.setText(backText.toString());
            holder.tvCardContent.setTextColor(Color.parseColor("#3B5BDB"));
            holder.ivCardImage.setVisibility(View.GONE); // Thường mặt sau không hiện ảnh hoặc tùy thiết kế
        }
    }

    @Override
    public int getItemCount() {
        return flashcardList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView itemCardView;
        TextView tvCardContent;
        ImageView ivCardImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemCardView = itemView.findViewById(R.id.itemCardView);
            tvCardContent = itemView.findViewById(R.id.tvCardContentContent);
            ivCardImage = itemView.findViewById(R.id.ivCardImage);
        }
    }
}