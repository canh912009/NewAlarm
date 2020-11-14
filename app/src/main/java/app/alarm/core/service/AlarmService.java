package app.alarm.core.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.Calendar;

import app.alarm.R;
import app.alarm.core.data.db.entities.Alarm;
import app.alarm.core.receiver.AlarmHelper;
import app.alarm.core.receiver.AlarmReceiver;
import app.alarm.ui.alarm.AlarmAlertActivity;
import app.alarm.ui.notification.PopupService;
import app.alarm.utils.AlarmPlayer;
import app.alarm.utils.StateUtils;
import app.alarm.utils.Utils;

public class AlarmService extends Service {
    private final String TAG = "AlarmService";

    private Context mContext;
    private AlarmPlayer mPlayer;

    private Alarm mItem = new Alarm();
    private InternalReceiver mInternalReceiver;
    private CountDownTimer mAlertTimer;
    private static final int MAX_TIME_OUT = 5 * 60; // 5 minute

    public AlarmService() {
        // Creator
    }

    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // https://stackoverflow.com/questions/44425584/context-startforegroundservice-did-not-then-call-service-startforeground
        notificationPopupServiceAlarm();
    }

    public static final String CHANNEL_ID = "NewAlarmServiceChannel";
    private void notificationPopupServiceAlarm() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "NewAlarm Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("")
                    .setContentText("").build();

            startForeground(0, notification);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(Utils.SET_ON_TIME_PERFORMANCE, "onStartCommand: intent = " + intent);
        if (intent == null) {
            stopSelf();
            return START_NOT_STICKY;
        }
        mContext = this;
        mItem = new Alarm();
        mItem.readFromIntent(intent);
        Log.i(TAG, "onStartCommand: mItem = " + mItem.toString());
        if (StateUtils.needToShowAsFullScreen(mContext)) {
            // For user binary debugging
            Log.d(Utils.SET_ON_TIME_PERFORMANCE, "onStartCommand callAlarmAlertActivity 1");
            callAlarmAlertActivity(intent);
        } else if (!(Utils.getTopActivity(mContext).equals(".ui.alarm.AlarmAlertActivity"))) {
            Log.d(Utils.SET_ON_TIME_PERFORMANCE, "onStartCommand not call callAlarmAlertActivity");
            Intent alert = new Intent();
            alert.setClass(mContext,PopupService.class);
            alert.putExtra(Alarm.ALARM_DATA, intent.getByteArrayExtra(Alarm.ALARM_DATA));
            mContext.startService(alert);
            /*showNotificationReminder(mContext);*/

        } else {
            Log.d(Utils.SET_ON_TIME_PERFORMANCE, "onStartCommand callAlarmAlertActivity 2");
            callAlarmAlertActivity(intent);
        }
        setRegisterInternalReceiver();
        setAlarmSound();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy start");

        unregisterReceiver();
        stopForeground(true);

        Log.d(TAG, "onDestroy end");
        super.onDestroy();
    }

    private void setAlarmSound() {
        mPlayer = new AlarmPlayer(this);
        Utils.Song song = Utils.getSong(mItem.getUriSound());
        String uri = null;
        if (song != null) {
            uri = song.mUri;
        }
        Log.i(TAG, "setAlarmSound: mItem.getUriSound() = " + uri);
        mPlayer.setPlayResource(uri);
        int timeOut = getTimeoutInMs(mContext);
        Log.i(TAG, "onStartCommand: getTimeoutInMs() = " + timeOut);
        if (timeOut > 0) {
            mAlertTimer = new CountDownTimer(timeOut * 1000, 1000) { // 30000 = 30s
                @Override
                public void onTick(long l) {
                    Log.i(TAG, "setAlarmSound: onTick: seconds remaining: = " + l / 1000);
                }

                @Override
                public void onFinish() {
                    Log.i(TAG, "setAlarmSound: done");
                    sendAlertActivityStopIntent(mContext, mItem.getId());
                    stopAlarm();
                }
            }.start();
        }
    }

    private void setRegisterInternalReceiver() {
        final IntentFilter internalFilter = new IntentFilter();
        internalFilter.addAction(Alarm.ACTION_LOCAL_TIMER_ALERT_START);
        internalFilter.addAction(Alarm.ACTION_LOCAL_ALARM_ALERT_STOP);
        if (mInternalReceiver == null) {
            mInternalReceiver = new InternalReceiver();
        }
        registerReceiver(mInternalReceiver, internalFilter);
    }


    private void unregisterReceiver() {
        if (mInternalReceiver != null) {
            unregisterReceiver(mInternalReceiver);
            mInternalReceiver = null;
            Log.d(TAG, "unregisterReceiver LocalBroadcastManager");
        }
    }

    private class InternalReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "InternalReceiver receive action : " + action);

            switch (action) {
                case Alarm.ACTION_LOCAL_TIMER_ALERT_START:
                    playAlarm();
                    break;
                case Alarm.ACTION_LOCAL_ALARM_ALERT_STOP:
                    stopAlarm();
                    break;
                default:
                    break;
            }
        }
    }

    private void stopPlayer() {
        Log.d(TAG, "stopPlayer()");

        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer = null;
        }
    }

    private void callAlarmAlertActivity(Intent intent) {
        Log.d(Utils.SET_ON_TIME_PERFORMANCE, "callAlarmAlertActivity");

        Intent alert = new Intent();
        alert.setClass(mContext, AlarmAlertActivity.class);
        alert.putExtra(Alarm.ALARM_DATA, intent.getByteArrayExtra(Alarm.ALARM_DATA));
        alert.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION);
        mContext.startActivity(alert);
    }


    private void playAlarm() {
        if (mPlayer != null) {
            mPlayer.playService();
        }
    }

    private void stopAlarm() {
        if (mAlertTimer != null) {
            mAlertTimer.cancel();
            mAlertTimer = null;
        }
        stopPlayer();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                stopSelf();
            }
        }, 50);
    }

    public static void sendAlertActivityStopIntent(Context context, long id) {
        Intent iAlarmStarted = new Intent();
        iAlarmStarted.setAction(Alarm.ACTION_ALERT_STOP);
        iAlarmStarted.putExtra(Alarm.ALERT_ALARM_ID, id);
        context.sendBroadcast(iAlarmStarted);
    }

    private int getTimeoutInMs(Context context) {
        int timeOut = -1;
        int nextTime = getNextAlarmOfNextAlarm(context);
        Log.i(TAG, "getTimeoutInMs: nextTime = " + nextTime);
        if (nextTime > -1) {
            // if nextAlarm is same date with mItem -> nextAlarm.getAlarmTime() is always > mItem.getAlarmTime()
            // if nextAlarm is not same date with mItem -> we only care nextAlarm > 1 day for this case
            timeOut = (Utils.convertAlarmTimeToMs(nextTime) - Utils.convertAlarmTimeToMs(mItem.getAlarmTime()) - 3);
            if (timeOut > MAX_TIME_OUT) {
                timeOut = MAX_TIME_OUT;
            }
            return timeOut;
        }

        return timeOut;
    }

    private int getNextAlarmOfNextAlarm(Context context) { // return AlarmTime
        int nextTime = -1;
        Alarm nextAlarm = AlarmHelper.getNextAlarm(context);
        Log.i(TAG, "getNextAlarmOfNextAlarm: extAlarm = " + nextAlarm);
        if (nextAlarm != null) {
            int alarmTime = nextAlarm.getAlarmTime();
            Calendar cFromAlarmTime = Calendar.getInstance();
            cFromAlarmTime.set(Calendar.HOUR_OF_DAY, alarmTime / 100);
            cFromAlarmTime.set(Calendar.MINUTE, alarmTime % 100);
            cFromAlarmTime.set(Calendar.SECOND, 0);
            cFromAlarmTime.set(Calendar.MILLISECOND, 0);

            int today = (cFromAlarmTime.get(Calendar.DAY_OF_WEEK) + 5) % 7;
            if (nextAlarm.isOneTimeAlarm()) {
                return alarmTime;
            } else if (Utils.isSet(today, nextAlarm.getRepeatType())) {
                return alarmTime;
            } else {
                return alarmTime + 24 * 100;
            }
        }
        return nextTime;
    }
}
