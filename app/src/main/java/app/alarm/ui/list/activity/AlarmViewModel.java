package app.alarm.ui.list.activity;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

import java.util.List;

import app.alarm.core.data.db.AppDatabase;
import app.alarm.core.data.db.dao.AlarmDao;
import app.alarm.core.data.db.entities.Alarm;
import io.reactivex.Observable;

public class AlarmViewModel extends AndroidViewModel {
    private AlarmDao mAlarmDao;

    public AlarmViewModel(@NonNull Application application) {
        super(application);
        mAlarmDao = AppDatabase.getInstance(application).alarmDao();
    }

    public Observable<Integer> deleteAlarm(Alarm alarm) {
        return Observable.fromCallable(() -> mAlarmDao.deleteAlarm(alarm));
    }

    public Observable<Integer> deleteAlarmWithId(int id) {
        return Observable.fromCallable(() -> mAlarmDao.deleteAlarmById(id));
    }

    public Observable<Integer> updateAlarmByActive(long id, int active) {
        return Observable.fromCallable(() -> mAlarmDao.updateAlarmByActive(id, active));
    }

    public Observable<List<Alarm>> getListAlarms() {
        return Observable.fromCallable(() -> mAlarmDao.getAllAlarm());
    }

}
