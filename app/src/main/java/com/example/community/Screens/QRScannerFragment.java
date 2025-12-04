package com.example.community.Screens;

import android.Manifest;
import android.content.pm.PackageManager;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.community.EventService;
import com.example.community.QRCodeService;
import com.example.community.R;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.List;

/**
 * Fragment for scanning QR codes to navigate to events.
 */
public class QRScannerFragment extends Fragment {

    private static final String TAG = "QRScannerFragment";
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 200;

    private DecoratedBarcodeView barcodeView;
    private Button cancelButton;
    private TextView instructionsTextView;

    private EventService eventService;
    private QRCodeService qrCodeService;
    private boolean isScanning = false;

    /**
     * Inflates the QR scanner layout.
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
        View view = inflater.inflate(R.layout.qr_scanner_fragment, container, false);
        return view;
    }

    /**
     * Initializes the QR scanner and sets up the UI components.
     *
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        eventService = new EventService();
        qrCodeService = new QRCodeService();

        barcodeView = view.findViewById(R.id.barcode_scanner);
        cancelButton = view.findViewById(R.id.cancelScanButton);
        instructionsTextView = view.findViewById(R.id.scanInstructions);

        cancelButton.setOnClickListener(v -> {
            Log.d(TAG, "Cancel button clicked");
            NavHostFragment.findNavController(this).navigateUp();
        });

        checkCameraPermissionAndStart();
    }

    /**
     * Check camera permission and start scanning if granted.
     */
    private void checkCameraPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission already granted");
            startScanning();
        } else {
            Log.d(TAG, "Requesting camera permission");
            requestCameraPermission();
        }
    }

    /**
     * Request camera permission from user.
     */
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(
                requireActivity(),
                new String[]{Manifest.permission.CAMERA},
                CAMERA_PERMISSION_REQUEST_CODE
        );
    }

    /**
     * Handle the result of camera permission request.
     *
     * @param requestCode The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either {@link android.content.pm.PackageManager#PERMISSION_GRANTED}
     *     or {@link android.content.pm.PackageManager#PERMISSION_DENIED}. Never null.
     *
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Camera permission granted");
                startScanning();
            } else {
                Log.e(TAG, "Camera permission denied");
                Toast.makeText(getContext(), "Camera permission is required to scan QR codes", Toast.LENGTH_LONG).show();
                NavHostFragment.findNavController(this).navigateUp();
            }
        }
    }

    /**
     * Start the QR code scanning process.
     */
    private void startScanning() {
        Log.d(TAG, "Starting QR code scanner");
        isScanning = true;

        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result != null && isScanning) {
                    handleScanResult(result.getText());
                }
            }

            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {
                // Visual feedback could be added here if needed
            }
        });
    }

    /**
     * Handle the scanned QR code result.
     * @param qrContent The raw content from the QR code
     */
    private void handleScanResult(String qrContent) {
        Log.d(TAG, "QR code scanned: " + qrContent);

        // Stop scanning to prevent multiple scans
        isScanning = false;
        barcodeView.pause();

        // Parse the QR code content to get eventID
        String eventID = qrCodeService.parseQRCodeContent(qrContent);

        if (eventID == null || eventID.isEmpty()) {
            Log.e(TAG, "Invalid QR code format");
            Toast.makeText(getContext(), "Invalid QR code. Please scan an event QR code.", Toast.LENGTH_SHORT).show();
            resumeScanning();
            return;
        }

        Log.d(TAG, "Parsed eventID: " + eventID);
        validateAndNavigateToEvent(eventID);
    }

    /**
     * Validate that the event exists and navigate to it.
     * @param eventID The ID of the event to validate
     */
    private void validateAndNavigateToEvent(String eventID) {
        Log.d(TAG, "Validating event: " + eventID);
        instructionsTextView.setText("Loading event...");

        eventService.getEvent(eventID)
                .addOnSuccessListener(event -> {
                    if (event == null) {
                        Log.e(TAG, "Event not found: " + eventID);
                        Toast.makeText(getContext(), "Event not found. The QR code may be invalid.", Toast.LENGTH_SHORT).show();
                        resumeScanning();
                        return;
                    }

                    Log.d(TAG, "Event found: " + event.getTitle() + ", navigating to event description");
                    navigateToEventDescription(eventID);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to validate event", e);
                    Toast.makeText(getContext(), "Failed to load event. Please try again.", Toast.LENGTH_SHORT).show();
                    resumeScanning();
                });
    }

    /**
     * Navigate to the event description page.
     * @param eventID The ID of the event to display
     */
    private void navigateToEventDescription(String eventID) {
        Bundle args = new Bundle();
        args.putString("event_id", eventID);

        try {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_QRScannerFragment_to_EventDescriptionFragment, args);
        } catch (Exception e) {
            Log.e(TAG, "Navigation failed", e);
            Toast.makeText(getContext(), "Failed to open event", Toast.LENGTH_SHORT).show();
            resumeScanning();
        }
    }

    /**
     * Resume scanning after handling an error.
     */
    private void resumeScanning() {
        if (getView() != null) {
            instructionsTextView.setText("Point camera at QR code");
            isScanning = true;
            barcodeView.resume();
        }
    }

    /**
     * Resume scanning when the fragment is resumed.
     */
    @Override
    public void onResume() {
        super.onResume();
        if (barcodeView != null && isScanning) {
            barcodeView.resume();
        }
    }

    /**
     * Pause scanning when the fragment is paused.
     */
    @Override
    public void onPause() {
        super.onPause();
        if (barcodeView != null) {
            barcodeView.pause();
        }
    }

    /**
     * Clean up resources when the fragment is destroyed.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isScanning = false;
        if (barcodeView != null) {
            barcodeView.pause();
        }
    }
}