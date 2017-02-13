package us.stump.imgurapitest.api.model;

/**
 * GSON Model for a "basic" Imgur response
 *
 * This is the basic response for requests that do not return data. If the POST request has a Basic model it will return the id.
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
