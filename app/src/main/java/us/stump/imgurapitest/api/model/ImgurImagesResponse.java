package us.stump.imgurapitest.api.model;

import java.util.List;

/**
 * GSON Model for a "images" Imgur response
 *
 * This is the response that will be returned when making a request for the user's images.
 */

public class ImgurImagesResponse extends ImgurBasicResponse {

    /**
     * List of ImgurImage objects
     */
    private List<ImgurImage> data;

    /**
     * List of ImgurImage objects
     */
    public List<ImgurImage> getData() {
        return data;
    }
}
