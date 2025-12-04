package com.example.community;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.sql.Timestamp;

public class WaitingListEntryTest {

    @Test
    public void testHasStatus() {
        WaitingListEntry entry = new WaitingListEntry();
        entry.setStatus(EntryStatus.INVITED);

        assertTrue(entry.hasStatus(EntryStatus.INVITED));
        assertFalse(entry.hasStatus(EntryStatus.ACCEPTED));
    }

    @Test
    public void testMarkAsJoined() {
        WaitingListEntry entry = new WaitingListEntry();
        entry.markAsJoined();
        assertEquals(EntryStatus.WAITING, entry.getStatus());
    }

    @Test
    public void testMarkAsAccepted() {
        WaitingListEntry entry = new WaitingListEntry();
        entry.markAsAccepted();
        assertEquals(EntryStatus.ACCEPTED, entry.getStatus());
    }

    @Test
    public void testMarkAsDeclined() {
        WaitingListEntry entry = new WaitingListEntry();
        entry.markAsDeclined();
        assertEquals(EntryStatus.DECLINED, entry.getStatus());
    }

    @Test
    public void testMarkAsCancelled() {
        WaitingListEntry entry = new WaitingListEntry();
        entry.markAsCancelled();
        assertEquals(EntryStatus.CANCELLED, entry.getStatus());
    }

}


