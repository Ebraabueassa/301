package com.example.community.Screens;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.community.R;
import com.example.community.UserService;

/**
 * Fragment representing the splash screen of the app.
 * Displays a login button after a short delay with a fade-in animation.
 * Handles user authentication and navigation to role selection.
 */
public class SplashPageFragment extends Fragment {

    private UserService userService;
    private Button loginButton;
    private Handler handler;

    /**
     * Inflates the splash screen layout.
     *
     * @param inflater           LayoutInflater to inflate views
     * @param container          Parent view container
     * @param savedInstanceState Saved state bundle
     * @return Inflated fragment view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.start_page, container, false);
    }

    /**
     * Called after the fragment's view is created.
     * Initializes UserService, sets up the login button, and handles fade-in animation.
     *
     * @param view               The fragment's view
     * @param savedInstanceState Saved state bundle
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userService = new UserService();
        loginButton = view.findViewById(R.id.loginButton);
        handler = new Handler(Looper.getMainLooper());

        // Handle login button click
        loginButton.setOnClickListener(v -> userService.splashScreenDeviceAuthentication()
                .addOnSuccessListener(user -> NavHostFragment.findNavController(this)
                        .navigate(R.id.action_SplashPageFragment_to_RoleSelectFragment))
        );

        // Fade-in animation for login button after 2 seconds
        handler.postDelayed(() -> {
            AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
            fadeIn.setDuration(500);
            loginButton.startAnimation(fadeIn);
            loginButton.setVisibility(View.VISIBLE);
        }, 2000);
    }
}
