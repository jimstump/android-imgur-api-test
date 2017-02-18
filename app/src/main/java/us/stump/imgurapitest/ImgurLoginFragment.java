package us.stump.imgurapitest;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import us.stump.imgurapitest.api.model.ImgurAccessToken;


/**
 * A {@link Fragment} that shows the imgur OAuth 2 login screen.
 * Activities that contain this fragment must implement the
 * {@link OnImgurTokenReceivedListener} interface
 * to handle interaction events.
 * Use the {@link ImgurLoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ImgurLoginFragment extends Fragment {

    /**
     * Key used in the Bundle that ImgurLoginFragment.newInstance() creates to pass
     * our Imgur API Application's app id to the Fragment.
     */
    private static final String ARG_IMGUR_APP_ID = "imgur_app_id";

    /**
     * Key used in the Bundle that ImgurLoginFragment.newInstance() creates to pass
     * our Imgur API Application's app redirect URL to the Fragment.
     */
    private static final String ARG_IMGUR_APP_REDIRECT = "imgur_app_redirect";

    /**
     * The base URL to Imgur's OAuth 2 login page.
     */
    private static final String OAUTH_AUTHORIZE_BASE = "https://api.imgur.com/oauth2/authorize";

    /**
     * Our Imgur API Application's app id
     */
    private String imgurAppId;

    /**
     * Our Imgur API Application's app redirect URL
     */
    private String imgurAppRedirect;

    /**
     * Our Imgur API Application's app redirect URL
     */
    public String getImgurAppRedirect() {
        return imgurAppRedirect;
    }


    /**
     * The WebView that will display our OAuth 2 login page.
     */
    private WebView loginWebview;

    /**
     * Instance of the class that will receive the Access Token we receive after successful login.
     */
    private OnImgurTokenReceivedListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ImgurLoginFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param imgurAppId app_id used with the Imgur API.
     * @param imgurAppRedirect Authorization callback URL of the imgur app.
     * @return A new instance of fragment ImgurLoginFragment.
     */
    public static ImgurLoginFragment newInstance(String imgurAppId, String imgurAppRedirect) {
        ImgurLoginFragment fragment = new ImgurLoginFragment();
        Bundle args = new Bundle();
        args.putString(ARG_IMGUR_APP_ID, imgurAppId);
        args.putString(ARG_IMGUR_APP_REDIRECT, imgurAppRedirect);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // grab the parameters to use from the Bundle
        if (getArguments() != null) {
            imgurAppId = getArguments().getString(ARG_IMGUR_APP_ID);
            imgurAppRedirect = getArguments().getString(ARG_IMGUR_APP_REDIRECT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_imgur_login, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // get a reference to our WebView
        loginWebview = (WebView) getActivity().findViewById(R.id.imgur_oauth_webview);

        // make sure that JavaScript is enabled
        WebSettings webSettings = loginWebview.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // attach our web client that will monitor the WebView's URL looking for our app redirect URL
        loginWebview.setWebViewClient(new LoginWebViewClient(this));

        // load our Imgur OAuth login url
        loginWebview.loadUrl(generateLoginUrl().toString());
    }

    @Override
    public void onResume() {
        super.onResume();

        // we invalidate the options menu to make sure that the displayed options are relevant to this view
        getActivity().invalidateOptionsMenu();
    }

    @SuppressWarnings("deprecation")
    // http://stackoverflow.com/a/31950789
    public void clearCookies()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            //Log.d("imgur", "Using clearCookies code for API >=" + String.valueOf(Build.VERSION_CODES.LOLLIPOP_MR1));
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            //Log.d("imgur", "Using clearCookies code for API <" + String.valueOf(Build.VERSION_CODES.LOLLIPOP_MR1));
            CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(getActivity());
            cookieSyncMngr.startSync();
            CookieManager cookieManager=CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }
    }

    /**
     * Clears the WebView's Cache, History, and Cookies
     */
    public void clearHistoryAndCookies()
    {
        if (loginWebview != null) {
            loginWebview.clearCache(true);
            loginWebview.clearHistory();

            clearCookies();
        }
    }

    /**
     * Handles the receipt of our Access Token after a successful login
     * @param accessToken The Access Token representing our login
     */
    public void onImgurTokenReceived(ImgurAccessToken accessToken) {
        clearHistoryAndCookies();

        // pass our Access Token to our listener
        if (mListener != null) {
            mListener.OnImgurTokenReceived(accessToken);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnImgurTokenReceivedListener) {
            mListener = (OnImgurTokenReceivedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnImgurTokenReceivedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (loginWebview != null) {
            clearHistoryAndCookies();

            loginWebview.destroy();
            loginWebview = null;
        }
        mListener = null;
    }

    /**
     * Generate the full URL to the Imgur OAuth 2 login page.
     *
     * This URL includes our app id and the response type we want (token).
     * @return Full Uri to the Imgur OAuth 2 login page.
     */
    private Uri generateLoginUrl()
    {
        Uri.Builder loginUriBuilder = Uri.parse(OAUTH_AUTHORIZE_BASE).buildUpon();
        loginUriBuilder.appendQueryParameter("client_id", imgurAppId);
        loginUriBuilder.appendQueryParameter("response_type", "token");

        return loginUriBuilder.build();
    }

    /**
     * Our WebViewClient for the OAuth 2 login flow.
     *
     * This will monitor the WebView's URL looking for our app redirect URL.
     */
    private class LoginWebViewClient extends WebViewClient
    {
        /**
         * The instance of our login fragment.
         *
         * This is needed so we can give it the parsed ImgurAccessToken on successful login.
         */
        private ImgurLoginFragment loginFragment;

        /**
         * The access token we recently handled.
         *
         * This is used to prevent multiple events getting triggered in API 21+ due to both
         * shouldOverrideUrlLoading signatures getting called.
         */
        private ImgurAccessToken handledAccessToken;

        /**
         * Construct our WebViewClient for the OAuth 2 login flow.
         * @param loginFragment The loginFragment to interact with.
         */
        LoginWebViewClient(ImgurLoginFragment loginFragment)
        {
            this.loginFragment = loginFragment;
        }

        @Override
        @RequiresApi(21)
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            if (request != null)
            {
                Uri requestUri = request.getUrl();
                Log.v("imgurWebView", "request: "+requestUri.toString());

                if (isAppRedirect(requestUri))
                {
                    Log.v("imgurWebView", "We need to handle this URL ourselves");
                    handleAppRedirect(view, requestUri);
                }
                else
                {
                    Log.v("imgurWebView", "Not the app redirect");
                }
            }

            return super.shouldOverrideUrlLoading(view, request);
        }

        @Override
        @SuppressWarnings("deprecation")
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.v("imgurWebView", "URL: "+url);

            Uri requestUri = Uri.parse(url);

            if (isAppRedirect(requestUri))
            {
                Log.v("imgurWebView", "We need to handle this URL ourselves");
                handleAppRedirect(view, requestUri);
            }
            else
            {
                Log.v("imgurWebView", "Not the app redirect");
            }

            return super.shouldOverrideUrlLoading(view, url);
        }

        /**
         * Determines if the page represented by the given Uri is our app redirect URL.
         * @param url The Uri to check
         * @return True if the Uri represents our app redirect URL, false otherwise.
         */
        private boolean isAppRedirect(Uri url)
        {
            if (url == null){
                return false;
            }

            String appRedirect = loginFragment.getImgurAppRedirect();

            if (appRedirect == null){
                return false;
            }

            // This is our app redirect if the Uri without the fragment/hash equals our app redirect URL
            // but the fragment isn't empty (since that is where the token information will be stored)
            Uri urlNoFragment = url.buildUpon().fragment("").build();
            String fragment = url.getFragment();

            return urlNoFragment.toString().equals(appRedirect) && !TextUtils.isEmpty(fragment);
        }

        /**
         * Handle finding the app redirect.
         * @param view The WebView that is trying to load the app redirect URL.
         * @param url The full URL that we determined was our app redirect URL.
         */
        private void handleAppRedirect(WebView view, Uri url)
        {
            if (loginFragment != null) {
                String fragment = url.getFragment();

                // parse the access token information from the fragment/hash
                ImgurAccessToken accessToken = ImgurAccessToken.parseFromFragment(fragment);

                // on API higher than 21, both signatures for shouldOverrideUrlLoading get called
                // only handle the first one
                if (handledAccessToken == null || !accessToken.equals(handledAccessToken)) {
                    handledAccessToken = accessToken;

                    // stop the WebView from loading the app redirect URL (since we don't need the
                    // page to load to handle it).
                    view.stopLoading();
                    view.loadUrl("about:blank");

                    // pass our access token to our fragment
                    loginFragment.onImgurTokenReceived(accessToken);
                }
            }
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
    public interface OnImgurTokenReceivedListener {
        /**
         * Handle the access token after a successful login.
         * @param accessToken The new access token
         */
        void OnImgurTokenReceived(ImgurAccessToken accessToken);
    }
}
