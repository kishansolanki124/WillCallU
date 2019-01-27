package app.com.willcallu.room_db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "profile_table")
public class Profile {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    @ColumnInfo(name = "name")
    public String mName;

    @NonNull
    @ColumnInfo(name = "image")
    public String mImage;

    @NonNull
    @ColumnInfo(name = "start_time")
    public String mStartTime;

    @NonNull
    @ColumnInfo(name = "end_time")
    public String mEndTime;
    @NonNull
    @ColumnInfo(name = "week_days")
    public String mWeekDays;
    @NonNull
    @ColumnInfo(name = "message")
    public String mMessage;

    public Profile(@NonNull String mName, @NonNull String mImage, @NonNull String mStartTime,
                   @NonNull String mEndTime, @NonNull String mWeekDays, @NonNull String mMessage) {
        this.mName = mName;
        this.mImage = mImage;
        this.mStartTime = mStartTime;
        this.mEndTime = mEndTime;
        this.mWeekDays = mWeekDays;
        this.mMessage = mMessage;
    }

    @NonNull
    public String getMName() {
        return mName;
    }

    public void setMName(@NonNull String mName) {
        this.mName = mName;
    }

    @NonNull
    public String getMImage() {
        return mImage;
    }

    public void setMImage(@NonNull String mImage) {
        this.mImage = mImage;
    }

    @NonNull
    public String getMStartTime() {
        return mStartTime;
    }

    public void setMStartTime(@NonNull String mStartTime) {
        this.mStartTime = mStartTime;
    }

    @NonNull
    public String getMEndTime() {
        return mEndTime;
    }

    public void setMEndTime(@NonNull String mEndTime) {
        this.mEndTime = mEndTime;
    }

    @NonNull
    public String getMWeekDays() {
        return mWeekDays;
    }

    public void setMWeekDays(@NonNull String mWeekDays) {
        this.mWeekDays = mWeekDays;
    }

    @NonNull
    public String getMMessage() {
        return mMessage;
    }

    public void setMMessage(@NonNull String mMessage) {
        this.mMessage = mMessage;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}