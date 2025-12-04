package com.example.community.Screens;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Dialog fragment that confirms account deletion with the user.
 * <p>
 *     DialogFragment displays a confirmation dialog asking users to verify their deletion
 *     of their account. Provides two options: "Cancel" to dismiss, "Delete" to confirm.
 *     If the user confirms, the result is sent back to the parent fragment via setFragmentResult.
 *
 * </p>
 */
public class DeleteAccountConfirmDialogFragment extends DialogFragment {

    /**
     * Request key used when sending the result back to the parent fragment.
     */
    public static final String REQUEST_KEY = "delete_account_request";

    /**
     * Bundle key for the result confirmation.
     */
    public static final String RESULT_CONFIRMED = "confirmed";

    /**
     * Creates and returns the account deletion confirmation dialog.
     * <p>
     *     How it works:
     *     <ul>
     *         <li>Creates a MaterialAlertDialogBuilder with a confirmation message</li>
     *         <li>Adds a "Cancel" button to dismiss the dialog</li>
     *         <li>Adds a "Delete" button to confirm the deletion</li>
     *         <li>When "Delete" is clicked, sends a result back to the parent fragment</li>
     *     </ul>
     * </p>
     *
     * @param savedInstanceState The last saved instance state of the Fragment,
     * or null if this is a freshly created Fragment.
     *
     * @return The created dialog.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new MaterialAlertDialogBuilder(requireContext())
                .setMessage("Are you sure you want to delete your CommUnity account?")
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .setPositiveButton("Delete", (d, w) -> {
                    Bundle result = new Bundle();
                    result.putBoolean(RESULT_CONFIRMED, true);
                    getParentFragmentManager().setFragmentResult(REQUEST_KEY, result);
                })
                .create();
    }
}