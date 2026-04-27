package com.example.cinemabookingapp.data.remote.datasource;

import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.Movie;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MovieRemoteDataSource {

    private static final String COLLECTION_MOVIES = "movies";

    private final FirebaseFirestore firestore;

    public MovieRemoteDataSource() {
        this(FirebaseFirestore.getInstance());
    }

    public MovieRemoteDataSource(FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    public void createMovie(Movie movie, ResultCallback<Movie> callback) {
        String movieId = readString(movie, "movieId", "id");
        if (movieId == null || movieId.trim().isEmpty()) {
            movieId = firestore.collection(COLLECTION_MOVIES).document().getId();
        }

        Map<String, Object> movieData = movieToMap(movie);
        movieData.put("movieId", movieId);
        movieData.put("updatedAt", System.currentTimeMillis());

        if (!movieData.containsKey("createdAt")) {
            movieData.put("createdAt", System.currentTimeMillis());
        }

        final String finalMovieId = movieId;
        final Map<String, Object> finalMovieData = movieData;

        firestore.collection(COLLECTION_MOVIES)
                .document(finalMovieId)
                .set(finalMovieData)
                .addOnSuccessListener(unused -> {
                    Movie savedMovie = mapDocumentToMovie(finalMovieId, finalMovieData);
                    if (callback != null) {
                        callback.onSuccess(savedMovie);
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onError(messageOrDefault(e, "Không thể tạo phim"));
                    }
                });
    }

    public void getMovieById(String movieId, ResultCallback<Movie> callback) {
        if (movieId == null || movieId.trim().isEmpty()) {
            if (callback != null) {
                callback.onError("movieId không hợp lệ");
            }
            return;
        }

        firestore.collection(COLLECTION_MOVIES)
                .document(movieId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists() || !isVisible(documentSnapshot)) {
                        if (callback != null) {
                            callback.onError("Không tìm thấy phim");
                        }
                        return;
                    }

                    Movie movie = mapSnapshotToMovie(documentSnapshot);
                    if (callback != null) {
                        callback.onSuccess(movie);
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onError(messageOrDefault(e, "Không thể tải phim"));
                    }
                });
    }

    public void getAllMovies(ResultCallback<List<Movie>> callback) {
        firestore.collection(COLLECTION_MOVIES)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Movie> movies = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        if (!isVisible(document)) {
                            continue;
                        }
                        movies.add(mapSnapshotToMovie(document));
                    }

                    if (callback != null) {
                        callback.onSuccess(movies);
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onError(messageOrDefault(e, "Không thể tải danh sách phim"));
                    }
                });
    }

    public void getMoviesByStatus(String status, ResultCallback<List<Movie>> callback) {
        getAllMovies(new ResultCallback<List<Movie>>() {
            @Override
            public void onSuccess(List<Movie> movies) {
                List<Movie> filteredMovies = new ArrayList<>();
                for (Movie movie : movies) {
                    String movieStatus = normalizeStatus(readString(movie, "status"));
                    if (status != null && status.equals(movieStatus)) {
                        filteredMovies.add(movie);
                    }
                }
                if (callback != null) {
                    callback.onSuccess(filteredMovies);
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (callback != null) {
                    callback.onError(errorMessage);
                }
            }
        });
    }

    public void searchMovies(String keyword, ResultCallback<List<Movie>> callback) {
        final String safeKeyword = keyword == null ? "" : keyword.trim().toLowerCase(Locale.getDefault());

        getAllMovies(new ResultCallback<List<Movie>>() {
            @Override
            public void onSuccess(List<Movie> movies) {
                if (safeKeyword.isEmpty()) {
                    if (callback != null) {
                        callback.onSuccess(movies);
                    }
                    return;
                }

                List<Movie> filteredMovies = new ArrayList<>();
                for (Movie movie : movies) {
                    String title = readString(movie, "title");
                    if (title != null && title.toLowerCase(Locale.getDefault()).contains(safeKeyword)) {
                        filteredMovies.add(movie);
                    }
                }

                if (callback != null) {
                    callback.onSuccess(filteredMovies);
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (callback != null) {
                    callback.onError(errorMessage);
                }
            }
        });
    }

    public void updateMovie(Movie movie, ResultCallback<Movie> callback) {
        String movieId = readString(movie, "movieId", "id");
        if (movieId == null || movieId.trim().isEmpty()) {
            if (callback != null) {
                callback.onError("movieId không hợp lệ");
            }
            return;
        }

        Map<String, Object> movieData = movieToMap(movie);
        movieData.put("movieId", movieId);
        movieData.put("updatedAt", System.currentTimeMillis());

        final String finalMovieId = movieId;
        final Map<String, Object> finalMovieData = movieData;

        firestore.collection(COLLECTION_MOVIES)
                .document(finalMovieId)
                .set(finalMovieData)
                .addOnSuccessListener(unused -> {
                    Movie updatedMovie = mapDocumentToMovie(finalMovieId, finalMovieData);
                    if (callback != null) {
                        callback.onSuccess(updatedMovie);
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onError(messageOrDefault(e, "Không thể cập nhật phim"));
                    }
                });
    }

    public void softDeleteMovie(String movieId, ResultCallback<Void> callback) {
        if (movieId == null || movieId.trim().isEmpty()) {
            if (callback != null) {
                callback.onError("movieId không hợp lệ");
            }
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("isActive", false);
        updates.put("deleted", true);
        updates.put("updatedAt", System.currentTimeMillis());

        firestore.collection(COLLECTION_MOVIES)
                .document(movieId)
                .update(updates)
                .addOnSuccessListener(unused -> {
                    if (callback != null) {
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onError(messageOrDefault(e, "Không thể xoá mềm phim"));
                    }
                });
    }

    private boolean isVisible(DocumentSnapshot documentSnapshot) {
        Boolean isActive = documentSnapshot.getBoolean("isActive");
        if (isActive != null) {
            return isActive;
        }

        Boolean deleted = documentSnapshot.getBoolean("deleted");
        if (deleted != null) {
            return !deleted;
        }

        return true;
    }

    private Movie mapSnapshotToMovie(DocumentSnapshot documentSnapshot) {
        return mapDocumentToMovie(documentSnapshot.getId(), documentSnapshot.getData());
    }

    private Movie mapDocumentToMovie(String movieId, Map<String, Object> data) {
        Movie movie = createMovieInstance();
        if (movie == null) {
            return null;
        }

        setValue(movie, "movieId", movieId);
        setValue(movie, "id", movieId);

        if (data == null) {
            return movie;
        }

        setValue(movie, "title", data.get("title"));
        setValue(movie, "imageUrl", firstNonNull(data.get("imageUrl"), data.get("posterUrl")));
        setValue(movie, "posterUrl", firstNonNull(data.get("posterUrl"), data.get("imageUrl")));
        setValue(movie, "rating", firstNonNull(data.get("rating"), data.get("ratingAvg")));
        setValue(movie, "ratingAvg", data.get("ratingAvg"));
        setValue(movie, "ratingCount", data.get("ratingCount"));
        setValue(movie, "ageRating", firstNonNull(data.get("ageRating"), data.get("age")));
        setValue(movie, "status", normalizeStatus(String.valueOf(firstNonNull(data.get("status"), ""))));
        setValue(movie, "description", data.get("description"));
        setValue(movie, "durationMinutes", data.get("durationMinutes"));
        setValue(movie, "duration", data.get("duration"));
        setValue(movie, "genres", data.get("genres"));
        setValue(movie, "genre", data.get("genre"));
        setValue(movie, "language", data.get("language"));
        setValue(movie, "createdAt", data.get("createdAt"));
        setValue(movie, "updatedAt", data.get("updatedAt"));
        setValue(movie, "isActive", data.get("isActive"));
        setValue(movie, "deleted", data.get("deleted"));

        return movie;
    }

    private Movie createMovieInstance() {
        try {
            return Movie.class.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            return null;
        }
    }

    private Map<String, Object> movieToMap(Movie movie) {
        Map<String, Object> data = new LinkedHashMap<>();
        putIfNotNull(data, "movieId", readValue(movie, "movieId", "id"));
        putIfNotNull(data, "title", readValue(movie, "title"));
        putIfNotNull(data, "imageUrl", readValue(movie, "imageUrl"));
        putIfNotNull(data, "posterUrl", readValue(movie, "posterUrl"));
        putIfNotNull(data, "rating", readValue(movie, "rating"));
        putIfNotNull(data, "ratingAvg", readValue(movie, "ratingAvg"));
        putIfNotNull(data, "ratingCount", readValue(movie, "ratingCount"));
        putIfNotNull(data, "ageRating", readValue(movie, "ageRating"));
        putIfNotNull(data, "status", normalizeStatus(readString(movie, "status")));
        putIfNotNull(data, "description", readValue(movie, "description"));
        putIfNotNull(data, "durationMinutes", readValue(movie, "durationMinutes"));
        putIfNotNull(data, "duration", readValue(movie, "duration"));
        putIfNotNull(data, "genres", readValue(movie, "genres"));
        putIfNotNull(data, "genre", readValue(movie, "genre"));
        putIfNotNull(data, "language", readValue(movie, "language"));
        putIfNotNull(data, "isActive", readValue(movie, "isActive"));
        putIfNotNull(data, "deleted", readValue(movie, "deleted"));
        putIfNotNull(data, "createdAt", readValue(movie, "createdAt"));
        putIfNotNull(data, "updatedAt", readValue(movie, "updatedAt"));

        return data;
    }

    private void putIfNotNull(Map<String, Object> data, String key, Object value) {
        if (value != null) {
            data.put(key, value);
        }
    }

    private Object firstNonNull(Object first, Object second) {
        return first != null ? first : second;
    }

    private String normalizeStatus(String status) {
        if (status == null) {
            return null;
        }

        String normalized = status.trim().toUpperCase(Locale.getDefault());
        if ("NOW_SHOWING".equals(normalized) || "NOW SHOWING".equals(normalized)) {
            return "NOW_SHOWING";
        }
        if ("COMING_SOON".equals(normalized) || "COMING SOON".equals(normalized)) {
            return "COMING_SOON";
        }
        return normalized;
    }

    private String messageOrDefault(Exception e, String defaultMessage) {
        if (e != null && e.getMessage() != null && !e.getMessage().trim().isEmpty()) {
            return e.getMessage();
        }
        return defaultMessage;
    }

    private String readString(Object target, String... possibleNames) {
        Object value = readValue(target, possibleNames);
        return value == null ? null : String.valueOf(value);
    }

    private Object readValue(Object target, String... possibleNames) {
        if (target == null || possibleNames == null) {
            return null;
        }

        Class<?> clazz = target.getClass();

        for (String name : possibleNames) {
            if (name == null || name.trim().isEmpty()) {
                continue;
            }

            String capitalized = capitalize(name);

            try {
                Method getter = clazz.getMethod("get" + capitalized);
                return getter.invoke(target);
            } catch (Exception ignored) {
            }

            try {
                Method getter = clazz.getMethod("is" + capitalized);
                return getter.invoke(target);
            } catch (Exception ignored) {
            }

            try {
                Field field = clazz.getDeclaredField(name);
                field.setAccessible(true);
                return field.get(target);
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    private void setValue(Object target, String propertyName, Object value) {
        if (target == null || propertyName == null || value == null) {
            return;
        }

        Class<?> clazz = target.getClass();
        String capitalized = capitalize(propertyName);
        String setterName = "set" + capitalized;

        for (Method method : clazz.getMethods()) {
            if (!method.getName().equals(setterName) || method.getParameterCount() != 1) {
                continue;
            }

            try {
                Class<?> parameterType = method.getParameterTypes()[0];
                method.invoke(target, convertValue(value, parameterType));
                return;
            } catch (Exception ignored) {
            }
        }

        try {
            Field field = clazz.getDeclaredField(propertyName);
            field.setAccessible(true);
            field.set(target, convertValue(value, field.getType()));
        } catch (Exception ignored) {
        }
    }

    private Object convertValue(Object value, Class<?> targetType) {
        if (value == null || targetType == null) {
            return null;
        }

        if (targetType.isAssignableFrom(value.getClass())) {
            return value;
        }

        if (targetType == String.class) {
            return String.valueOf(value);
        }

        if (targetType == int.class || targetType == Integer.class) {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            return Integer.parseInt(String.valueOf(value));
        }

        if (targetType == long.class || targetType == Long.class) {
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
            return Long.parseLong(String.valueOf(value));
        }

        if (targetType == double.class || targetType == Double.class) {
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
            return Double.parseDouble(String.valueOf(value));
        }

        if (targetType == float.class || targetType == Float.class) {
            if (value instanceof Number) {
                return ((Number) value).floatValue();
            }
            return Float.parseFloat(String.valueOf(value));
        }

        if (targetType == boolean.class || targetType == Boolean.class) {
            if (value instanceof Boolean) {
                return value;
            }
            return Boolean.parseBoolean(String.valueOf(value));
        }

        return value;
    }

    private String capitalize(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return value.substring(0, 1).toUpperCase(Locale.getDefault()) + value.substring(1);
    }
}