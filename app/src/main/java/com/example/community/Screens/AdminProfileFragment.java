package com.example.community.Screens;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.community.R;
import com.example.community.Role;
import com.example.community.User;
import com.example.community.UserService;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for admins to view and manage entrant (regular users) accounts.
 * <p>
 *     Displays a list of all entrant accounts and allows admins to delete any account.
 * </p>
 * <p>
 *     Loads all users with ENTRANT role and displays them in a RecyclerView. When delete is clicked,
 *     a confirmation dialog appeats. Upon confirmation account is deleted and removed from the
 *     displayed list
 * </p>
 *
 * @see UserService
 * @see com.example.community.ArrayAdapters.AdminHostAdapter
 * @see DeleteAccountConfirmDialogFragment
 */
public class AdminProfileFragment extends Fragment implements com.example.community.ArrayAdapters.AdminHostAdapter.OnHostListener {

    /**
     * Button to go back to previous fragment
     */
    Button backButton;

    /**
     * Tag for logging
     */
    private static final String TAG = "AdminProfileFragment";

    /**
     * RecyclerView displaying the list of entrant accounts
     */
    private RecyclerView recyclerView;

    /**
     * Adapter for managing the RecyclerView items and delete button.
     */
    private com.example.community.ArrayAdapters.AdminHostAdapter adapter;

    /**
     * List of entrant users
     */
    private List<User> userList;

    /**
     * Service for user management
     */
    private UserService userService;


    /**
     * Inflates the layout for this fragment
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
     * Initializes the fragment's UI. Sets up buttons, Recycler View, and adapter. Registers
     * fragment result listener for deletion confirmations, loads entrant users, and sets up
     * click listeners
     *
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView headerTitle = view.findViewById(R.id.headerTitle);
        headerTitle.setText("Entrant");

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
     * Deletes user and removes from RecyclerView lsit
     * <p>
     *     How it works:
     *     <ul>
     *         <li>Calls user service to delete the account</li>
     *         <li>Removes user from list and adapter on success</li>
     *         <li>Updates RecyclerView</li>
     *         <li>Shows toast message</li>
     *     </ul>
     * </p>
     * @param userID identifier for the user to be deleted
     * @param position position of the user in the list
     */
    private void performDeleteUser(String userID, int position) {
        userService.deleteUserCascade(userID).addOnCompleteListener(task -> {
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
     * Loads all entrant users and populates the RecyclerView
     * <p>
     *     How it works:
     *     <ul>
     *         <li>Retrieves all users from the database</li>
     *         <li>Clears the existing list of users</li>
     *         <li>Adds only entrant users to the list</li>
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
                    if (user.getRole() == Role.ENTRANT) {
                        userList.add(user);

                    }
                }
                adapter.notifyDataSetChanged();
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
            NavHostFragment.findNavController(AdminProfileFragment.this)
                    .navigate(R.id.action_AdminProfileFragment_to_AdminHomeFragment);
        });
    }

}
