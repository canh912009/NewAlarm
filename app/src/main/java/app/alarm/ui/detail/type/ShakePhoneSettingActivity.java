package app.alarm.ui.detail.type;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;

import app.alarm.R;
import app.alarm.core.presentation.BaseActivity;
import app.alarm.utils.AppConstants;

public class ShakePhoneSettingActivity extends BaseActivity implements View.OnClickListener {
    private NumberPicker mNumberPicker;
    private TextView mTextCountShake;

    private SeekBar mSeekBar;
    private TextView mStartTv;
    private TextView mEndTv;
    private TextView mFloatingTv;

    static final String[] mShakes = {"5", "10", "15", "20", "25", "30", "35", "40",
            "45", "50", "55", "60"};

    private boolean mIsCreateAlarm = false;
    private int mCountShake;
    private int mLevel;
    private String mSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shake_phone_setting);

        initView();
    }


    public void initView() {
        initActionBar();

        mTextCountShake = findViewById(R.id.text_count_shake);
        mNumberPicker = findViewById(R.id.number_picker);

        mNumberPicker.setMaxValue(mShakes.length);
        mNumberPicker.setMinValue(1);
        mNumberPicker.setDisplayedValues(mShakes);

        mStartTv = findViewById(R.id.startTv);
        mEndTv = findViewById(R.id.endTv);
        mFloatingTv = findViewById(R.id.floatingTv);
        mSeekBar = findViewById(R.id.seekbar);

        mSeekBar.setMax(2);
        mStartTv.setText(getString(R.string.alarm_level_easy));
        mEndTv.setText(getString(R.string.alarm_level_hard));

        Intent intent = getIntent();
        mIsCreateAlarm = intent.getBooleanExtra(AppConstants.ARG_INTENT_TYPE_CREATE_ALARM, true);

        if (mIsCreateAlarm) {
            mCountShake = 5; // default
            mNumberPicker.setValue(1);
            mLevel = 0;
            mSeekBar.setProgress(0);
        } else {
            String[] mAlarmSetting = intent.getStringExtra(AppConstants.ARG_INTENT_ALARM_TYPE_SETTING).split("/");
            mCountShake = Integer.parseInt(mAlarmSetting[0]);
            mNumberPicker.setValue(mCountShake / 5);
            mLevel = Integer.parseInt(mAlarmSetting[1]);
            mSeekBar.setProgress(mLevel);
        }
        mTextCountShake.setText(getString(R.string.alarm_shake_count_time, Integer.parseInt(mShakes[mNumberPicker.getValue() - 1])));

        initEvent();
    }

    public void initActionBar() {
        setTittle(getString(R.string.alarm_tittle_shake_phone));
        setImageLeftButton(ContextCompat.getDrawable(this, R.drawable.ic_back));
        setButtonListener(this);
    }

    public void initEvent() {
        mNumberPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            if (newVal > 1) {
                mCountShake = Integer.parseInt(mShakes[newVal - 1]);
            } else {
                mCountShake = Integer.parseInt(mShakes[0]);
            }

            mTextCountShake.setText(getString(R.string.alarm_shake_count_time, mCountShake));
        });

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mLevel = progress;
                if (progress == 1) {
                    mFloatingTv.setVisibility(View.VISIBLE);
                    mFloatingTv.setText(getString(R.string.alarm_level_normal));
                } else {
                    mFloatingTv.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void sendResultShakeSetting() {
        mSetting = mCountShake + "/" + mLevel;
        Intent intent = new Intent(this, AlarmTypeActivity.class);
        intent.putExtra(AppConstants.ARG_INTENT_ALARM_TYPE_SETTING, mSetting);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.alarm_ab_left_button:
                super.onBackPressed();
                break;
            case R.id.alarm_ab_right_button:
                sendResultShakeSetting();
                break;
            default:
                break;
        }
    }

}
