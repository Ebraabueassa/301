package com.example.community;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static org.hamcrest.Matchers.equalTo;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;

import androidx.test.espresso.contrib.PickerActions;

import android.widget.DatePicker;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.example.community.MainActivity;

@RunWith(AndroidJUnit4.class)
public class OrganizerTests {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testFullOrganizerCreateEventFlow() throws Exception {

        // --- Step 1: Splash + Login ---
        Thread.sleep(3000);
        onView(withId(R.id.loginButton)).perform(click());
        Thread.sleep(3000);
        onView(withId(R.id.buttonHost)).perform(click());
        Thread.sleep(3000);

        // --- Step 2: Create Profile (Mandatory) ---
        onView(withId(R.id.buttonMyProfile)).perform(click());
        Thread.sleep(3000);
        onView(withId(R.id.name_box))
                .perform(typeText("TestFirstName"), closeSoftKeyboard());
        onView(withId(R.id.email_box))
                .perform(typeText("test@example.com"), closeSoftKeyboard());
        onView(withId(R.id.phone_box))
                .perform(typeText("7801234567"), closeSoftKeyboard());
        Thread.sleep(3000);

        onView(withId(R.id.save_button)).perform(click());
        Thread.sleep(2000);

        // --- Step 3: Click Create Event ---
        onView(withId(R.id.buttonCreate)).perform(click());
        Thread.sleep(1500);

        // --- Step 4: Fill Out Event Form ---
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

        // US 02.02.03 - enable geolocation
        onView(withId(R.id.checkboxGeolocationRequired)).perform(click());

        // --- Event Start Date ---
        onView(withId(R.id.inputEventStart)).perform(click());
        onView(withClassName(equalTo(DatePicker.class.getName())))
                .perform(PickerActions.setDate(2025, 1, 15));
        onView(withId(android.R.id.button1)).perform(click());
        Thread.sleep(1000);

        // --- Event End Date ---
        onView(withId(R.id.inputEventEnd)).perform(click());
        onView(withClassName(equalTo(DatePicker.class.getName())))
                .perform(PickerActions.setDate(2025, 1, 19));
        onView(withId(android.R.id.button1)).perform(click());
        Thread.sleep(1000);

        // US 02.01.04 - Registration period
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

        // --- Step 5: Submit Event ---
        onView(withId(R.id.buttonSubmit))
                .perform(scrollTo(), click());

        Thread.sleep(5000);

        // --- Step 6: Click on the event created ---
        onView(withId(R.id.HostEventView))
                .perform(actionOnItem(
                        hasDescendant(withText("Winter Cup Tournament")),
                        click()
                ));

        // --- Step 7: Click buttons in event details ---
        Thread.sleep(3000);

        clickButton(R.id.viewWaitlistButton);
        Thread.sleep(3000);// US 02.02.01 As an organizer I want to view the list of entrants who joined my event waiting list
        onView(withText("CLOSE"))
                .perform(click());
        Thread.sleep(3000);


        clickButton(R.id.viewInvitedButton);//US 02.06.01 As an organizer I want to view a list of all chosen entrants who are invited to apply.

        Thread.sleep(3000);
        onView(withText("CLOSE"))
                .perform(click());
        Thread.sleep(3000);

        clickButton(R.id.viewCancelledButton);          // US 02.06.02 As an organizer I want to see a list of all the cancelled entrants.
        Thread.sleep(3000);
        onView(withText("CLOSE"))
                .perform(click());
        Thread.sleep(3000);

        clickButton(R.id.viewAttendeesButton);          // US 02.06.03 As an organizer I want to see a final list of entrants who enrolled for the event.
        Thread.sleep(3000);
        onView(withText("CLOSE"))
                .perform(click());
        Thread.sleep(3000);

        clickButton(R.id.viewDeclinedButton);           // Cancelled/Declined
        Thread.sleep(3000);
        onView(withText("CLOSE"))
                .perform(click());
        Thread.sleep(3000);

        clickButton(R.id.runLotteryButton);             // Run Lottery

        Thread.sleep(3000);
        onView(withText("CANCEL"))
                .perform(click());
        Thread.sleep(3000);
        clickButton(R.id.exportAttendeesButton);        // US 02.06.05 As an organizer I want to export a final list of entrants who enrolled for the event in CSV format.
        Thread.sleep(3000);
        clickButton(R.id.organizerEventDescriptionBackButton);



        // --- Step 1: Click the Notify button ---
        onView(withId(R.id.buttonNotify))
                .perform(click());

        Thread.sleep(3000);

        // --- Step 2: Select the event "Winter Cup Tournament" ---
        onView(withId(R.id.notifyEventRecyclerView))
                .check(matches(hasDescendant(withText("Winter Cup Tournament"))))
                .perform(actionOnItem(
                        hasDescendant(withText("Winter Cup Tournament")),
                        click()));
        Thread.sleep(3000);


    }

    // --- Helper function for scrolling to and clicking buttons ---
    private void clickButton(int buttonId) {
        onView(withId(buttonId))
                .perform(scrollTo(), click());
    }

}
