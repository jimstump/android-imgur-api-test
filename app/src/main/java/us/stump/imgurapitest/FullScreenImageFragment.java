package us.stump.imgurapitest;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import us.stump.imgurapitest.api.model.ImgurImage;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FullScreenImageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FullScreenImageFragment extends Fragment implements View.OnClickListener {
    private static final String ARG_IMAGE = "imgurImage";

    private ImgurImage image;
    private ImageView imageView;
    private ProgressBar progressBar;
    private ImageButton deleteImageBtn;

    private OnDeleteButtonClickedListener mListener;

    public FullScreenImageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param image Parameter 1.
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
            image = (ImgurImage) getArguments().getParcelable(ARG_IMAGE);
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

        imageView = (ImageView) getActivity().findViewById(R.id.full_screen_image);
        progressBar = (ProgressBar) getActivity().findViewById(R.id.full_screen_image_progress);
        deleteImageBtn = (ImageButton)  getActivity().findViewById(R.id.single_image_delete_button);
        deleteImageBtn.setOnClickListener(this);

        loadImage(image.getLink());
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().invalidateOptionsMenu();
    }

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

    public void onDeleteButtonPressed() {
        if (mListener != null) {
            mListener.onDeleteButtonClicked(image);
        }
    }



    public void loadImage(String url)
    {
        Glide.with(this)
                .load(url)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        Log.e("imgur", "Image Load FAILED - "+e.toString());
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
        deleteImageBtn.setOnClickListener(null);
        deleteImageBtn = null;
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
        void onDeleteButtonClicked(ImgurImage item);
    }

}
