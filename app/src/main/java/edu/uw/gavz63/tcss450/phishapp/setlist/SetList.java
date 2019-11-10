package edu.uw.gavz63.tcss450.phishapp.setlist;

import android.os.Parcel;
import android.os.Parcelable;
import java.io.Serializable;

/**
 * Class to encapsulate a Phish.net Set List. Building an Object requires a publish date and title.
 *
 * Optional fields include URL, teaser, and Author.
 *
 *
 * @author Charles Bryan, Gavin Montes
 * @version 14 September 2018
 */
public class SetList implements Serializable, Parcelable {

    private final String mDate;
    private final String mLocation;
    private final String mVenue;
    private final String mData;
    private final String mNotes;
    private final String mUrl;

    protected SetList (Parcel in) {
        mDate = in.readString();
        mLocation = in.readString();
        mVenue = in.readString();
        mData = in.readString();
        mNotes = in.readString();
        mUrl = in.readString();
    }

    public static final Creator<SetList> CREATOR = new Creator<SetList>() {
        @Override
        public SetList createFromParcel(Parcel in) {
            return new SetList(in);
        }

        @Override
        public SetList[] newArray(int size) {
            return new SetList[size];
        }
    };

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mDate);
        dest.writeString(mLocation);
        dest.writeString(mVenue);
        dest.writeString(mData);
        dest.writeString(mNotes);
        dest.writeString(mUrl);
    }

    /**
     * Helper class for building SetLists.
     *
     * @author Charles Bryan, Gavin Montes
     */
    public static class Builder {
        private final String mDate;
        private final String mLocation;
        private String mVenue = "";
        private String mData = "";
        private String mNotes = "";
        private String mUrl = "";


        /**
         * Constructs a new Builder.
         *
         * @param theDate the published date of the blog post
         * @param theLocation the title of the blog post
         */
        public Builder(String theDate, String theLocation) {
            this.mDate = theDate;
            this.mLocation = theLocation;
        }

        /**
         * Add an optional url for the full blog post.
         * @param val an optional url for the full blog post
         * @return the Builder of this BlogPost
         */
        public Builder addUrl(final String val) {
            mUrl = val;
            return this;
        }

        /**
         * Add an optional teaser for the full blog post.
         * @param val an optional url teaser for the full blog post.
         * @return the Builder of this BlogPost
         */
        public Builder addVenue(final String val) {
            mVenue = val;
            return this;
        }

        /**
         * Add an optional author of the blog post.
         * @param val an optional author of the blog post.
         * @return the Builder of this BlogPost
         */
        public Builder addData(final String val) {
            mData = val;
            return this;
        }

        /**
         * Add an optional author of the blog post.
         * @param val an optional author of the blog post.
         * @return the Builder of this BlogPost
         */
        public Builder addNotes(final String val) {
            mNotes = val;
            return this;
        }

        public SetList build() {
            return new SetList(this);
        }
    }

    private SetList(final Builder builder) {
        this.mDate = builder.mDate;
        this.mLocation = builder.mLocation;
        this.mVenue = builder.mVenue;
        this.mData = builder.mData;
        this.mNotes = builder.mNotes;
        this.mUrl = builder.mUrl;
    }

    public String getDate() { return mDate; }

    public String getLocation() { return mLocation; }

    public String getVenue() { return mVenue; }

    public String getData() { return mData; }

    public String getNotes() { return mNotes; }

    public String getUrl() { return mUrl; }
}
