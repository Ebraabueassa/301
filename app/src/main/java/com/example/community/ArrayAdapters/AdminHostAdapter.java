package com.example.community.ArrayAdapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.community.R;
import com.example.community.User;
import com.example.community.UserService;

import java.util.List;

/**
 * Adapter for the admin host list
 */
public class AdminHostAdapter extends RecyclerView.Adapter<AdminHostAdapter.HostViewHolder> {

    private List<User> userList;
    private final UserService userService;
    private final Context context;

    /**
     * Interface for the delete button click listener
     */
    public interface OnHostListener {
        void onDeleteClicked(User user, int position);
    }

    private final OnHostListener onHostListener;

    /**
     * Constructs an AdminHostAdapter
     *
     * @param context the application context
     * @param userList te list of users to display
     * @param onHostListener the listener for host actions
     */
    public AdminHostAdapter(Context context, List<User> userList, OnHostListener onHostListener) {
        this.context = context;
        this.userList = userList;
        this.userService = new UserService();
        this.onHostListener = onHostListener;
    }

    /**
     * Creates a new view holder for the admin host list
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return the new view holder
     */
    @NonNull
    @Override
    public HostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())

                .inflate(R.layout.admin_host_profile, parent, false);
        return new HostViewHolder(view);
    }

    /**
     * Binds the data to the view holder
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull HostViewHolder holder, int position) {
        User user = userList.get(position);

        String displayName = (user.getUsername() != null && !user.getUsername().isEmpty()) ? user.getUsername() : user.getUserID();
        holder.hostNameTextView.setText(displayName);

        // Set click listener for the "Delete" button
        holder.deleteButton.setOnClickListener(v -> {
            if (onHostListener != null) {
                onHostListener.onDeleteClicked(user, holder.getAdapterPosition());
            }
        });
    }

    /**
     * Returns total number of items in the user list
     *
     * @return size of the user list
     */
    @Override
    public int getItemCount() {
        return userList.size();
    }

    // ViewHolder class that holds the views for each item
    public static class HostViewHolder extends RecyclerView.ViewHolder {
        TextView hostNameTextView;
        Button deleteButton;

        public HostViewHolder(@NonNull View itemView) {
            super(itemView);
            hostNameTextView = itemView.findViewById(R.id.hostName);
            deleteButton = itemView.findViewById(R.id.buttonDelete);
        }
    }
}
