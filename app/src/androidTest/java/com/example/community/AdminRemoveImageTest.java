package com.example.community;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.UiController;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.community.ArrayAdapters.ImageArrayAdapter;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.view.View;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminRemoveImageTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    // Helper to click child view inside RecyclerView item
    public static ViewAction clickChildViewWithId(final int id) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return Matchers.any(View.class);
            }

            @Override
            public String getDescription() {
                return "Click on a child view with specified id.";
            }

            @Override
            public void perform(UiController uiController, View view) {
                View v = view.findViewById(id);
                if (v != null) {
                    v.performClick();
                }
            }
        };
    }

    @Before
    public void navigateToAdminHome() throws InterruptedException {
        Thread.sleep(3000); // splash screen wait
        onView(withId(R.id.loginButton)).perform(click());
        Thread.sleep(2000);
        onView(withId(R.id.buttonAdmin)).perform(click());
        Thread.sleep(2000);
        onView(withId(R.id.buttonEvent)).check(matches(isDisplayed())); // verify we're on admin home
    }

    @Test
    public void testAdminRemoveFirstImage() throws InterruptedException {
        final String[] firstUploader = new String[1];

        // Step 1: Click the Images button to go to AdminImageFragment
        onView(withId(R.id.buttonImage)).perform(click());
        Thread.sleep(2000);

        // Step 2: Get first image's "Uploaded by" text from RecyclerView
        activityScenarioRule.getScenario().onActivity(activity -> {
            RecyclerView rv = activity.findViewById(R.id.imageView);
            if (rv != null && rv.getAdapter() != null && rv.getAdapter().getItemCount() > 0) {
                ImageArrayAdapter.ImageViewHolder holder =
                        (ImageArrayAdapter.ImageViewHolder) rv.findViewHolderForAdapterPosition(0);
                if (holder != null) {
                    firstUploader[0] = holder.imageInfo.getText().toString();
                }
            }
        });

        Thread.sleep(1000);

        // Step 3: Click Remove button on first item
        onView(withId(R.id.imageView))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0,
                        clickChildViewWithId(R.id.buttonRemove)));
        Thread.sleep(1000);

        // Step 4: Confirm deletion in dialog
        onView(withText("Delete")).perform(click());
        Thread.sleep(2000);


    }
}
