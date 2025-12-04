package com.example.community;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Dialog fragment for confirming user's intention to leave a CommUnity event.
 * Provides a confirmation dialog with leave and cancel options.
 */
public class LeaveCommUnityDialogFragment extends DialogFragment {

    /**
     * Request key for fragment result communication.
     */
    public static final String REQUEST_KEY = "leave_community_request";
    /**
     * Result key indicating user confirmed the action.
     */
    public static final String RESULT_CONFIRMED = "confirmed";

    /**
     * Creates the confirmation dialog for leaving an event.
     *
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     * @return Return a new Dialog instance to be displayed by the fragment.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new MaterialAlertDialogBuilder(requireContext())
                .setMessage("Are you sure you want to leave this CommUnity event?")
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .setPositiveButton("Leave", (d, w) -> {
                    Bundle result = new Bundle();
                    result.putBoolean(RESULT_CONFIRMED, true);
                    getParentFragmentManager().setFragmentResult(REQUEST_KEY, result);
                })
                .create();
    }
}
