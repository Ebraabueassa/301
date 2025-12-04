package com.example.community;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.espresso.contrib.RecyclerViewActions;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class TotalEntrantsWaitlistTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    /**
     * Helper method to click a view and wait for 3 seconds
     */
    private void clickAndWait(int viewId) throws InterruptedException {
        onView(withId(viewId)).perform(click());
        Thread.sleep(3000); // 3-second wait
    }

    @Before
    public void navigateToUserHome() throws InterruptedException {
        // Wait for splash screen
        Thread.sleep(3000);

        // Click Login button
        onView(withId(R.id.loginButton)).check(matches(isDisplayed()));
        clickAndWait(R.id.loginButton);

        // Select user type
        onView(withId(R.id.buttonUser)).check(matches(isDisplayed()));
        clickAndWait(R.id.buttonUser);

        // Now we are on the entrant home/event list page
    }
        //US 01.05.04 As an entrant, I want to know how many total entrants are on the waiting list for an event.
    @Test
    public void testNavigateToEventDetails() throws InterruptedException {
        // Click the first event in the RecyclerView to go to the event details page
        onView(withId(R.id.event_list))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        Thread.sleep(3000);


    }
}
