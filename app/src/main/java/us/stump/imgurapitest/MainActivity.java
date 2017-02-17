package us.stump.imgurapitest;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import us.stump.imgurapitest.api.model.ImgurAccessToken;
import us.stump.imgurapitest.api.model.ImgurBasicResponse;
import us.stump.imgurapitest.api.model.ImgurImage;
import us.stump.imgurapitest.api.service.ImgurClient;
import us.stump.imgurapitest.api.service.ImgurServiceGenerator;

import com.ipaulpro.afilechooser.utils.FileUtils;

public class MainActivity extends AppCompatActivity implements
        LoginButtonFragment.OnLoginButtonClickedListener,
        ImgurImageListFragment.OnListFragmentInteractionListener,
        ImgurLoginFragment.OnImgurTokenReceivedListener,
        FullScreenImageFragment.OnDeleteButtonClickedListener
{
    private static final String FRAGMENT_TAG_LOGIN = "login";
    private static final String FRAGMENT_TAG_OAUTH_LOGIN = "oauthlogin";
    private static final String FRAGMENT_TAG_GALLERY = "gallery";
    private static final String FRAGMENT_TAG_IMAGE_VIEW = "imageview";
    private static final String SHARED_PREF_ACCESS_TOKEN = "imgurAccessToken";

    private int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // If we're being restored from a previous state,
        // then we don't need to do anything and should return or else
        // we could end up with overlapping fragments.
        if (savedInstanceState != null) {
            return;
        }

        // check to see if we have an access token
        ImgurAccessToken accessToken = retrieveAuthToken();

        if (accessToken != null && !accessToken.isExpired()) {
            // handle the access token
            OnImgurTokenReceived(accessToken);
        } else {
            // logout, (which will clear the access token if it was expired
            // and show the login button screen
            logout();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.v("imgur", "onCreateOptionsMenu");

        super.onCreateOptionsMenu(menu);

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //Log.v("imgur", "onPrepareOptionsMenu");
        super.onPrepareOptionsMenu(menu);

        MenuItem addImageButton = menu.findItem(R.id.action_add_image);
        MenuItem logoutButton = menu.findItem(R.id.action_logout);

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment f;

        if ((f = fragmentManager.findFragmentByTag(FRAGMENT_TAG_LOGIN)) != null && f.isVisible()) {
            //Log.v("imgur", "we are on the main login screen (the default screen)");
            if (addImageButton != null) {
                addImageButton.setVisible(false);
            }
            if (logoutButton != null) {
                logoutButton.setVisible(false);
            }
        } else if((f = fragmentManager.findFragmentByTag(FRAGMENT_TAG_OAUTH_LOGIN)) != null && f.isVisible()) {
            //Log.v("imgur", "we are on the imgur OAuth login screen");
            if (addImageButton != null) {
                addImageButton.setVisible(false);
            }
            if (logoutButton != null) {
                logoutButton.setVisible(false);
            }
        } else if((f = fragmentManager.findFragmentByTag(FRAGMENT_TAG_GALLERY)) != null && f.isVisible()) {
            //Log.v("imgur", "we are on the image gallery screen");
            if (addImageButton != null) {
                addImageButton.setVisible(true);
            }
            if (logoutButton != null) {
                logoutButton.setVisible(true);
            }
        } else if((f = fragmentManager.findFragmentByTag(FRAGMENT_TAG_IMAGE_VIEW)) != null && f.isVisible()) {
            //Log.v("imgur", "we are on the single image screen");
            if (addImageButton != null) {
                addImageButton.setVisible(false);
            }
            if (logoutButton != null) {
                logoutButton.setVisible(true);
            }
        } else {
            //Log.v("imgur", "what screen is this?");
            if (addImageButton != null) {
                addImageButton.setVisible(false);
            }
            if (logoutButton != null) {
                logoutButton.setVisible(false);
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_image:
                // User chose the "Add Image" item
                Log.v("imgur", "Add an image!");

                onAddButtonClicked();

                return true;

            case R.id.action_logout:
                // User chose the "Logout" item
                Log.v("imgur", "Logout!");

                //http://stackoverflow.com/a/2478662
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle("Logout");
                builder.setMessage("Are you sure you want to log out of imgur?");

                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // log the user out
                        logout();

                        dialog.dismiss();
                    }
                });

                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // Do nothing
                        dialog.dismiss();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();

                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    private void logout()
    {
        clearAuthToken();

        clearBackStack();

        LoginButtonFragment newFragment = new LoginButtonFragment();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.fragment_container, newFragment, FRAGMENT_TAG_LOGIN);

        // Commit the transaction
        transaction.commit();
    }

    private void clearBackStack() {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        while (fragmentManager.getBackStackEntryCount() != 0) {
            fragmentManager.popBackStackImmediate();
        }
    }

    public void onLoginButtonClicked()
    {
        Log.v("imgur", "Login Clicked!!");

        // pull our imgur API app's app_id and app_secret
        String imgurAppId = Constants.getStoredSecret(Constants.SECRET_IMGUR_APP_ID);
        String imgurAppSecret = Constants.getStoredSecret(Constants.SECRET_IMGUR_APP_SECRET);
        String imgurAppRedirect = Constants.getStoredSecret(Constants.SECRET_IMGUR_APP_CALLBACK_URL);

        // create the imgur login fragment
        // this is the fragment that will perform the OAuth 2 handshake and give us an access token
        ImgurLoginFragment newFragment = ImgurLoginFragment.newInstance(imgurAppId, imgurAppSecret, imgurAppRedirect);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.fragment_container, newFragment, FRAGMENT_TAG_OAUTH_LOGIN);
        transaction.addToBackStack(FRAGMENT_TAG_OAUTH_LOGIN);

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
            logout();
            return;
        }

        // store the auth token for future reference
        storeAuthToken(auth_token);

        //getSupportFragmentManager().popBackStack(FRAGMENT_TAG_OAUTH_LOGIN, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        clearBackStack();

        ImgurImageListFragment newFragment = ImgurImageListFragment.newInstance(auth_token);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and DO NOT add the transaction to the back stack since we don't want the user to
        // be able to navigate back
        transaction.replace(R.id.fragment_container, newFragment, FRAGMENT_TAG_GALLERY);

        // Commit the transaction
        transaction.commit();
    }

    public void onListFragmentInteraction(ImgurImage item)
    {
        Log.v("imgur", "Image Tapped!!");
        Log.v("imgur", item.toString());


        FullScreenImageFragment newFragment = FullScreenImageFragment.newInstance(item);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.fragment_container, newFragment, FRAGMENT_TAG_IMAGE_VIEW);
        transaction.addToBackStack(FRAGMENT_TAG_IMAGE_VIEW);

        // Commit the transaction
        transaction.commit();
    }

    public void onAddButtonClicked()
    {
        Intent intent = new Intent();
        // Show only images, no videos or anything else
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        // Always show the chooser (if there are multiple options available)
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();
            Log.v("imgur", "Received image: "+uri.toString());

            uploadFile(uri);
        }
    }

    private void uploadFile(Uri fileUri) {
        Log.v("imgur", "uploadFile: fileuri = "+fileUri.toString());

        InputStream in;
        try {
            in = getContentResolver().openInputStream(fileUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        if (in == null) {
            Log.e("imgur", "InputStream is null");
        }

        byte[] inBytes = readInputStream(in);

        if (inBytes == null) {
            Log.e("imgur", "inBytes is null");
            return;
        } else if (inBytes.length == 0) {
            Log.e("imgur", "inBytes is empty");
            return;
        }

        createImgurClient(this.retrieveAuthToken());

        String filename = null;
        MediaType contentType = MediaType.parse(getContentResolver().getType(fileUri));

        File file = FileUtils.getFile(this, fileUri);

        if (file != null) {
            Log.v("imgur", "uploadFile: file = : " + file.toString());
            filename = file.getName();
        } else {
            Log.e("imgur", "uploadFile: file is null");
        }


        // create RequestBody instance from file
        /*RequestBody requestFile = RequestBody.create(
                        MediaType.parse(getContentResolver().getType(fileUri)),
                        file
                );*/
        RequestBody requestFile = RequestBody.create(contentType, inBytes);

        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", filename, requestFile);

        // finally, execute the request
        Call<ImgurBasicResponse> call = client.addImage(body);
        call.enqueue(new Callback<ImgurBasicResponse>() {
            @Override
            public void onResponse(Call<ImgurBasicResponse> call,
                                   Response<ImgurBasicResponse> response) {
                Log.v("Upload", "success");
                Log.v("imgur", response.toString());
                Log.v("imgur", "response Successful? "+Boolean.toString(response.isSuccessful()));
                ImgurBasicResponse body = response.body();
                if (response.isSuccessful() && response.body() != null && response.body().getSuccess()) {
                    Log.v("imgur", "Upload was a success");

                    ImgurImageListFragment gallery = (ImgurImageListFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_GALLERY);

                    if (gallery != null){
                        gallery.refreshList();
                    }
                }
            }

            @Override
            public void onFailure(Call<ImgurBasicResponse> call, Throwable t) {
                Log.e("Upload error:", t.getMessage());
            }
        });
    }

    // http://stackoverflow.com/a/1264737/7577505
    private byte[] readInputStream(InputStream in)
    {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        try {
            while ((nRead = in.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return buffer.toByteArray();
    }

    public void onDeleteButtonClicked(final ImgurImage item)
    {
        Log.v("imgur", "Image Delete Tapped!!");
        Log.v("imgur", item.toString());

        //http://stackoverflow.com/a/2478662
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Delete Image");
        builder.setMessage("Are you sure you want to permanently delete this image?");

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // actually delete the image
                deleteImage(item);

                dialog.dismiss();
            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                // Do nothing
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void deleteImage(final ImgurImage item)
    {
        createImgurClient(this.retrieveAuthToken());

        Call<Void> call = client.deleteImage("me", item.getDeletehash());

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                // The network call was a success and we got a response
                Log.e("imgur", "API DELETE request succeeded");
                Log.v("imgur", Boolean.toString(response.isSuccessful()));
                Log.v("imgur", Integer.toString(response.code()));
                Log.v("imgur", response.message());

                FragmentManager fragmentManager = getSupportFragmentManager();
                if (response.isSuccessful() && !fragmentManager.isDestroyed()) {
                    // go "back"
                    fragmentManager.popBackStackImmediate(FRAGMENT_TAG_IMAGE_VIEW, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                    // remove item from list and refresh view
                    ImgurImageListFragment gallery = (ImgurImageListFragment) fragmentManager.findFragmentByTag(FRAGMENT_TAG_GALLERY);

                    if (gallery != null){
                        gallery.removeImage(item);
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // the network call was a failure
                // TODO: handle error
                Log.e("imgur", "API DELETE request failed");
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


    private ImgurClient client;
    private void createImgurClient(ImgurAccessToken auth_token)
    {
        if (client == null) {
            client = ImgurServiceGenerator.createService(ImgurClient.class, auth_token.toAuthorizationHeader());
        }
    }
}
