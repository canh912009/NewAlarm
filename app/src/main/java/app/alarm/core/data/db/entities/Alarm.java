package app.alarm.core.data.db.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.content.Intent;
import android.os.Parcel;

import java.io.Serializable;

import app.alarm.utils.CalenderUtil;

@Entity(tableName = "alarms")
public class Alarm implements Serializable {

    private static final String TAG = "Alarm";

    public static final String ALARM_ALERT = "app.alarm.core.receiver.ALARM_ALERT";
    public static final String ALARM_DATA = "app.alarm.core.receiver.ALARM_DATA";
    public static final String IMPENDING_ALERT = "app.alarm.core.receiver.IMPENDING_ALERT";
    public static final String ALERT_ALARM_ID = "alertAlarmID";
    public static final String ACTION_ALARM_STOPPED_IN_ALERT = "app.alarm.core.receiver.ALARM_STOPPED_IN_ALERT";

    public static final String ALARM_DEFAULT_RINGTONE_URI = "content://media/internal/audio/media/48";
    public static final String ACTION_LOCAL_ALARM_ALERT_STOP = "app.alarm.core.receiver.ACTION_LOCAL_ALARM_ALERT_STOP";
    public static final String ACTION_LOCAL_TIMER_ALERT_START = "app.alarm.core.receiver.ACTION_LOCAL_TIMER_ALERT_START";

    public static final String ACTION_ALERT_STOP = "app.alarm.core.receiver.alarm.ACTION_ALERT_STOP";

    public static final int ALARM_METHOD_DEFAULT = 0;
    public static final int ALARM_METHOD_TAKE_A_PHOTO = 1;
    public static final int ALARM_METHOD_SHAKE_PHONE = 2;
    public static final int ALARM_METHOD_MATH_PROBLEMS = 3;

    public static final int ALERT_TYPE_ALERT = 1;

    public static final int ALARM_INACTIVE = 0;
    public static final int ALARM_ACTIVE = 1;
    public static final int ALARM_AT_ONCE = 0;

    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name = "active")
    private int mActive;
    @ColumnInfo(name = "alarm_name")
    private String mAlarmName;
    @ColumnInfo(name = "alarm_date")
    private long mAlarmDate;
    @ColumnInfo(name = "alarm_time")
    private int mAlarmTime;
    @ColumnInfo(name = "repeat_type")
    private int mRepeatType;
    @ColumnInfo(name = "alarm_type")
    private int mAlarmType;
    @ColumnInfo(name = "snooze_time")
    private int mSnoozeTime;
    @ColumnInfo(name = "alarm_settings")
    private String mAlarmSettings;
    @ColumnInfo(name = "uri_sound")
    private String mUriSound;

    public Alarm() {
    }

    public Alarm(int mActive, String mAlarmName, long mAlarmDate, int mAlarmTime, int mRepeatType, int mAlarmType, String mAlarmSettings, int mSnoozeTime, String mUriSound) {
        this.mActive = mActive;
        this.mAlarmName = mAlarmName;
        this.mAlarmDate = mAlarmDate;
        this.mAlarmTime = mAlarmTime;
        this.mRepeatType = mRepeatType;
        this.mAlarmType = mAlarmType;
        this.mSnoozeTime = mSnoozeTime;
        this.mUriSound = mUriSound;
        this.mAlarmSettings = mAlarmSettings;
    }

    public Alarm(long id, int mActive, String mAlarmName, long mAlarmDate, int mAlarmTime, int mRepeatType, int mAlarmType, String mAlarmSettings, int mSnoozeTime, String mUriSound) {
        this(mActive, mAlarmName, mAlarmDate, mAlarmTime, mRepeatType, mAlarmType, mAlarmSettings, mSnoozeTime, mUriSound);
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getActive() {
        return mActive;
    }

    public void setActive(int mActive) {
        this.mActive = mActive;
    }

    public String getAlarmName() {
        return mAlarmName;
    }

    public void setAlarmName(String mAlarmName) {
        this.mAlarmName = mAlarmName;
    }

    public long getAlarmDate() {
        return mAlarmDate;
    }

    public void setAlarmDate(long mAlertDate) {
        this.mAlarmDate = mAlertDate;
    }

    public int getAlarmTime() {
        return mAlarmTime;
    }

    public void setAlarmTime(int mAlarmTime) {
        this.mAlarmTime = mAlarmTime;
    }

    public int getRepeatType() {
        return mRepeatType;
    }

    public void setRepeatType(int mRepeatType) {
        this.mRepeatType = mRepeatType;
    }

    public int getAlarmType() {
        return mAlarmType;
    }

    public void setAlarmType(int mAlarmType) {
        this.mAlarmType = mAlarmType;
    }

    public int getSnoozeTime() {
        return mSnoozeTime;
    }

    public void setSnoozeTime(int mSnoozeTime) {
        this.mSnoozeTime = mSnoozeTime;
    }

    public String getAlarmSettings() {
        return mAlarmSettings;
    }

    public void setAlarmSettings(String mAlarmSettings) {
        this.mAlarmSettings = mAlarmSettings;
    }

    public String getUriSound() {
        return mUriSound;
    }

    public void setUriSound(String mUriSound) {
        this.mUriSound = mUriSound;
    }

    public void writeToParcel(Parcel dest) {
        dest.writeLong(id);
        dest.writeInt(mActive);
        dest.writeString(mAlarmName);
        dest.writeLong(mAlarmDate);
        dest.writeInt(mAlarmTime);
        dest.writeInt(mRepeatType);
        dest.writeInt(mAlarmType);
        dest.writeInt(mSnoozeTime);
        dest.writeString(mAlarmSettings);
        dest.writeString(mUriSound);
    }

    public void readFromIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        final byte[] alarmData = intent.getByteArrayExtra(Alarm.ALARM_DATA);

        if (alarmData != null) {
            Parcel in = Parcel.obtain();
            in.unmarshall(alarmData, 0, alarmData.length);
            in.setDataPosition(0);
            this.id = in.readLong();
            this.mActive = in.readInt();
            this.mAlarmName = in.readString();
            this.mAlarmDate = in.readLong();
            this.mAlarmTime = in.readInt();
            this.mRepeatType = in.readInt();
            this.mAlarmType = in.readInt();
            this.mSnoozeTime = in.readInt();
            this.mAlarmSettings = in.readString();
            this.mUriSound = in.readString();
            in.recycle();
        }
    }

    public boolean isOneTimeAlarm() {
        return (mRepeatType & 0x1111111) == Alarm.ALARM_AT_ONCE;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("mActive = ");
        builder.append(String.valueOf(mActive));

        builder.append(", mAlarmName = ");
        builder.append(String.valueOf(mAlarmName));

        builder.append(", mAlarmDate = ");
        builder.append(String.valueOf(CalenderUtil.convertMilisecondssToDetailTime(mAlarmDate)));

        builder.append(", mAlarmTime = ");
        builder.append(String.valueOf(mAlarmTime));

        builder.append(", mRepeatType = ");
        builder.append(String.valueOf(mRepeatType));

        builder.append(", mAlarmType = ");
        builder.append(String.valueOf(mAlarmType));

        builder.append(", mSnoozeTime = ");
        builder.append(String.valueOf(mSnoozeTime));

        builder.append(", mAlarmSettings = ");
        builder.append(String.valueOf(mAlarmSettings));

        builder.append(", mUriSound = ");
        builder.append(String.valueOf(mUriSound));

        return builder.toString();
    }
}