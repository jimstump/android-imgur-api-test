package us.stump.imgurapitest;

import android.content.Context;
import android.content.Intent;
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

import java.util.List;

import us.stump.imgurapitest.api.model.ImgurAccessToken;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnImgurTokenReceivedListener} interface
 * to handle interaction events.
 * Use the {@link ImgurLoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ImgurLoginFragment extends Fragment {
    private static final String ARG_IMGUR_APP_ID = "imgur_app_id";
    private static final String ARG_IMGUR_APP_SECRET = "imgur_app_secret";
    private static final String ARG_IMGUR_APP_REDIRECT = "imgur_app_redirect";

    private static final String OATH_AUTHORIZE_BASE = "https://api.imgur.com/oauth2/authorize";

    private String imgurAppId;
    private String imgurAppSecret;

    private String imgurAppRedirect;

    public String getImgurAppRedirect() {
        return imgurAppRedirect;
    }

    private WebView loginWebview;

    private OnImgurTokenReceivedListener mListener;

    public ImgurLoginFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param imgurAppId app_id used with the Imgur API.
     * @param imgurAppSecret app_secret used with the Imgur API.
     * @return A new instance of fragment ImgurLoginFragment.
     */
    public static ImgurLoginFragment newInstance(String imgurAppId, String imgurAppSecret, String imgurAppRedirect) {
        ImgurLoginFragment fragment = new ImgurLoginFragment();
        Bundle args = new Bundle();
        args.putString(ARG_IMGUR_APP_ID, imgurAppId);
        args.putString(ARG_IMGUR_APP_SECRET, imgurAppSecret);
        args.putString(ARG_IMGUR_APP_REDIRECT, imgurAppRedirect);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            imgurAppId = getArguments().getString(ARG_IMGUR_APP_ID);
            imgurAppSecret = getArguments().getString(ARG_IMGUR_APP_SECRET);
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

        // setup the onclick listener for the login button
        loginWebview = (WebView) getActivity().findViewById(R.id.imgur_oauth_webview);

        // make sure that JavaScript is enabled
        WebSettings webSettings = loginWebview.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // attach our web client
        loginWebview.setWebViewClient(new LoginWebViewClient(this));

        // load our imgur OAuth login url
        loginWebview.loadUrl(generateLoginUrl().toString());
    }

    @SuppressWarnings("deprecation")
    // http://stackoverflow.com/a/31950789
    public void clearCookies()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            Log.d("imgur", "Using clearCookies code for API >=" + String.valueOf(Build.VERSION_CODES.LOLLIPOP_MR1));
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            Log.d("imgur", "Using clearCookies code for API <" + String.valueOf(Build.VERSION_CODES.LOLLIPOP_MR1));
            CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(getActivity());
            cookieSyncMngr.startSync();
            CookieManager cookieManager=CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }
    }

    public void clearHistoryAndCookies()
    {
        if (loginWebview != null) {
            loginWebview.clearCache(true);
            loginWebview.clearHistory();

            clearCookies();
        }
    }

    public void onImgurTokenReceived(ImgurAccessToken auth_token) {
        clearHistoryAndCookies();

        if (mListener != null) {
            mListener.OnImgurTokenReceived(auth_token);
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

    private Uri generateLoginUrl()
    {
        Uri.Builder loginUriBuilder = Uri.parse(OATH_AUTHORIZE_BASE).buildUpon();
        loginUriBuilder.appendQueryParameter("client_id", imgurAppId);
        loginUriBuilder.appendQueryParameter("response_type", "token");

        return loginUriBuilder.build();
    }

    private class LoginWebViewClient extends WebViewClient
    {
        private ImgurLoginFragment loginFragment;
        private ImgurAccessToken handledAccessToken;

        public LoginWebViewClient(ImgurLoginFragment loginFragment)
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

        private boolean isAppRedirect(Uri url)
        {
            if (url == null){
                return false;
            }

            String appRedirect = loginFragment.getImgurAppRedirect();

            if (appRedirect == null){
                return false;
            }

            Uri urlNoFragment = url.buildUpon().fragment("").build();
            String fragment = url.getFragment();

            return urlNoFragment.toString().equals(appRedirect) && !TextUtils.isEmpty(fragment);
        }

        private void handleAppRedirect(WebView view, Uri url)
        {
            if (loginFragment != null) {
                String fragment = url.getFragment();

                ImgurAccessToken accessToken = ImgurAccessToken.parseFromFragment(fragment);

                // on API higher than 21, both signatures for shouldOverrideUrlLoading get called
                // only handle the first one
                if (handledAccessToken == null || !accessToken.equals(handledAccessToken)) {
                    handledAccessToken = accessToken;

                    view.stopLoading();
                    view.loadUrl("about:blank");
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
        void OnImgurTokenReceived(ImgurAccessToken auth_token);
    }
}
