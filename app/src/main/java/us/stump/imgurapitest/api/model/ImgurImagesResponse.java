package us.stump.imgurapitest.api.model;

import java.util.List;

/**
 * Created by jim on 2/11/2017.
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
