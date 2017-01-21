package com.zagorapps.utilities_suite.state.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by eyssy on 21/01/2017.
 */

public class ClipboardData implements Parcelable
{
    private String contents;
    private long dateCreatedMillis;

    public static final Creator<ClipboardData> CREATOR = new Creator<ClipboardData>()
    {
        @Override
        public ClipboardData createFromParcel(Parcel in)
        {
            return new ClipboardData(in);
        }

        @Override
        public ClipboardData[] newArray(int size)
        {
            return new ClipboardData[size];
        }
    };

    public String getContents()
    {
        return contents;
    }

    public long getDateCreatedMillis()
    {
        return dateCreatedMillis;
    }

    public ClipboardData(long dateCreatedMillis, String contents)
    {
        this.dateCreatedMillis = dateCreatedMillis;
        this.contents = contents;
    }

    protected ClipboardData(Parcel in)
    {
        dateCreatedMillis = in.readLong();
        contents = in.readString();
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeLong(dateCreatedMillis);
        dest.writeString(contents);
    }
}
