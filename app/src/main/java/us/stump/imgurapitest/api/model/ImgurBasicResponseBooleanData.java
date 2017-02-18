package us.stump.imgurapitest.api.model;

/**
 * GSON Model for a "basic" Imgur response
 *
 * This is the basic response for requests that return Boolean data.
 *
 * See: https://api.imgur.com/models/basic
 */

public class ImgurBasicResponseBooleanData extends ImgurBasicResponse {

    /**
     * Boolean response
     */
    private Boolean data;

    /**
     * Boolean response
     */
    public Boolean getData() {
        return data;
    }
}
