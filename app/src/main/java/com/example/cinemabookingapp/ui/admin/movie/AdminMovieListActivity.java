package com.example.cinemabookingapp.ui.admin.movie;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.ui.admin.movie.adapter.AdminMovieAdapter;
import com.example.cinemabookingapp.ui.admin.movie.model.AdminMovieItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.*;

public class AdminMovieListActivity extends AppCompatActivity {

    private RecyclerView rv;
    private List<AdminMovieItem> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_movie_list);

        rv = findViewById(R.id.rvMovies);
        FloatingActionButton fab = findViewById(R.id.fabAddMovie);

        list = mockData();

        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new AdminMovieAdapter(list, new AdminMovieAdapter.OnMovieActionListener() {
            @Override
            public void onEdit(AdminMovieItem m) {
                startActivity(new Intent(AdminMovieListActivity.this, AdminMovieFormActivity.class));
            }

            @Override
            public void onDelete(AdminMovieItem m) {
                list.remove(m);
                rv.getAdapter().notifyDataSetChanged();
            }

            @Override
            public void onClick(AdminMovieItem m) {
                startActivity(new Intent(AdminMovieListActivity.this, AdminMovieDetailActivity.class));
            }
        }));

        fab.setOnClickListener(v ->
                startActivity(new Intent(this, AdminMovieFormActivity.class)));
    }

    private List<AdminMovieItem> mockData() {
        List<AdminMovieItem> l = new ArrayList<>();
        l.add(new AdminMovieItem("1","Avengers","", "ACTIVE",120));
        l.add(new AdminMovieItem("2","Batman","", "INACTIVE",110));
        return l;
    }
}