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

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminBrowseTests {

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    private void clickAndWait(int id) throws InterruptedException {
        onView(withId(id)).perform(click());
        Thread.sleep(2000);
    }

    @Before
    public void navigateToAdminHome() throws InterruptedException {
        // Wait for splash screen
        Thread.sleep(3000);

        // Click Login
        clickAndWait(R.id.loginButton);
        Thread.sleep(3000);
        // Click Admin selection button
        clickAndWait(R.id.buttonAdmin);
        Thread.sleep(3000);

    }

    @Test
    public void testAdminBrowseEvents() throws InterruptedException {
        clickAndWait(R.id.buttonEvent);
    }

    @Test
    public void testAdminBrowseProfiles() throws InterruptedException {
        clickAndWait(R.id.buttonProfile);
    }

    @Test
    public void testAdminBrowseImages() throws InterruptedException {
        clickAndWait(R.id.buttonImage);
    }

    // US 03.08.01 As an administrator, I want to review logs of all notifications sent to entrants by organizers.
    @Test
    public void testAdminReviewNotificationLogs() throws InterruptedException {
        // Navigate to Notifications Log page
        clickAndWait(R.id.buttonNotification); // replace with actual button ID

        Thread.sleep(2000); // wait for page to load

    }

}
