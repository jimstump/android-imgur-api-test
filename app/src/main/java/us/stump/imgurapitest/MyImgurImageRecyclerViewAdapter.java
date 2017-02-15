package us.stump.imgurapitest;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

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

    private List<ImgurImage> mValues = new ArrayList<>();
    private final OnListFragmentInteractionListener mListener;


    public MyImgurImageRecyclerViewAdapter(OnListFragmentInteractionListener listener) {
        mListener = listener;
    }

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
        holder.mItem = mValues.get(position);

        holder.loadImage(holder.mItem.getLink());



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

    public void setItemsAndNotify(List<ImgurImage> items) {
        this.mValues = items;
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView mThumbnail;
        public ImgurImage mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mThumbnail = (ImageView) view.findViewById(R.id.imgur_thumbnail);
        }

        public void loadImage(String url)
        {
            Glide.with(mView.getContext())
                    .load(url)
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            ProgressBar progressBar = (ProgressBar) mView.findViewById(R.id.image_progress);

                            progressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            ProgressBar progressBar = (ProgressBar) mView.findViewById(R.id.image_progress);

                            progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .centerCrop()
                    .into(mThumbnail);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mItem.toString() + "'";
        }
    }
}
