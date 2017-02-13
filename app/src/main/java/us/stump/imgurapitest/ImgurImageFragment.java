package us.stump.imgurapitest;

import android.content.Context;
import android.os.Bundle;
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
import us.stump.imgurapitest.dummy.DummyContent;
import us.stump.imgurapitest.dummy.DummyContent.DummyItem;

import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class ImgurImageFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_ACCESS_TOKEN = "accessToken";
    private static final String ARG_COLUMN_COUNT = "column-count";

    // TODO: Customize parameters
    private int mColumnCount = 2;
    private ImgurAccessToken accessToken;

    private ImgurClient client;
    private RecyclerView recyclerView;
    private OnListFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ImgurImageFragment() {
    }

    public static ImgurImageFragment newInstance(ImgurAccessToken accessToken) {
        return ImgurImageFragment.newInstance(accessToken, 0);
    }

    public static ImgurImageFragment newInstance(ImgurAccessToken accessToken, int columnCount) {
        ImgurImageFragment fragment = new ImgurImageFragment();
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
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            recyclerView.setAdapter(new MyImgurImageRecyclerViewAdapter(DummyContent.EMPTY_ITEMS, mListener));
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
        Log.v("imgur", "loadUserImages");
        if (accessToken != null) {
            createImgurClient(accessToken);

            Call<ImgurImagesResponse> call = client.imagesForUser("me", 0);

            call.enqueue(new Callback<ImgurImagesResponse>() {
                @Override
                public void onResponse(Call<ImgurImagesResponse> call, Response<ImgurImagesResponse> response) {
                    // The network call was a success and we got a response
                    // TODO: use the repository list and display it
                    Log.e("imgur", "API request succeeded");
                    Log.v("imgur", Boolean.toString(response.isSuccessful()));
                    Log.v("imgur", Integer.toString(response.code()));
                    Log.v("imgur", response.message());
                    ImgurImagesResponse body = response.body();
                    if (body != null) {
                        Log.v("imgur", body.getStatus().toString());
                        Log.v("imgur", body.getSuccess().toString());
                        List<ImgurImage> data = body.getData();

                        // TODO: Use the response data instead of the dummy content
                        recyclerView.setAdapter(new MyImgurImageRecyclerViewAdapter(DummyContent.ITEMS, mListener));

                        if (data != null)
                        {
                            int i;
                            int length = data.size();
                            for(i=0; i<length; i++)
                            {
                                ImgurImage image = data.get(i);
                                Log.v("imgur", image.getLink());
                            }
                        }
                    }

                }

                @Override
                public void onFailure(Call<ImgurImagesResponse> call, Throwable t) {
                    // the network call was a failure
                    // TODO: handle error
                    Log.e("imgur", "API request failed");
                    Log.e("imgur", t.getMessage());
                    Log.e("imgur", call.request().toString());
                    RequestBody body = call.request().body();
                    if(body != null)
                        Log.e("imgur", body.toString());
                    else
                        Log.e("imgur", "null");
                }
            });
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
        void onListFragmentInteraction(DummyItem item);
    }
}
