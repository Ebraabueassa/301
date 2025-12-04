package com.example.community.Screens;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.community.R;
import com.example.community.User;
import com.example.community.UserService;

/**
 * Fragment representing the profile screen for an entrant.
 * Allows viewing and editing of the user's name, email, and phone number.
 * Provides the option to delete the user's account.
 */
public class EntrantUserProfileFragment extends Fragment {

    private EditText nameBox, emailBox, phoneBox;
    private Button saveButton, deleteAccountButton;
    private UserService userService;
    private User currentUser;

    /**
     * Inflates the fragment's layout.
     *
     * @param inflater           LayoutInflater object used to inflate views
     * @param container          Parent container for the fragment
     * @param savedInstanceState Saved instance state bundle
     * @return The inflated view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.entrant_profile, container, false);
    }

    /**
     * Called after the view has been created.
     * Initializes UI elements, loads the current user's information,
     * sets up listeners for saving changes, and for deleting the account.
     *
     * @param view               The fragment's view
     * @param savedInstanceState Saved instance state bundle
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind UI elements
        nameBox = view.findViewById(R.id.name_box);
        emailBox = view.findViewById(R.id.email_box);
        phoneBox = view.findViewById(R.id.phone_box);
        saveButton = view.findViewById(R.id.save_button);
        deleteAccountButton = view.findViewById(R.id.delete_unity);

        // Initialize UserService
        userService = new UserService();
        String deviceToken = userService.getDeviceToken();

        // Load current user details by device token
        userService.getByDeviceToken(deviceToken)
                .addOnSuccessListener(user -> {
                    if (user == null) {
                        Log.e("EntrantUserProfileFragment", "User not found: " + deviceToken);
                        throw new IllegalArgumentException("User not found: " + deviceToken);
                    }
                    currentUser = user;
                    nameBox.setText(user.getUsername());
                    emailBox.setText(user.getEmail());
                    phoneBox.setText(user.getPhoneNumber());
                });

        // Save button updates user information and navigates back
        saveButton.setOnClickListener(v -> {
            String newName = nameBox.getText().toString();
            String newEmail = emailBox.getText().toString();
            String newPhone = phoneBox.getText().toString();

            currentUser.setUsername(newName);
            currentUser.setEmail(newEmail);
            currentUser.setPhoneNumber(newPhone);

            userService.updateUser(currentUser);
            NavHostFragment.findNavController(EntrantUserProfileFragment.this).popBackStack();
        });

        // Delete account button opens DeleteAccountFragment dialog
        deleteAccountButton.setOnClickListener(v -> {
            DeleteAccountFragment deleteDialog = DeleteAccountFragment.newInstance(currentUser.getUserID());
            deleteDialog.show(getParentFragmentManager(), "DeleteAccountFragment");
        });
    }
}
