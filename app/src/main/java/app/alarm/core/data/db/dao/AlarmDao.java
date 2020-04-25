package app.alarm.core.data.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import app.alarm.core.data.db.entities.Alarm;

@Dao
public interface AlarmDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertAlarm(Alarm alarms);

    @Query("SELECT * FROM alarms")
    List<Alarm> getAllAlarm();

    @Delete
    int deleteAlarm(Alarm alarms);

    @Query("DELETE FROM alarms WHERE id = :id")
    int deleteAlarmById(int id);

    @Update
    int updateAlarm(Alarm alarms);

    @Query("SELECT * FROM alarms WHERE active > 0")
    List<Alarm> getAllAlarmsIsActive();

    @Query("UPDATE alarms SET active = :active WHERE id = :id")
    int updateAlarmByActive(long id, int active);

    @Query("UPDATE alarms SET alarm_date = :alertAlarmTime WHERE id = :id")
    int updateAlarmWithNewAlertAlarm(long id, long alertAlarmTime);
}
