package us.stump.imgurapitest;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
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
    /**
     * Tag for our login button fragment
     */
    private static final String FRAGMENT_TAG_LOGIN = "login";

    /**
     * Tag for our Imgur OAuth 2 login flow fragment
     */
    private static final String FRAGMENT_TAG_OAUTH_LOGIN = "oauthlogin";

    /**
     * Tag for our image gallery fragment
     */
    private static final String FRAGMENT_TAG_GALLERY = "gallery";

    /**
     * Tag for our single image view fragment
     */
    private static final String FRAGMENT_TAG_IMAGE_VIEW = "imageview";

    /**
     * Key used to store our access token in SharedPreferences.
     */
    private static final String SHARED_PREF_ACCESS_TOKEN = "imgurAccessToken";

    /**
     * Constant representing our image picker request
     */
    private final int PICK_IMAGE_REQUEST = 1;

    /**
     * Constant representing our READ_EXTERNAL_STORAGE permission request
     */
    private final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 4;

    /**
     * Stores state information related to whether we already showed the user an explanation as to
     * why we need the external storage permission
     */
    private boolean alreadyExplainedReadExternalStorage = false;

    /**
     * ImgurClient that is used to make requests to the Imgur API.
     */
    private ImgurClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // setup our action bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // If we're being restored from a previous state,
        // then we don't need to do anything and should return or else
        // we could end up with overlapping fragments.
        if (savedInstanceState != null) {
            return;
        }

        // check to see if we have an access token
        ImgurAccessToken accessToken = retrieveAccessToken();

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
        //Log.v("imgur", "onCreateOptionsMenu");

        // IMPORTANT: If you don't call the super call, long presses in the WebView cause the app to crash
        super.onCreateOptionsMenu(menu);

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //Log.v("imgur", "onPrepareOptionsMenu");
        super.onPrepareOptionsMenu(menu);

        // These are the button in the action bar that change visibility depending on which view we are on
        MenuItem addImageButton = menu.findItem(R.id.action_add_image);
        MenuItem refreshButton = menu.findItem(R.id.action_refresh);
        MenuItem logoutButton = menu.findItem(R.id.action_logout);

        // The action bar toolbar, which we'll change the layout parameters of depending on which view we are on
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment f;

        if ((f = fragmentManager.findFragmentByTag(FRAGMENT_TAG_LOGIN)) != null && f.isVisible()) {
            // main login button screen, hide all of the buttons since we aren't logged in
            //Log.v("imgur", "we are on the main login screen (the default screen)");
            if (addImageButton != null) {
                addImageButton.setVisible(false);
            }
            if (refreshButton != null) {
                refreshButton.setVisible(false);
            }
            if (logoutButton != null) {
                logoutButton.setVisible(false);
            }
            if (toolbar != null) {
                AppBarLayout.LayoutParams layoutParams = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();

                if (layoutParams != null) {
                    // always show the app bar
                    layoutParams.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
                }
            }
        } else if((f = fragmentManager.findFragmentByTag(FRAGMENT_TAG_OAUTH_LOGIN)) != null && f.isVisible()) {
            // OAuth 2 login screen, hide all of the buttons since we aren't logged in
            //Log.v("imgur", "we are on the imgur OAuth login screen");
            if (addImageButton != null) {
                addImageButton.setVisible(false);
            }
            if (refreshButton != null) {
                refreshButton.setVisible(false);
            }
            if (logoutButton != null) {
                logoutButton.setVisible(false);
            }
            if (toolbar != null) {
                AppBarLayout.LayoutParams layoutParams = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();

                if (layoutParams != null) {
                    // always show the app bar
                    layoutParams.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
                }
            }
        } else if((f = fragmentManager.findFragmentByTag(FRAGMENT_TAG_GALLERY)) != null && f.isVisible()) {
            // Image gallery, show all of the buttons
            //Log.v("imgur", "we are on the image gallery screen");
            if (addImageButton != null) {
                addImageButton.setVisible(true);
            }
            if (refreshButton != null) {
                refreshButton.setVisible(true);
            }
            if (logoutButton != null) {
                logoutButton.setVisible(true);
            }
            if (toolbar != null) {
                AppBarLayout.LayoutParams layoutParams = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();

                if (layoutParams != null) {
                    // collapse the app bar on scroll
                    layoutParams.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
                }
            }
        } else if((f = fragmentManager.findFragmentByTag(FRAGMENT_TAG_IMAGE_VIEW)) != null && f.isVisible()) {
            // Single Image view, hide the "add" and "refresh" buttons since they are only relevant for the image gallery
            //Log.v("imgur", "we are on the single image screen");
            if (addImageButton != null) {
                addImageButton.setVisible(false);
            }
            if (refreshButton != null) {
                refreshButton.setVisible(false);
            }
            if (logoutButton != null) {
                logoutButton.setVisible(true);
            }
            if (toolbar != null) {
                AppBarLayout.LayoutParams layoutParams = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();

                if (layoutParams != null) {
                    // always show the app bar
                    layoutParams.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
                }
            }
        } else {
            //Log.v("imgur", "what screen is this?");
            if (addImageButton != null) {
                addImageButton.setVisible(false);
            }
            if (refreshButton != null) {
                refreshButton.setVisible(false);
            }
            if (logoutButton != null) {
                logoutButton.setVisible(false);
            }
            if (toolbar != null) {
                AppBarLayout.LayoutParams layoutParams = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();

                if (layoutParams != null) {
                    // always show the app bar
                    layoutParams.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
                }
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

            case R.id.action_refresh:
                // User chose the "Refresh List" item
                Log.v("imgur", "Refresh!");

                onRefreshListButtonClicked();
                return true;

            case R.id.action_logout:
                // User chose the "Logout" item
                Log.v("imgur", "Logout!");

                onLogoutButtonClicked();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Handle the "Add Image" button click/tap event.
     *
     * This creates a ACTION_GET_CONTENT Intent requesting an image that we will upload to Imgur.
     */
    private void onAddButtonClicked()
    {
        Log.v("imgur", "onAddButtonClicked");
        // check for permission first
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.v("imgur", "need to ask for permission");

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) && !alreadyExplainedReadExternalStorage) {
                Log.v("imgur", "ask for explanation...");

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                toastErrorMessage("External Storage is needed to access images to upload");
                alreadyExplainedReadExternalStorage = true;

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            }
        } else {
            Log.v("imgur", "permissions should be granted, requesting image");
            doAddImageIntent();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                Log.v("imgur", "permission result for external storage");
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.v("imgur", "permission was granted");

                    // permission was granted
                    doAddImageIntent();

                } else {
                    // permission denied
                    toastErrorMessage("External Storage is needed to access images to upload");
                }
                return;
            }
        }
    }

    /**
     * Actually request an image from the system
     */
    private void doAddImageIntent()
    {
        Log.v("imgur", "requesting an image from the system");
        Intent intent = new Intent();
        // Show only images, no videos or anything else
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        // Always show the chooser (if there are multiple options available)
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    /**
     * Handle the "Refresh List" button click/tap event.
     *
     * This is force a refresh of the user's image gallery.
     */
    private void onRefreshListButtonClicked()
    {
        ImgurImageListFragment gallery = (ImgurImageListFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_GALLERY);

        if (gallery != null){
            gallery.refreshList();
        }
    }

    /**
     * Handle the "Logout" button click/tap event.
     *
     * After confirmation from the user, this will log the user out of Imgur.
     */
    private void onLogoutButtonClicked()
    {
        // Make sure the user really wants to logout before throwing away their access token

        // See: http://stackoverflow.com/a/2478662
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(getString(R.string.confirm_logout_title));
        builder.setMessage(getString(R.string.confirm_logout_message));

        builder.setPositiveButton(getString(R.string.confirm_action_yes), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // log the user out
                logout();

                dialog.dismiss();
            }
        });

        builder.setNegativeButton(getString(R.string.confirm_action_no), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Log the user out.
     *
     * This clears the access token and the backstack and then shows the main login button view.
     */
    private void logout()
    {
        clearAccessToken();

        clearBackStack();

        LoginButtonFragment newFragment = new LoginButtonFragment();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and DO NOT add the transaction to the back stack since we don't want the user to
        // be able to navigate back
        transaction.replace(R.id.fragment_container, newFragment, FRAGMENT_TAG_LOGIN);

        // Commit the transaction
        transaction.commit();
    }

    /**
     * Helper function that pops everything off the backstack
     */
    private void clearBackStack() {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        while (fragmentManager.getBackStackEntryCount() != 0) {
            fragmentManager.popBackStackImmediate();
        }
    }

    /**
     * The main login button was clicked.
     *
     * Show the Imgur OAuth 2 login screen.
     */
    public void onLoginButtonClicked()
    {
        Log.v("imgur", "Login Clicked!!");

        // pull our Imgur API app's app_id and callback url
        String imgurAppId = Constants.getStoredSecret(Constants.SECRET_IMGUR_APP_ID);
        String imgurAppRedirect = Constants.getStoredSecret(Constants.SECRET_IMGUR_APP_CALLBACK_URL);

        // create the Imgur login fragment
        // this is the fragment that will perform the OAuth 2 handshake and give us an access token
        ImgurLoginFragment newFragment = ImgurLoginFragment.newInstance(imgurAppId, imgurAppRedirect);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.fragment_container, newFragment, FRAGMENT_TAG_OAUTH_LOGIN);
        transaction.addToBackStack(FRAGMENT_TAG_OAUTH_LOGIN);

        // Commit the transaction
        transaction.commit();
    }

    /**
     * Clear the access token that is stored in SharedPreferences.
     */
    private void clearAccessToken()
    {
        Log.v("imgur", "clearAccessToken");
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(SHARED_PREF_ACCESS_TOKEN);
        editor.apply();
    }

    /**
     * Store the given access token in SharedPreferences.
     * @param accessToken The access token to store.
     */
    private void storeAccessToken(ImgurAccessToken accessToken)
    {
        // We are going to store our ImgurAccessToken in SharedPreferences as a serialized JSON string.
        // This is the easiest way to serialized our object for SharedPreferences storage.
        Log.v("imgur", "Storing token: "+accessToken.toString());

        // convert the ImgurAccessToken instance to JSON
        Gson gson = new Gson();
        String authTokenJson = gson.toJson(accessToken);
        Log.v("imgur", "token as JSON: "+authTokenJson);

        // store the JSON string in SharedPreferences
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(SHARED_PREF_ACCESS_TOKEN, authTokenJson);
        editor.apply();
    }

    /**
     * Retrieve the access token from SharedPreferences.
     * @return The access token that was stored, or null if nothing was stored.
     */
    private ImgurAccessToken retrieveAccessToken()
    {
        ImgurAccessToken accessToken = null;

        // grab the JSON string from SharedPreferences
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String authTokenJson = settings.getString(SHARED_PREF_ACCESS_TOKEN, null);
        Log.v("imgur", "Retrieving token as JSON: "+authTokenJson);

        // if we found an access token, convert hydrate a ImgurAccessToken instance
        if (!TextUtils.isEmpty(authTokenJson)) {
            Gson gson = new Gson();
            try {
                accessToken = gson.fromJson(authTokenJson, ImgurAccessToken.class);
                Log.v("imgur", "token as object: "+accessToken);
            }
            catch (JsonSyntaxException e) {
                Log.e("imgur", "Error unserializing access token from storage - "+e.toString());
            }
        }

        return accessToken;
    }

    /**
     * Handle getting a new access token from the OAuth 2 login flow or SharedPreferences.
     *
     * Show the image gallery screen.
     * @param accessToken The new Imgur access token
     */
    public void OnImgurTokenReceived(ImgurAccessToken accessToken)
    {
        Log.v("imgur", "Received token: "+accessToken.toString());

        // If the token has expired (probably because it came from SharedPreferences)
        // log the user out and then have him/her log in again.
        if (accessToken.isExpired()) {
            toastErrorMessage(getString(R.string.access_token_expired));
            Log.v("imgur", "Token Is expired!");
            logout();
            return;
        }

        // store the access token for future reference
        storeAccessToken(accessToken);

        // clear the backstack since we don't want the user to hit the back button
        // and end up in the OAuth 2 login flow (or at the main login button screen)
        // that is what the logout button is for.
        clearBackStack();

        ImgurImageListFragment newFragment = ImgurImageListFragment.newInstance(accessToken);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and DO NOT add the transaction to the back stack since we don't want the user to
        // be able to navigate back
        transaction.replace(R.id.fragment_container, newFragment, FRAGMENT_TAG_GALLERY);

        // Commit the transaction
        transaction.commit();
    }

    /**
     * Handle selecting an image from the image gallery.
     *
     * Show the single image screen.
     * @param item The image that was selected.
     */
    public void onListFragmentInteraction(ImgurImage item)
    {
        Log.v("imgur", "Image Tapped!!");

        FullScreenImageFragment newFragment = FullScreenImageFragment.newInstance(item);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.fragment_container, newFragment, FRAGMENT_TAG_IMAGE_VIEW);
        transaction.addToBackStack(FRAGMENT_TAG_IMAGE_VIEW);

        // Commit the transaction
        transaction.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // if this is the result of our image picker request and we have an image, upload it to Imgur
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            Log.v("imgur", "Received image: "+uri.toString());

            uploadFile(uri);
        }
    }

    /**
     * Upload the image represented by the Uri to the user's Imgur account.
     * @param fileUri Uri representing the image in the Storage Access Framework (SAF)
     */
    private void uploadFile(Uri fileUri) {
        Log.v("imgur", "uploadFile: fileuri = "+fileUri.toString());

        // Since this image might not actually be backed by a file,
        // we can't use the File class to get its contents
        // Instead we use an InputStream to read in the image.
        InputStream in;
        try {
            in = getContentResolver().openInputStream(fileUri);
        } catch (FileNotFoundException e) {
            toastErrorMessage(getString(R.string.image_read_failed));
            e.printStackTrace();
            return;
        }

        if (in == null) {
            toastErrorMessage(getString(R.string.image_read_failed));
            Log.e("imgur", "InputStream is null");
            return;
        }

        // read the bytes of the image into a byte array
        byte[] inBytes = readInputStream(in);

        if (inBytes == null) {
            toastErrorMessage(getString(R.string.image_read_failed));
            Log.e("imgur", "inBytes is null");
            return;
        } else if (inBytes.length == 0) {
            toastErrorMessage(getString(R.string.image_file_empty));
            Log.e("imgur", "inBytes is empty");
            return;
        }

        // create our Imgur API client with our stored access token
        ImgurAccessToken accessToken = retrieveAccessToken();

        if (accessToken == null) {
            toastErrorMessage(getString(R.string.access_token_expired));
            Log.e("imgur", "access token is null during image upload");
            logout();
            return;
        }

        createImgurClient(accessToken);

        // Lookup the content type of the image we are going to upload
        MediaType contentType = MediaType.parse(getContentResolver().getType(fileUri));

        // see if we can get the original filename for the image
        String filename = null;
        File file = FileUtils.getFile(this, fileUri);

        if (file != null) {
            Log.v("imgur", "uploadFile: file = : " + file.toString());
            filename = file.getName();
        } else {
            Log.e("imgur", "uploadFile: file is null");
        }

        // create RequestBody instance from our byte array
        RequestBody requestFile = RequestBody.create(contentType, inBytes);

        // Generate the multipart form data for our request body
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", filename, requestFile);

        // finally, execute the image upload request
        Call<ImgurBasicResponse> call = client.addImage(body);
        call.enqueue(new Callback<ImgurBasicResponse>() {
            @Override
            public void onResponse(Call<ImgurBasicResponse> call,
                                   Response<ImgurBasicResponse> response) {
                Log.v("imgur", "Image upload complete");

                ImgurBasicResponse body = response.body();
                if (response.isSuccessful() && body != null && body.getSuccess()) {
                    Log.v("imgur", "Upload was a success");

                    // tell the image gallery to refresh, which will load the new image
                    ImgurImageListFragment gallery = (ImgurImageListFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_GALLERY);

                    if (gallery != null){
                        gallery.refreshList();
                    }
                } else {
                    toastErrorMessage(getString(R.string.image_upload_failed));
                }
            }

            @Override
            public void onFailure(Call<ImgurBasicResponse> call, Throwable t) {
                toastErrorMessage(getString(R.string.image_upload_failed));
                Log.e("Upload error:", (t != null) ? t.getMessage() : "null");
            }
        });
    }

    /**
     * Read the InputStream into a byte array.
     *
     * See: http://stackoverflow.com/a/1264737/7577505
     * @param in InputStream to read the data from
     * @return A byte array of the data from the InputStream
     */
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

    /**
     * Handle deleting a single image.
     *
     * After confirmation from the user, this will permanently delete the image from Imgur.
     * @param item The image we want to delete
     */
    public void onDeleteButtonClicked(final ImgurImage item)
    {
        Log.v("imgur", "Image Delete Tapped!!");
        Log.v("imgur", item.toString());

        // See: http://stackoverflow.com/a/2478662
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(getString(R.string.confirm_deleteimage_title));
        builder.setMessage(getString(R.string.confirm_deleteimage_message));

        builder.setPositiveButton(getString(R.string.confirm_action_yes), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // actually delete the image
                deleteImage(item);

                dialog.dismiss();
            }
        });

        builder.setNegativeButton(getString(R.string.confirm_action_no), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Permanently delete the given image from Imgur.
     * @param item The image to delete.
     */
    private void deleteImage(final ImgurImage item)
    {
        // create our Imgur API client with our stored access token
        ImgurAccessToken accessToken = retrieveAccessToken();

        if (accessToken == null) {
            toastErrorMessage(getString(R.string.access_token_expired));
            Log.e("imgur", "access token is null during image delete");
            logout();
            return;
        }

        String imageDeleteHash = item.getDeletehash();

        if (imageDeleteHash == null) {
            toastErrorMessage(getString(R.string.image_no_deletehash));
            Log.e("imgur", "image delete hash is null");
            return;
        }

        createImgurClient(accessToken);

        Call<Void> call = client.deleteImage("me", imageDeleteHash);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                // The network call was a success and we got a response
                Log.e("imgur", "API DELETE request complete");

                FragmentManager fragmentManager = getSupportFragmentManager();
                if (response.isSuccessful() && !fragmentManager.isDestroyed()) {
                    Log.e("imgur", "request was successful");

                    // go "back" to the image gallery
                    fragmentManager.popBackStackImmediate(FRAGMENT_TAG_IMAGE_VIEW, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                    // remove item from the list and refresh the image gallery view
                    ImgurImageListFragment gallery = (ImgurImageListFragment) fragmentManager.findFragmentByTag(FRAGMENT_TAG_GALLERY);

                    if (gallery != null){
                        gallery.removeImage(item);
                    }
                } else {
                    toastErrorMessage(getString(R.string.image_delete_failed));
                    Log.e("imgur", "request failed");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // the network call was a failure
                toastErrorMessage(getString(R.string.image_delete_failed));
                Log.e("Delete error:", (t != null) ? t.getMessage() : "null");
            }
        });
    }

    /**
     * Displays the given error message to the user.
     * @param error The error message to display.
     */
    public void toastErrorMessage(String error) {
        Snackbar.make(findViewById(R.id.main_content), error, Snackbar.LENGTH_LONG)
                .show();
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
}
