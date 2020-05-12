package app.alarm.core.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import androidx.core.content.ContextCompat;

import app.alarm.core.data.db.entities.Alarm;
import app.alarm.core.service.AlarmService;
import app.alarm.utils.CalenderUtil;
import app.alarm.utils.StateUtil;
import app.alarm.utils.Utils;

public class AlarmNoticeReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmSecurityReceiver";

    private final Handler mHandler = new Handler();
    private static final int PENDING_ALARM = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }

        String action = intent.getAction();
        if (action == null) {
            return;
        }

        Log.d(TAG, "onReceive() : action = " + action);

        switch (action) {
            case Alarm.ALARM_ALERT:
                Log.d("CANHCANH", "onReceive: ");
                receiveAlarmAlertIntent(context, intent);
                break;

            default:
                break;
        }
    }

    private void receiveAlarmAlertIntent(Context context, Intent intent) {
        Alarm item = new Alarm();
        item.readFromIntent(intent);

        Log.d(Utils.SET_ON_TIME_PERFORMANCE, "AlarmNoticeReceiver: item = " + item.toString() + ", AlarmDate = " + CalenderUtil.convertMilisecondssToDetailTime(item.getAlarmDate()));
        int notificationType = getAlertNotificationType(context, item, intent.getAction());

        if (notificationType == Alarm.ALERT_TYPE_ALERT) {
            if (StateUtil.isInCall(context)) {
                showNotification(context, item);
                waitForCallEnd(context, intent, PENDING_ALARM);
            } else {
                startAlarmService(context, intent);
            }
            updateAndEnableNextAlarm(context, item);
        }
    }

    private void updateAndEnableNextAlarm(Context context, Alarm item) {
        AlarmHelper.enableNextAlert(context);
    }

    private void waitForCallEnd(Context context, Intent intent, int pendingEvent) {

    }

    private void showNotification(Context context, Alarm item) {
    }

    private int getAlertNotificationType(Context context, Alarm item, String action) {

        return Alarm.ALERT_TYPE_ALERT;
    }

    public void startAlarmService(Context context, Intent intent) {
        Log.d(Utils.SET_ON_TIME_PERFORMANCE, "->startAlarmService");

        Intent alert = new Intent();
        alert.setClass(context, AlarmService.class);
        alert.putExtra(Alarm.ALARM_DATA, intent.getByteArrayExtra(Alarm.ALARM_DATA));
        context.startService(alert);
        Log.d(Utils.SET_ON_TIME_PERFORMANCE, "<-startAlarmService");
    }
}
