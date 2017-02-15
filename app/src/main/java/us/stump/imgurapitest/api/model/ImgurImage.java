package us.stump.imgurapitest.api.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * GSON Model for a Imgur Image object
 * See: https://api.imgur.com/models/image
 */

public class ImgurImage implements Parcelable {

    /**
     * The ID for the image
     */
    private String id;

    /**
     * The ID for the image
     */
    public String getId() {
        return id;
    }

    /**
     * The title of the image.
     */
    private String title;

    /**
     * The title of the image.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Description of the image.
     */
    private String description;

    /**
     * Description of the image.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Time uploaded, epoch time
     */
    private Integer datetime;

    /**
     * Time uploaded, epoch time
     */
    public Integer getDatetime() {
        return datetime;
    }

    /**
     * Time uploaded, as a Date
     */
    public Date getDatetimeAsDate() {
        long dateTimeMilliseconds = datetime * 1000;

        return new Date(dateTimeMilliseconds);
    }

    /**
     * Image MIME type.
     */
    private String type;

    /**
     * Image MIME type.
     */
    public String getType() {
        return type;
    }

    /**
     * is the image animated
     */
    private Boolean animated;

    /**
     * is the image animated
     */
    public Boolean getAnimated() {
        return animated;
    }

    /**
     * The width of the image in pixels.
     */
    private Integer width;

    /**
     * The width of the image in pixels.
     */
    public Integer getWidth() {
        return width;
    }

    /**
     * The height of the image in pixels
     */
    private Integer height;

    /**
     * The height of the image in pixels
     */
    public Integer getHeight() {
        return height;
    }

    /**
     * The size of the image in bytes
     */
    private Integer size;

    /**
     * The size of the image in bytes
     */
    public Integer getSize() {
        return size;
    }

    /**
     * The number of image views
     */
    private Integer views;

    /**
     * The number of image views
     */
    public Integer getViews() {
        return views;
    }

    /**
     * Bandwidth consumed by the image in bytes
     */
    private Double bandwidth;

    /**
     * Bandwidth consumed by the image in bytes
     */
    public Double getBandwidth() {
        return bandwidth;
    }

    /**
     * OPTIONAL, the deletehash, if you're logged in as the image owner
     */
    private String deletehash;

    /**
     * OPTIONAL, the deletehash, if you're logged in as the image owner
     */
    public String getDeletehash() {
        return deletehash;
    }

    /**
     * OPTIONAL, the original filename, if you're logged in as the image owner
     */
    private String name;

    /**
     * OPTIONAL, the original filename, if you're logged in as the image owner
     */
    public String getName() {
        return name;
    }

    /**
     * If the image has been categorized by our backend then this will contain the section the image belongs in. (funny, cats, adviceanimals, wtf, etc)
     */
    private String section;

    /**
     * If the image has been categorized by our backend then this will contain the section the image belongs in. (funny, cats, adviceanimals, wtf, etc)
     */
    public String getSection() {
        return section;
    }

     /**
     * The direct link to the the image. (Note: if fetching an animated GIF that was over 20MB in original size, a .gif thumbnail will be returned)
     */
    private String link;

    /**
     * The direct link to the the image. (Note: if fetching an animated GIF that was over 20MB in original size, a .gif thumbnail will be returned)
     */
    public String getLink() {
        return link;
    }

    /**
     * OPTIONAL, The .gifv link. Only available if the image is animated and type is 'image/gif'.
     */
    private String gifv;

    /**
     * OPTIONAL, The .gifv link. Only available if the image is animated and type is 'image/gif'.
     */
    public String getGifv() {
        return gifv;
    }

    /**
     * OPTIONAL, The direct link to the .mp4. Only available if the image is animated and type is 'image/gif'.
     */
    private String mp4;

    /**
     * OPTIONAL, The direct link to the .mp4. Only available if the image is animated and type is 'image/gif'.
     */
    public String getMp4() {
        return mp4;
    }

