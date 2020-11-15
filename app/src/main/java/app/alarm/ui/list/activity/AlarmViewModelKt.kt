package app.alarm.ui.list.activity

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.alarm.core.data.db.AppDatabase
import app.alarm.core.data.db.dao.AlarmDao
import app.alarm.core.data.db.entities.Alarm
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class AlarmViewModelKt(application: Application) : AndroidViewModel(application) {
    private val mAlarmDao: AlarmDao = AppDatabase.getInstance(application).alarmDao()

    fun deleteAlarm(alarm: Alarm?): Flow<Int> {
        return flow { emit(mAlarmDao.deleteAlarm(alarm)) }
    }

    fun deleteAlarmWithId(id: Int): Flow<Int> {
        return flow { emit(mAlarmDao.deleteAlarmById(id)) }
    }

    fun updateAlarmByActive(id: Long, active: Int): Flow<Int> {
        return flow { emit(mAlarmDao.updateAlarmByActive(id, active)) }
    }

    fun getListAlarms() : Flow<List<Alarm>> {
        return flow { emit(mAlarmDao.allAlarm) }
    }
}