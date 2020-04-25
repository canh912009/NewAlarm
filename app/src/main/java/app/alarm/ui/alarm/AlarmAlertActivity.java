package app.alarm.ui.alarm;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import app.alarm.R;
import app.alarm.core.data.db.entities.Alarm;
import app.alarm.core.receiver.AlarmHelper;
import app.alarm.utils.Utils;

public class AlarmAlertActivity extends AppCompatActivity {

    private static final String TAG = "AlarmAlertActivity";
    public static final String KEY_ALARM = "KEY_ALARM";

    private final Alarm mAlarm = new Alarm();
    private Activity mContext;
    private InternalAlertReceiver mInternalReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_alert);
        setWindowOnTop();
        Log.i(TAG, "onCreate: ");

        mContext = this;
        setRegisterInternalReceiver();
        sendAlarmStartedIntent(this, mAlarm.getId());

        getAlarmDataFromIntent();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.alarm_alert_container, AlarmAlertFragment.newInstance(mAlarm)).commit();
    }

    private void getAlarmDataFromIntent() {
        Log.d(TAG, "getAlarmDataFromIntent");
        Intent intent = getIntent();

        Log.d(TAG, "if (mItem != null) {");
        mAlarm.readFromIntent(intent);
    }

    private void setWindowOnTop() {
        Window window = this.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                // | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                // | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);

        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int width = displaymetrics.widthPixels;

        lp.copyFrom(window.getAttributes());
        lp.width = (int) (width * 0.85);
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        window.setAttributes(lp);
        window.setBackgroundDrawable(getDrawable(R.drawable.dialog_background));
    }

    public void dismissAlarm() {
        AlarmHelper.enableNextAlert(this);
        finish();
    }

    public void snoozeAlarm() {
        Log.d(Utils.SET_ON_TIME_PERFORMANCE, "MethodDefault: snoozeAlarm: = " + mAlarm.toString());
        AlarmHelper.enableAlarmSnooze(this, mAlarm);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: ");

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver();
    }

    public static void sendAlarmStartedIntent(Context context, long id) {
        Intent iAlarmStarted = new Intent();
        iAlarmStarted.setAction(Alarm.ACTION_LOCAL_TIMER_ALERT_START);
        Log.i(Utils.SET_ON_TIME_PERFORMANCE, "AlarmAlertActivity: sendAlarmStartedIntent: ACTION_LOCAL_TIMER_ALERT_START");
        iAlarmStarted.putExtra(Alarm.ALERT_ALARM_ID, id);
        context.sendBroadcast(iAlarmStarted);
    }

    private void setRegisterInternalReceiver() {
        final IntentFilter internalFilter = new IntentFilter();
        internalFilter.addAction(Alarm.ACTION_ALERT_STOP);
        if (mInternalReceiver == null) {
            mInternalReceiver = new InternalAlertReceiver();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.alarm_alert_container);
        fragment.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {

    }

    private class InternalAlertReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "AlarmAlertActivity: InternalAlertReceiver: receive action : " + action);

            if (action == null) {
                return;
            }

            switch (action) {
                case Alarm.ACTION_ALERT_STOP:
                    new Handler().postDelayed(() -> mContext.finish(), 50);
                    break;
                default:
                    break;
            }
        }
    }
}
