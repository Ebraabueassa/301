package com.example.community;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class EntrantEventHistory {

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

        // Login button
        onView(withId(R.id.loginButton)).check(matches(isDisplayed()));
        clickAndWait(R.id.loginButton);

        // Select user
        onView(withId(R.id.buttonUser)).check(matches(isDisplayed()));
        clickAndWait(R.id.buttonUser);

        // Now we are on the entrant home/event list page
    }
 //US 01.02.03 As an entrant, I want to have a history of events I have registered for, whether I was selected or not.
    @Test
    public void testViewEventHistory() throws InterruptedException {
        // Click the "Event History" button
        onView(withId(R.id.event_history)).check(matches(isDisplayed()));
        clickAndWait(R.id.event_history);

    }
}
