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
import androidx.fragment.app.DialogFragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.community.R;
import com.example.community.UserService;

/**
 * A DialogFragment that allows the user to delete their CommUnity account.
 * Displays a confirmation dialog and handles account deletion via {@link UserService}.
 */
public class DeleteAccountFragment extends DialogFragment {

    private static final String TAG = "DeleteAccountFragment";

    private UserService userService;
    private String userId;
    private Button cancelDelete, confirmDelete;

    /**
     * Creates a new instance of {@link DeleteAccountFragment} with the specified user ID.
     *
     * @param userId The ID of the user whose account will be deleted.
     * @return A new instance of DeleteAccountFragment.
     */
    public static DeleteAccountFragment newInstance(String userId) {
        DeleteAccountFragment fragment = new DeleteAccountFragment();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        fragment.setArguments(args);

        return fragment;
    }

    /**
     * Called when the fragment is first created.
     * Initializes the {@link UserService} and retrieves the user ID from arguments.
     *
     * @param savedInstanceState Saved instance state bundle.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userService = new UserService();
        if (getArguments() != null) {
            userId = getArguments().getString("userId");
        }
    }

    /**
     * Inflates the layout for this fragment.
     *
     * @param inflater           LayoutInflater object to inflate views.
     * @param container          Parent container for the fragment.
     * @param savedInstanceState Saved instance state bundle.
     * @return The inflated view.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.delete_unity, container, false);
    }

    /**
     * Called after the view has been created.
     * Sets up button click listeners for canceling or confirming the account deletion.
     *
     * @param view               The fragment's view.
     * @param savedInstanceState Saved instance state bundle.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cancelDelete = view.findViewById(R.id.cancel_popup);
        confirmDelete = view.findViewById(R.id.delete_unity);

        cancelDelete.setOnClickListener(v -> dismiss());

        // Confirm deletion and call UserService to delete the account
        confirmDelete.setOnClickListener(v -> {
            Log.d(TAG, "Delete button clicked for user: " + userId);

            // disable buttons during deletion so it doesn't act weird
            confirmDelete.setEnabled(false);
            cancelDelete.setEnabled(false);


            userService.deleteUser(userId)
                    .addOnSuccessListener(task -> {
                        Log.d(TAG, "Account deleted successfully");

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getActivity(), "Account deleted successfully", Toast.LENGTH_SHORT).show();
                                dismiss();

                                // go back to splash page
                                try {
                                    NavHostFragment.findNavController(DeleteAccountFragment.this)
                                            .navigate(R.id.action_EntrantUserProfileFragment_to_SplashPageFragment);
                                } catch (Exception e) {
                                    Log.e(TAG, "Navigation failed", e);
                                }
                            });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to delete account", e);

                        if (getActivity() != null) { // failed to delete account
                            getActivity().runOnUiThread(() -> {
                                // re-enable buttons
                                confirmDelete.setEnabled(true);
                                cancelDelete.setEnabled(true);

                                String errorMessage = "Failed to delete account";
                                if (e.getMessage() != null) {
                                    errorMessage += ": " + e.getMessage();
                                }
                                Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();

                            });
                        }
                    });
        });
    }
}
