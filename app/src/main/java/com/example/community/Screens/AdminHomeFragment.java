package com.example.community.Screens;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;


import com.example.community.R;

import com.example.community.UserService;

/**
 * Home Fragment for Admin users.
 * <p>
 *     Serves as the main hub for admins. Provides navigation to event management,
 *     organzier management, user profile settings, image management, and notification
 *     managemnt.
 * </p>
 * <p>
 *     Displays navigation buttons, each leading to different admin features. Back button allows admin
 *     to return to role selection screen.
 * </p>
 *
 *
 * @see AdminEventFragment
 * @see AdminHostFragment
 * @see AdminProfileFragment
 * @see AdminImageFragment
 * @see AdminNotificationFragment
 * @see RoleSelectFragment
 *
 */
public class AdminHomeFragment extends Fragment {

    /**
     * UI components
     */
    private Button buttonEvent, buttonHost, buttonProfile, buttonImage, buttonNotification, buttonBack;

    private UserService userService;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        return inflater.inflate(R.layout.admin_home_page, container, false);
    }

    /**
     * Initializes the fragment's UI. Sets up click listeners for all the buttons.
     *
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@Nullable View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI components
        buttonEvent = view.findViewById(R.id.buttonEvent);
        buttonHost = view.findViewById(R.id.buttonHost);
        buttonProfile = view.findViewById(R.id.buttonProfile);
        buttonImage = view.findViewById(R.id.buttonImage);
        buttonNotification = view.findViewById(R.id.buttonNotification);
        buttonBack = view.findViewById(R.id.buttonBack);



        userService = new UserService();
        setUpClickListener();


    }

    /**
     * Sets up click listeners for admin navigation buttons.
     */
    private void setUpClickListener() {
        buttonEvent.setOnClickListener(v -> {
            NavHostFragment.findNavController(AdminHomeFragment.this)
                    .navigate(R.id.action_AdminHomeFragment_to_AdminEventFragment);
        });

        buttonHost.setOnClickListener(v -> {
            NavHostFragment.findNavController(AdminHomeFragment.this)
                    .navigate(R.id.action_AdminHomeFragment_to_AdminHostFragment);
        });

        buttonProfile.setOnClickListener(v -> {
            NavHostFragment.findNavController(AdminHomeFragment.this)
                    .navigate(R.id.action_AdminHomeFragment_to_AdminProfileFragment);
        });

        buttonImage.setOnClickListener(v -> {
            NavHostFragment.findNavController(AdminHomeFragment.this)
                    .navigate(R.id.action_AdminHomeFragment_to_AdminImageFragment);
        });

        buttonNotification.setOnClickListener(v -> {
            NavHostFragment.findNavController(AdminHomeFragment.this)
                    .navigate(R.id.action_AdminHomeFragment_to_AdminNotificationFragment);
        });
        buttonBack.setOnClickListener(v -> {
            NavHostFragment.findNavController(AdminHomeFragment.this)
                    .navigate(R.id.action_AdminHomeFragment_to_RoleSelectFragment);
        });
    }
}
