package us.stump.imgurapitest;


import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import us.stump.imgurapitest.api.model.ImgurImage;

/**
 * A {@link Fragment} subclass that shows a single {@link ImgurImage} image fullscreen.
 * Use the {@link FullScreenImageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FullScreenImageFragment extends Fragment implements View.OnClickListener {
    /**
     * Key used in the Bundle that FullScreenImageFragment.newInstance() creates to pass
     * our ImgurImage instance to the Fragment.
     */
    private static final String ARG_IMAGE = "imgurImage";

    /**
     * Model for the image we are displaying in this view.
     */
    private ImgurImage image;

    /**
     * ImageView that our image will be loaded into.
     */
    private ImageView imageView;

    /**
     * VideoView that is used to play the mp4 version of gifs
     */
    private VideoView videoView;

    /**
     * ProgressBar indeterminant loader graphic that is displayed while our image is loading.
     */
    private ProgressBar progressBar;

    /**
     * Our "Image Delete" button, which will ultimately permanently delete the given image from our webservice.
     */
    private ImageButton deleteImageBtn;

    /**
     * Instance of the class that will handle the "Image Delete" button action.
     */
    private OnDeleteButtonClickedListener mListener;

    /**
     * Make our fragment
     */
    public FullScreenImageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param image The ImgurImage model for the image to display.
     * @return A new instance of fragment FullScreenImageFragment.
     */
    public static FullScreenImageFragment newInstance(ImgurImage image) {
        FullScreenImageFragment fragment = new FullScreenImageFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_IMAGE, image);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            image = getArguments().getParcelable(ARG_IMAGE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_full_screen_image, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // find our views
        imageView = (ImageView) getActivity().findViewById(R.id.full_screen_image);
        videoView = (VideoView) getActivity().findViewById(R.id.full_screen_video);
        progressBar = (ProgressBar) getActivity().findViewById(R.id.full_screen_image_progress);
        deleteImageBtn = (ImageButton)  getActivity().findViewById(R.id.single_image_delete_button);

        // setup our "click" listener for the delete image button
        deleteImageBtn.setOnClickListener(this);

        if ("image/gif".equals(image.getType()) && image.getMp4() != null) {
            // this is a gif with an mp4 file,
            // so play that instead of showing the image (which is probably a thumbnail)
            loadMp4(image.getMp4(), image.getLooping());
        } else {
            // download and display the image
            loadImage(image.getLink());
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // we invalidate the options menu to make sure that the displayed options are relevant to this view
        getActivity().invalidateOptionsMenu();
    }

    /**
     * Receive our button clicks
     * @param v The view that was clicked
     */
    public void onClick(final View v) { //check for what button is pressed
        switch (v.getId()) {
            case R.id.single_image_delete_button:
                onDeleteButtonPressed();
                break;
            default:
                Log.e("imgur", "Clicked on unknown button: "+v.getId());
                break;
        }
    }

    /**
     * Performs the "Image Delete" action when the delete button is clicked.
     *
     * This really just invokes the action on our OnDeleteButtonClickedListener object.
     */
    public void onDeleteButtonPressed() {
        if (mListener != null) {
            mListener.onDeleteButtonClicked(image);
        }
    }

    /**
     * Download and display the image at the provided url.
     * @param url URL for the image to display
     */
    public void loadImage(String url)
    {
        // hide the video view and show the image view
        videoView.setVisibility(View.INVISIBLE);
        imageView.setVisibility(View.VISIBLE);

        Glide.with(this)
                .load(url)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        Log.e("imgur", "Image Load FAILED - "+((e != null) ? e.toString() : "null"));
                        toastErrorMessage(getString(R.string.image_download_failed));
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        Log.e("imgur", "Image loaded successfully");
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                        return false;
                    }
                })
                .into(imageView);
    }

    /**
     * Download and display the mp4 at the provided url.
     * @param url URL for the video to display
     * @param looping Whether the video should loop or not.
     */
    public void loadMp4(String url, final Boolean looping) {
        // hide the image view and show the video view
        imageView.setVisibility(View.INVISIBLE);
        videoView.setVisibility(View.VISIBLE);

        videoView.setVideoPath(url);
        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                //Log.v("imgur", "Video error: "+Integer.toString(what)+" extra "+Integer.toString(extra));
                toastErrorMessage(getString(R.string.video_playback_failed));
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                return false;
            }
        });
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                //Log.v("imgur", "onPrepared");
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                mp.setLooping(looping);
                mp.start();
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnDeleteButtonClickedListener) {
            mListener = (OnDeleteButtonClickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnDeleteButtonClickedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        imageView = null;
        progressBar = null;
        if (deleteImageBtn != null) {
            deleteImageBtn.setOnClickListener(null);
            deleteImageBtn = null;
        }
    }

    /**
     * Displays the given error message to the user.
     * @param error The error message to display to the user.
     */
    public void toastErrorMessage(String error) {
        if (imageView != null) {
            Snackbar.make(imageView, error, Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnDeleteButtonClickedListener {
        /**
         * Handle the "Image Delete" action for the given image.
         * @param item The image we want to delete
         */
        void onDeleteButtonClicked(ImgurImage item);
    }

}
