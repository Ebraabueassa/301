package com.example.community;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.List;

/**
 * Service layer for image operations.
 * Handles business logic for uploading and managing event images.
 */
public class ImageService {
    private final ImageRepository imageRepository;
    private final EventRepository eventRepository;

    /**
     * Creates a new ImageService instance.
     * Initializes required repositories.
     */
    public ImageService() {
        this.imageRepository = new ImageRepository();
        this.eventRepository = new EventRepository();
    }

    /**
     * Uploads event poster and updates the Event document.
     * US 02.04.01
     *
     * @param eventID    The event ID
     * @param imageData  The image bytes
     * @param uploadedBy User ID who is uploading the poster
     * @return Task that resolves to Image object with storagePath and download URL
     */
    public Task<Image> uploadEventPoster(String eventID, byte[] imageData, String uploadedBy, Boolean deleteOldPoster) {
        // Validate input
        if (imageData == null || imageData.length == 0) {
            throw new IllegalArgumentException("Image data cannot be null or empty");
        }

        if (deleteOldPoster) {
            return deleteEventPoster(eventID)
                    .continueWithTask(task -> uploadNewPoster(eventID, imageData, uploadedBy));
        } else {
            return uploadNewPoster(eventID, imageData, uploadedBy);
        }
    }

//    old version below
//    /**
//     * Uploads event poster and updates the Event document.
//     * US 02.04.01
//     *
//     * @param eventID    The event ID
//     * @param imageData  The image bytes
//     * @param uploadedBy User ID who is uploading the poster
//     * @return Task that resolves to Image object with storagePath and download URL
//     */
//    public Task<Image> uploadEventPoster(String eventID, byte[] imageData, String uploadedBy) {
//        // Validate input
//        if (imageData == null || imageData.length == 0) {
//            throw new IllegalArgumentException("Image data cannot be null or empty");
//        }
//
//        // Construct storage path (Service knows business rules)
//        String storagePath = "images/events/" + eventID + "/poster.jpg";
//
//        // Repository does the heavy lifting (Storage + Firestore)
//        return imageRepository.upload(imageData, storagePath, uploadedBy)
//                .onSuccessTask(image -> {
//                    // Update Event document with poster URL and imageID
//                    return eventRepository.getByID(eventID)
//                            .onSuccessTask(event -> {
//                                if (event == null) {
//                                    throw new IllegalArgumentException("Event not found");
//                                }
//                                event.setPosterImageURL(image.getImageURL());
//                                event.setPosterImageID(image.getImageID());
//                                return eventRepository.update(event).continueWith(t -> image);
//                            });
//                });
//    }

    private Task<Image> uploadNewPoster(String eventID, byte[] imageData, String uploadedBy) {

        // Construct storage path (Service knows business rules)
        String storagePath = "images/events/" + eventID + "/poster.jpg";

        // Repository does the heavy lifting (Storage + Firestore)
        return imageRepository.upload(imageData, storagePath, uploadedBy)
                .onSuccessTask(image -> {
                    // Update Event document with poster URL and imageID
                    return eventRepository.getByID(eventID)
                            .onSuccessTask(event -> {
                                if (event == null) {
                                    throw new IllegalArgumentException("Event not found");
                                }
                                event.setPosterImageURL(image.getImageURL());
                                event.setPosterImageID(image.getImageID());
                                return eventRepository.update(event).continueWith(t -> image);
                            });
                });

    }

    public Task<String> getEventPosterURL(String eventID) {
        return eventRepository.getByID(eventID)
                .continueWith(eventTask -> {
                    if (!eventTask.isSuccessful()) {
                        throw eventTask.getException();
                    }
                    Event event = eventTask.getResult();
                    if (event == null) {
                        throw new IllegalArgumentException("Event not found");
                    }
                    return event.getPosterImageURL();
                });
    }

    /**
     * Deletes event poster from both Storage/Firestore and clears the Event document
     * US 02.04.02
     *
     * @param eventID The event ID
     * @return Task that completes when both Storage/Firestore and Event are updated
     */
    public Task<Void> deleteEventPoster(String eventID) {
        // Get the Event to retrieve the posterImageID
        return eventRepository.getByID(eventID)
                .onSuccessTask(event -> {
                    if (event == null) throw new IllegalArgumentException("Event not found");
                    String posterImageID = event.getPosterImageID();

                    Task<Void> deleteImgTask = (posterImageID != null)
                            ? imageRepository.delete(posterImageID)
                            : Tasks.forResult(null);

                    event.setPosterImageID(null);
                    event.setPosterImageURL(null);

                    return deleteImgTask.continueWithTask(t -> eventRepository.update(event));
                });
    }

    /**
     * Generic helper method for uploading event-related images
     * Can be used by other services (like QRCodeService) for consistency
     * US 02.04.01, US 02.04.02, US 02.01.01
     *
     * @param eventID    The event ID
     * @param imageData  The image bytes
     * @param filename   The filename (e.g., "poster.jpg", "qrcode.png")
     * @param uploadedBy User ID who is uploading
     * @return Task that resolves to Image object
     */
    public Task<Image> uploadEventImage(String eventID, byte[] imageData,
                                        String filename, String uploadedBy) {
        String storagePath = "images/events/" + eventID + "/" + filename;
        return imageRepository.upload(imageData, storagePath, uploadedBy);
    }

    // US 03.06.01
    public Task<List<Image>> listAllImages() {
        return imageRepository.getAll();
    }


}
