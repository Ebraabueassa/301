package com.example.community.Screens;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.community.R;
import com.example.community.Role;
import com.example.community.UserService;

/**
 * Fragment that allows the user to select their role in the app:
 * Entrant (User), Host (Organizer), or Admin.
 * Sets the role in Firebase and navigates to the corresponding home screen.
 */
public class RoleSelectFragment extends Fragment {

    Button buttonUser, buttonHost, buttonAdmin;
    UserService userService;

    /**
     * Inflates the role selection layout.
     *
     * @param inflater           LayoutInflater to inflate views
     * @param container          Parent view container
     * @param savedInstanceState Saved state bundle
     * @return Inflated fragment view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.role_select, container, false);
    }

    /**
     * Called after the fragment's view is created.
     * Sets up button click listeners for each role and navigates to the corresponding home screen.
     *
     * @param view               The fragment's view
     * @param savedInstanceState Saved state bundle
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        buttonUser = view.findViewById(R.id.buttonUser);
        buttonHost = view.findViewById(R.id.buttonHost);
        buttonAdmin = view.findViewById(R.id.buttonAdmin);

        userService = new UserService();

        String deviceToken = userService.getDeviceToken();

        // Set role to Entrant (User)
        buttonUser.setOnClickListener(v -> {
            if (deviceToken != null) {
                userService.getUserIDByDeviceToken(deviceToken)
                        .addOnSuccessListener(userId -> userService.setRole(userId, Role.ENTRANT))
                        .addOnFailureListener(e ->
                                Toast.makeText(getContext(),
                                        "Failed to set role: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show()
                        );
            }

            NavHostFragment.findNavController(RoleSelectFragment.this)
                    .navigate(R.id.action_RoleSelectFragment_to_EntrantHomeFragment);
        });

        // Set role to Host (Organizer)
        buttonHost.setOnClickListener(v -> {
            userService.getUserIDByDeviceToken(deviceToken)
                    .addOnSuccessListener(userId -> userService.setRole(userId, Role.ORGANIZER)
                            .addOnSuccessListener(task ->
                                    NavHostFragment.findNavController(RoleSelectFragment.this)
                                            .navigate(R.id.action_RoleSelectFragment_to_OrganizerHomeFragment)
                            )
                    );
        });

        // Set role to Admin
        buttonAdmin.setOnClickListener(v -> {
            userService.getUserIDByDeviceToken(deviceToken)
                    .addOnSuccessListener(userId -> userService.setRole(userId, Role.ADMIN)
                            .addOnSuccessListener(task ->
                                    NavHostFragment.findNavController(RoleSelectFragment.this)
                                            .navigate(R.id.action_RoleSelectFragment_to_AdminHomeFragment)
                            )
                    );
        });
    }
}
