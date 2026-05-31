package com.example.cinemabookingapp.ui.customer.adapter;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.domain.model.Review;

import java.util.ArrayList;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private List<Review> reviewList = new ArrayList<>();
    private final OnReviewInteractionListener interactionListener;

    public interface OnReviewInteractionListener {
        void onReplyClick(Review parentReview);
        void onLikeClick(Review review);
        void onDislikeClick(Review review);
    }

    public ReviewAdapter(OnReviewInteractionListener listener) {
        this.interactionListener = listener;
    }

    public void setReviews(List<Review> reviews) {
        this.reviewList = reviews != null ? reviews : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviewList.get(position);
        holder.bind(review);
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvTime, tvRating, tvContent, btnReply;
        LinearLayout layoutReplies;
        ImageView btnLike, btnDislike;
        TextView tvLikeCount, tvDislikeCount;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvContent = itemView.findViewById(R.id.tvContent);
            btnReply = itemView.findViewById(R.id.btnReply);
            layoutReplies = itemView.findViewById(R.id.layoutReplies);
            btnLike = itemView.findViewById(R.id.btnLike);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
            btnDislike = itemView.findViewById(R.id.btnDislike);
            tvDislikeCount = itemView.findViewById(R.id.tvDislikeCount);
        }

        public void bind(Review review) {
            // Hiển thị tạm userId, thường sẽ join thêm UserRepository để lấy Tên User thật
            tvUserName.setText(review.userId != null && !review.userId.isEmpty() ? "User " + review.userId.substring(0, 5) : "Khách");
            tvContent.setText(review.content);
            tvRating.setText("★ " + review.rating);
            
            CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                    review.createdAt, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS);
            tvTime.setText(timeAgo);

            tvLikeCount.setText(String.valueOf(review.likes));
            tvDislikeCount.setText(String.valueOf(review.dislikes));

            String currentUserId = "user_123"; // TODO: Lấy User ID thực tế

            boolean isLiked = review.likedBy != null && review.likedBy.contains(currentUserId);
            boolean isDisliked = review.dislikedBy != null && review.dislikedBy.contains(currentUserId);

            btnLike.setImageResource(isLiked ? R.drawable.ic_thumb_up_filled : R.drawable.ic_thumb_up_outline);
            btnDislike.setImageResource(isDisliked ? R.drawable.ic_thumb_down_filled : R.drawable.ic_thumb_down_outline);

            btnReply.setOnClickListener(v -> {
                if (interactionListener != null) {
                    interactionListener.onReplyClick(review);
                }
            });
            btnLike.setOnClickListener(v -> {
                animateClick(v);
                if (interactionListener != null) interactionListener.onLikeClick(review);
            });
            btnDislike.setOnClickListener(v -> {
                animateClick(v);
                if (interactionListener != null) interactionListener.onDislikeClick(review);
            });

            layoutReplies.removeAllViews();
            if (review.replies != null && !review.replies.isEmpty()) {
                Context context = itemView.getContext();
                LayoutInflater inflater = LayoutInflater.from(context);
                for (Review reply : review.replies) {
                    View replyView = inflater.inflate(R.layout.item_reply, layoutReplies, false);
                    
                    TextView tvReplyUserName = replyView.findViewById(R.id.tvReplyUserName);
                    TextView tvReplyTime = replyView.findViewById(R.id.tvReplyTime);
                    TextView tvReplyContent = replyView.findViewById(R.id.tvReplyContent);

                    tvReplyUserName.setText(reply.userId != null && !reply.userId.isEmpty() ? "User " + reply.userId.substring(0, 5) : "Khách");
                    tvReplyContent.setText(reply.content);
                    
                    CharSequence replyTimeAgo = DateUtils.getRelativeTimeSpanString(
                            reply.createdAt, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS);
                    tvReplyTime.setText(replyTimeAgo);

                    ImageView btnReplyLike = replyView.findViewById(R.id.btnReplyLike);
                    TextView tvReplyLikeCount = replyView.findViewById(R.id.tvReplyLikeCount);
                    ImageView btnReplyDislike = replyView.findViewById(R.id.btnReplyDislike);
                    TextView tvReplyDislikeCount = replyView.findViewById(R.id.tvReplyDislikeCount);

                    tvReplyLikeCount.setText(String.valueOf(reply.likes));
                    tvReplyDislikeCount.setText(String.valueOf(reply.dislikes));

                    boolean isReplyLiked = reply.likedBy != null && reply.likedBy.contains(currentUserId);
                    boolean isReplyDisliked = reply.dislikedBy != null && reply.dislikedBy.contains(currentUserId);

                    btnReplyLike.setImageResource(isReplyLiked ? R.drawable.ic_thumb_up_filled : R.drawable.ic_thumb_up_outline);
                    btnReplyDislike.setImageResource(isReplyDisliked ? R.drawable.ic_thumb_down_filled : R.drawable.ic_thumb_down_outline);

                    btnReplyLike.setOnClickListener(v -> {
                        animateClick(v);
                        if (interactionListener != null) interactionListener.onLikeClick(reply);
                    });
                    btnReplyDislike.setOnClickListener(v -> {
                        animateClick(v);
                        if (interactionListener != null) interactionListener.onDislikeClick(reply);
                    });

                    layoutReplies.addView(replyView);
                }
            }
        }
    }

    private void animateClick(View view) {
        view.animate().scaleX(1.3f).scaleY(1.3f).setDuration(150).withEndAction(() -> {
            view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(150).start();
        }).start();
    }
}
