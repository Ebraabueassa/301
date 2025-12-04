package com.example.community;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.graphics.Insets;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

/**
 * The main activity that hosts the navigation graph and toolbar.
 * <p>
 *     Activity manages the application's navigations structure using a NavHostFragment and a NavController.
 *     Provides toolbar that is conditionally visible depending on the current navigation destination.
 * </p>
 * <p>
 *     Activity handles:
 *     <ul>
 *         <li>Window insets and system padding for edge-to-edge display</li>
 *         <li>Navigation between app sections</li>
 *         <li>Toolbar visibility based on navigation destination</li>
 *         <li>Theme settings through a menu</li>
 *     </ul>
 * </p>
 *
 * @see NavHostFragment
 * @see NavController
 * @see Toolbar
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Tool bar widget at the top of the activity
     */
    private Toolbar toolbar;

    /**
     * Initializes the activity, sets up the window insets listener for
     * edge-to-edge layout, configures the toolbar, and sets up navigation
     * with destination change listeners.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (view, insets) -> {
            Insets systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemInsets.left, systemInsets.top, systemInsets.right, systemInsets.bottom);
            return insets;
        });


        toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_activity_main);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();

            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                if (destination.getId() == R.id.RoleSelectFragment ||
                        destination.getId() == R.id.EntrantHomeFragment ||
                        destination.getId() == R.id.AdminHomeFragment ||
                        destination.getId() == R.id.OrganizerHomeFragment) {

                    toolbar.setVisibility(View.VISIBLE);
                } else {
                    toolbar.setVisibility(View.GONE);
                }
            });
        }
    }

    /**
     * Inflates the options menu, adding items from the menu resource.
     *
     * @param menu The options menu in which you place your items.
     *
     * @return true if the menu was successfully created.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    /**
     * Handles menu item selections in the options menu
     *
     * @param item The menu item that was selected.
     *
     * @return true if the menu item was handled, false otherwise.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.theme_system) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            return true;
        } else if (id == R.id.theme_light) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            return true;
        } else if (id == R.id.theme_dark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
