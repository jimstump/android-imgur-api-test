package us.stump.imgurapitest.api.model;

/**
 * GSON Model for a "basic" Imgur response
 *
 * This is the basic response for requests that return Integer data. If the POST request has a Basic model it will return the id.
 *
 * See: https://api.imgur.com/models/basic
 */

public class ImgurBasicResponseIntegerData extends ImgurBasicResponse {

    /**
     * Integer response
     */
    private Integer data;

    /**
     * Integer response
     */
    public Integer getData() {
        return data;
    }
}
