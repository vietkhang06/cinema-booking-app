package com.example.cinemabookingapp.ui.admin.movie;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cinemabookingapp.R;

public class AdminMovieFormActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_admin_movie_form);

        EditText edtTitle = findViewById(R.id.edtTitle);
        Button btnSave = findViewById(R.id.btnSave);

        btnSave.setOnClickListener(v -> finish());
    }
}