package com.example.cinemabookingapp.ui.admin.movie.adapter;

import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabookingapp.R;

import java.util.List;
import com.example.cinemabookingapp.ui.admin.movie.model.AdminMovieItem;

public class AdminMovieAdapter extends RecyclerView.Adapter<AdminMovieAdapter.MovieVH> {

    public interface OnMovieActionListener {
        void onEdit(AdminMovieItem movie);
        void onDelete(AdminMovieItem movie);
        void onClick(AdminMovieItem movie);
    }

    private final List<AdminMovieItem> list;
    private final OnMovieActionListener listener;

    public AdminMovieAdapter(List<AdminMovieItem> list, OnMovieActionListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MovieVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MovieVH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_movie, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MovieVH h, int pos) {
        AdminMovieItem m = list.get(pos);

        h.tvTitle.setText(m.title);
        h.tvDuration.setText(m.duration + " phút");
        h.tvStatus.setText(m.status);

        h.itemView.setOnClickListener(v -> listener.onClick(m));
        h.btnEdit.setOnClickListener(v -> listener.onEdit(m));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(m));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class MovieVH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDuration, tvStatus;
        ImageButton btnEdit, btnDelete;

        MovieVH(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvMovieTitle);
            tvDuration = v.findViewById(R.id.tvMovieDuration);
            tvStatus = v.findViewById(R.id.tvMovieStatus);
            btnEdit = v.findViewById(R.id.btnEditMovie);
            btnDelete = v.findViewById(R.id.btnDeleteMovie);
        }
    }
}