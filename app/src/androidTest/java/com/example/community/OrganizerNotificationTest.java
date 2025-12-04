package com.example.community;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static org.hamcrest.Matchers.equalTo;

import androidx.test.espresso.contrib.PickerActions;
import android.widget.DatePicker;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class OrganizerNotificationTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    // --- Runs before each test ---
    @Before
    public void navigateToNotifyScreenAndCreateEvent() throws InterruptedException {
        // Wait for splash screen to finish
        Thread.sleep(3000);

        // --- Login ---
        onView(withId(R.id.loginButton)).perform(click());
        Thread.sleep(3000);

        // --- Choose Host role ---
        onView(withId(R.id.buttonHost)).perform(click());
        Thread.sleep(3000);

        // --- Create Organizer Profile ---
        onView(withId(R.id.buttonMyProfile)).perform(click());
        Thread.sleep(3000);
        onView(withId(R.id.name_box)).perform(typeText("TestFirstName"), closeSoftKeyboard());
        onView(withId(R.id.email_box)).perform(typeText("test@example.com"), closeSoftKeyboard());
        onView(withId(R.id.phone_box)).perform(typeText("7801234567"), closeSoftKeyboard());
        Thread.sleep(3000);
        onView(withId(R.id.save_button)).perform(click());
        Thread.sleep(2000);

        // --- Create Event "Winter Cup Tournament" ---
        onView(withId(R.id.buttonCreate)).perform(click());
        Thread.sleep(1500);

        onView(withId(R.id.inputEventName))
                .perform(typeText("Winter Cup Tournament"), closeSoftKeyboard());
        onView(withId(R.id.inputDescription))
                .perform(typeText("Annual winter cup for all Alberta entrants."), closeSoftKeyboard());
        onView(withId(R.id.inputEventLocation))
                .perform(typeText("Edmonton Community Arena"), closeSoftKeyboard());
        onView(withId(R.id.inputMaxParticipants))
                .perform(typeText("50"), closeSoftKeyboard());
        onView(withId(R.id.inputWaitingListSize))
                .perform(typeText("30"), closeSoftKeyboard());

        // Enable geolocation
        onView(withId(R.id.checkboxGeolocationRequired)).perform(click());

        // Event Start Date
        onView(withId(R.id.inputEventStart)).perform(click());
        onView(withClassName(equalTo(DatePicker.class.getName())))
                .perform(PickerActions.setDate(2025, 1, 15));
        onView(withId(android.R.id.button1)).perform(click());
        Thread.sleep(1000);

        // Event End Date
        onView(withId(R.id.inputEventEnd)).perform(click());
        onView(withClassName(equalTo(DatePicker.class.getName())))
                .perform(PickerActions.setDate(2025, 1, 19));
        onView(withId(android.R.id.button1)).perform(click());
        Thread.sleep(1000);

        // Registration period
        onView(withId(R.id.inputRegistrationStart)).perform(click());
        onView(withClassName(equalTo(DatePicker.class.getName())))
                .perform(PickerActions.setDate(2025, 1, 1));
        onView(withId(android.R.id.button1)).perform(click());
        Thread.sleep(1000);

        onView(withId(R.id.inputRegistrationEnd)).perform(click());
        onView(withClassName(equalTo(DatePicker.class.getName())))
                .perform(PickerActions.setDate(2025, 1, 10));
        onView(withId(android.R.id.button1)).perform(click());
        Thread.sleep(3000);

        // Submit Event
        onView(withId(R.id.buttonSubmit)).perform(scrollTo(), click());
        Thread.sleep(5000);

        // --- Go to Notify Screen ---
        onView(withId(R.id.buttonNotify)).perform(click());
        Thread.sleep(3000);
    }

    @Test
    public void testNotifyEventFlow() throws InterruptedException {

        // --- Step 1: Select the event "Winter Cup Tournament" and send notifications ---

        // To WAITING list
        onView(withId(R.id.notifyEventRecyclerView))
                .check(matches(hasDescendant(withText("Winter Cup Tournament"))))
                .perform(actionOnItem(
                        hasDescendant(withText("Winter Cup Tournament")),
                        click()));
        onView(withText("SEND TO WAITING LIST ENTRANTS")).perform(click());
        onView(withId(R.id.inputNotificationTitle))
                .perform(typeText("Test Notification Title"), closeSoftKeyboard());
        onView(withId(R.id.inputNotifyMessage))
                .perform(typeText("This is a test notification message."), closeSoftKeyboard());
        Thread.sleep(1000);
        onView(withId(R.id.buttonSend)).perform(click());
        Thread.sleep(3000);

        // To INVITED list
        onView(withId(R.id.notifyEventRecyclerView))
                .perform(actionOnItem(
                        hasDescendant(withText("Winter Cup Tournament")),
                        click()));
        onView(withText("SEND TO INVITED ENTRANTS")).perform(click());
        onView(withId(R.id.inputNotificationTitle))
                .perform(typeText("Test Notification Title"), closeSoftKeyboard());
        onView(withId(R.id.inputNotifyMessage))
                .perform(typeText("This is a test notification message."), closeSoftKeyboard());
        Thread.sleep(1000);
        onView(withId(R.id.buttonSend)).perform(click());
        Thread.sleep(3000);

        // To CANCELLED list
        onView(withId(R.id.notifyEventRecyclerView))
                .perform(actionOnItem(
                        hasDescendant(withText("Winter Cup Tournament")),
                        click()));
        onView(withText("SEND TO CANCELLED ENTRANTS")).perform(click());
        onView(withId(R.id.inputNotificationTitle))
                .perform(typeText("Test Notification Title"), closeSoftKeyboard());
        onView(withId(R.id.inputNotifyMessage))
                .perform(typeText("This is a test notification message."), closeSoftKeyboard());
        Thread.sleep(1000);
        onView(withId(R.id.buttonSend)).perform(click());
        Thread.sleep(1000);
        onView(withText("Back to Main Menu")).perform(click());
        Thread.sleep(3000);

        // --- US 02.02.02: View geolocation of entrants in event waiting list ---

        // Click the Geolocation button
        onView(withId(R.id.buttonGeolocation))
                .perform(click());

        Thread.sleep(3000);

        // Select the "Winter Cup Tournament" from the RecyclerView
        onView(withId(R.id.geolocationEventList))
                .perform(actionOnItem(
                        hasDescendant(withText("Winter Cup Tournament")),
                        click()));

        // At this point, the app should open the map for the selected event
        Thread.sleep(1000);

        onView(withText("Back")).perform(click());



    }
}
