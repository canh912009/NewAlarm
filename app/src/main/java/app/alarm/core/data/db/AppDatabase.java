package app.alarm.core.data.db;


import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import android.content.Context;

import app.alarm.core.data.db.dao.AlarmDao;
import app.alarm.core.data.db.entities.Alarm;
import app.alarm.core.data.db.utils.Converters;
import app.alarm.core.data.db.utils.DBMigration;

@Database(entities = {Alarm.class}, version = 1, exportSchema = false)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase sAppDatabase;

    private static final String DATABASE_NAME = "Alarms.db";

    public abstract AlarmDao alarmDao();

    public static AppDatabase getInstance(Context context) {
        if (sAppDatabase == null) {
            sAppDatabase = Room.databaseBuilder(context, AppDatabase.class, DATABASE_NAME)
                    .addMigrations(DBMigration.mMigration)
                    .allowMainThreadQueries()
                    .build();
        }

        return sAppDatabase;
    }
}
