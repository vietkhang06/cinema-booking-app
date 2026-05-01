package com.example.cinemabookingapp.ui.admin.cinema.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.domain.model.Cinema;

import java.util.ArrayList;
import java.util.List;

public class AdminCinemaAdapter extends RecyclerView.Adapter<AdminCinemaAdapter.Holder> {

    private List<Cinema> list = new ArrayList<>();

    public void submitList(List<Cinema> newList) {
        list = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup p, int v) {
        return new Holder(LayoutInflater.from(p.getContext())
                .inflate(R.layout.item_admin_cinema, p, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int i) {
        Cinema c = list.get(i);

        h.name.setText(c.name != null ? c.name : "N/A");
        h.address.setText(c.address != null ? c.address : "N/A");
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView name, address;

        Holder(View v) {
            super(v);
            name = v.findViewById(R.id.tvName);
            address = v.findViewById(R.id.tvAddress);
        }
    }
}