package com.example.community;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class EntrantHomePageTest {

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

    // US 01.01.04 - Filter events based on name
    @Test
    public void testOpenFilterAndApply() throws InterruptedException {
        // Click the Filter button to open the filter page
        onView(withId(R.id.filterButton)).check(matches(isDisplayed()));
        clickAndWait(R.id.filterButton);

        // Type the first name of the event into the keyword EditText
        onView(withId(R.id.inputKeyword))
                .perform(androidx.test.espresso.action.ViewActions.typeText("EventFirstName"));

        // Press the Apply button
        onView(withId(R.id.buttonApplyFilter)).perform(click());
        Thread.sleep(3000);

    }

    // US 01.05.05 - Open guide
    @Test
    public void testOpenGuide() throws InterruptedException {
        // Click the Guide button
        onView(withId(R.id.guideButton)).check(matches(isDisplayed()));
        clickAndWait(R.id.guideButton);

        // Verify that the guide screen is displayed
        // Example: verify a view in UserGuideFragment is displayed
        // onView(withId(R.id.guide_title)).check(matches(isDisplayed()));
    }
}
