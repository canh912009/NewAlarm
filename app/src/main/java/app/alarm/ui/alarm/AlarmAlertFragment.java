package app.alarm.ui.alarm;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Calendar;

import app.alarm.R;
import app.alarm.core.data.db.entities.Alarm;
import app.alarm.ui.alarm.view.Method;
import app.alarm.ui.alarm.view.MethodCalculator;
import app.alarm.ui.alarm.view.MethodDefault;
import app.alarm.ui.alarm.view.MethodShake;
import app.alarm.ui.alarm.view.MethodTakePhoto;
import app.alarm.utils.Utils;

@SuppressLint("ValidFragment")
public class AlarmAlertFragment extends Fragment {

    private static final String TAG = "RenameDialogFragment";

    private Activity mContext;
    private Alarm mAlarm;
    private Method mMethod;

    public static AlarmAlertFragment newInstance(Alarm alarm) {
        Bundle args = new Bundle();
        args.putSerializable(AlarmAlertActivity.KEY_ALARM, alarm);
        AlarmAlertFragment fragment = new AlarmAlertFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        if (getArguments() != null) {
            mAlarm = (Alarm) getArguments().getSerializable(AlarmAlertActivity.KEY_ALARM);
            if (mAlarm != null) {
                mMethod = getMethod(mAlarm);
            } else {
                finish();
            }
        }

        if (mMethod == null) {
            finish();
        }

        mMethod.create();
        Log.d(Utils.SET_ON_TIME_PERFORMANCE, "AlarmAlertFragment: onCreate: mAlarm = " + mAlarm.toString());
    }

    private void finish() {
        if (mContext != null) {
            mContext.finish();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_alarm_alert_method, container, false);
        initView(rootView);
        mMethod.initView(rootView);
        return rootView;
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void initView(View rootView) {
        TextView mAlarmTime = rootView.findViewById(R.id.alarm_time);
        TextView mAlarmTimeAPM = rootView.findViewById(R.id.alarm_time_apm);

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) % 100;
        int minute = Calendar.getInstance().get(Calendar.MINUTE) % 100;

        String str;
        if (Utils.isMode24Hour(mContext)) {
            str = Utils.formatTwoDigitTime(hour) + ":" + Utils.formatTwoDigitTime(minute);
            mAlarmTimeAPM.setVisibility(View.GONE);
        } else {
            mAlarmTimeAPM.setVisibility(View.VISIBLE);
            if (hour >= 12) {
                if (hour == 12) {
                    str = String.format("%s:%s", Utils.formatTwoDigitTime(12), Utils.formatTwoDigitTime(minute));
                } else {
                    str = String.format("%s:%s", Utils.formatTwoDigitTime(hour - 12), Utils.formatTwoDigitTime(minute));
                }
                mAlarmTimeAPM.setText(mContext.getResources().getString(R.string.time_pm));
            } else {
                if (hour == 0) {
                    hour = 12;
                }
                str = String.format("%s:%s", (hour), Utils.formatTwoDigitTime(minute));
                mAlarmTimeAPM.setText(mContext.getResources().getString(R.string.time_am));
            }
        }

        mAlarmTime.setText(str);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMethod.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMethod.pause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mMethod.onActivityResult(requestCode, resultCode, data);
    }

    private Method getMethod(Alarm alarm) {
        switch (alarm.getAlarmType()) {
            case Alarm.ALARM_METHOD_DEFAULT:
                return new MethodDefault(mContext, mAlarm);
            case Alarm.ALARM_METHOD_TAKE_A_PHOTO:
                return new MethodTakePhoto(mContext, mAlarm);
            case Alarm.ALARM_METHOD_SHAKE_PHONE:
                return new MethodShake(mContext, mAlarm);
            case Alarm.ALARM_METHOD_MATH_PROBLEMS:
                return new MethodCalculator(mContext, mAlarm);
            default:
                return null;
        }
    }
}
