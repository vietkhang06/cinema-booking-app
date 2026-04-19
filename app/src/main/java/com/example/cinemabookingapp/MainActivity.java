package com.example.cinemabookingapp;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.cinemabookingapp.data.remote.datasource.MovieRemoteDataSource;
import com.example.cinemabookingapp.domain.model.Movie;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MOVIE_TEST";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 👉 TEST FIRESTORE TẠI ĐÂY
        testFetchMovies();
        Log.d("TEST", "onCreate chạy");
    }

    private void testFetchMovies() {
        MovieRemoteDataSource ds = new MovieRemoteDataSource();

        ds.getMovies(new MovieRemoteDataSource.OnResult() {
            @Override
            public void onSuccess(List<Movie> movies) {
                if (movies == null || movies.isEmpty()) {
                    Log.d(TAG, "No movies found");
                    return;
                }

                for (Movie m : movies) {
                    Log.d(TAG, "Movie: " + m.title);
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error: " + error);
            }
        });
    }
}