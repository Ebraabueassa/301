package com.example.community;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.espresso.action.ViewActions;
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
public class EntrantProfileTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    private void clickAndWait(int viewId) throws InterruptedException {
        onView(withId(viewId)).perform(ViewActions.click());
        Thread.sleep(3000);
    }

    @Before
    public void navigatePastSplashScreen() throws InterruptedException {
        Thread.sleep(3000);

        onView(withId(R.id.loginButton)).check(matches(isDisplayed()));
        clickAndWait(R.id.loginButton);
        Thread.sleep(3000);

        onView(withId(R.id.buttonUser)).check(matches(isDisplayed()));
        clickAndWait(R.id.buttonUser);
        Thread.sleep(3000);

        onView(withId(R.id.my_profile)).check(matches(isDisplayed()));
        clickAndWait(R.id.my_profile);
    }
 //US 01.02.01 As an entrant, I want to provide my personal information such as name, email and optional phone number in the app
    @Test
    public void testEnterPersonalInformation() throws InterruptedException {
        // Type "Josiah Daniel" in the name_box
        onView(withId(R.id.name_box))
                .perform(ViewActions.clearText(), ViewActions.typeText("Josiah Daniel"));
        Thread.sleep(3000);
        // Type "josiah@example.com" in the email_box
        onView(withId(R.id.email_box))
                .perform(ViewActions.clearText(), ViewActions.typeText("josiah@example.com"));
        Thread.sleep(3000);
        // Type "7801234567" in the phone_box
        onView(withId(R.id.phone_box))
                .perform(ViewActions.clearText(), ViewActions.typeText("7801234567"));
        Thread.sleep(3000);
        clickAndWait(R.id.save_button);
    }

    //US 01.02.02 As an entrant I want to update information such as name, email and contact information on my profile
    @Test
    public void testUpdatePersonalInformation() throws InterruptedException {
        // Step 1: Fill default data and save
        onView(withId(R.id.name_box))
                .perform(ViewActions.clearText(), ViewActions.typeText("Default Name"));
        onView(withId(R.id.email_box))
                .perform(ViewActions.clearText(), ViewActions.typeText("default@example.com"));
        onView(withId(R.id.phone_box))
                .perform(ViewActions.clearText(), ViewActions.typeText("7800000000"));
        clickAndWait(R.id.save_button);
        Thread.sleep(3000);
        // Step 2: Navigate back to profile
        onView(withId(R.id.my_profile)).check(matches(isDisplayed()));
        clickAndWait(R.id.my_profile);
        Thread.sleep(3000);
        // Step 3: Replace with new data
        onView(withId(R.id.name_box))
                .perform(ViewActions.clearText(), ViewActions.typeText("Josiah D."));
        onView(withId(R.id.email_box))
                .perform(ViewActions.clearText(), ViewActions.typeText("josiah.d@example.com"));
        onView(withId(R.id.phone_box))
                .perform(ViewActions.clearText(), ViewActions.typeText("7807654321"));
        Thread.sleep(3000);
        clickAndWait(R.id.save_button);


    }



    //US 01.02.04 As an entrant, I want to delete my profile if I no longer wish to use the app.
    @Test
    public void testDeleteProfile() throws InterruptedException {
        clickAndWait(R.id.delete_unity);
        Thread.sleep(3000);
        // Confirm deletion dialog if it appears
        onView(withText("Delete Unity")).perform(ViewActions.click());
        Thread.sleep(3000);

    }
}
