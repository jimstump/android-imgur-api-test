package us.stump.imgurapitest.api.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.util.Date;

@SuppressWarnings("unused")
/**
 * Model to hold our Imgur Access Token (and related parameters)
 */
public final class ImgurAccessToken implements Parcelable {
    /**
     * The access token/authorization token for use with the Imgur API
     */
    private final String access_token;

    /**
     * The type of access token that we have.
     *
     * The Imgur API supports user-specific tokens using the "Bearer" type and
     * app-specific token with the "Client-ID" type.
     */
    private final String token_type;

    /**
     * The number of seconds (relative to the time it was created) that the token is good for.
     */
    private final String expires_in;

    /**
     * A token that can be used to refresh this access token.
     *
     * A refresh token is used to generate a new Access Token with the same permission/scope.
     */
    private final String refresh_token;

    /**
     * The username for the user that logged in.
     */
    private final String account_username;

    /**
     * The opaque user ID of the user that logged in.
     */
    private final String account_id;

    /**
     * The Date and time that this Access Token was created.
     * Used as a reference point for expiration.
     */
    private final Date created_at;

    /**
     * Construct an Access Token instance from all of its parts.
     *
     * @param access_token The access token/authorization token used with the API
     * @param token_type The type of access token this is Bearer or Client-ID
     * @param expires_in Number of seconds that this token is valid for
     * @param refresh_token A token that can be used to refresh this access token.
     * @param account_username The username for the user this represents.
     * @param account_id The id of the user this represents.
     */
    public ImgurAccessToken(String access_token, String token_type, String expires_in, String refresh_token, String account_username, String account_id)
    {
        this.access_token = access_token;
        this.token_type = token_type;
        this.expires_in = expires_in;
        this.refresh_token = refresh_token;
        this.account_username = account_username;
        this.account_id = account_id;

        this.created_at = new Date();
    }

    /**
     * Get the access token/authorization token for use with the Imgur API
     * @return The token/authorization token for use with the Imgur API
     */
    public String getAccessToken() {
        return access_token;
    }

    /**
     * Life of the token in seconds
     * @return Life of the token in seconds
     */
    public String getExpiresIn() {
        return expires_in;
    }

    /**
     * Get the approximate Date that this token will expire at.
     * @return Date that this token will expire at.
     */
    public Date getExpires() {
        long uts = created_at.getTime();

        if (!TextUtils.isEmpty(expires_in))
        {
            uts += (Long.parseLong(expires_in)) * 1000;
        }
        else
        {
            // if we don't have an expires time, default to 28 days
            // although the documentation mentions "1 month", I have been getting tokens that last 28 days
            uts += (Long.parseLong("2419200")) * 1000;
        }

        return new Date(uts);
    }

    /**
     * Determine if the token is currently expired.
     * @return True if the token is expired, false otherwise.
     */
    public Boolean isExpired()
    {
        return isExpired(new Date());
    }

    /**
     * Determine if the token will be considered expired on the given Date.
     * @param now Date used to check against.
     * @return True if the token is considered expired on the given Date, false otherwise.
     */
    public Boolean isExpired(Date now) {
        return !now.before(getExpires());
    }

    /**
     * Get the type of access token that we have.
     *
     * The Imgur API supports user-specific tokens using the "Bearer" type and
     * app-specific token with the "Client-ID" type.
     *
     * @return The type of access token that we have
     */
    public String getTokenType() {
        return token_type;
    }

    /**
     * Get the token that can be used to refresh this access token.
     *
     * A refresh token is used to generate a new Access Token with the same permission/scope.
     * @return The refresh token
     */
    public String getRefreshToken() {
        return refresh_token;
    }

    /**
     * Get the username for the user that logged in.
     * @return Username for the user that logged in
     */
    public String getAccountUsername() {
        return account_username;
    }

    /**
     * Get the user ID of the user that logged in.
     * @return User ID of the user that logged in.
     */
    public String getAccountId() {
        return account_id;
    }

    /**
     * Generate the value of the Authorization header to sent to the server in order to use this access token.
     * @return Value of the Authorization header to use.
     */
    public String toAuthorizationHeader() {
        if (token_type.toLowerCase().equals("bearer"))
        {
            return "Bearer "+access_token;
        }
        else if(token_type.toLowerCase().equals("client-id"))
        {
            return "Client-ID "+access_token;
        }
        else
        {
            return token_type+" "+access_token;
        }
    }

    /**
     * Generate an appropriate String value for this instance.
     *
     * This currently the same as the toAuthorizationHeader() result.
     * @return String representation
     */
    public String toString()
    {
        return toAuthorizationHeader();
    }

    /**
     * Helper function to generate a ImgurAccessToken instance from the URL fragment provided by the API.
     *
     * This comes from the App Redirect URL after requesting a "response_type" of "token"
     * from the Imgur OAuth API.
     *
     * @param fragment The "fragment" or "hash" from the app redirect URL that contains the access token information.
     * @return A ImgurAccessToken instance representing the given access token.
     */
    public static ImgurAccessToken parseFromFragment(String fragment)
    {
        Uri fragmentQuery = Uri.parse("?"+fragment);

        String access_token = fragmentQuery.getQueryParameter("access_token");
        String token_type = fragmentQuery.getQueryParameter("token_type");
        String expires_in = fragmentQuery.getQueryParameter("expires_in");
        String refresh_token = fragmentQuery.getQueryParameter("refresh_token");
        String account_username = fragmentQuery.getQueryParameter("account_username");
        String account_id = fragmentQuery.getQueryParameter("account_id");

        return new ImgurAccessToken(access_token, token_type, expires_in, refresh_token, account_username, account_id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ImgurAccessToken that = (ImgurAccessToken) o;

        if (!access_token.equals(that.access_token)) return false;
        return token_type.equals(that.token_type);
    }

    @Override
    public int hashCode() {
        int result = access_token.hashCode();
        result = 31 * result + token_type.hashCode();
        return result;
    }

    /* Parcelable functions */

    /**
     * Describe the kinds of special objects contained in this Parcelable instance's marshaled representation.
     *
     * We don't have any special objects, so always 0;
     *
     * @return Bitmask on special object constants
     */
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     * @param out Parcel to write out to
     * @param flags Additional flags about how the object should be written
     */
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(access_token);
        out.writeString(token_type);
        out.writeString(expires_in);
        out.writeString(refresh_token);
        out.writeString(account_username);
        out.writeString(account_id);
        out.writeSerializable(created_at);
    }

    /**
     * Parcelable creator for ImgurAccessTokens
     */
    public static final Parcelable.Creator<ImgurAccessToken> CREATOR
            = new Parcelable.Creator<ImgurAccessToken>() {
        public ImgurAccessToken createFromParcel(Parcel in) {
            return new ImgurAccessToken(in);
        }

        public ImgurAccessToken[] newArray(int size) {
            return new ImgurAccessToken[size];
        }
    };

    /**
     * Private constructor for use with a Parcel
     * @param in The stored Parcel
     */
    private ImgurAccessToken(Parcel in) {
        access_token = in.readString();
        token_type = in.readString();
        expires_in = in.readString();
        refresh_token = in.readString();
        account_username = in.readString();
        account_id = in.readString();
        created_at = (Date) in.readSerializable();
    }
}
