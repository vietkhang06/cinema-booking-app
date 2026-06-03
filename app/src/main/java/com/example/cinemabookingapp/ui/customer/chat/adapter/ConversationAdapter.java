package com.example.cinemabookingapp.ui.customer.chat.adapter;

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
import com.example.cinemabookingapp.domain.model.Conversation;
import com.example.cinemabookingapp.ui.customer.chat.model.ConversationItem;
import com.example.cinemabookingapp.utils.DateTimeConverter;
import com.google.android.material.card.MaterialCardView;

public class ConversationAdapter extends ListAdapter<ConversationItem, ConversationAdapter.ViewHolder> {

    // -------------------------------------------------------------------------
    // Click listener interface
    // -------------------------------------------------------------------------

    public interface OnConversationClickListener {
        void onConversationClick(ConversationItem conversation);
    }

    private final OnConversationClickListener clickListener;
    private String authUserId;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public ConversationAdapter(String authUserId, @NonNull OnConversationClickListener clickListener) {
        super(DIFF_CALLBACK);
        this.clickListener = clickListener;
        this.authUserId = authUserId;
    }

    // -------------------------------------------------------------------------
    // DiffUtil callback
    // -------------------------------------------------------------------------

    private static final DiffUtil.ItemCallback<ConversationItem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<ConversationItem>() {
                @Override
                public boolean areItemsTheSame(
                        @NonNull ConversationItem oldItem, @NonNull ConversationItem newItem) {
                    return oldItem.conversation.convoId.equals(newItem.conversation.convoId);
                }

                @Override
                public boolean areContentsTheSame(
                        @NonNull ConversationItem oldItem, @NonNull ConversationItem newItem) {
                    return false;
//                    return oldItem.conversation.lastMessage.content.equals(newItem.conversation.lastMessage.content)
//                            && oldItem.conversation.unreadCounts.hashCode() == newItem.conversation.unreadCounts.hashCode()
//                            && oldItem.isOnline == newItem.isOnline;
                }
            };

    // -------------------------------------------------------------------------
    // ViewHolder
    // -------------------------------------------------------------------------

    public static class ViewHolder extends RecyclerView.ViewHolder {

        final MaterialCardView cardConversation;
        final View onlineDot;
        final TextView tvContactName;
        final TextView tvTimestamp;
        final TextView tvPreview;
        final MaterialCardView badgeUnread;
        final TextView tvUnreadCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardConversation = itemView.findViewById(R.id.cardConversation);
            onlineDot        = itemView.findViewById(R.id.onlineBadge);
            tvContactName    = itemView.findViewById(R.id.tvContactName);
            tvTimestamp      = itemView.findViewById(R.id.tvTimestamp);
            tvPreview        = itemView.findViewById(R.id.tvPreview);
            badgeUnread      = itemView.findViewById(R.id.badgeUnread);
            tvUnreadCount    = itemView.findViewById(R.id.tvUnreadCount);
        }

        public void bind(
            String authUserId,
             ConversationItem conversation,
             OnConversationClickListener listener
        ) {

            // Online dot visibility
            onlineDot.setVisibility(conversation.isOnline ? View.VISIBLE : View.GONE);
            Log.i("ConversationAdapter", "Binding convo " + conversation.conversation.participants + " - online: " + conversation.isOnline);

            Conversation.UserSnapShot otherUser = conversation.conversation.participants.get(0);
            // Name, timestamp, preview
            tvContactName.setText(otherUser.name);
            tvTimestamp.setText(DateTimeConverter.convertToDateTimeString(conversation.conversation.lastMessageAt));
            tvPreview.setText(conversation.conversation.lastMessage.content);

            // Unread badge
            int unread = conversation.conversation.unreadCounts.get(authUserId);
            if (unread > 0) {
                badgeUnread.setVisibility(View.VISIBLE);
                tvUnreadCount.setText(String.valueOf(unread));
            } else {
                badgeUnread.setVisibility(View.GONE);
            }

            // Bold name when there are unread messages
            tvContactName.setTypeface(null,
                    unread > 0
                            ? android.graphics.Typeface.BOLD
                            : android.graphics.Typeface.NORMAL);

            // Row click
            cardConversation.setOnClickListener(
                    v -> listener.onConversationClick(conversation));
        }
    }

    // -------------------------------------------------------------------------
    // Adapter overrides
    // -------------------------------------------------------------------------

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conversation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(authUserId, getItem(position), clickListener);
    }
}
