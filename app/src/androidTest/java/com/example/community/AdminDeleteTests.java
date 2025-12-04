package com.example.community;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.AllOf.allOf;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
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
public class AdminDeleteTests {

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

    private void clickAndWait(int id) throws InterruptedException {
        onView(withId(id)).perform(click());
        Thread.sleep(2000);
    }

    @Before
    public void navigateToAdminHome() throws InterruptedException {
        Thread.sleep(3000); // splash screen wait
        clickAndWait(R.id.loginButton);
        Thread.sleep(3000);
        clickAndWait(R.id.buttonAdmin);
        Thread.sleep(3000);
    }

    // US 03.01.01 As an administrator, I want to be able to remove events
    @Test
    public void testAdminRemoveFirstEvent() throws InterruptedException {
        // Navigate to Admin Event page
        onView(withId(R.id.buttonEvent)).perform(click());
        Thread.sleep(3000); // wait for the page to load

        // Click Remove button on first event in RecyclerView
        onView(withId(R.id.adminEventView))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0,
                        clickChildViewWithId(R.id.buttonRemove)));

        Thread.sleep(1000); // wait for the confirmation dialog to appear


    }




    //US 03.02.01 As an administrator, I want to be able to remove profiles
    @Test
    public void testAdminRemoveFirstProfile() throws InterruptedException {
        // Navigate to Profile list
        onView(withId(R.id.buttonProfile)).perform(click());
        Thread.sleep(2000);

        // Click Delete on first item
        onView(withId(R.id.adminHostView))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0,
                        clickChildViewWithId(R.id.buttonDelete)));

        Thread.sleep(1000);

        // Confirm deletion
        onView(withText("Delete")).perform(click());
        Thread.sleep(2000);
    }

// US 03.07.01 As an administrator I want to remove organizers that violate app policy.
    @Test
    public void testAdminRemoveFirstHost() throws InterruptedException {
        // Navigate to Admin Host page
        onView(withId(R.id.buttonHost)).perform(click());
        Thread.sleep(2000);

        // Click Delete on first host
        onView(withId(R.id.adminHostView))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0,
                        clickChildViewWithId(R.id.buttonDelete)));

        Thread.sleep(1000);

        // Confirm deletion
        onView(withText("Delete")).perform(click());
        Thread.sleep(2000);
    }
}