    /**
     * The Content-Length of the .mp4. Only available if the image is animated and type is 'image/gif'. Note that a zero value (0) is possible if the video has not yet been generated
     */
    private Integer mp4_size;

    /**
     * The Content-Length of the .mp4. Only available if the image is animated and type is 'image/gif'. Note that a zero value (0) is possible if the video has not yet been generated
     */
    public Integer getMp4_size() {
        return mp4_size;
    }

    /**
     * OPTIONAL, Whether the image has a looping animation. Only available if the image is animated and type is 'image/gif'.
     */
    private Boolean looping;

    /**
     * OPTIONAL, Whether the image has a looping animation. Only available if the image is animated and type is 'image/gif'.
     */
    public Boolean getLooping() {
        return looping;
    }

    /**
     * Indicates if the current user favorited the image. Defaults to false if not signed in.
     */
    private Boolean favorite;

    /**
     * Indicates if the current user favorited the image. Defaults to false if not signed in.
     */
    public Boolean getFavorite() {
        return favorite;
    }

    /**
     * Indicates if the image has been marked as nsfw or not. Defaults to null if information is not available.
     */
    private Boolean nsfw;

    /**
     * Indicates if the image has been marked as nsfw or not. Defaults to null if information is not available.
     */
    public Boolean getNsfw() {
        return nsfw;
    }

    /**
     * The current user's vote on the album. null if not signed in, if the user hasn't voted on it, or if not submitted to the gallery.
     */
    private String vote;

    /**
     * The current user's vote on the album. null if not signed in, if the user hasn't voted on it, or if not submitted to the gallery.
     */
    public String getVote() {
        return vote;
    }

    /**
     * True if the image has been submitted to the gallery, false if otherwise.
     */
    private Boolean in_gallery;

    /**
     * True if the image has been submitted to the gallery, false if otherwise.
     */
    public Boolean getIn_gallery() {
        return in_gallery;
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
        out.writeString(id);
        out.writeString(title);
        out.writeString(description);
        out.writeInt(datetime);
        out.writeString(type);
        out.writeInt(animated ? 1 : 0);
        out.writeInt(width);
        out.writeInt(height);
        out.writeInt(size);
        out.writeInt(views);
        out.writeDouble(bandwidth);
        out.writeString(deletehash);
        out.writeString(name);
        out.writeString(section);
        out.writeString(link);
        out.writeString(gifv);
        out.writeString(mp4);
        out.writeInt(mp4_size);
        out.writeInt(looping ? 1 : 0);
        out.writeInt(favorite ? 1 : 0);
        out.writeInt(nsfw ? 1 : 0);
        out.writeString(vote);
        out.writeInt(in_gallery ? 1 : 0);
    }

    /**
     *
     */
    public static final Parcelable.Creator<ImgurImage> CREATOR
            = new Parcelable.Creator<ImgurImage>() {
        public ImgurImage createFromParcel(Parcel in) {
            return new ImgurImage(in);
        }

        public ImgurImage[] newArray(int size) {
            return new ImgurImage[size];
        }
    };

    /**
     * Private constructor for use with a Parcel
     * @param in The stored Parcel
     */
    private ImgurImage(Parcel in) {
        id = in.readString();
        title = in.readString();
        description = in.readString();
        datetime = in.readInt();
        type = in.readString();
        animated = (in.readInt() == 1);
        width = in.readInt();
        height = in.readInt();
        size = in.readInt();
        views = in.readInt();
        bandwidth = in.readDouble();
        deletehash = in.readString();
        name = in.readString();
        section = in.readString();
        link = in.readString();
        gifv = in.readString();
        mp4 = in.readString();
        mp4_size = in.readInt();
        looping = (in.readInt() == 1);
        favorite = (in.readInt() == 1);
        nsfw = (in.readInt() == 1);
        vote = in.readString();
        in_gallery = (in.readInt() == 1);
    }
}
