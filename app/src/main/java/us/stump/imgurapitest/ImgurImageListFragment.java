package us.stump.imgurapitest;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
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
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class ImgurImageListFragment extends Fragment {

    private static final String ARG_ACCESS_TOKEN = "accessToken";
    private static final String ARG_COLUMN_COUNT = "column-count";

    private int mColumnCount = 2;
    private ImgurAccessToken accessToken;

    private List<ImgurImage> images;

    private ImgurClient client;
    private RecyclerView recyclerView;
    private EndlessRecyclerViewScrollListener scrollListener;
    private OnListFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ImgurImageListFragment() {
    }

    public static ImgurImageListFragment newInstance(ImgurAccessToken accessToken) {
        return ImgurImageListFragment.newInstance(accessToken, 0);
    }

    public static ImgurImageListFragment newInstance(ImgurAccessToken accessToken, int columnCount) {
        ImgurImageListFragment fragment = new ImgurImageListFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_ACCESS_TOKEN, accessToken);
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey(ARG_ACCESS_TOKEN)) {
                ImgurAccessToken _accessToken = (ImgurAccessToken) args.getParcelable(ARG_ACCESS_TOKEN);

                if (_accessToken != null) {
                    this.accessToken = _accessToken;
                }
            }

            if (args.containsKey(ARG_COLUMN_COUNT)) {
                int _mColumnCount = args.getInt(ARG_COLUMN_COUNT);

                if (_mColumnCount > 0) {
                    this.mColumnCount = _mColumnCount;
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
            Context context = view.getContext();
            recyclerView = (RecyclerView) view;

            LinearLayoutManager linearLayoutManager = null;
            GridLayoutManager gridLayoutManager = null;

            if (mColumnCount <= 1) {
                linearLayoutManager = new LinearLayoutManager(context);
                recyclerView.setLayoutManager(linearLayoutManager);
            } else {
                gridLayoutManager = new GridLayoutManager(context, mColumnCount);
                recyclerView.setLayoutManager(gridLayoutManager);
            }

            // http://guides.codepath.com/android/Endless-Scrolling-with-AdapterViews-and-RecyclerView#implementing-with-recyclerview
            scrollListener = new EndlessRecyclerViewScrollListener((linearLayoutManager != null) ? linearLayoutManager : gridLayoutManager) {
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

            recyclerView.setAdapter(new MyImgurImageRecyclerViewAdapter(mListener));
        }
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
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
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        accessToken = null;
        mListener = null;
    }



    private void createImgurClient(ImgurAccessToken auth_token)
    {
        if (client == null) {
            client = ImgurServiceGenerator.createService(ImgurClient.class, auth_token.toAuthorizationHeader());
        }
    }

    private void loadUserImages()
    {
        loadUserImages(0, false);
    }

    private void loadUserImages(final int page, final boolean calledByEndlessScroll)
    {
        Log.v("imgur", "loadUserImages page: "+Integer.toString(page));

        if (images == null || images.isEmpty() || calledByEndlessScroll) {
            Log.v("imgur", "Need to load images");
            if (accessToken != null) {
                createImgurClient(accessToken);

                Call<ImgurImagesResponse> call = client.imagesForUser("me", page);

                call.enqueue(new Callback<ImgurImagesResponse>() {
                    @Override
                    public void onResponse(Call<ImgurImagesResponse> call, Response<ImgurImagesResponse> response) {
                        // The network call was a success and we got a response
                        Log.e("imgur", "API request succeeded");
                        Log.v("imgur", Boolean.toString(response.isSuccessful()));
                        Log.v("imgur", Integer.toString(response.code()));
                        Log.v("imgur", response.message());
                        ImgurImagesResponse body = response.body();

                        if (response.isSuccessful() && body != null) {
                            Log.v("imgur", body.getStatus().toString());
                            Log.v("imgur", body.getSuccess().toString());

                            int origSize = 0;
                            int numberNew = (body.getData() != null) ? body.getData().size() : 0;

                            if (page == 0) {
                                images = body.getData();
                            } else {
                                origSize = images.size();
                                images.addAll(body.getData());
                            }

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
                        }
                    }

                    @Override
                    public void onFailure(Call<ImgurImagesResponse> call, Throwable t) {
                        // the network call was a failure
                        toastErrorMessage("Couln't get your images.  Please try again later.");
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

    public void refreshList() {
        clearList();

        loadUserImages();
    }

    public void clearList() {
        // clear list
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
        // TODO: Update argument type and name
        void onListFragmentInteraction(ImgurImage item);
    }
}
