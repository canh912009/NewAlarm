package app.alarm.core.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;

import app.alarm.utils.AppConstants;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "AlarmReceiver";

    private final Handler mHandler = new Handler();

    public static final String CLICK_REMINDER_NOTIFICATION = "android.app.action.CLICK_REMINDER_NOTIFICATION";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }

        String action = intent.getAction();
        if (action == null) {
            return;
        }

        //WakeLock.acquire(context);

        Log.d(TAG, "onReceive() : action = " + action.substring(action.lastIndexOf('.')));

        switch (action) {
            case Intent.ACTION_LOCKED_BOOT_COMPLETED:
            case Intent.ACTION_BOOT_COMPLETED:
            case Intent.ACTION_TIME_CHANGED:
            case Intent.ACTION_TIMEZONE_CHANGED:
            case Intent.ACTION_LOCALE_CHANGED:
                AlarmHelper.enableNextAlert(context);
                break;
            default:
                break;
        }

        mHandler.postDelayed(() -> {
            Log.d(TAG, "mHandler.postDelayed(new Runnable() {");
            WakeLock.release();
        }, 3 * AppConstants.SECOND_MILLIS);

    }

    private static class WakeLock {

        private static PowerManager.WakeLock sWakeLock;

        private static synchronized void acquire(Context context) {
            if (sWakeLock == null || !sWakeLock.isHeld()) {
                PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                sWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
            /*
             * | PowerManager.ACQUIRE_CAUSES_WAKEUP |
             * PowerManager.ON_AFTER_RELEASE
             */
                        , "AlarmReceiver");
                sWakeLock.acquire();
            }
        }

        private static synchronized void release() {
            if (sWakeLock != null) {
                sWakeLock.release();
                sWakeLock = null;
            }
        }
    }
}
