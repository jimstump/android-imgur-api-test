package us.stump.imgurapitest;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import us.stump.imgurapitest.api.model.ImgurAccessToken;
import us.stump.imgurapitest.api.model.ImgurImage;
import us.stump.imgurapitest.api.model.ImgurImagesResponse;
import us.stump.imgurapitest.api.service.ImgurClient;
import us.stump.imgurapitest.api.service.ImgurServiceGenerator;

import java.util.List;

/**
 * A fragment representing a list of ImgurImage items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class ImgurImageListFragment extends Fragment {

    /**
     * Key used in the Bundle that ImgurImageListFragment.newInstance() creates to pass
     * our ImgurAccessToken instance to the Fragment.
     */
    private static final String ARG_ACCESS_TOKEN = "accessToken";

    /**
     * ImgurAccessToken to use when making a request for images from the API.
     */
    private ImgurAccessToken accessToken;

    /**
     * The list of ImgurImages that we need to display.
     */
    private List<ImgurImage> images;

    /**
     * ImgurClient that is used to make requests to the Imgur API.
     */
    private ImgurClient client;

    /**
     * The RecyclerView that will be used to lay out our images on screen.
     */
    private RecyclerView recyclerView;

    /**
     * Scroll Listener that determines the appropriate time to load more images in order to
     * implement an endless view of images.
     */
    private EndlessRecyclerViewScrollListener scrollListener;

    /**
     * Instance of the class that will handle the "Image Select" action.
     */
    private OnListFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ImgurImageListFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * This factory will use the default number of columns (2).
     *
     * @param accessToken ImgurAccessToken to use when making a request for images from the API.
     * @return A new instance of fragment ImgurImageListFragment.
     */
    public static ImgurImageListFragment newInstance(ImgurAccessToken accessToken) {
        ImgurImageListFragment fragment = new ImgurImageListFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_ACCESS_TOKEN, accessToken);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // grab the parameters to use from the Bundle
        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey(ARG_ACCESS_TOKEN)) {
                ImgurAccessToken _accessToken = args.getParcelable(ARG_ACCESS_TOKEN);

                if (_accessToken != null) {
                    this.accessToken = _accessToken;
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_imgurimage_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            recyclerView = (RecyclerView) view;

            // grab our layout manager (setup in the view XML)
            GridLayoutManager gridLayoutManager = (GridLayoutManager) recyclerView.getLayoutManager();

            // http://guides.codepath.com/android/Endless-Scrolling-with-AdapterViews-and-RecyclerView#implementing-with-recyclerview
            // Create our scroll listener to determine the best time to load more images
            scrollListener = new EndlessRecyclerViewScrollListener(gridLayoutManager) {
                protected int visibleThreshold = 10;

                @Override
                public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                    // Triggered only when new data needs to be appended to the list
                    // Add whatever code is needed to append new items to the bottom of the list
                    loadUserImages(page, true);
                }
            };
            // Adds the scroll listener to RecyclerView
            recyclerView.addOnScrollListener(scrollListener);

            // attach our view adapter to the RecyclerView
            recyclerView.setAdapter(new MyImgurImageRecyclerViewAdapter(mListener));
        }
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // we require a handler for the "Image Select" action
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (accessToken != null) {
            loadUserImages();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // we invalidate the options menu to make sure that the displayed options are relevant to this view
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        accessToken = null;
        mListener = null;
    }

    /**
     * Create our ImgurClient instance using the ImgurServiceGenerator
     * @param accessToken The access token to use with the requests.
     */
    private void createImgurClient(ImgurAccessToken accessToken)
    {
        if (client == null) {
            client = ImgurServiceGenerator.createService(ImgurClient.class, accessToken.toAuthorizationHeader());
        }
    }

    /**
     * Load the first page of images.
     */
    private void loadUserImages()
    {
        loadUserImages(0, false);
    }

    /**
     * Load the given page of images from the API.
     * @param page The page to load.  This would start at 0.
     * @param calledByEndlessScroll Whether the endless scroll listener is calling or not. If so, we always load the page (instead of using the cached images if they exist.
     */
    private void loadUserImages(final int page, final boolean calledByEndlessScroll)
    {
        Log.v("imgur", "loadUserImages page: "+Integer.toString(page));

        if (images == null || images.isEmpty() || calledByEndlessScroll) {
            Log.v("imgur", "Need to load images");
            if (accessToken != null) {
                createImgurClient(accessToken);

                // fetch images for the current user.
                Call<ImgurImagesResponse> call = client.imagesForUser("me", page);

                call.enqueue(new Callback<ImgurImagesResponse>() {
                    @Override
                    public void onResponse(Call<ImgurImagesResponse> call, Response<ImgurImagesResponse> response) {
                        // The network call was a success and we got a response
                        Log.e("imgur", "API request complete");
                        ImgurImagesResponse body = response.body();

                        if (response.isSuccessful() && body != null && body.getSuccess()) {
                            Log.v("imgur", "Request was successful");

                            // calculate the current body size and number of new images we just loaded
                            // these are used to update the RecyclerViewAdapter
                            int origSize = 0;
                            int numberNew = (body.getData() != null) ? body.getData().size() : 0;

                            if (page == 0) {
                                images = body.getData();
                            } else {
                                origSize = images.size();
                                images.addAll(body.getData());
                            }

                            // convert the origSize and numberNew values to final after the correct values have been determined
                            // the variables need to be final for use in the inner post handler.
                            final int origSizeFinal = origSize;
                            final int numberNewFinal = numberNew;

                            if (body.getSuccess()) {
                                final MyImgurImageRecyclerViewAdapter adapter = (MyImgurImageRecyclerViewAdapter) recyclerView.getAdapter();
                                if (adapter != null) {
                                    // Delay before notifying the adapter since the scroll listeners
                                    // can be called while RecyclerView data cannot be changed.
                                    recyclerView.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            // Notify adapter with appropriate notify methods
                                            if (page == 0) {
                                                adapter.setItemsAndNotify(images);
                                            } else {
                                                adapter.notifyItemRangeInserted(origSizeFinal, numberNewFinal);
                                            }
                                        }
                                    });

                                }
                            }
                        } else {
                            toastErrorMessage(getString(R.string.image_list_download_failed));
                        }
                    }

                    @Override
                    public void onFailure(Call<ImgurImagesResponse> call, Throwable t) {
                        // the network call was a failure
                        toastErrorMessage(getString(R.string.image_list_download_failed));
                        Log.e("imgur", "API request failed");
                        Log.e("imgur", t.getMessage());
                        Log.e("imgur", call.request().toString());
                        RequestBody body = call.request().body();
                        if (body != null)
                            Log.e("imgur", body.toString());
                        else
                            Log.e("imgur", "null");
                    }
                });
            }
        } else {
            Log.v("imgur", "I have the images that I need");
            // we have cached images, so just display those, instead of loading new ones.
            final MyImgurImageRecyclerViewAdapter adapter = (MyImgurImageRecyclerViewAdapter) recyclerView.getAdapter();
            if (adapter != null) {
                recyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        // Notify adapter with appropriate notify methods
                        adapter.setItemsAndNotify(images);
                    }
                });
            }
        }
    }

    /**
     * Remove the given image from the view.
     *
     * This is probably because the image has been deleted.
     * @param image The image to remove from the list.
     */
    public void removeImage(final ImgurImage image) {
        Log.v("imgur", "removeImage");
        final MyImgurImageRecyclerViewAdapter adapter = (MyImgurImageRecyclerViewAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    // Notify adapter with appropriate notify methods
                    adapter.removeItemAndNotify(image);
                }
            });
        }
    }

    /**
     * Refresh the image list.
     *
     * This clears the list and fetches new images from page 0.
     */
    public void refreshList() {
        clearList();

        loadUserImages();
    }

    /**
     * Clear the image list.
     */
    public void clearList() {
        // 1. clear list
        images = null;

        // 2. Notify the adapter of the update
        final MyImgurImageRecyclerViewAdapter adapter = (MyImgurImageRecyclerViewAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    // Notify adapter with appropriate notify methods
                    adapter.notifyDataSetChanged(); // or notifyItemRangeRemoved
                }
            });
        }

        // 3. Reset endless scroll listener when performing a new search
        scrollListener.resetState();
    }

    /**
     * Displays the given error message to the user.
     * @param error The error message to display.
     */
    public void toastErrorMessage(String error) {
        if (recyclerView != null) {
            Snackbar.make(recyclerView, error, Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        /**
         * Handle the "Image Select" action for the given image.
         * @param item The image that was selected.
         */
        void onListFragmentInteraction(ImgurImage item);
    }
}
