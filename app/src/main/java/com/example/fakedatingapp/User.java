package com.example.fakedatingapp;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class User implements Parcelable {
    private String userID;
    private String name;
    private int age;
    private boolean male;
    private @ServerTimestamp Date timestamp;
    private String g;
    private GeoPoint l;
    private double latitude, longitude;

    public User(String userID, String name, int age, boolean male, Date timestamp) {
        this.userID = userID;
        this.name = name;
        this.age = age;
        this.male = male;
        this.timestamp = timestamp;
    }

    public User() {

    }

    public User(String userID, String name, int age, boolean male, Date timestamp, String g, GeoPoint l) {
        this.userID = userID;
        this.name = name;
        this.age = age;
        this.male = male;
        this.timestamp = timestamp;
        this.g = g;
        this.l = l;
    }

    protected User(Parcel in) {
        userID = in.readString();
        name = in.readString();
        age = in.readInt();
        male = in.readInt() == 1;
        long tmpDate = in.readLong();
        this.timestamp = tmpDate == -1 ? null : new Date(tmpDate);
        g = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        l = new GeoPoint(latitude, longitude);
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public boolean isMale() {
        return male;
    }

    public void setMale(boolean male) {
        this.male = male;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getG() {
        return g;
    }

    public void setG(String g) {
        this.g = g;
    }

    public GeoPoint getL() {
        return l;
    }

    public void setL(GeoPoint l) {
        this.l = l;
    }

    @Override
    public String toString() {
        return "User{" +
                "userID='" + userID + '\'' +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", male=" + male +
                ", timestamp=" + timestamp +
                ", g='" + g + '\'' +
                ", l=" + l +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userID);
        dest.writeString(name);
        dest.writeInt(age);
        dest.writeInt(male ? 1 : 0);
        dest.writeLong(timestamp != null ? timestamp.getTime() : -1);
        dest.writeString(g);
        dest.writeDouble(l.getLatitude());
        dest.writeDouble(l.getLongitude());

    }
}