package com.cinemabooking.backend.service;

import com.cinemabooking.backend.dto.MovieDTO;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class MovieService {

    private static final Logger logger =
            LoggerFactory.getLogger(MovieService.class);

    @Autowired
    private Firestore firestore;

    private static final String COLLECTION = "movies";

    /**
     * Fetch all movies
     */
    public List<MovieDTO> getAllMovies(int page, int size)
            throws ExecutionException, InterruptedException {

        logger.info("Fetching movies from Firestore");

        ApiFuture<QuerySnapshot> future =
                firestore.collection(COLLECTION)
                        .limit(200)
                        .get();

        List<QueryDocumentSnapshot> documents =
                future.get().getDocuments();

        logger.info("Found {} movie documents", documents.size());

        List<MovieDTO> allMovies = documents.stream()
                .map(this::mapToDTO)
                .filter(m -> !Boolean.TRUE.equals(m.getDeleted()))
                .filter(m -> !Boolean.FALSE.equals(m.getIsActive()))
                .sorted((m1, m2) -> {
                    long t1 = m1.getUpdatedAt() != null
                            ? m1.getUpdatedAt()
                            : 0L;

                    long t2 = m2.getUpdatedAt() != null
                            ? m2.getUpdatedAt()
                            : 0L;

                    return Long.compare(t2, t1);
                })
                .collect(Collectors.toList());

        int start = page * size;

        if (start >= allMovies.size()) {
            return new ArrayList<>();
        }

        int end = Math.min(start + size, allMovies.size());

        return allMovies.subList(start, end);
    }

    /**
     * Get movie by Firestore document ID
     */
    public MovieDTO getMovieById(String id)
            throws ExecutionException, InterruptedException {

        DocumentSnapshot document =
                firestore.collection(COLLECTION)
                        .document(id)
                        .get()
                        .get();

        if (document.exists() && isVisible(document)) {
            return mapToDTO(document);
        }

        return null;
    }

    /**
     * Get movies by status
     */
    public List<MovieDTO> getMoviesByStatus(
            String status,
            int page,
            int size
    ) throws ExecutionException, InterruptedException {

        List<MovieDTO> allMovies =
                getAllMovies(0, 1000);

        List<MovieDTO> filtered = allMovies.stream()
                .filter(m ->
                        status.equalsIgnoreCase(m.getStatus()))
                .collect(Collectors.toList());

        int start = page * size;

        if (start >= filtered.size()) {
            return new ArrayList<>();
        }

        int end = Math.min(start + size, filtered.size());

        return filtered.subList(start, end);
    }

    /**
     * Search movies
     */
    public List<MovieDTO> searchMovies(String keyword)
            throws ExecutionException, InterruptedException {

        List<MovieDTO> allMovies =
                getAllMovies(0, 1000);

        String lowerKeyword = keyword.toLowerCase();

        return allMovies.stream()
                .filter(m ->
                        m.getTitle() != null &&
                                m.getTitle()
                                        .toLowerCase()
                                        .contains(lowerKeyword))
                .collect(Collectors.toList());
    }

    /**
     * Visibility filter
     */
    private boolean isVisible(DocumentSnapshot doc) {

        Boolean deleted = doc.getBoolean("deleted");
        Boolean isActive = doc.getBoolean("isActive");

        return !Boolean.TRUE.equals(deleted)
                && !Boolean.FALSE.equals(isActive);
    }

    /**
     * Firestore -> DTO
     */
    private MovieDTO mapToDTO(DocumentSnapshot doc) {

        logger.info("""
                Mapping movie:
                firestoreId={}
                customMovieId={}
                title={}
                """,
                doc.getId(),
                doc.getString("movieId"),
                doc.getString("title")
        );

        return MovieDTO.builder()

                /**
                 * IMPORTANT:
                 * REAL FIRESTORE DOCUMENT ID
                 */
                .id(doc.getId())

                /**
                 * custom movie code
                 */
                .movieId(doc.getString("movieId"))

                .title(doc.getString("title"))
                .description(doc.getString("description"))

                .language(doc.getString("language"))

                .ageRating(
                        doc.contains("ageRating")
                                ? doc.getString("ageRating")
                                : doc.getString("age")
                )

                .posterUrl(
                        doc.contains("posterUrl")
                                ? doc.getString("posterUrl")
                                : doc.getString("imageUrl")
                )

                .trailerUrl(doc.getString("trailerUrl"))

                .ratingAvg(
                        doc.getDouble("ratingAvg") != null
                                ? doc.getDouble("ratingAvg")
                                : doc.getDouble("rating")
                )

                .ratingCount(
                        doc.getLong("ratingCount") != null
                                ? doc.getLong("ratingCount").intValue()
                                : 0
                )

                .status(doc.getString("status"))

                .genres((List<String>) doc.get("genres"))

                .durationMinutes(
                        doc.getLong("durationMinutes") != null
                                ? doc.getLong("durationMinutes").intValue()
                                : (
                                doc.getLong("duration") != null
                                        ? doc.getLong("duration").intValue()
                                        : 0
                        )
                )

                .createdAt(doc.getLong("createdAt"))
                .updatedAt(doc.getLong("updatedAt"))

                .isActive(
                        doc.getBoolean("isActive") != null
                                ? doc.getBoolean("isActive")
                                : true
                )

                .deleted(
                        doc.getBoolean("deleted") != null
                                ? doc.getBoolean("deleted")
                                : false
                )

                .build();
    }
}