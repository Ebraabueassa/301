package com.example.community;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class EventTest {

    private Event event;
    private static final String USER_A = "userA";
    private static final String USER_B = "userB";

    @Before
    public void setUp() {
        event = new Event();
    }

    @Test
    public void testAddUserToWaitlist() {
        event.addUserToWaitlist(USER_A);
        assertTrue("Should have user in waitlist", event.hasUserInWaitlist(USER_A));
        assertEquals("Should have 1 user in waitlist", 1, event.getWaitListUserIDs().size());
    }

    @Test
    public void testAddUserToWaitlistException() {
        event.addUserToWaitlist(USER_A);
        assertThrows(IllegalArgumentException.class, () -> event.addUserToWaitlist(USER_A));
    }

    @Test
    public void testRemoveUserFromWaitlist() {
        event.addUserToWaitlist(USER_A);
        event.removeUserFromWaitlist(USER_A);
        assertFalse("Should not have user in waitlist", event.hasUserInWaitlist(USER_A));
        assertEquals("Should have 0 users in waitlist", 0, event.getWaitListUserIDs().size());
    }

    @Test
    public void testRemoveUserFromWaitlistException() {
        event.addUserToWaitlist(USER_A);
        assertThrows(IllegalArgumentException.class, () -> event.removeUserFromWaitlist(USER_B));
    }

    @Test
    public void testAddUserToInvitedList() {
        event.addUserToInvitedList(USER_A);
        assertTrue("Should have user in invited list", event.hasUserInInvitedList(USER_A));
        assertEquals("Should have 1 user in invited list", 1, event.getInvitedListUserIDs().size());
    }

    @Test
    public void testAddUserToInvitedListException() {
        event.addUserToInvitedList(USER_A);
        assertThrows(IllegalArgumentException.class, () -> event.addUserToInvitedList(USER_A));
    }

    @Test
    public void testRemoveUserFromInvitedList() {
        event.addUserToInvitedList(USER_A);
        event.removeUserFromInvitedList(USER_A);
        assertFalse("Should not have user in invited list", event.hasUserInInvitedList(USER_A));
        assertEquals("Should have 0 users in invited list", 0, event.getInvitedListUserIDs().size());
    }

    @Test
    public void testRemoveUserFromInvitedListException() {
        event.addUserToInvitedList(USER_A);
        assertThrows(IllegalArgumentException.class, () -> event.removeUserFromInvitedList(USER_B));
    }

    @Test
    public void testAddUserToAttendeeList() {
        event.addUserToAttendeeList(USER_A);
        assertTrue("Should have user in attendee list", event.hasUserInAttendeeList(USER_A));
        assertEquals("Should have 1 user in attendee list", 1, event.getAttendeeListUserIDs().size());
    }

    @Test
    public void testAddUserToAttendeeListException() {
        event.addUserToAttendeeList(USER_A);
        assertThrows(IllegalArgumentException.class, () -> event.addUserToAttendeeList(USER_A));
    }

    @Test
    public void testRemoveUserFromAttendeeList() {
        event.addUserToAttendeeList(USER_A);
        event.removeUserFromAttendeeList(USER_A);
        assertFalse("Should not have user in attendee list", event.hasUserInAttendeeList(USER_A));
        assertEquals("Should have 0 users in attendee list", 0, event.getAttendeeListUserIDs().size());
    }

    @Test
    public void testRemoveUserFromAttendeeListException() {
        event.addUserToAttendeeList(USER_A);
        assertThrows(IllegalArgumentException.class, () -> event.removeUserFromAttendeeList(USER_B));
    }

    @Test
    public void testAddUserToCancelledList() {
        event.addUserToCancelledList(USER_A);
        assertTrue("Should have user in cancelled list", event.hasUserInCancelledList(USER_A));
        assertEquals("Should have 1 user in cancelled list", 1, event.getCancelledListUserIDs().size());
    }

    @Test
    public void testAddUserToCancelledListException() {
        event.addUserToCancelledList(USER_A);
        assertThrows(IllegalArgumentException.class, () -> event.addUserToCancelledList(USER_A));
    }

    @Test
    public void testRemoveUserFromCancelledList() {
        event.addUserToCancelledList(USER_A);
        event.removeUserFromCancelledList(USER_A);
        assertFalse("Should not have user in cancelled list", event.hasUserInCancelledList(USER_A));
        assertEquals("Should have 0 users in cancelled list", 0, event.getCancelledListUserIDs().size());
    }

    @Test
    public void testRemoveUserFromCancelledListException() {
        event.addUserToCancelledList(USER_A);
        assertThrows(IllegalArgumentException.class, () -> event.removeUserFromCancelledList(USER_B));
    }
}
