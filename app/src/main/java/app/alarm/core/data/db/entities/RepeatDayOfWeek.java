package app.alarm.core.data.db.entities;

import android.util.Log;

import java.util.Calendar;

import app.alarm.utils.CalenderUtil;
import app.alarm.utils.Utils;

public class RepeatDayOfWeek {

    private int mRepeatType = 0;
    private int mAlarmTime;
    private Alarm mAlarm;
    private long mNewAlertTime;

    public RepeatDayOfWeek(Alarm alarm) {
        mAlarm = alarm;
        mRepeatType = alarm.getRepeatType();
        mAlarmTime = alarm.getAlarmTime();
    }

    public boolean[] getBooleanArray() {
        boolean[] ret = new boolean[7];
        for (int i = 0; i < 7; i++) {
            ret[i] = isSet(i);
        }
        return ret;
    }

    public Alarm calculateNextAlarm() {
        Calendar cFromAlarmTime = Calendar.getInstance();
        cFromAlarmTime.set(Calendar.HOUR_OF_DAY, mAlarmTime / 100);
        cFromAlarmTime.set(Calendar.MINUTE, mAlarmTime % 100);
        cFromAlarmTime.set(Calendar.SECOND, 0);
        cFromAlarmTime.set(Calendar.MILLISECOND, 0);
        if (!advanceCalendarValid(cFromAlarmTime)) {
            Log.i(Utils.SET_ON_TIME_PERFORMANCE, "RepeatDayOfWeek: !advanceCalendarValid(cFromAlarmTime) return  = " + null);
            return null;
        }

        mNewAlertTime = cFromAlarmTime.getTimeInMillis();
        mAlarm.setAlarmDate(mNewAlertTime);

        Log.i(Utils.SET_ON_TIME_PERFORMANCE, "RepeatDayOfWeek: calculateNextAlarm: mAlarmTime = " + mAlarmTime);
        Log.i(Utils.SET_ON_TIME_PERFORMANCE, "RepeatDayOfWeek: calculateNextAlarm: cFromAlarmTime = " + CalenderUtil.convertMilisecondssToDetailTime(cFromAlarmTime.getTimeInMillis()) + " " + Utils.getAmPm(cFromAlarmTime));
        Log.i(Utils.SET_ON_TIME_PERFORMANCE, "RepeatDayOfWeek: calculateNextAlarm: mNewAlertTime = " + CalenderUtil.convertMilisecondssToDetailTime(mNewAlertTime) + " " + Utils.getAmPm(cFromAlarmTime));
        Log.i(Utils.SET_ON_TIME_PERFORMANCE, "RepeatDayOfWeek: calculateNextAlarm: mAlarm = " + mAlarm.toString());
        return mAlarm;
    }

    private boolean advanceCalendarValid(Calendar cFromAlarmTime) {
        Calendar now = Calendar.getInstance();
        // if alarm is behind current time, advance one day
        Log.i(Utils.SET_ON_TIME_PERFORMANCE, "advanceCalendarValid: cFromAlarmTime = " + CalenderUtil.convertMilisecondssToDetailTime(cFromAlarmTime.getTimeInMillis())
                + ", now = " + CalenderUtil.convertMilisecondssToDetailTime(now.getTimeInMillis()));
        if (cFromAlarmTime.before(now)) {
            if (mAlarm.isOneTimeAlarm()) {
                return false;
            }
            cFromAlarmTime.add(Calendar.DAY_OF_YEAR, 1);
        }
        int addDays = getNextAlarm(cFromAlarmTime);
        Log.i(Utils.SET_ON_TIME_PERFORMANCE, "advanceCalendarValid: addDays = " + addDays);
        if (addDays > -1) {
            cFromAlarmTime.add(Calendar.DAY_OF_WEEK, addDays);
        }
        return true;
    }

    /**
     * returns number of days from today until next alarm
     *
     * @param cFromAlarmTime must be set to today
     */
    public int getNextAlarm(Calendar cFromAlarmTime) {
        if (mRepeatType == 0) return -1;
        int today = (cFromAlarmTime.get(Calendar.DAY_OF_WEEK) + 5) % 7;
        Log.i(Utils.SET_ON_TIME_PERFORMANCE, "RepeatDayOfWeek: getNextAlarm: today = " + today);

        // today: 0 -> mon, 1-> tue, ....., 6-> sun

        int day = 0;
        int dayCount = 0;
        for (; dayCount < 7; dayCount++) {
            day = (today + dayCount) % 7;
            if (isSet(day)) {
                break;
            }
        }

        return dayCount;
    }

    private boolean isSet(int day) {
        return (mRepeatType & 1 << day) > 0;
    }
}
