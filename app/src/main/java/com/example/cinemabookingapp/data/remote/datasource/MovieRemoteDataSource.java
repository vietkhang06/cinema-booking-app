package com.example.cinemabookingapp.data.remote.datasource;

import com.example.cinemabookingapp.domain.model.Movie;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MovieRemoteDataSource {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void getMovies(OnResult callback) {
        db.collection("movies")
                .whereEqualTo("deleted", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Movie> list = new ArrayList<>();

                    queryDocumentSnapshots.forEach(doc -> {
                        Movie movie = doc.toObject(Movie.class);
                        list.add(movie);
                    });

                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }

    public interface OnResult {
        void onSuccess(List<Movie> movies);
        void onError(String error);
    }
}