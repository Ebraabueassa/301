package com.example.community.ArrayAdapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.community.R;
import com.example.community.User;
import com.example.community.WaitingListEntry;

import java.util.List;

/**
 * RecyclerView Adapter for displaying a list of {@link User} objects.
 * Shows each user's name, email, and phone number.
 */
public class UserArrayAdapter extends RecyclerView.Adapter<UserArrayAdapter.ViewHolder> {
    /** Tag for logging*/
    private static final String TAG = "UserArrayAdapter";

    /** List of users to display in the RecyclerView */
    private List<User> users;
    private String listType;
    private OnUserSelectionListener selectionListener;

    /**
     * ViewHolder class for caching references to the views in each user list item.
     */
    public class ViewHolder extends RecyclerView.ViewHolder {

        /** TextView displaying the user's name */
        TextView userNameTextView;

        /** TextView displaying the user's email */
        TextView userEmailTextView;

        /** TextView displaying the user's phone number */
        TextView userPhoneNumberTextView;

        /** Checkbox for the invited listType*/
        CheckBox userSelectionCheckbox;

        /**
         * Constructor for ViewHolder.
         *
         * @param itemView Root view of the RecyclerView item.
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameTextView = itemView.findViewById(R.id.userNameInList);
            userEmailTextView = itemView.findViewById(R.id.userEmailInList);
            userPhoneNumberTextView = itemView.findViewById(R.id.userPhoneInList);
            userSelectionCheckbox = itemView.findViewById(R.id.userSelectionCheckbox);
        }
    }

    /**
     * Constructor for UserArrayAdapter.
     *
     * @param users List of User objects to display.
     * @param listType The type of list being displayed
     */
    public UserArrayAdapter(List<User> users, String listType){
        this.users = users;
        this.listType = listType;
    }

    public void setSelectionListener(OnUserSelectionListener listener){
        this.selectionListener = listener;
    }

    /**
     * Called when RecyclerView needs a new {@link ViewHolder}.
     *
     * @param parent The parent ViewGroup into which the new view will be added.
     * @param viewType The view type of the new view.
     * @return A new ViewHolder instance.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.organizer_event_user_lists_item, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Called by RecyclerView to display data at the specified position.
     * Populates the user's name, email, and phone number.
     *
     * @param holder The ViewHolder to bind data to.
     * @param position Position of the item in the users list.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);
        String displayName = user.getUsername();
        holder.userNameTextView.setText(displayName);
        holder.userEmailTextView.setText(user.getEmail());

        String phone = (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty())
                ? "Phone: " + user.getPhoneNumber()
                : "Phone: No phone number provided";
        holder.userPhoneNumberTextView.setText(phone);

        Log.d(TAG, "onBindViewHolder: listType = " + listType);

        holder.userSelectionCheckbox.setOnCheckedChangeListener(null);
        holder.userSelectionCheckbox.setChecked(false);

        if ("invited".equals(listType)) {
            holder.userSelectionCheckbox.setVisibility(View. VISIBLE);
            holder.userSelectionCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                Log.d(TAG, "Checkbox changed for user:  " + user.getUserID() + " isChecked = " + isChecked);
                if (selectionListener != null) {
                    selectionListener.onUserSelectionChanged(user.getUserID(), isChecked);
                }
            });
        } else {
            Log.d(TAG, "Checkbox hidden for user:  " + user.getUserID());
            holder.userSelectionCheckbox.setVisibility(View. GONE);
            holder.userSelectionCheckbox.setChecked(false);
        }
    }

    /**
     * Returns the total number of users in the adapter.
     *
     * @return Size of the users list.
     */
    @Override
    public int getItemCount() {
        return users.size();
    }

    public interface OnUserSelectionListener {
        void onUserSelectionChanged(String userId, boolean selected);
    }
}
