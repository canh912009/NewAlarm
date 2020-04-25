package app.alarm.ui.alarm.view;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import app.alarm.R;
import app.alarm.core.data.db.entities.Alarm;

public class MethodShake extends Method implements SensorEventListener {
    private static final String TAG = "MethodShake";
    private boolean mIsInit;
    private Sensor mAccelerometer;
    private SensorManager mSensorManager;
    private float x1, x2, x3;
    private float ERROR = (float) 0.0;

    private TextView mTimeOfShake;

    public MethodShake(Activity context, Alarm alarm) {
        super(context, alarm);
    }

    @Override
    public void initView(View rootView) {
        LinearLayout mLinearLayout = rootView.findViewById(R.id.ln_shake);
        mLinearLayout.setVisibility(View.VISIBLE);
        mTimeOfShake = rootView.findViewById(R.id.time_of_shake);
        mTimeOfShake.setText(String.valueOf(mCount));
    }

    @Override
    public void create() {
        super.create();
        mSensorManager = (SensorManager) mContext.getSystemService(mContext.SENSOR_SERVICE);
        List<Sensor> listOfSensorsOnDevice = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        for (int i = 0; i < listOfSensorsOnDevice.size(); i++) {
            if (listOfSensorsOnDevice.get(i).getType() == Sensor.TYPE_ACCELEROMETER) {
                Toast.makeText(mContext, "ACCELEROMETER sensor is available on device", Toast.LENGTH_SHORT).show();
                mIsInit = false;
                mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Toast.makeText(mContext, "ACCELEROMETER sensor is NOT available on device", Toast.LENGTH_SHORT).show();
            }
        }
        initLevel();
    }


    private void initLevel() {
        Log.i(TAG, "initLevel: mLevel = " + mLevel);
        switch (mLevel) {
            case 0:
                ERROR = (float) 7.0;
                break;
            case 1:
                ERROR = (float) 20.0;
                break;
            case 2:
                ERROR = (float) 40.0;
                break;
            default:
                ERROR = (float) 20.0;
                break;
        }
    }

    @Override
    public void resume() {
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void pause() {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    @Override
    public void onSensorChanged(SensorEvent e) {
        if (mCount >= 0) {
            setTimeShakeText();
        }
        if (mCount == 0) {
            mCount = -1;
            finishAlarm();
        }
        float x, y, z;
        x = e.values[0];
        y = e.values[1];
        z = e.values[2];

        if (!mIsInit) {
            x1 = x;
            x2 = y;
            x3 = z;
            mIsInit = true;
        } else {
            float diffX = Math.abs(x1 - x);
            float diffY = Math.abs(x2 - y);
            float diffZ = Math.abs(x3 - z);

            //Handling ACCELEROMETER Noise
            if (diffX < ERROR) {
                diffX = (float) 0.0;
            }

            if (diffY < ERROR) {
                diffY = (float) 0.0;
            }

            if (diffZ < ERROR) {
                diffZ = (float) 0.0;
            }

            x1 = x;
            x2 = y;
            x3 = z;

            //Horizontal Shake Detected!
            if (diffX > diffY) {
                mCount -= 1;
            }
        }
    }

    private void setTimeShakeText() {
        String timeShake = mContext.getResources().getString(R.string.shake_times, mCount);
        String count = String.valueOf(mCount);

        SpannableString spanText = new SpannableString(timeShake);

        if (timeShake.contains(count)) {
            int indexNumber = timeShake.indexOf(String.valueOf(count));
            spanText.setSpan(new AbsoluteSizeSpan((int) mContext.getResources().getDimension(R.dimen.alarm_method_shake_text_count_size)),
                    indexNumber, indexNumber + count.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        mTimeOfShake.setText(spanText);
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
