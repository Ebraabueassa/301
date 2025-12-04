package com.example.community.ArrayAdapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.community.Image;
import com.example.community.R;
import com.example.community.UserRepository;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Adapter for displaying a list of images
 */
public class ImageArrayAdapter extends RecyclerView.Adapter<ImageArrayAdapter.ImageViewHolder> {

    private final List<Image> imageList;
    private final OnImageDeleteListener listener;

    /**
     * Interface for the delete button click listener
     */
    public interface OnImageDeleteListener {
        void onDeleteClick(Image image, int position);
    }

    /**
     * Constructs an ImageArrayAdapter
     *
     * @param imageList list of images to display
     * @param listener listener for handling image deletion
     */
    public ImageArrayAdapter(List<Image> imageList, OnImageDeleteListener listener) {
        this.imageList = imageList;
        this.listener = listener;
    }

    /**
     * Creates a new view holder for the image list
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return
     */
    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_image, parent, false);
        return new ImageViewHolder(view);
    }

    /**
     * Binds the data to the view holder
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Image image = imageList.get(position);
        String uploaderID = image.getUploadedBy();

        if (uploaderID != null && !uploaderID.isEmpty()) {
            UserRepository userRepository = new UserRepository();

            userRepository.getByUserID(uploaderID)
                    .addOnSuccessListener(user -> {
                        if (user != null) {
                            String displayName = (user.getUsername() != null && !user.getUsername().isEmpty())
                                    ? user.getUsername()
                                    : "Unknown Name";

                            holder.imageInfo.setText("Uploaded by: \n" + displayName);
                        } else {
                            holder.imageInfo.setText("Uploaded by: \nUnknown User");
                        }
                    })
                    .addOnFailureListener(e -> {
                        holder.imageInfo.setText("Uploaded by: Error loading name");
                    });
        } else {
            holder.imageInfo.setText("Uploaded by: \nUnknown");
        }

        if (image.getImageURL() != null && !image.getImageURL().isEmpty()) {
            Picasso.get()
                    .load(image.getImageURL())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.stat_notify_error)
                    .into(holder.imageView);
        } else {
            holder.imageView.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(image, holder.getAdapterPosition());
            }
        });
    }

    /**
     * Returns total number of item,s in the list
     * @return
     */
    @Override
    public int getItemCount() {
        return imageList.size();
    }

    /**
     * ViewHolder class that holds the views for each item
     */
    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public TextView imageInfo;
        Button deleteButton;

        /**
         * Constructs an ImageViewHolder
         *
         * @param itemView the view to hold the image and information
         */
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imgContent);
            imageInfo = itemView.findViewById(R.id.posterName);
            deleteButton = itemView.findViewById(R.id.buttonRemove);
        }
    }
}
