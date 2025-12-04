package com.example.community;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class UserTest {

    private User user;
    private static final String TEST_DEVICE_TOKEN = "test_device_token_123";
    private static final String TEST_USER_ID = "user_123";
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PHONE = "123-456-7890";

    @Before
    public void setUp() {
        user = new User();                 // create a fresh User for each test

        // optional: if you actually need these values in other tests
        user.setDeviceToken(TEST_DEVICE_TOKEN);
        user.setUserID(TEST_USER_ID);
        user.setUsername(TEST_USERNAME);
        user.setEmail(TEST_EMAIL);
        user.setPhoneNumber(TEST_PHONE);
    }

    @Before
    public void SetUP() {
//        user = new User(TEST_USER_ID);
    }

    @Test
    public void testEnableNotificationsWhenEnabled() {
        user.setReceiveNotifications(true);
        user.enableNotifications();
        assertTrue("Notifications should remain enabled", user.getReceiveNotifications());
    }

    @Test
    public void testEnableNotificationsWhenDisabled() {
        user.setReceiveNotifications(false);
        user.enableNotifications();
        assertTrue("Notifications should be enabled", user.getReceiveNotifications());
    }

    @Test
    public void testDisableNotificationsWhenEnabled() {
        user.setReceiveNotifications(true);
        user.disableNotifications();
        assertFalse("Notifications should be disabled", user.getReceiveNotifications());
    }

    @Test
    public void testDisableNotificationsWhenDisabled() {
        user.setReceiveNotifications(false);
        user.disableNotifications();
        assertFalse("Notifications should remain disabled", user.getReceiveNotifications());
    }

    @Test
    public void testAddInterest() {
        user.addInterest("music");
        assertTrue("Should have music interest", user.getInterests().contains("music"));
        assertEquals("Should have 1 interest", 1, user.getInterests().size());
    }

    @Test
    public void testAddInterestDuplicateException() {
        user.addInterest("music");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            user.addInterest("music");
        });
    }

    @Test
    public void testRemoveInterest() {
        user.addInterest("music");
        user.removeInterest("music");
        assertFalse("Should not have music interest", user.getInterests().contains("music"));
        assertEquals("Should have 0 interests", 0, user.getInterests().size());
    }

    @Test
    public void testRemoveInterestNotFoundException() {
        user.addInterest("music");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            user.removeInterest("art");
        });
    }

    @Test
    public void testHasInterest() {
        user.addInterest("music");
        assertTrue("Should have music interest", user.hasInterest("music"));
        assertFalse("Should not have art interest", user.hasInterest("art"));
    }

    @Test
    public void testInterestNull() {
        user.addInterest(null);
        assertEquals("Should have 0 interests", 0, user.getInterests().size());
    }


    @Test
    public void testAddEventToWaitlist() {
        user.addEventToWaitlist("event_123");
        assertTrue("Should have event in waitlist", user.hasEventInWaitlist("event_123"));
        assertEquals("Should have 1 event in waitlist", 1, user.getWaitingListsJoinedIDs().size());
    }

    @Test
    public void testAddEventToWaitlistException() {
        user.addEventToWaitlist("event_123");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            user.addEventToWaitlist("event_123");
        });
    }

    @Test
    public void testRemoveEventFromWaitlist() {
        user.addEventToWaitlist("event_123");
        user.removeEventFromWaitingList("event_123");
        assertFalse("Should not have event in waitlist", user.hasEventInWaitlist("event_123"));
        assertEquals("Should have 0 events in waitlist", 0, user.getWaitingListsJoinedIDs().size());
    }

    @Test
    public void testRemoveEventFromWaitlistException() {
        user.addEventToWaitlist("event_123");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            user.removeEventFromWaitingList("event_456");
        });
    }

    @Test
    public void testHasEventInWaitlist() {
        user.addEventToWaitlist("event_123");
        assertTrue("Should have event in waitlist", user.hasEventInWaitlist("event_123"));
        assertFalse("Should not have event in waitlist", user.hasEventInWaitlist("event_456"));
    }

    @Test
    public void testAddEventToAttendingList() {
        user.addEventToWaitlist("event_123");
        user.addEventToAttendingList("event_123");
        assertTrue("Should have event in attending list", user.hasEventInAttendingList("event_123"));
        assertEquals("Should have 1 event in attending list", 1, user.getAttendingListsIDs().size());
    }

    @Test
    public void testAddEventToAttendingListException() {
        user.addEventToWaitlist("event_123");
        user.addEventToAttendingList("event_123");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            user.addEventToAttendingList("event_123");
        });
    }

    @Test
    public void testRemoveEventFromAttendingList() {
        user.addEventToWaitlist("event_123");
        user.addEventToAttendingList("event_123");
        user.removeEventFromAttendingList("event_123");
        assertFalse("Should not have event in attending list", user.hasEventInAttendingList("event_123"));
        assertEquals("Should have 0 events in attending list", 0, user.getAttendingListsIDs().size());
    }

    @Test
    public void testRemoveEventFromAttendingListException() {
        user.addEventToWaitlist("event_123");
        user.addEventToAttendingList("event_123");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            user.removeEventFromAttendingList("event_456");
        });
    }

    @Test
    public void testHasEventInAttendingList() {
        user.addEventToWaitlist("event_123");
        user.addEventToAttendingList("event_123");
        assertTrue("Should have event in attending list", user.hasEventInAttendingList("event_123"));
        assertFalse("Should not have event in attending list", user.hasEventInAttendingList("event_456"));
    }

    @Test
    public void testAddToRegistrationHistory() {
        user.addToRegistrationHistory("event_123");
        assertTrue("Should have event in registration history", user.hasEventInRegistrationHistory("event_123"));
        assertEquals("Should have 1 event in registration history", 1, user.getRegistrationHistoryIDs().size());
    }

    @Test
    public void testAddToRegistrationHistoryException() {
        user.addToRegistrationHistory("event_123");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            user.addToRegistrationHistory("event_123");
        });
    }

    @Test
    public void testHasEventInRegistrationHistory() {
        user.addToRegistrationHistory("event_123");
        assertTrue("Should have event in registration history", user.hasEventInRegistrationHistory("event_123"));
        assertFalse("Should not have event in registration history", user.hasEventInRegistrationHistory("event_456"));
    }

    @Test
    public void testHasPhoneNumberTrue() {
        user.setPhoneNumber("1234567890");
        assertTrue("Should have phone number", user.hasPhoneNumber());
    }

    @Test
    public void testHasPhoneNumberFalse() {
        user.setPhoneNumber(null);
        assertFalse("Should not have phone number", user.hasPhoneNumber());
    }

}
