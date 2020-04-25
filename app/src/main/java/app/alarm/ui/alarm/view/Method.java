package app.alarm.ui.alarm.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import app.alarm.core.data.db.entities.Alarm;
import app.alarm.ui.alarm.AlarmAlertActivity;
import app.alarm.utils.Utils;


public abstract class Method {

    private static final String TAG = "Method_Alarm";

    final Alarm mAlarm;
    View mRootView;
    final Activity mContext;

    int mCount = 0;
    int mLevel = 0;

    String mPathImage = "";

    public Method(Activity context, Alarm alarm) {
        mContext = context;
        mAlarm = alarm;
    }

    public abstract void initView(View rootView);

    public void create() {
        initSettingValue();
    }

    public abstract void resume();

    public abstract void pause();

    public abstract void onActivityResult(int requestCode, int resultCode, Intent data);

    public void snoozeAlarm() {
        sendStopAlarmAlertIntent(mContext);
        ((AlarmAlertActivity) mContext).snoozeAlarm();
    }

    public void finishAlarm() {
        sendStopAlarmAlertIntent(mContext);
        ((AlarmAlertActivity) mContext).dismissAlarm();
    }

    public void sendStopAlarmAlertIntent(Context context) {
        Intent intent = new Intent(Alarm.ACTION_ALARM_STOPPED_IN_ALERT);
        context.sendBroadcast(intent);
        sendLocalStopAlarmAlertIntent(context);
    }

    public static void sendLocalStopAlarmAlertIntent(Context context) {
        context.sendBroadcast(new Intent(Alarm.ACTION_LOCAL_ALARM_ALERT_STOP));
    }

    private void initSettingValue() {
        String string = mAlarm.getAlarmSettings();
        int alarmType = mAlarm.getAlarmType();
        if (string != null) {
            if (alarmType != Alarm.ALARM_METHOD_TAKE_A_PHOTO) {
                String[] parts = string.split("/");
                mCount = Integer.valueOf(parts[0]);
                mLevel = Integer.valueOf(parts[1]);
            } else {
                mPathImage = string;
            }
        }

        Log.d(Utils.SET_ON_TIME_PERFORMANCE, "initSettingValue: mCount = " + mCount + ", mLevel = " + mLevel);
    }
}
