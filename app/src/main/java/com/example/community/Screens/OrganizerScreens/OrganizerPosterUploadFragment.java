package com.example.community.Screens.OrganizerScreens;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.community.ImageService;
import com.example.community.R;
import com.example.community.UserService;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Fragment that allows the organizer to upload a poster image for a specific event.
 * Handles image selection from gallery, permissions, preview, and uploading to Firebase.
 */
public class OrganizerPosterUploadFragment extends Fragment {

    private static final String TAG = "OrganizerPosterUploadFragment";
    private static final String ARG_EVENT_ID = "event_id";

    /** The event ID for which the poster is being uploaded */
    private String eventId;

    /** The current organizer ID */
    private String currentOrganizerId;

    /** Byte array of the selected image */
    private byte[] selectedImageData;

    /** UI elements */
    private TextView previewTextLabel;
    private ImageView imagePosterPreviewImageView;
    private Button buttonUploadPoster, buttonSubmitPoster, cancelButton;
    private ProgressBar progressBar;

    /** Services for image upload and user info */
    private ImageService imageService;
    private UserService userService;

    /** Activity result launchers for image selection and permission requests */
    private ActivityResultLauncher<Intent> imagePicker;
    private ActivityResultLauncher<String> requestPermissionsLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.organizer_poster_upload_page, container, false);
    }

    /**
     * Initializes UI elements, retrieves the current organizer ID, and sets up click listeners.
     *
     * @param view Root view of the fragment
     * @param savedInstanceState Previously saved state
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imageService = new ImageService();
        userService = new UserService();

        // Get event ID from arguments
        eventId = getArguments() != null ? getArguments().getString(ARG_EVENT_ID) : null;
        if (eventId == null) {
            Toast.makeText(getActivity(), "Event ID is not valid", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).navigateUp();
            return;
        }

        // Initialize UI components
        previewTextLabel = view.findViewById(R.id.previewTextLabel);
        imagePosterPreviewImageView = view.findViewById(R.id.imagePosterPreview);
        buttonUploadPoster = view.findViewById(R.id.buttonUploadPoster);
        buttonSubmitPoster = view.findViewById(R.id.buttonSubmitPoster);
        cancelButton = view.findViewById(R.id.cancelButton);
        progressBar = view.findViewById(R.id.uploadProgressBar);

        // Fetch current organizer ID from device token
        String deviceToken = userService.getDeviceToken();
        userService.getUserIDByDeviceToken(deviceToken)
                .addOnSuccessListener(userId -> currentOrganizerId = userId)
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get the user ID for the current organizer");
                    Toast.makeText(getActivity(), "Failed to get the organizer information", Toast.LENGTH_SHORT).show();
                });

        // Set up button actions
        buttonUploadPoster.setOnClickListener(v -> openImagePicker());
        buttonSubmitPoster.setOnClickListener(v -> uploadImage());
        cancelButton.setOnClickListener(v -> clearImage());

        // Initialize ActivityResultLaunchers
        imagePicker();
        requestPermissions();
    }

    /**
     * Registers the image picker launcher to allow selecting an image from the device gallery.
     */
    private void imagePicker() {
        imagePicker = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            previewTextLabel.setVisibility(View.VISIBLE);
                            Picasso.get()
                                    .load(imageUri)
                                    .fit()
                                    .centerCrop()
                                    .into(imagePosterPreviewImageView);
                            imagePosterPreviewImageView.setVisibility(View.VISIBLE);

                            try {
                                selectedImageData = getBytesFromUri(imageUri);
                                buttonSubmitPoster.setEnabled(true);
                                Toast.makeText(getContext(), "Image selected. Ready to be uploaded", Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                Log.e(TAG, "Failed to read image data", e);
                                Toast.makeText(getActivity(), "Failed to read image data", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    /**
     * Registers the permission request launcher to request gallery/media access at runtime.
     */
    private void requestPermissions() {
        requestPermissionsLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openImagePicker();
                    } else {
                        Toast.makeText(getActivity(), "Permission denied. Please grant permission to upload an image", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Opens the device image picker if permissions are granted, otherwise requests them.
     */
    private void openImagePicker() {
        if (areMediaPermissionsGranted()) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            imagePicker.launch(intent);
        } else {
            requestMediaPermissions();
        }
    }

    /**
     * Checks if the app has permission to read images from the device.
     *
     * @return True if permission granted, false otherwise
     */
    private boolean areMediaPermissionsGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    /**
     * Requests the necessary permissions to access images from the device gallery.
     */
    private void requestMediaPermissions() {
        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;
        requestPermissionsLauncher.launch(permission);
    }

    /**
     * Clears the selected image preview and disables the submit button.
     */
    private void clearImage() {
        selectedImageData = null;
        buttonSubmitPoster.setEnabled(false);
        previewTextLabel.setVisibility(View.GONE);
        imagePosterPreviewImageView.setVisibility(View.GONE);
    }

    /**
     * Converts a content URI of an image into a byte array for uploading to Firebase.
     *
     * @param uri The URI of the selected image
     * @return Byte array of image data
     * @throws IOException If the image cannot be read
     */
    private byte[] getBytesFromUri(Uri uri) throws IOException {
        InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        inputStream.close();
        return byteBuffer.toByteArray();
    }

    /**
     * Uploads the selected image to the event's poster location using ImageService.
     * Handles progress bar and enables/disables buttons during upload.
     */
    private void uploadImage() {
        if (selectedImageData == null) {
            Toast.makeText(getContext(), "No image selected", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        buttonSubmitPoster.setEnabled(false);
        cancelButton.setEnabled(false);

        imageService.uploadEventPoster(eventId, selectedImageData, currentOrganizerId, true)
                .addOnSuccessListener(imageUrl -> {
                    progressBar.setVisibility(View.GONE);
                    buttonSubmitPoster.setEnabled(true);
                    cancelButton.setEnabled(true);
                    Toast.makeText(getContext(), "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(this).navigateUp();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    buttonSubmitPoster.setEnabled(true);
                    cancelButton.setEnabled(true);
                    Toast.makeText(getContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                });
    }
}
