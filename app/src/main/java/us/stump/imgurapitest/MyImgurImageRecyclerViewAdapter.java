package us.stump.imgurapitest;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import us.stump.imgurapitest.ImgurImageListFragment.OnListFragmentInteractionListener;
import us.stump.imgurapitest.api.model.ImgurImage;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link ImgurImage} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class MyImgurImageRecyclerViewAdapter extends RecyclerView.Adapter<MyImgurImageRecyclerViewAdapter.ViewHolder> {

    /**
     * List of ImgurImages to display
     */
    private List<ImgurImage> mValues = new ArrayList<>();

    /**
     * Instance of the class that will receive the tap/click event for the ImgurImage.
     */
    private final OnListFragmentInteractionListener mListener;

    /**
     * Constructor for our MyImgurImageRecyclerViewAdapter
     * @param listener Instance of the class that will receive the tap/click event for the ImgurImage.
     */
    public MyImgurImageRecyclerViewAdapter(OnListFragmentInteractionListener listener) {
        mListener = listener;
    }

    /**
     * Constructor for our MyImgurImageRecyclerViewAdapter
     * @param items List of ImgurImages to display
     * @param listener Instance of the class that will receive the tap/click event for the ImgurImage.
     */
    public MyImgurImageRecyclerViewAdapter(List<ImgurImage> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_imgurimage, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        // give our view holder the ImgurImage to show
        holder.mItem = mValues.get(position);

        // tell the view holder to load the image now
        holder.loadImage();

        // setup our click/tap listener that will let our listener know the image was tapped on
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    /**
     * Use the given list of ImgurImages and update the view.
     * @param items The new list of ImgurImages
     */
    public void setItemsAndNotify(List<ImgurImage> items) {
        this.mValues = items;
        notifyDataSetChanged();
    }

    /**
     * Remove the given ImgurImage from the list and update the view.
     * @param image The ImgurImage we want to remove from the list.
     */
    public void removeItemAndNotify(ImgurImage image) {
        Log.v("imgur", "removeItemAndNotify: "+image.toString());
        Boolean didListChange = false;
        while(mValues.contains(image)) {
            mValues.remove(image);
            didListChange = true;
        }

        if (didListChange) {
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    /**
     * Our RecyclerView.ViewHolder
     */
    class ViewHolder extends RecyclerView.ViewHolder {
        /**
         * The view we are "holding"
         */
        final View mView;

        /**
         * The ImageView to display our ImgurImage
         */
        final ImageView mThumbnail;

        /**
         * The ImgurImage to display
         */
        ImgurImage mItem;

        /**
         * Construct our ViewHolder
         * @param view The view we are "holding"
         */
        ViewHolder(View view) {
            super(view);

            // grab our views
            mView = view;
            mThumbnail = (ImageView) view.findViewById(R.id.imgur_thumbnail);
        }

        /**
         * Load the ImgurImage we want to display
         */
        void loadImage()
        {
            String url = mItem.getLink();

            // load the image with Glide
            Glide.with(mView.getContext())
                    .load(url)
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            // The image failed to download
                            // We don't want to show an error message because there might be a lot of images in our list

                            // hide our progress bar
                            ProgressBar progressBar = (ProgressBar) mView.findViewById(R.id.image_progress);
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            // The image loaded successfully.

                            // hide our progress bar
                            ProgressBar progressBar = (ProgressBar) mView.findViewById(R.id.image_progress);
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .centerCrop() // center crop to make our image grid a little prettier
                    .into(mThumbnail);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mItem.toString() + "'";
        }
    }
}
