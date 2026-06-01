package com.example.smartflashcard.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartflashcard.R;
import com.example.smartflashcard.data.DeckModel;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.List;

public class DeckAdapter extends RecyclerView.Adapter<DeckAdapter.DeckViewHolder> {

    private List<DeckModel> deckList;
    private OnDeckClickListener clickListener;

    public interface OnDeckClickListener {
        void onDeckClick(DeckModel deck);
    }

    public DeckAdapter(List<DeckModel> deckList, OnDeckClickListener clickListener) {
        this.deckList = deckList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public DeckViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_deck, parent, false);
        return new DeckViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeckViewHolder holder, int position) {
        DeckModel deck = deckList.get(position);

        holder.tvDeckName.setText(deck.getName());
        holder.tvDeckDescription.setText(deck.getDescription());

        // Thiết lập trạng thái chờ ban đầu trong lúc luồng mạng truy vấn dữ liệu
        holder.tvCardCount.setText("0 thẻ");
        holder.deckProgress.setProgress(0);
        holder.tvProgressPercent.setText("0%");

        // 📊 ĐẾM SỐ LƯỢNG THẺ THẬT: Truy vấn tự động từ nhánh Flashcards theo từng bộ Deck ID
        if (deck.getId() != null) {
            FirebaseDatabase.getInstance("https://smart-7d648-default-rtdb.asia-southeast1.firebasedatabase.app")
                    .getReference("Flashcards")
                    .orderByChild("deckId")
                    .equalTo(deck.getId())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            long totalCards = snapshot.getChildrenCount();
                            holder.tvCardCount.setText(totalCards + " thẻ");

                            // Sau khi có tổng số thẻ con, kích hoạt tiếp luồng tính % học tập thực tế
                            loadRealtimeProgress(deck.getId(), totalCards, holder);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
        }

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onDeckClick(deck);
            }
        });
    }

    // 📊 TÍNH TIẾN TRÌNH % THẬT: Quét bảng Progress theo ID của Tài khoản đang đăng nhập
    private void loadRealtimeProgress(String deckId, long totalCards, DeckViewHolder holder) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null || totalCards == 0) return;

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase.getInstance("https://smart-7d648-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("Progress")
                .child(uid)
                .child(deckId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Long currentPos = snapshot.child("currentPosition").getValue(Long.class);
                            if (currentPos != null) {
                                // Thuật toán tính phần trăm tiến độ học thực tế
                                int percent = (int) ((currentPos * 100) / totalCards);
                                if (percent > 100) percent = 100;

                                holder.deckProgress.setProgress(percent);
                                holder.tvProgressPercent.setText(percent + "%");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    @Override
    public int getItemCount() {
        return deckList != null ? deckList.size() : 0;
    }

    static class DeckViewHolder extends RecyclerView.ViewHolder {
        TextView tvDeckName, tvDeckDescription, tvCardCount, tvProgressPercent;
        LinearProgressIndicator deckProgress;

        public DeckViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDeckName = itemView.findViewById(R.id.tvDeckName);
            tvDeckDescription = itemView.findViewById(R.id.tvDeckDescription);
            tvCardCount = itemView.findViewById(R.id.tvCardCount);
            tvProgressPercent = itemView.findViewById(R.id.tvProgressPercent);
            deckProgress = itemView.findViewById(R.id.deckProgress);
        }
    }
}