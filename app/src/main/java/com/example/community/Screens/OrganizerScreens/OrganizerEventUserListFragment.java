package com.example.community. Screens.OrganizerScreens;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.community.ArrayAdapters.UserArrayAdapter;
import com.example.community.EventService;
import com.example.community.LotteryService;
import com.example.community.R;
import com.example.community.User;
import com.example.community.UserService;
import com.example.community.WaitingListEntry;
import com.example.community.WaitingListEntryService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * DialogFragment that displays list of event entrants filtered by their status.
 * <p>
 *     Fragment shows entrants of an event in various categories:
 *     <ul>
 *         <li>Waitlist</li>
 *         <li>Invited</li>
 *         <li>Attendees</li>
 *         <li>Cancelled</li>
 *         <li>Declined</li>
 *     </ul>
 *     Provides organizers with user information and allows the cancellation of entrants
 *     that have been invited. Cancellation triggers a re-run of the lottery.
 *     Fragment receives event ID and list type through navigation argument
 * </p>
 *
 * @see UserArrayAdapter
 * @see WaitingListEntryService
 *
 */
public class OrganizerEventUserListFragment extends DialogFragment {

    /** Tag for logging */
    public static final String TAG = "OrganizerEventUserListFragment";

    /** Argument for event id to pass into fragment's arguments bundle */
    private static final String ARG_EVENT_ID = "event_id";

    /** Argument for list type to pass into fragment's arguments bundle */
    private static final String ARG_LIST_TYPE = "list_type";

    /** ID of the event whose user list is displayed */
    private String eventId;

    /** Type of list to display: "waitlist", "invited", "attendees", "cancelled", "declined" */
    private String listType;

    /** List of users currently displayed */
    public List<User> usersList;
    /**
     * List of waiting list entries corresponding to the users in the list.
     * Used for operations like cancelling invites.
     */
    private List<WaitingListEntry> waitingListEntries;
    /**
     * Set of user IDS that have been selected by organizer when on invited list type
     */
    private Set<String> selectedUserIds;

    /** RecyclerView and adapter for displaying the user list */
    private RecyclerView userListRecyclerView;
    private UserArrayAdapter userArrayAdapter;

    /** UI elements */
    private TextView listTitle;
    private Button closeListButton;
    private Button cancelUsersButton;

    /** Services for accessing waiting list entries and user data */
    private WaitingListEntryService waitingListEntryService;

    /**Service for managing user data */
    private UserService userService;

    /** Service for managing event data */
    private EventService eventService;
    /** Service for managing lottery operations */
    private LotteryService lotteryService;

