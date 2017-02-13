package us.stump.imgurapitest.api.model;

/**
 * GSON Model for a "basic" Imgur response
 *
 * This is the basic response for requests that do not return data. If the POST request has a Basic model it will return the id.
 *
 * See: https://api.imgur.com/models/basic
 */

public class ImgurBasicResponse {

    /**
     * Was the request successful
     */
    private Boolean success;

    /**
     * Was the request successful
     */
    public Boolean getSuccess() {
        return success;
    }

    /**
     * HTTP Status Code
     */
    private Integer status;

    /**
     * HTTP Status Code
     */
    public Integer getStatus() {
        return status;
    }
}
