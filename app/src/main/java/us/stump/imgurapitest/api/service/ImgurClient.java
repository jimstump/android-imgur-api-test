package us.stump.imgurapitest.api.service;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import us.stump.imgurapitest.api.model.ImgurBasicResponse;
import us.stump.imgurapitest.api.model.ImgurImage;
import us.stump.imgurapitest.api.model.ImgurImagesResponse;

/**
 * Retrofit2 interface for the Imgur API.
 */
public interface ImgurClient {

    /**
     * Get a list of images for the given user
     * @param username The username for the user (or "me" for the currently logged in user)
     * @param page The page of data you want, for pagination (starting at 0)
     * @return A List of image objects
     */
    @GET("account/{username}/images/{page}")
    Call<ImgurImagesResponse> imagesForUser(@Path("username") String username, @Path("page") Integer page);

    /**
     * Get the data for an image, by id.
     * @param id The id for the image
     * @return Image object representing the given image
     */
    @GET("image/{id}")
    Call<ImgurImage> imageById(@Path("id") String id);

    /**
     * Upload a new image
     * @return
     */
    @Multipart
    @POST("image")
    Call<ImgurBasicResponse> addImage(@Part MultipartBody.Part image);

    /**
     * Delete a new image
     * @param username
     * @param deletehash
     * @return
     */
    @DELETE("account/{username}/image/{deletehash}")
    Call<Void> deleteImage(@Path("username") String username, @Path("deletehash") String deletehash);
}