    /**
     * creates a new instance of the fragment with the given event ID and list type
     *
     * @param eventId the identifier of the event
     * @param listType type of list to be displayed
     *
     * @return configured OrganizerEventUserListFragment
     */
    public static OrganizerEventUserListFragment newInstance(String eventId, String listType) {
        OrganizerEventUserListFragment fragment = new OrganizerEventUserListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        args.putString(ARG_LIST_TYPE, listType);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Inflates the dialog's layout view
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return the view of the fragment's layout
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.organizer_event_user_lists_dialog, container, false);
        return view;
    }

    /**
     * Initializes the fragment's UI elements and services. Sets up the RecyclerView
     * adapter with user click listeners, and loads the appropriate list of users
     * based on the list type.
     *
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        waitingListEntryService = new WaitingListEntryService();
        userService = new UserService();
        eventService = new EventService();
        lotteryService = new LotteryService();

        listTitle = view.findViewById(R.id.listTitle);
        userListRecyclerView = view.findViewById(R.id.userListRecyclerView);
        closeListButton = view.findViewById(R.id.closeListButton);
        cancelUsersButton = view.findViewById(R.id.cancelUsersButton);

        usersList = new ArrayList<>();
        waitingListEntries = new ArrayList<>();
        selectedUserIds = new HashSet<>();


        if (getArguments() != null) {
            eventId = getArguments().getString(ARG_EVENT_ID);
            listType = getArguments().getString(ARG_LIST_TYPE);
        }

        userArrayAdapter = new UserArrayAdapter(usersList, listType);
        userArrayAdapter.setSelectionListener(((userId, selected) -> onUserSelectionChanged(userId, selected)));
        userListRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        userListRecyclerView.setAdapter(userArrayAdapter);

        closeListButton.setOnClickListener(v -> dismiss());

        cancelUsersButton.setOnClickListener(v -> cancelSelectedUsers());



        loadUsersList();

    }

    /**
     * Loads the appropriate list of users based on the list type.
     * Sets the title of the list and the visibility of the cancel users button if
     * the list type is "invited". For every other list type the cancel button is hidden.
     */
    private void loadUsersList() {
        if (eventId == null || listType == null) {
            Toast.makeText(getContext(), "Parameters received are invalid", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        switch(listType) {
            case "waitlist":
                listTitle.setText("Waitlist");
                cancelUsersButton.setVisibility(View.GONE);
                loadWaitlistUsers();
                break;
            case "invited":
                listTitle.setText("Invited");
                cancelUsersButton.setVisibility(View.VISIBLE);
                loadInvitedUsers();
                break;
            case "attendees":
                listTitle.setText("Attendees");
                cancelUsersButton.setVisibility(View.GONE);
                loadAttendeesUsers();
                break;
            case "cancelled":
                listTitle.setText("Cancelled");
                cancelUsersButton.setVisibility(View.GONE);
                loadCancelledUsers();
                break;
            case "declined":
                listTitle.setText("Declined");
                cancelUsersButton.setVisibility(View.GONE);
                loadDeclinedUsers();
                break;
            default:
                Toast.makeText(getContext(), "List Type not valid", Toast.LENGTH_SHORT).show();
                dismiss();
        }
    }

    /**
     * Loads the waitlisted entrants of the event
     */
    private void loadWaitlistUsers() {
        waitingListEntryService.getWaitlistEntries(eventId)
                .addOnSuccessListener(entries -> {
                    loadUsers(entries);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load waitlist entries", e);
                    dismiss();
                });
    }

    /**
     * Loads the invited entrants of the event
     */
    private void loadInvitedUsers() {
        waitingListEntryService.getInvitedList(eventId)
                .addOnSuccessListener(entries -> {
                    waitingListEntries.clear();
                    waitingListEntries.addAll(entries);
                    loadUsers(entries);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load invited entries", e);
                    dismiss();
                });
    }

    /**
     * Loads the confirmed attendees of the event
     */
    private void loadAttendeesUsers() {
        waitingListEntryService.getAcceptedList(eventId)
                .addOnSuccessListener(entries -> {
                    loadUsers(entries);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load attendees entries", e);
                    dismiss();
                });

    }

    /**
     * Loads the cancelled entrants of the event
     */
    private void loadCancelledUsers() {
        waitingListEntryService.getCancelledList(eventId)
                .addOnSuccessListener(entries -> {
                    loadUsers(entries);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load cancelled entries", e);
                    dismiss();
                });
    }

    /**
     * Loads the declined entrants of the event
     */
    private void loadDeclinedUsers() {
        waitingListEntryService.getDeclinedList(eventId)
                .addOnSuccessListener(entries -> {
                    waitingListEntries.clear();
                    waitingListEntries.addAll(entries);
                    loadUsers(entries);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load declined entries", e);
                    dismiss();
                });
    }

    /**
     * Converts the entries to user objects and loads them into RecyclerView
     *
     * @param entries the waiting list entries to convert to users
     */
    private void loadUsers(List<WaitingListEntry> entries) {
        usersList.clear();
        selectedUserIds.clear();

        if (entries.isEmpty()) {
            userArrayAdapter.notifyDataSetChanged();
            return;
        }

        int[] loadedUsersCount = {0};

        /**
         * For each entry, get the user object and add it to the list
         */
        for (WaitingListEntry entry : entries) {
            userService.getByUserID(entry.getUserID())
                    .addOnSuccessListener(user -> {
                        usersList.add(user);
                        loadedUsersCount[0]++;

                        // If all users are loaded, notify adapter
                        if (loadedUsersCount[0] == entries.size()) {
                            userArrayAdapter.notifyDataSetChanged();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to load user", e);
                        loadedUsersCount[0]++;

                        if (loadedUsersCount[0] == entries.size()) {
                            userArrayAdapter.notifyDataSetChanged();
                        }
                    });
        }
    }

    /**
     * Callback from adapter when user checkbox is toggled.
     * Adds user ID to selectedUserIds if selected, removes it if deselected.
     *
     * @param userId ID of the user whose selection changed
     * @param selected true if the user is selected, false otherwise
     */
    public void onUserSelectionChanged(String userId, boolean selected) {
        if (selected) {
            selectedUserIds.add(userId);
        } else {
            selectedUserIds.remove(userId);
        }
    }

    /**
     * Cancels all selected users from the invited list
     * <p>
     *     How it works:
     *     <ul>
     *         <li>Validates that at least one user is selected</li>
     *         <li>Finds the waiting list entries for the selected users</li>
     *         <li>Calls the service to cancel each selected invitation</li>
     *         <li>Re-runs the lottery selection with the number of cancelled users</li>
     *         <li>NReloads invited users list and clears selection</li>
     *     </ul>
     * </p>
     */
    private void cancelSelectedUsers() {
        if (selectedUserIds.isEmpty()) {
            Toast.makeText(getContext(), "Please select at least one user", Toast.LENGTH_SHORT).show();
            return;
        }

        // Find the waitlist entries for the selected users
        List<WaitingListEntry> entriesToCancel = new ArrayList<>();
        for (WaitingListEntry entry : waitingListEntries) {
            if (selectedUserIds.contains(entry.getUserID())) {
                entriesToCancel.add(entry);
            }
        }

        // Cancel each entry
        int[] cancelledCount = {0};
        int totalToCancel = entriesToCancel.size();

        for (WaitingListEntry entry : entriesToCancel) {
            waitingListEntryService.cancelInvite(entry.getUserID(), eventId)
                    .addOnSuccessListener(v -> {
                        cancelledCount[0]++;
                        if (cancelledCount[0] == totalToCancel) {
                            Toast.makeText(getContext(), "Selected users cancelled successfully", Toast.LENGTH_SHORT).show();
                            runLotteryAgain(totalToCancel);
                            selectedUserIds.clear();
                            loadInvitedUsers();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to cancel user", e);
                        cancelledCount[0]++;
                        if (cancelledCount[0] == totalToCancel) {
                            Toast.makeText(getContext(), "Error cancelling some users", Toast.LENGTH_SHORT).show();
                            selectedUserIds.clear();
                            loadInvitedUsers();
                        }
                    });
        }
    }

    /**
     * Re-runs the lottery selection with the number of cancelled users
     *
     * @param sampleSize the number of spots to fill from the waitlist
     */
    private void runLotteryAgain(int sampleSize) {
        eventService.getOrganizerID(eventId)
                .addOnSuccessListener(organizerID -> {
                    lotteryService.runLottery(organizerID, eventId, sampleSize)
                            .addOnSuccessListener(v -> {
                                Log.d(TAG, "Lottery ran successfully");
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to run lottery", e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get organizer ID", e);
                });
    }

    /**
     * Called when the dialog is opened. Configures the dialog window size
     */
    @Override
    public void onStart() {
        super.onStart();
        // Set dialog size
        if (getDialog() != null && getDialog().getWindow() != null) {
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.75);
            getDialog().getWindow().setLayout(width, height);
        }
    }
}