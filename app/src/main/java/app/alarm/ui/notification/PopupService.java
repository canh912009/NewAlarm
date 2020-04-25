package app.alarm.ui.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DateFormatSymbols;
import java.util.Calendar;

import app.alarm.R;
import app.alarm.core.data.db.entities.Alarm;
import app.alarm.core.receiver.AlarmHelper;
import app.alarm.utils.Utils;

public class PopupService extends HeadUpService {
    private final String TAG = "PopupService";

    private final boolean SNOOZED = true;

    private final boolean STOPPED = false;
    private RelativeLayout mAlarmAlertPopup;
    private TextView mAlarmName;
    private TextView mCurrentTime;
    private TextView mCurrentTime_AmPm;
    private View mSnoozeBtn;
    private Alarm mAlarm = null;
    private String mAmPm;
    private InternalAlertReceiver mInternalReceiver;

    private final View.OnClickListener mClickListener = v -> {
        int id = v.getId();
        Log.d(TAG, "onClick(View " + v + ", mId " + id + ")...");

        if (mIsAnimationRunning) {
            Log.d(TAG, "animation is running");
            return;
        }

        switch (id) {
            case R.id.popup_dismissBtn:
                animateForHide();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finishAlarm(STOPPED);
                    }
                }, 250);
                break;
            case R.id.popup_snoozeBtn:
                animateForHide();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finishAlarm(SNOOZED);
                    }
                }, 250);
                break;
            default:
                break;
        }
    };

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate()");
        super.onCreate();
    }

    public static void sendAlarmStartedIntent(Context context, long id) {
        Intent iAlarmStarted = new Intent();
        iAlarmStarted.setAction(Alarm.ACTION_LOCAL_TIMER_ALERT_START);
        Log.i(Utils.SET_ON_TIME_PERFORMANCE, "PopupService: sendAlarmStartedIntent: ACTION_LOCAL_TIMER_ALERT_START");
        iAlarmStarted.putExtra(Alarm.ALERT_ALARM_ID, id);
        context.sendBroadcast(iAlarmStarted);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");

        if (intent == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        mContext = this;
        mAlarm = new Alarm();
        mAlarm.readFromIntent(intent);

        setRegisterInternalReceiver();

        sendAlarmStartedIntent(this, mAlarm.getId());

        sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        init();
        updateTimeDisplay();

        return START_STICKY;
    }

    private void updateTimeDisplay() {
        int hour, min;
        Calendar c = Calendar.getInstance();
        boolean m24HMode = DateFormat.is24HourFormat(this);
        hour = c.get(Calendar.HOUR_OF_DAY);
        min = c.get(Calendar.MINUTE);

        if (mCurrentTime != null) {
            if (m24HMode) {
                mCurrentTime.setText(getResources()
                        .getString(R.string.alarm_alert_time_hour_digit_two, hour, min));
                mCurrentTime_AmPm.setVisibility(View.GONE);
            } else {
                hour = Utils.getAmPmHour(hour);

                String[] mAmPmTexts = new DateFormatSymbols().getAmPmStrings();
                if (hour < 0) {
                    mAmPm = mAmPmTexts[0]; // am
                    hour *= -1;
                } else {
                    mAmPm = mAmPmTexts[1]; // pm
                }
                mCurrentTime_AmPm.setVisibility(View.VISIBLE);
                mCurrentTime_AmPm.setText(mAmPm);

                mCurrentTime.setText(getString(R.string.alarm_alert_time_hour_digit_one, hour, min));
            }
        }

        if (mAlarmName != null) {
            if (mAlarm != null && mAlarm.getAlarmName() != null && mAlarm.getAlarmName().length() > 0) {
                mAlarmName.setVisibility(View.VISIBLE);
                mAlarmName.setText(mAlarm.getAlarmName());

            } else {
                mAlarmName.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        removeInstance();
        unregisterReceiver();

        if (mAlarmAlertPopup != null) {
            mAlarmAlertPopup.removeAllViews();
            mAlarmAlertPopup = null;
        }

        super.onDestroy();
    }

    private void removeInstance() {
        Log.d(TAG, "removeInstance()");

        if (mContext != null) {
            mContext = null;
        }
    }

    private void finishAlarm(boolean isStopBySnoozeBtn) {
        Log.d(Utils.SET_ON_TIME_PERFORMANCE, "PopupService: finishAlarm: isStopBySnoozeBtn = " + isStopBySnoozeBtn);
        sendStopAlarmAlertIntent(mContext, false);
        if (mAlarm != null) {
            if (isStopBySnoozeBtn) {
                AlarmHelper.enableAlarmSnooze(this, mAlarm);
            } else {
                AlarmHelper.enableNextAlert(this);
            }
        }

        stopSelf();
    }

    public  void sendStopAlarmAlertIntent(Context context, boolean bTimeOut) {
        Intent intent = new Intent(Alarm.ACTION_ALARM_STOPPED_IN_ALERT);
        context.sendBroadcast(intent);

        if (!bTimeOut) {
            sendLocalStopAlarmAlertIntent(context);
        }
    }

    public static void sendLocalStopAlarmAlertIntent(Context context) {
        context.sendBroadcast(new Intent(Alarm.ACTION_LOCAL_ALARM_ALERT_STOP));
    }

    @Override
    public void animateForShow() {
        Log.d(TAG, "animateForShow");
        super.animateForShow();
    }

    @Override
    public void animateForHide() {
        Log.d(TAG, "animateForHide");
        super.animateForHide();
    }

    @Override
    public void animateForSlideOut(boolean toLeft) {
        super.animateForSlideOut(toLeft);
        Log.d(TAG, "animateForSlideOut ");
        finishAlarm(STOPPED);
    }

    private void setFontFromOpenTheme() {
        Typeface tf = Utils.getFontFromOpenTheme(mContext);
        if (tf == null) {
            tf = Typeface.create("sans-serif-light", Typeface.NORMAL);
        }

        if (tf != null) {
            mCurrentTime.setTypeface(tf);
        }
    }

    private void init() {
        if (mAlarm.getSnoozeTime() > 0) {
            mSnoozeBtn.setVisibility(View.VISIBLE);
        }
    }


    @Override
    protected void onCreateCustomView(ViewGroup root) {
        Log.d(TAG, "onCreateCustomView");

        LayoutInflater inflater = LayoutInflater.from(getContext());
        mAlarmAlertPopup = ((RelativeLayout) inflater.inflate(R.layout.alarm_alert_popup_view,
                root));

        mCurrentTime = ((TextView) mAlarmAlertPopup
                .findViewById(R.id.popup_alarm_alert_current_time));

        mCurrentTime_AmPm = (TextView) mAlarmAlertPopup
                .findViewById(R.id.popup_alarm_alert_current_time_ampm);

        mAlarmName = ((TextView) mAlarmAlertPopup.findViewById(R.id.alarm_name_popup));

        View dismissBtn = mAlarmAlertPopup.findViewById(R.id.popup_dismissBtn);
        dismissBtn.setOnClickListener(mClickListener);
        dismissBtn.setContentDescription(getResources().getString(R.string.dismiss) + ' '
                + getResources().getString(R.string.button));
        mSnoozeBtn = mAlarmAlertPopup.findViewById(R.id.popup_snoozeBtn);
        mSnoozeBtn.setOnClickListener(mClickListener);
        mSnoozeBtn.setContentDescription(getResources().getString(R.string.snooze) + ' '
                + getResources().getString(R.string.button));

        setFontFromOpenTheme();

        setShowButtonBackground(mAlarmAlertPopup.findViewById(R.id.textview_dismissBtn));
        setShowButtonBackground(mAlarmAlertPopup.findViewById(R.id.textview_snoozeBtn));
    }

    private void setShowButtonBackground(View view) {
        view.setBackgroundResource(R.drawable.hun_alert_btn_bg);
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

    private class InternalAlertReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "AlarmAlertActivity: InternalAlertReceiver: receive action : " + action);

            switch (action) {
                case Alarm.ACTION_ALERT_STOP:
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            stopSelf();
                        }
                    }, 50);
                    break;
                default:
                    break;
            }
        }
    }
}
