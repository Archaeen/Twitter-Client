package de.andy.client.twitter.android;

import android.os.Parcel;
import android.os.Parcelable;

public class Tweet implements Parcelable{

    private String user;
    private String content;
    private double longitude;
    private double latitude;

    public Tweet() {
        this.user = user;
        this.content = content;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public Tweet(String user, String content, double longitude, double latitude) {
        this.user = user;
        this.content = content;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String getContent() {
        return content;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(user);
        parcel.writeString(content);
        parcel.writeDouble(longitude);
        parcel.writeDouble(latitude);
    }

    public static final Parcelable.Creator<Tweet> CREATOR = new Parcelable.Creator<Tweet>() {

        @Override
        public Tweet createFromParcel(Parcel parcel) {
            return new Tweet();
        }

        @Override
        public Tweet[] newArray(int i) {
            return new Tweet[i];
        }
    };
}
