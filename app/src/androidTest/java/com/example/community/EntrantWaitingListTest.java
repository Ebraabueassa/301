package com.example.community;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.Manifest;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import com.example.community.MainActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class EntrantWaitingListTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    // Auto-grant geolocation permissions (avoids system popup)
    @Rule
    public GrantPermissionRule permissionRule =
            GrantPermissionRule.grant(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            );

    @Before
    public void setupEntrant() throws Exception {
        Thread.sleep(3000);

        onView(withId(R.id.loginButton)).perform(click());
        Thread.sleep(3000);

        onView(withId(R.id.buttonUser)).perform(click());
        Thread.sleep(3000);
    }

    // -------------------------------------------------------------
    // US 01.01.01 — Join the waiting list
    // -------------------------------------------------------------
    @Test
    public void testJoinWaitingList() throws Exception {

        // US 01.01.03 — List of joinable events
        onView(withId(R.id.event_list))
                .perform(actionOnItemAtPosition(0, click()));
        Thread.sleep(2000);

        // Join waiting list
        onView(withId(R.id.waitlistButton))
                .perform(click());
        Thread.sleep(2000);

        //  US 01.01.02 As an entrant, I want to leave the waiting list for a specific event
        onView(withId(R.id.waitlistButton))
                .perform(click());
        Thread.sleep(2000);


    }


}
