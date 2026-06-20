package com.example.cinemabookingapp.ui.features.movie.adapter;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Date;
import android.content.Context;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Date;
import android.text.format.DateUtils;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Date;
import android.view.LayoutInflater;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Date;
import android.view.View;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Date;
import android.view.ViewGroup;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Date;
import android.widget.ImageView;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Date;
import android.widget.LinearLayout;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Date;
import android.widget.TextView;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Date;
import androidx.annotation.NonNull;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Date;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Date;
import com.example.cinemabookingapp.R;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Date;
import com.example.cinemabookingapp.domain.model.Review;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Date;
import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Date;
import java.util.List;

public class ReviewAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    public interface ReviewActionListener {
        void onLikeClick(Review review, int position);
        void onDislikeClick(Review review, int position);
        void onReplyClick(Review review, int position);
        default void onDeleteClick(Review review, int position) {}
    }

    private List<Review> reviewList = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private ReviewActionListener listener;
    private String currentUserId;

    public void setListener(ReviewActionListener listener, String currentUserId) {
        this.listener = listener;
        this.currentUserId = currentUserId;
    }

    public void setReviews(List<Review> reviews) {
        this.reviewList = reviews != null ? reviews : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addReviews(List<Review> reviews) {
        if (reviews != null && !reviews.isEmpty()) {
            int startPos = this.reviewList.size();
            this.reviewList.addAll(reviews);
            notifyItemRangeInserted(startPos, reviews.size());
        }
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        holder.bind(reviewList.get(position));
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvReviewDate, tvReviewContent;
        android.widget.RatingBar ratingBarReview;
        View btnLike, btnDislike, btnReply;
        TextView tvLikeCount, tvDislikeCount, tvReplyCount;
        android.widget.ImageView imgLike, imgDislike, imgUserAvatar;

        ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvReviewDate = itemView.findViewById(R.id.tvReviewDate);
            tvReviewContent = itemView.findViewById(R.id.tvReviewContent);
            ratingBarReview = itemView.findViewById(R.id.ratingBarReview);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnDislike = itemView.findViewById(R.id.btnDislike);
            btnReply = itemView.findViewById(R.id.btnReply);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
            tvDislikeCount = itemView.findViewById(R.id.tvDislikeCount);
            tvReplyCount = itemView.findViewById(R.id.tvReplyCount);
            imgLike = itemView.findViewById(R.id.imgLike);
            imgDislike = itemView.findViewById(R.id.imgDislike);
            imgUserAvatar = itemView.findViewById(R.id.imgUserAvatar);
            
            btnLike.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onLikeClick(reviewList.get(getAdapterPosition()), getAdapterPosition());
                }
            });
            btnDislike.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onDislikeClick(reviewList.get(getAdapterPosition()), getAdapterPosition());
                }
            });
            btnReply.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onReplyClick(reviewList.get(getAdapterPosition()), getAdapterPosition());
                }
            });
            itemView.setOnLongClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onDeleteClick(reviewList.get(getAdapterPosition()), getAdapterPosition());
                    return true;
                }
                return false;
            });
        }

        void bind(Review review) {
            if (review.userId != null) {
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("users").document(review.userId).get()
                        .addOnSuccessListener(doc -> {
                            if (doc.exists()) {
                                if (doc.getString("name") != null) {
                                    tvUserName.setText(doc.getString("name"));
                                } else {
                                    tvUserName.setText("NgÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Âi dÃƒÆ’Ã‚Â¹ng");
                                }
                                String avatarUrl = doc.getString("avatarUrl");
                                if (avatarUrl != null && !avatarUrl.isEmpty()) {
                                    com.bumptech.glide.Glide.with(itemView.getContext())
                                            .load(avatarUrl)
                                            .placeholder(R.drawable.ic_user_avatar_24)
                                            .into(imgUserAvatar);
                                } else {
                                    imgUserAvatar.setImageResource(R.drawable.ic_user_avatar_24);
                                }
                            } else {
                                tvUserName.setText("NgÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Âi dÃƒÆ’Ã‚Â¹ng");
                                imgUserAvatar.setImageResource(R.drawable.ic_user_avatar_24);
                            }
                        })
                        .addOnFailureListener(e -> tvUserName.setText("NgÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Âi dÃƒÆ’Ã‚Â¹ng"));
            } else {
                tvUserName.setText("NgÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Âi dÃƒÆ’Ã‚Â¹ng");
            }

            if (review.createdAt > 0) {
                tvReviewDate.setText(dateFormat.format(new Date(review.createdAt)));
            }

            if ("hidden".equals(review.status)) {
                tvReviewContent.setVisibility(View.VISIBLE);
                tvReviewContent.setText("BÃƒÆ’Ã‚Â¬nh luÃƒÂ¡Ã‚ÂºÃ‚Â­n nÃƒÆ’Ã‚Â y Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Â£ bÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹ quÃƒÂ¡Ã‚ÂºÃ‚Â£n trÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹ viÃƒÆ’Ã‚Âªn ÃƒÂ¡Ã‚ÂºÃ‚Â©n.");
                tvReviewContent.setTextColor(android.graphics.Color.GRAY);
                tvReviewContent.setTypeface(null, android.graphics.Typeface.ITALIC);
            } else if (review.content == null || review.content.trim().isEmpty()) {
                tvReviewContent.setVisibility(View.GONE);
            } else {
                tvReviewContent.setVisibility(View.VISIBLE);
                tvReviewContent.setText(review.content);
                tvReviewContent.setTextColor(android.graphics.Color.parseColor("#444444"));
                tvReviewContent.setTypeface(null, android.graphics.Typeface.NORMAL);
            }
            
            if (review.rating != null && review.rating > 0) {
                ratingBarReview.setVisibility(View.VISIBLE);
                ratingBarReview.setRating(review.rating);
            } else {
                ratingBarReview.setVisibility(View.GONE);
            }
            
            tvLikeCount.setText(String.valueOf(review.likedBy != null ? review.likedBy.size() : 0));
            tvDislikeCount.setText(String.valueOf(review.dislikedBy != null ? review.dislikedBy.size() : 0));
            tvReplyCount.setText(review.replyCount != null && review.replyCount > 0 ? review.replyCount + " TrÃƒÂ¡Ã‚ÂºÃ‚Â£ lÃƒÂ¡Ã‚Â»Ã‚Âi" : "TrÃƒÂ¡Ã‚ÂºÃ‚Â£ lÃƒÂ¡Ã‚Â»Ã‚Âi");

            boolean isLiked = currentUserId != null && review.likedBy != null && review.likedBy.contains(currentUserId);
            boolean isDisliked = currentUserId != null && review.dislikedBy != null && review.dislikedBy.contains(currentUserId);
            
            imgLike.setColorFilter(isLiked ? android.graphics.Color.parseColor("#1E4F8F") : android.graphics.Color.parseColor("#888888"));
            tvLikeCount.setTextColor(isLiked ? android.graphics.Color.parseColor("#1E4F8F") : android.graphics.Color.parseColor("#888888"));
            
            imgDislike.setColorFilter(isDisliked ? android.graphics.Color.parseColor("#E06A00") : android.graphics.Color.parseColor("#888888"));
            tvDislikeCount.setTextColor(isDisliked ? android.graphics.Color.parseColor("#E06A00") : android.graphics.Color.parseColor("#888888"));
        }
    }
}
