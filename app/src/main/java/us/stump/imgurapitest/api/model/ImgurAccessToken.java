package us.stump.imgurapitest.api.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import java.util.Date;

/**
 * Model to hold our Imgur Access Token (are related parameters)
 */

public final class ImgurAccessToken implements Parcelable {
    private final String access_token;
    private final String token_type;
    private final String expires_in;
    private final String refresh_token;
    private final String account_username;
    private final String account_id;

    private final Date created_at;

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

    public String getAccessToken() {
        return access_token;
    }

    /**
     * Life of the token in seconds
     * @return
     */
    public String getExpiresIn() {
        return expires_in;
    }

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
            uts += 2419200 * 1000;
        }

        return new Date(uts);
    }

    public Boolean isExpired()
    {
        return isExpired(new Date());
    }

    public Boolean isExpired(Date now) {
        return !now.before(getExpires());
    }

    public String getTokenType() {
        return token_type;
    }

    public String getRefreshToken() {
        return refresh_token;
    }

    public String getAccountUsername() {
        return account_username;
    }

    public String getAccountId() {
        return account_id;
    }

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

    public String toString()
    {
        return toAuthorizationHeader();
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
     *
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
