package us.stump.imgurapitest;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import us.stump.imgurapitest.api.model.ImgurAccessToken;
import us.stump.imgurapitest.dummy.DummyContent;

public class MainActivity extends AppCompatActivity implements
        LoginButtonFragment.OnLoginButtonClickedListener,
        ImgurImageFragment.OnListFragmentInteractionListener,
        ImgurLoginFragment.OnImgurTokenReceivedListener
{
    private static final String SHARED_PREF_ACCESS_TOKEN = "imgurAccessToken";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // If we're being restored from a previous state,
        // then we don't need to do anything and should return or else
        // we could end up with overlapping fragments.
        if (savedInstanceState != null) {
            return;
        }

        // create our LoginFragement, which is kind of like a splash screen with a login button
        LoginButtonFragment firstFragment = new LoginButtonFragment();

        // In case this activity was started with special instructions from an
        // Intent, pass the Intent's extras to the fragment as arguments
        firstFragment.setArguments(getIntent().getExtras());

        // Add the fragment to the 'fragment_container' FrameLayout
        getSupportFragmentManager().beginTransaction()
                .add(R.id.activity_main, firstFragment).commit();

        //clearAuthToken();

        // check to see if we have an access token
        ImgurAccessToken accessToken = (ImgurAccessToken) getIntent().getParcelableExtra("accessToken");
        if (accessToken == null) {
            accessToken = retrieveAuthToken();
        }
        if (accessToken != null && !accessToken.isExpired()) {
            OnImgurTokenReceived(accessToken);
        }
    }

    public void onLoginButtonClicked()
    {
        Log.v("imgur", "Login Clicked!!");

        // pull our imgur API app's app_id and app_secret
        String imgurAppId = Constants.getStoredSecret(Constants.SECRET_IMGUR_APP_ID);
        String imgurAppSecret = Constants.getStoredSecret(Constants.SECRET_IMGUR_APP_SECRET);

        // create the imgur login fragment
        // this is the fragment that will perform the OAuth 2 handshake and give us an access token
        ImgurLoginFragment newFragment = ImgurLoginFragment.newInstance(imgurAppId, imgurAppSecret);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.activity_main, newFragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }

    private void clearAuthToken()
    {
        Log.v("imgur", "clearAuthToken");
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(SHARED_PREF_ACCESS_TOKEN);
        editor.apply();
    }

    private void storeAuthToken(ImgurAccessToken auth_token)
    {
        Log.v("imgur", "Storing token: "+auth_token.toString());
        Gson gson = new Gson();
        String authTokenJson = gson.toJson(auth_token);
        Log.v("imgur", "token as JSON: "+authTokenJson);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(SHARED_PREF_ACCESS_TOKEN, authTokenJson);
        editor.apply();
    }

    private ImgurAccessToken retrieveAuthToken()
    {
        ImgurAccessToken auth_token = null;

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String authTokenJson = settings.getString(SHARED_PREF_ACCESS_TOKEN, null);
        Log.v("imgur", "Retrieving token as JSON: "+authTokenJson);

        if (!TextUtils.isEmpty(authTokenJson)) {
            Gson gson = new Gson();
            try {
                auth_token = gson.fromJson(authTokenJson, ImgurAccessToken.class);
                Log.v("imgur", "token as object: "+auth_token);
            }
            catch (JsonSyntaxException e) {
                Log.e("imgur", "Error unserializing access token from storage - "+e.toString());
            }
        }

        return auth_token;
    }

    public void OnImgurTokenReceived(ImgurAccessToken auth_token)
    {
        Log.v("imgur", "Received token: "+auth_token.toString());

        if (auth_token.isExpired()) {
            Log.v("imgur", "Token Is expired!");
            clearAuthToken();

            LoginButtonFragment newFragment = new LoginButtonFragment();

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack so the user can navigate back
            transaction.replace(R.id.activity_main, newFragment);
            transaction.addToBackStack(null);

            // Commit the transaction
            transaction.commit();
            return;
        }

        // store the auth token for future reference
        storeAuthToken(auth_token);


        ImgurImageFragment newFragment = ImgurImageFragment.newInstance(auth_token);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.activity_main, newFragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }

    public void onListFragmentInteraction(DummyContent.DummyItem item)
    {
        Log.v("imgur", "Dummy Item Clicked!!");
    }
}
