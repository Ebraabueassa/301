package com.example.community;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Service class responsible for managing lotteries for events.
 * Handles selecting winners from a waitlist, marking them as invited,
 * and sending notifications to winners and losers.
 */
public class LotteryService {
    private final String TAG = "LotteryService";

    private WaitlistRepository waitlistRepository;
    private EventRepository eventRepository;

    private NotificationService notificationService;
    private WaitingListEntryService waitingListEntryService;

    /**
     * Constructs a new LotteryService and initializes required repositories and services.
     */
    public LotteryService() {
        this.waitlistRepository = new WaitlistRepository();
        this.eventRepository = new EventRepository();
        this.notificationService = new NotificationService();
        this.waitingListEntryService = new WaitingListEntryService();
    }

    /**
     * Runs a lottery for a specific event organized by a given organizer.
     * Selects a sample of waitlisted users, marks winners as invited, and sends notifications.
     *
     * @param organizerID The user ID of the organizer running the lottery
     * @param eventID     The ID of the event
     * @param sampleSize  The number of winners to select
     * @return A Task representing the asynchronous completion of the lottery process
     */
    public Task<Void> runLottery(String organizerID, String eventID, int sampleSize) {
        return eventRepository.getByID(eventID)
                .continueWithTask(eventTask -> {
                    Event event = eventTask.getResult();
                    if (event == null) {
                        return Tasks.forException(new IllegalArgumentException("Event not found"));
                    }
                    if (!event.getOrganizerID().equals(organizerID)) {
                        return Tasks.forException(new IllegalArgumentException("User is not organizer of event"));
                    }

                    Integer maxCapacity = event.getMaxCapacity();
                    Integer currentCapacity = event.getCurrentCapacity();
                    if (maxCapacity == null || currentCapacity == null){
                        return Tasks.forException(new IllegalArgumentException("Event capacity is not set"));
                    }

                    int availableSlots = maxCapacity - currentCapacity;
                    if (availableSlots <= 0) {
                        return Tasks.forException(new IllegalArgumentException("No available slots for event"));
                    }

                    if (sampleSize < 1) {
                        return Tasks.forException(new IllegalArgumentException("Sample size must be at least 1"));
                    }
                    if (sampleSize > availableSlots) {
                        return Tasks.forException(new IllegalArgumentException("Sample size must be less than or equal to available slots"));
                    }

                    return waitlistRepository.listByEventAndStatus(eventID, EntryStatus.WAITING)
                            .continueWithTask(entriesTask -> {
                                if (!entriesTask.isSuccessful()) {
                                    throw entriesTask.getException();
                                }
                                List<WaitingListEntry> waitingEntries = entriesTask.getResult();
                                if (waitingEntries == null || waitingEntries.isEmpty()) {
                                    return Tasks.forException(new IllegalArgumentException("No users on waitlist"));
                                }

                                int slotsToFill = Math.min(sampleSize, waitingEntries.size());
                                List<WaitingListEntry> lotteryWinners = selectLotteryWinners(waitingEntries, slotsToFill);
                                List<WaitingListEntry> lotteryLosers = getLotteryLosers(waitingEntries, lotteryWinners);

                                return markAsInvited(organizerID, eventID, lotteryWinners)
                                        .continueWithTask(inviteTask -> {
                                            if (!inviteTask.isSuccessful()) {
                                                throw inviteTask.getException();
                                            }
                                            return sendNotifications(lotteryWinners, lotteryLosers, eventID);
                                        });
                            });
                });
    }

    /**
     * Randomly selects winners from the provided list of entries.
     *
     * @param entriesList The list of entries to select from
     * @param slotsToFill The number of winners to select
     * @param <T>         The type of entries
     * @return A list containing the selected winners
     */
    private <T> List<T> selectLotteryWinners(List<T> entriesList, int slotsToFill) {
        if (slotsToFill >= entriesList.size()) {
            return new ArrayList<>(entriesList);
        }

        List<T> copy = new ArrayList<>(entriesList);
        Random random = new Random();

        for (int i = 0; i < slotsToFill; i++) {
            int randomIndex = i + random.nextInt(copy.size() - i);
            T temp = copy.get(i);
            copy.set(i, copy.get(randomIndex));
            copy.set(randomIndex, temp);
        }

        return copy.subList(0, slotsToFill);
    }

    /**
     * Returns a list of waitlist entries that were not selected as winners.
     *
     * @param waitingEntries The full list of waiting entries
     * @param lotteryWinners The selected winners
     * @return A list of losers
     */
    private List<WaitingListEntry> getLotteryLosers(List<WaitingListEntry> waitingEntries, List<WaitingListEntry> lotteryWinners) {
        List<WaitingListEntry> losers = new ArrayList<>(waitingEntries);
        losers.removeAll(lotteryWinners);
        return losers;
    }

    /**
     * Marks the provided lottery winners as invited in the backend.
     *
     * @param organizerID    The ID of the event organizer
     * @param eventID        The ID of the event
     * @param lotteryWinners List of winners to mark as invited
     * @return A Task representing the asynchronous completion of the invitations
     */
    private Task<Void> markAsInvited(String organizerID, String eventID, List<WaitingListEntry> lotteryWinners) {
        List<Task<Void>> inviteTasks = new ArrayList<>();
        for (WaitingListEntry entry : lotteryWinners) {
            inviteTasks.add(waitingListEntryService.invite(organizerID, eventID, entry.getUserID()));
        }
        return Tasks.whenAll(inviteTasks);
    }

    /**
     * Sends notifications to both winners and losers of the lottery.
     *
     * @param lotteryWinners The list of winners
     * @param lotteryLosers  The list of losers
     * @param eventID        The ID of the event
     * @return A Task representing the asynchronous completion of notifications
     */
    private Task<Void> sendNotifications(List<WaitingListEntry> lotteryWinners,
                                         List<WaitingListEntry> lotteryLosers,
                                         String eventID) {
        List<Task<Void>> notificationTasks = new ArrayList<>();
        notificationTasks.add(notificationService.notifyWinners(eventID, lotteryWinners));
        notificationTasks.add(notificationService.notifyLosers(eventID, lotteryLosers));
        return Tasks.whenAll(notificationTasks);
    }
}
