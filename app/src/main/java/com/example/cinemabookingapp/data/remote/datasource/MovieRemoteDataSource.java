package com.example.cinemabookingapp.data.remote.datasource;

import com.example.cinemabookingapp.core.constants.FirestoreCollections;
import com.example.cinemabookingapp.data.dto.MovieDTO;
import com.example.cinemabookingapp.data.remote.firebase.FirebaseProvider;
import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MovieRemoteDataSource {

    private final FirebaseFirestore firestore = FirebaseProvider.provideFirestore();

    public void getAllMovies(ResultCallback<List<MovieDTO>> callback) {
        firestore.collection(FirestoreCollections.MOVIES)
                .whereEqualTo("deleted", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<MovieDTO> list = new ArrayList<>();
//                    list = queryDocumentSnapshots.getDocuments().stream()
//                        .map(doc -> doc.toObject(MovieDTO.class))
//                        .filter(Objects::nonNull)
//                        .collect(Collectors.toList());
                    queryDocumentSnapshots.getDocuments().forEach(doc -> {
                        MovieDTO dto = doc.toObject(MovieDTO.class);
                        if (dto != null) {
                            list.add(dto);
                        }
                    });
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onError(getMessage(e)));
    }

    public void getMovieById(String movieId, ResultCallback<MovieDTO> callback) {
        firestore.collection(FirestoreCollections.MOVIES)
                .document(movieId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        callback.onError("Movie not found");
                        return;
                    }

                    MovieDTO dto = documentSnapshot.toObject(MovieDTO.class);
                    if (dto == null) {
                        callback.onError("Movie data is empty");
                        return;
                    }

                    callback.onSuccess(dto);
                })
                .addOnFailureListener(e -> callback.onError(getMessage(e)));
    }

    private String getMessage(Exception e) {
        return e != null && e.getMessage() != null ? e.getMessage() : "Unknown error";
    }
}