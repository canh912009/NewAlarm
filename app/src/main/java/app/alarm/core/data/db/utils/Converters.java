package app.alarm.core.data.db.utils;

import android.arch.persistence.room.TypeConverter;

import java.util.Date;

public class Converters {
    @TypeConverter
    public static Date fromTimestamp(long time) {
        return new Date(time);
    }

    @TypeConverter
    public static long dateToTimestamp(Date date) {
        return date.getTime();
    }
}
