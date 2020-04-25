package app.alarm.core.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;
import java.util.List;

import app.alarm.core.data.db.AppDatabase;
import app.alarm.core.data.db.dao.AlarmDao;
import app.alarm.core.data.db.entities.Alarm;
import app.alarm.core.data.db.entities.RepeatDayOfWeek;
import app.alarm.utils.CalenderUtil;
import app.alarm.utils.Utils;

public class AlarmHelper {

    private static final String TAG = "AlarmHelper";

    private static PendingIntent mPendingIntent;

    private static PendingIntent mPendingIntent2;

    // Intent action for an AlarmManager alarm serving only to set the next
    // alarm indicators
    private static final String INDICATOR_ACTION = "indicator";

    public static void enableNextAlert(Context context) {
        cancelPendingIntent(context);
        Alarm item = getNextAlarm(context);
        Log.d(Utils.SET_ON_TIME_PERFORMANCE, "AlarmHelper: enableNextAlert next alarm : " + (item == null ? "null" : item.toString()));
        sendBroadCast(context, item);
    }

    public static void enableAlarmSnooze(Context context, Alarm alarm) {
        cancelPendingIntent(context);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, alarm.getSnoozeTime());
        alarm.setAlarmDate(calendar.getTimeInMillis());

        Log.d(Utils.SET_ON_TIME_PERFORMANCE, "AlarmHelper: enableAlarmSnooze next alarm : " + (alarm == null ? "null" : alarm.toString()));
        sendBroadCast(context, alarm);
    }

    private static void sendBroadCast(Context context, Alarm item) {
        if (item != null) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(item.getAlarmDate());
            Log.d(Utils.SET_ON_TIME_PERFORMANCE, "AlarmHelper: enableNextAlert time : " + CalenderUtil.convertMilisecondssToDetailTime(item.getAlarmDate()));
            Toast.makeText(context, "Next Alarm at: " + CalenderUtil.convertMilisecondssToDetailTime(item.getAlarmDate()), Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(Alarm.ALARM_ALERT);
            Parcel out = Parcel.obtain();
            item.writeToParcel(out);
            out.setDataPosition(0);
            intent.putExtra(Alarm.ALARM_DATA, out.marshall());
            intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND | Intent.FLAG_RECEIVER_NO_ABORT);
            mPendingIntent = PendingIntent.getBroadcast(context, 0, intent,
                    PendingIntent.FLAG_CANCEL_CURRENT);

            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            am.setExact(AlarmManager.RTC_WAKEUP, item.getAlarmDate(), mPendingIntent);
            setStatusBarIcon(context, item, intent);
        } else {
            setStatusBarIcon(context, null, null);
        }
    }

    private static void setStatusBarIcon(Context context, Alarm item, Intent intent) {
        int flags = item == null ? PendingIntent.FLAG_NO_CREATE : 0;
        PendingIntent operation = PendingIntent.getBroadcast(context, 0 /* requestCode */,
                createIndicatorIntent(context), flags);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (item != null) {
            long alarmTime = item.getAlarmDate();
            PendingIntent viewIntent = PendingIntent.getActivity(context, item.hashCode(), intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager.AlarmClockInfo info = new AlarmManager.AlarmClockInfo(alarmTime,
                    viewIntent);
            alarmManager.setAlarmClock(info, operation);
        } else if (operation != null) {
            alarmManager.cancel(operation);
        }
    }
    private static Intent createIndicatorIntent(Context context) {
        return new Intent(context, AlarmHelper.class).setAction(INDICATOR_ACTION);
    }

    public static Alarm getNextAlarm(Context context) {
        List<Alarm> alarms = getAlarmDaoInstance(context).getAllAlarmsIsActive();
        Log.i(Utils.SET_ON_TIME_PERFORMANCE, "AlarmHelper: alarms.size =" + alarms.size());

        long firstAlertAlarm = Long.MAX_VALUE;
        Alarm alarmWithNewAlertTime = null;
        for (int i = 0; i < alarms.size(); i++) {
            Alarm alarm = alarms.get(i);
            Log.i(Utils.SET_ON_TIME_PERFORMANCE, "AlarmHelper: +  " + ", alarm =" + alarm.toString());
            RepeatDayOfWeek repeatDayOfWeek = new RepeatDayOfWeek(alarm);
            Alarm newAlarm = repeatDayOfWeek.calculateNextAlarm();
            if (newAlarm != null && newAlarm.getAlarmDate() < firstAlertAlarm) {
                firstAlertAlarm = newAlarm.getAlarmDate();
                alarmWithNewAlertTime = newAlarm;
            }
        }

        Alarm firstAlarm;
        if (alarmWithNewAlertTime != null) {
            firstAlarm = alarmWithNewAlertTime;
            getAlarmDaoInstance(context).updateAlarmWithNewAlertAlarm(firstAlarm.getId(), firstAlarm.getAlarmDate());
            return firstAlarm;
        }

        return null;
    }

    private static AlarmDao getAlarmDaoInstance(Context context) {
        return AppDatabase.getInstance(context).alarmDao();
    }

    private static void cancelPendingIntent(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (mPendingIntent != null) {
            am.cancel(mPendingIntent);
        } else {
            // The alarm is deactivated. But it goes off.
            Log.d(TAG, "cancelPendingIntent mPendingIntent != null");
            Intent intent = new Intent(Alarm.ALARM_ALERT);
            mPendingIntent = PendingIntent.getBroadcast(context, 0, intent,
                    // don't change it to FLAG_CANCEL_CURRENT
                    PendingIntent.FLAG_UPDATE_CURRENT);
            am.cancel(mPendingIntent);
        }

        mPendingIntent = null;

        if (mPendingIntent2 != null) {
            am.cancel(mPendingIntent2);
        } else {
            // The alarm is deactivated. But it goes off.
            Log.d(TAG, "cancelPendingIntent mPendingIntent2 != null");
            Intent intent = new Intent(Alarm.IMPENDING_ALERT);
            intent.setPackage("PKG_NAME");
            mPendingIntent2 = PendingIntent.getBroadcast(context, 0, intent,
                    // don't change it to FLAG_CANCEL_CURRENT
                    PendingIntent.FLAG_UPDATE_CURRENT);
            am.cancel(mPendingIntent2);
        }

        mPendingIntent2 = null;
    }
}
