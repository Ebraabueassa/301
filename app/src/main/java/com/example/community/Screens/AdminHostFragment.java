package com.example.community.Screens;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.community.R;
import com.example.community.User;
import com.example.community.UserService;
import com.example.community.Role;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for adminstarators to view and manage organizers
 * <p>
 *     Fragment displays a list of all organizers and provides options to delete their accounts.
 * </p>
 * <p>
 *     Loads all users with ORGANIZER role and displays them in a RecyclerView. When admin clicks
 *     delete, confirmation dialog appeats. When confirmed, the account is deleted and removed from the
 *     displayed list of organizers.
 * </p>
 */
public class AdminHostFragment extends Fragment implements com.example.community.ArrayAdapters.AdminHostAdapter.OnHostListener {

    /**
     * Button to navigate back to admin home fragment
     */
    Button backButton;

    /**
     * Tag for logging
     */
    private static final String TAG = "AdminHostFragment";

    /**
     * Recycler view to display list of organizers
     */
    private RecyclerView recyclerView;

    /**
     * Adapter for manging the RecyclerView items and delete button events.
     */
    private com.example.community.ArrayAdapters.AdminHostAdapter adapter;

    /**
     * List of organizer users loaded
     */
    private List<User> userList;

    /**
     * Service for user management
     */
    private UserService userService;

    /**
     * Navigation controller for navigation
     */
    private NavController navController;

    /**
     * Inflates the fragment's layout view
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return The View for the fragment's UI
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.admin_host_page, container, false);
    }

    /**
     * Initializes the fragment's UI. Sets up RecuclerView Adapter, registers a fragment result
     * listener for deletion confirmations, loads organizer data, and sets up click listeners.
     *
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        navController = Navigation.findNavController(view);
        backButton = view.findViewById(R.id.buttonBack);
        userService = new UserService();
        userList = new ArrayList<>();

        recyclerView = view.findViewById(R.id.adminHostView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new com.example.community.ArrayAdapters.AdminHostAdapter(getContext(), userList, this);
        recyclerView.setAdapter(adapter);

        getParentFragmentManager().setFragmentResultListener(
                DeleteAccountConfirmDialogFragment.REQUEST_KEY,
                this.getViewLifecycleOwner(),
                (requestKey, result) -> {
                    if (result.getBoolean(DeleteAccountConfirmDialogFragment.RESULT_CONFIRMED)) {

                        Fragment dialogFragment = getParentFragmentManager().findFragmentByTag("DeleteAccountConfirmDialog");
                        if (dialogFragment != null && dialogFragment.getArguments() != null) {
                            String userID = dialogFragment.getArguments().getString("userID");
                            int position = dialogFragment.getArguments().getInt("position");

                            performDeleteUser(userID, position);
                        }
                    }
                }
        );

        loadUsers();
        setUpClickListener();
    }

    /**
     * Deletes a user account and updates RecyclerView
     * <p>
     *     How it works:
     *     <ul>
     *         <li>Calls service to delete user account</li>
     *         <li>Removes user from displayed list at the specified poistion</li>
     *         <li>Refreshes the list in the UI</li>
     *         <li>Shows a toast message</li>
     *     </ul>
     * </p>
     *
     * @param userID Identifier of the user to be deleted
     * @param position Position of the user in the RecyclerView list
     */
    private void performDeleteUser(String userID, int position) {
        userService.deleteUser(userID).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "User deleted successfully: " + userID);

                if (position < userList.size()) {
                    userList.remove(position);
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, userList.size());
                }
                Toast.makeText(getContext(), "User deleted.", Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG, "Failed to delete user", task.getException());
                Toast.makeText(getContext(), "Failed to delete user.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Loads all organizer users and populates the RecyclerView
     * <p>
     *     How it works:
     *     <ul>
     *         <li>Retrieves all users from the database</li>
     *         <li>Clears the existing list of users</li>
     *         <li>Adds only organizer users to the list</li>
     *         <li>Refreshes the list in the UI</li>
     *     </ul>
     * </p>
     *
     * Displays error message if the user loading fails.
     */
    private void loadUsers() {
        userService.getAllUsers().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<User> allUsers = task.getResult();
                userList.clear();

                for (User user : allUsers) {
                    if (user.getRole() == Role.ORGANIZER) {
                        userList.add(user);

                    }
                }
                adapter.notifyDataSetChanged(); // Refresh the list in the UI
                Log.d(TAG, "Successfully loaded " + userList.size() + " users.");
            } else {
                Log.e(TAG, "Failed to load users", task.getException());
                Toast.makeText(getContext(), "Failed to load users.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Handles delete button clicks. Creates a confirmation dialog to confirm deletion with
     * the user ID and list position passed as arguments.
     *
     * @param user organizer user that is to be deleted
     * @param position the position of the user in the RecyclerView list
     */
    @Override
    public void onDeleteClicked(User user, int position) {
        DeleteAccountConfirmDialogFragment dialog = new DeleteAccountConfirmDialogFragment();

        Bundle args = new Bundle();
        args.putString("userID", user.getUserID());
        args.putInt("position", position);
        dialog.setArguments(args);

        dialog.show(getParentFragmentManager(), "DeleteAccountConfirmDialog");
    }

    /**
     * Sets up click listeners for the back button.
     */
    private void setUpClickListener() {
        backButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(AdminHostFragment.this)
                    .navigate(R.id.action_AdminHostFragment_to_AdminHomeFragment);
        });
    }
}
