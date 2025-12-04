package com.example.community.Screens;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.community.ArrayAdapters.ImageArrayAdapter;
import com.example.community.ImageService;
import com.example.community.R;
import com.example.community.Image;
import java.util.ArrayList;
import com.example.community.Event;

/**
 * Fragment for admins to view and manage event poster images.
 * <p>
 *     Displays a list of all event poster images uploaded by organizers. Admins can view image details
 *     and perform deletion of images.
 * </p>
 * <p>
 *     Loads all events from the database, gets the poster image information, and displays them in a
 *     RecyclerView. When an admin clicks delete, a confirmation dialog is shown. If confirmed, the
 *     image is deleted from the database and the list is updated.
 * </p>
 *
 * @see ImageArrayAdapter
 * @see ImageService
 * @see Image
 * @see Event
 */
public class AdminImageFragment extends Fragment {

    /**
     * Button to go back to previous fragment
     */
    Button backButton;

    /**
     * List of poster images from the events
     */
    private ArrayList<com.example.community.Image> imagesArrayList;

    /**
     * Adapter for managing the RecyclerView items and delete button handling
     */
    private ImageArrayAdapter imageArrayAdapter;

    /**
     * Service for managing image data
     */
    private ImageService imageService;

    /**
     * RecyclerView displaying the list of eent poster images
     */
    private RecyclerView adminImageView;


    /**
     * Inflates the layout for this fragment
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return The View for the fragment's UI
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.admin_image_page, container, false);
    }

    /**
     * Initializes services and UI. Sets up the RecyclerView adapter with image delete listeners,
     * loads poster images, and sets up the back button listener.
     *
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adminImageView = view.findViewById(R.id.imageView);
        backButton = view.findViewById(R.id.buttonBack);

        imageService = new ImageService();
        imagesArrayList = new ArrayList<>();

        adminImageView.setLayoutManager(new LinearLayoutManager(getContext()));

        imageArrayAdapter = new ImageArrayAdapter(imagesArrayList, new ImageArrayAdapter.OnImageDeleteListener() {
            @Override
            public void onDeleteClick(Image image, int position) {
                onDeleteClicked(image, position);
            }
        });

        adminImageView.setAdapter(imageArrayAdapter);

        loadImages();
        setUpClickListener();
    }

    /**
     * Loads all event poster images and fills the RecyclerView
     * <p>
     *     How it works:
     *     <ul>
     *         <li>Queries all events from the Firestore</li>
     *         <li>For each event, if it has a poster image, it is added to the ArrayList</li>
     *         <li>The ArrayList is then passed to the adapter</li>
     *         <li>Notifies the adapter to refresh the RecyclerView</li>
     *     </ul>
     * </p>
     * Displays an error if event loading or image loading fails
     */
    private void loadImages() {
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    imagesArrayList.clear();

                    for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Event event = document.toObject(Event.class);

                            if (event.getPosterImageURL() != null && !event.getPosterImageURL().isEmpty()) {

                                Image img = new Image();
                                img.setImageID(event.getPosterImageID());
                                img.setImageURL(event.getPosterImageURL());

                                if (event.getOrganizerID() != null) {
                                    img.setUploadedBy(event.getOrganizerID());
                                } else {
                                    img.setUploadedBy("Unknown Organizer");
                                }

                                imagesArrayList.add(img);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing event for poster", e);
                        }
                    }

                    imageArrayAdapter.notifyDataSetChanged();

                    if (imagesArrayList.isEmpty()) {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "No event posters found.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading event posters", e);
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Failed to load images", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    /**
     * Sets up the back button listener to navigate back to the previous fragment
     */
    private void setUpClickListener() {
        backButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(AdminImageFragment.this).navigateUp();
        });
    }

    /**
     * Displays confirmation dialog and deltes a poster image froma an event
     * <p>
     *     How it works:
     *     <ul>
     *         <li>Shows a confirmation dialog asking the admin to confirm deletion</li>
     *         <li>Queries the Firestore for the event with the matching poster image ID</li>
     *         <li>Calls imageService to delete the event poster image from the event</li>
     *         <li>Removes the image from the ArrayList</li>
     *         <li>Notifies the adapter to refresh the RecyclerView</li>
     *     </ul>
     * </p>
     *
     * @param image the poster image to be deleted
     * @param position the position of the image in the RecyclerView list
     */
    public void onDeleteClicked(Image image, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Image")
                .setMessage("Are you sure you want to delete this image?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            .collection("events")
                            .whereEqualTo("posterImageID", image.getImageID())
                            .limit(1)
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                Event event = querySnapshot.getDocuments().get(0).toObject(Event.class);
                                    imageService.deleteEventPoster(event.getEventID())
                                            .addOnSuccessListener(aVoid -> {
                                                if (position >= 0 && position < imagesArrayList.size()) {
                                                    imagesArrayList.remove(position);
                                                    imageArrayAdapter.notifyItemRemoved(position);
                                                    imageArrayAdapter.notifyItemRangeChanged(position, imagesArrayList.size() - position);
                                                }
                                                Toast.makeText(getContext(), "Image deleted", Toast.LENGTH_SHORT).show();
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e(TAG, "Delete failed", e);
                                                Toast.makeText(getContext(), "Failed to delete image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
