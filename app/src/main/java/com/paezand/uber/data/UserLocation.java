package com.paezand.uber.data;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

public class UserLocation implements Parcelable {

    private String userName;

    private Location location;

    public UserLocation(String userName, Location location) {
        this.userName = userName;
        this.location = location;
    }

    public String getUserName() {
        return userName;
    }

    public Location getLocation() {
        return location;
    }

    protected UserLocation(Parcel in) {
        userName = in.readString();
        location = in.readParcelable(Location.class.getClassLoader());
    }

    public static final Creator<UserLocation> CREATOR = new Creator<UserLocation>() {
        @Override
        public UserLocation createFromParcel(Parcel in) {
            return new UserLocation(in);
        }

        @Override
        public UserLocation[] newArray(int size) {
            return new UserLocation[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(userName);
        parcel.writeParcelable(location, i);
    }
}
