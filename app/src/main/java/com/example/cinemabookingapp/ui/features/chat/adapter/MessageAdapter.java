package com.example.cinemabookingapp.ui.features.chat.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.domain.model.ChatMessage;
import com.example.cinemabookingapp.utils.DateTimeConverter;

/**
 * MessageAdapter — RecyclerView adapter for a chat thread.
 *
 * Two view types:
 *   VIEW_TYPE_SENT     → item_message_sent.xml     (right-aligned, black bubble)
 *   VIEW_TYPE_RECEIVED → item_message_received.xml (left-aligned, grey bubble)
 *
 * Extends {@link ListAdapter} with {@link DiffUtil} for efficient list diffing.
 *
 * Usage:
 *   MessageAdapter adapter = new MessageAdapter();
 *   recyclerView.setAdapter(adapter);
 *   adapter.submitList(messages);   // call whenever the list changes
 */
public class MessageAdapter extends ListAdapter<ChatMessage, RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT     = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------
    String authUserId;
    public MessageAdapter(String authUserId) {
        super(DIFF_CALLBACK);
        this.authUserId = authUserId;
    }

    // -------------------------------------------------------------------------
    // DiffUtil callback
    // -------------------------------------------------------------------------

    private static final DiffUtil.ItemCallback<ChatMessage> DIFF_CALLBACK =
        new DiffUtil.ItemCallback<ChatMessage>() {

            @Override
            public boolean areItemsTheSame(@NonNull ChatMessage oldItem, @NonNull ChatMessage newItem) {
                return oldItem.messageId == newItem.messageId;
            }

            @Override
            public boolean areContentsTheSame(@NonNull ChatMessage oldItem, @NonNull ChatMessage newItem) {
                return oldItem.content.equals(newItem.content)
                        && oldItem.sentAt == newItem.sentAt;
            }
        };

    // -------------------------------------------------------------------------
    // ViewHolder — Sent
    // -------------------------------------------------------------------------

    public static class SentMessageViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvMessageText;
        private final TextView tvTimestamp;

        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessageText = itemView.findViewById(R.id.tvMessageText);
            tvTimestamp   = itemView.findViewById(R.id.tvTimestamp);
        }

        public void bind(ChatMessage message) {
            tvMessageText.setText(message.content);
            tvTimestamp.setText(DateTimeConverter.convertToDateTimeString(message.sentAt));
        }
    }

    // -------------------------------------------------------------------------
    // ViewHolder — Received
    // -------------------------------------------------------------------------

    public static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvMessageText;
        private final TextView tvTimestamp;

        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessageText = itemView.findViewById(R.id.tvMessageText);
            tvTimestamp   = itemView.findViewById(R.id.tvTimestamp);
        }

        public void bind(ChatMessage message) {
            tvMessageText.setText(message.content);
            tvTimestamp.setText(DateTimeConverter.convertToDateTimeString(message.sentAt));
        }
    }

    // -------------------------------------------------------------------------
    // Adapter overrides
    // -------------------------------------------------------------------------

    @Override
    public int getItemViewType(int position) {
        return getItem(position).senderId.equals(authUserId) ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_TYPE_SENT) {
            View view = inflater.inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = getItem(position);
        Log.i("MessageAdapter", "Binding message: " + message.content + " (senderId: " + message.senderId + ")");
        if (holder instanceof SentMessageViewHolder) {
            ((SentMessageViewHolder) holder).bind(message);
        } else if (holder instanceof ReceivedMessageViewHolder) {
            ((ReceivedMessageViewHolder) holder).bind(message);
        }
    }
}