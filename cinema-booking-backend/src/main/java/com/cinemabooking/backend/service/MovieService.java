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

    private static final Logger logger = LoggerFactory.getLogger(MovieService.class);

    @Autowired
    private Firestore firestore;

    private static final String COLLECTION = "movies";

    /**
     * Fetch all movies with lenient filtering (handles missing deleted/isActive fields).
     */
    public List<MovieDTO> getAllMovies(int page, int size) throws ExecutionException, InterruptedException {
        logger.info("Fetching movies - page: {}, size: {}", page, size);
        
        try {
            // We fetch all non-deleted movies. 
            // To be lenient with old data, we fetch a larger set and filter in Java.
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION)
                    .orderBy("updatedAt", Query.Direction.DESCENDING)
                    .limit(100) // Fetch a reasonable batch
                    .get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            logger.info("Total documents fetched from Firestore: {}", documents.size());

            List<MovieDTO> allMovies = documents.stream()
                    .map(this::mapToDTO)
                    .filter(m -> !Boolean.TRUE.equals(m.getDeleted())) // Keep if deleted is null or false
                    .filter(m -> !Boolean.FALSE.equals(m.getIsActive())) // Keep if isActive is null or true
                    .collect(Collectors.toList());

            logger.info("Movies after lenient filtering: {}. IDs: {}", 
                    allMovies.size(), 
                    allMovies.stream().map(MovieDTO::getMovieId).collect(Collectors.toList()));

            // Simple pagination
            int start = page * size;
            if (start >= allMovies.size()) return new ArrayList<>();
            int end = Math.min(start + size, allMovies.size());
            
            return allMovies.subList(start, end);

        } catch (Exception e) {
            logger.error("Error in getAllMovies: {}", e.getMessage());
            throw e;
        }
    }

    public MovieDTO getMovieById(String id) throws ExecutionException, InterruptedException {
        try {
            DocumentSnapshot document = firestore.collection(COLLECTION).document(id).get().get();
            if (document.exists() && isVisible(document)) {
                return mapToDTO(document);
            }
        } catch (Exception e) {
            logger.error("Error fetching movie by ID {}: {}", id, e.getMessage());
        }
        return null;
    }

    public List<MovieDTO> getMoviesByStatus(String status, int page, int size) throws ExecutionException, InterruptedException {
        logger.info("Fetching movies by status: {}", status);
        List<MovieDTO> allMovies = getAllMovies(0, 1000); // Fetch all for filtering
        
        List<MovieDTO> filtered = allMovies.stream()
                .filter(m -> status.equalsIgnoreCase(m.getStatus()))
                .collect(Collectors.toList());

        logger.info("Movies with status {}: {}", status, filtered.size());

        int start = page * size;
        if (start >= filtered.size()) return new ArrayList<>();
        int end = Math.min(start + size, filtered.size());
        
        return filtered.subList(start, end);
    }

    public List<MovieDTO> searchMovies(String keyword) throws ExecutionException, InterruptedException {
        logger.info("Searching movies with keyword: {}", keyword);
        List<MovieDTO> allMovies = getAllMovies(0, 1000);
        
        String lowerKeyword = keyword.toLowerCase();
        return allMovies.stream()
                .filter(m -> m.getTitle() != null && m.getTitle().toLowerCase().contains(lowerKeyword))
                .collect(Collectors.toList());
    }

    private boolean isVisible(DocumentSnapshot doc) {
        Boolean deleted = doc.getBoolean("deleted");
        Boolean isActive = doc.getBoolean("isActive");
        // Lenient: visible if deleted is not true AND isActive is not false
        return !Boolean.TRUE.equals(deleted) && !Boolean.FALSE.equals(isActive);
    }

    private MovieDTO mapToDTO(DocumentSnapshot doc) {
        return MovieDTO.builder()
                .movieId(doc.getId())
                .title(doc.getString("title"))
                .description(doc.getString("description"))
                .language(doc.getString("language"))
                .ageRating(doc.contains("ageRating") ? doc.getString("ageRating") : doc.getString("age"))
                .posterUrl(doc.contains("posterUrl") ? doc.getString("posterUrl") : doc.getString("imageUrl"))
                .trailerUrl(doc.getString("trailerUrl"))
                .ratingAvg(doc.getDouble("ratingAvg") != null ? doc.getDouble("ratingAvg") : doc.getDouble("rating"))
                .ratingCount(doc.getLong("ratingCount") != null ? doc.getLong("ratingCount").intValue() : 0)
                .status(doc.getString("status"))
                .genres((List<String>) doc.get("genres"))
                .durationMinutes(doc.getLong("durationMinutes") != null ? doc.getLong("durationMinutes").intValue() : 
                                (doc.getLong("duration") != null ? doc.getLong("duration").intValue() : 0))
                .createdAt(doc.getLong("createdAt"))
                .updatedAt(doc.getLong("updatedAt"))
                .isActive(doc.getBoolean("isActive") != null ? doc.getBoolean("isActive") : true)
                .deleted(doc.getBoolean("deleted") != null ? doc.getBoolean("deleted") : false)
                .build();
    }
}
