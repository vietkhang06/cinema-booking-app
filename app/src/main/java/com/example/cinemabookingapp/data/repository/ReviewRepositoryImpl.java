package com.example.cinemabookingapp.data.repository;

import com.example.cinemabookingapp.data.dto.ReviewDTO;
import com.example.cinemabookingapp.data.mapper.ReviewMapper;
import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.Review;
import com.example.cinemabookingapp.domain.repository.ReviewRepository;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReviewRepositoryImpl implements ReviewRepository {

    private final FirebaseFirestore db;
    private final String COLLECTION_REVIEWS = "reviews";

    public ReviewRepositoryImpl() {
        this.db = FirebaseFirestore.getInstance();
    }

    @Override
    public void createReview(Review review, ResultCallback<Review> callback) {
        String id = db.collection(COLLECTION_REVIEWS).document().getId();
        review.reviewId = id;
        review.createdAt = System.currentTimeMillis();
        review.updatedAt = System.currentTimeMillis();
        review.parentId = null; // Đảm bảo review gốc không có parentId
        review.replyCount = 0;

        ReviewDTO dto = ReviewMapper.toDTO(review);
        db.collection(COLLECTION_REVIEWS).document(id).set(dto)
                .addOnSuccessListener(aVoid -> callback.onSuccess(review))
                // FIX: Extract the String message from the Exception
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void addReply(String parentReviewId, Review reply, ResultCallback<Review> callback) {
        String id = db.collection(COLLECTION_REVIEWS).document().getId();
        reply.reviewId = id;
        reply.parentId = parentReviewId;
        reply.createdAt = System.currentTimeMillis();
        reply.updatedAt = System.currentTimeMillis();
        reply.rating = 0; // Reply không có rating

        ReviewDTO dto = ReviewMapper.toDTO(reply);

        db.runTransaction(transaction -> {
                    // Đọc parent review để tăng số lượng replyCount
                    com.google.firebase.firestore.DocumentReference parentRef = db.collection(COLLECTION_REVIEWS).document(parentReviewId);
                    com.google.firebase.firestore.DocumentSnapshot parentSnapshot = transaction.get(parentRef);
                    if (!parentSnapshot.exists()) {
                        throw new com.google.firebase.firestore.FirebaseFirestoreException("Parent review not found", com.google.firebase.firestore.FirebaseFirestoreException.Code.NOT_FOUND);
                    }
                    ReviewDTO parentDto = parentSnapshot.toObject(ReviewDTO.class);
                    int currentReplyCount = parentDto != null ? parentDto.replyCount : 0;

                    // Thêm reply mới
                    com.google.firebase.firestore.DocumentReference replyRef = db.collection(COLLECTION_REVIEWS).document(id);
                    transaction.set(replyRef, dto);

                    // Cập nhật count ở parent review
                    transaction.update(parentRef, "replyCount", currentReplyCount + 1);

                    return reply;
                }).addOnSuccessListener(callback::onSuccess)
                // FIX: Extract the String message from the Exception
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void getReviewsByMovieId(String movieId, ResultCallback<List<Review>> callback) {
        db.collection(COLLECTION_REVIEWS)
                .whereEqualTo("movieId", movieId)
                .whereEqualTo("deleted", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Review> parents = new ArrayList<>();
                    Map<String, List<Review>> repliesMap = new HashMap<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        ReviewDTO dto = doc.toObject(ReviewDTO.class);
                        Review model = ReviewMapper.toDomain(dto);

                        if (model.parentId == null || model.parentId.isEmpty()) {
                            parents.add(model);
                        } else {
                            if (!repliesMap.containsKey(model.parentId)) {
                                repliesMap.put(model.parentId, new ArrayList<>());
                            }
                            repliesMap.get(model.parentId).add(model);
                        }
                    }

                    // Gom các reply vào danh sách của review gốc
                    for (Review parent : parents) {
                        if (repliesMap.containsKey(parent.reviewId)) {
                            parent.replies = repliesMap.get(parent.reviewId);
                            // Sắp xếp reply theo thời gian tăng dần (cũ nhất ở trên)
                            parent.replies.sort((r1, r2) -> Long.compare(r1.createdAt, r2.createdAt));
                        } else {
                            parent.replies = new ArrayList<>();
                        }
                    }

                    // Sắp xếp parent review theo thời gian giảm dần (mới nhất ở trên)
                    parents.sort((p1, p2) -> Long.compare(p2.createdAt, p1.createdAt));

                    callback.onSuccess(parents);
                })
                // FIX: Extract the String message from the Exception
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void getReviewById(String reviewId, ResultCallback<Review> callback) {
        db.collection(COLLECTION_REVIEWS).document(reviewId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        ReviewDTO dto = documentSnapshot.toObject(ReviewDTO.class);
                        callback.onSuccess(ReviewMapper.toDomain(dto));
                    } else {
                        // FIX: Pass a String directly instead of instantiating a new Exception
                        callback.onError("Review not found");
                    }
                })
                // FIX: Extract the String message from the Exception
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void getReviewsByUserId(String userId, ResultCallback<List<Review>> callback) {
        db.collection(COLLECTION_REVIEWS)
                .whereEqualTo("userId", userId)
                .whereEqualTo("deleted", false)
                .whereEqualTo("parentId", null) // Thường chỉ lấy review gốc của user
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Review> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        list.add(ReviewMapper.toDomain(doc.toObject(ReviewDTO.class)));
                    }
                    callback.onSuccess(list);
                })
                // FIX: Extract the String message from the Exception
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void updateReview(Review review, ResultCallback<Review> callback) {
        review.updatedAt = System.currentTimeMillis();
        ReviewDTO dto = ReviewMapper.toDTO(review);
        db.collection(COLLECTION_REVIEWS).document(review.reviewId).set(dto)
                .addOnSuccessListener(aVoid -> callback.onSuccess(review))
                // FIX: Extract the String message from the Exception
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void hideReview(String reviewId, ResultCallback<Review> callback) {
        db.collection(COLLECTION_REVIEWS).document(reviewId).update("status", "hidden")
                .addOnSuccessListener(aVoid -> getReviewById(reviewId, callback))
                // FIX: Extract the String message from the Exception
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void deleteReview(String reviewId, ResultCallback<Void> callback) {
        db.collection(COLLECTION_REVIEWS).document(reviewId).update("deleted", true)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                // FIX: Extract the String message from the Exception
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void likeReview(String reviewId, String userId, ResultCallback<Review> callback) {
        com.google.firebase.firestore.DocumentReference docRef = db.collection(COLLECTION_REVIEWS).document(reviewId);
        db.runTransaction(transaction -> {
            com.google.firebase.firestore.DocumentSnapshot snapshot = transaction.get(docRef);
            if (!snapshot.exists()) {
                throw new com.google.firebase.firestore.FirebaseFirestoreException("Review not found", com.google.firebase.firestore.FirebaseFirestoreException.Code.NOT_FOUND);
            }
            ReviewDTO dto = snapshot.toObject(ReviewDTO.class);
            if (dto == null) {
                throw new com.google.firebase.firestore.FirebaseFirestoreException("Review data is null", com.google.firebase.firestore.FirebaseFirestoreException.Code.INTERNAL);
            }

            if (dto.likedBy == null) {
                dto.likedBy = new java.util.ArrayList<>();
            }
            if (dto.dislikedBy == null) {
                dto.dislikedBy = new java.util.ArrayList<>();
            }

            if (dto.likedBy.contains(userId)) {
                // Đã like -> Unlike
                dto.likedBy.remove(userId);
                dto.likes = Math.max(0, dto.likes - 1);
            } else {
                // Chưa like -> Like
                dto.likedBy.add(userId);
                dto.likes = dto.likes + 1;
                // Nếu đang dislike thì bỏ dislike
                if (dto.dislikedBy.contains(userId)) {
                    dto.dislikedBy.remove(userId);
                    dto.dislikes = Math.max(0, dto.dislikes - 1);
                }
            }

            transaction.update(docRef,
                    "likes", dto.likes,
                    "dislikes", dto.dislikes,
                    "likedBy", dto.likedBy,
                    "dislikedBy", dto.dislikedBy
            );

            return ReviewMapper.toDomain(dto);
        }).addOnSuccessListener(callback::onSuccess)
        .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void dislikeReview(String reviewId, String userId, ResultCallback<Review> callback) {
        com.google.firebase.firestore.DocumentReference docRef = db.collection(COLLECTION_REVIEWS).document(reviewId);
        db.runTransaction(transaction -> {
            com.google.firebase.firestore.DocumentSnapshot snapshot = transaction.get(docRef);
            if (!snapshot.exists()) {
                throw new com.google.firebase.firestore.FirebaseFirestoreException("Review not found", com.google.firebase.firestore.FirebaseFirestoreException.Code.NOT_FOUND);
            }
            ReviewDTO dto = snapshot.toObject(ReviewDTO.class);
            if (dto == null) {
                throw new com.google.firebase.firestore.FirebaseFirestoreException("Review data is null", com.google.firebase.firestore.FirebaseFirestoreException.Code.INTERNAL);
            }

            if (dto.likedBy == null) {
                dto.likedBy = new java.util.ArrayList<>();
            }
            if (dto.dislikedBy == null) {
                dto.dislikedBy = new java.util.ArrayList<>();
            }

            if (dto.dislikedBy.contains(userId)) {
                // Đã dislike -> Undislike
                dto.dislikedBy.remove(userId);
                dto.dislikes = Math.max(0, dto.dislikes - 1);
            } else {
                // Chưa dislike -> Dislike
                dto.dislikedBy.add(userId);
                dto.dislikes = dto.dislikes + 1;
                // Nếu đang like thì bỏ like
                if (dto.likedBy.contains(userId)) {
                    dto.likedBy.remove(userId);
                    dto.likes = Math.max(0, dto.likes - 1);
                }
            }

            transaction.update(docRef,
                    "likes", dto.likes,
                    "dislikes", dto.dislikes,
                    "likedBy", dto.likedBy,
                    "dislikedBy", dto.dislikedBy
            );

            return ReviewMapper.toDomain(dto);
        }).addOnSuccessListener(callback::onSuccess)
        .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}