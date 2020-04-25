package app.alarm.ui.detail.type;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;

import app.alarm.R;
import app.alarm.core.presentation.BaseActivity;
import app.alarm.utils.AppConstants;

public class MathProblemSettingActivity extends BaseActivity implements View.OnClickListener {
    private NumberPicker mNumberPicker;
    private TextView mTextNumOfMathPro;

    private SeekBar mSeekBar;
    private TextView mStartTv;
    private TextView mEndTv;
    private TextView mFloatingTv;

    private boolean mIsCreateAlarm = false;
    private int mCountProblem;
    private int mLevel;
    private String mSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_math_problem_setting);

        initView();
    }

    public void initView() {
        initActionBar();

        mTextNumOfMathPro = (TextView) findViewById(R.id.text_count_shake);
        mNumberPicker = (NumberPicker) findViewById(R.id.number_picker);

        mNumberPicker.setMaxValue(20);
        mNumberPicker.setMinValue(1);

        mStartTv = (TextView) findViewById(R.id.startTv);
        mEndTv = (TextView) findViewById(R.id.endTv);
        mFloatingTv = (TextView) findViewById(R.id.math_pro_text);
        mSeekBar = (SeekBar) findViewById(R.id.seekbar);

        mSeekBar.setMax(2);
        mStartTv.setText(getString(R.string.alarm_level_easy));
        mEndTv.setText(getString(R.string.alarm_level_hard));

        Intent intent = getIntent();
        mIsCreateAlarm = intent.getBooleanExtra(AppConstants.ARG_INTENT_TYPE_CREATE_ALARM, true);

        if (mIsCreateAlarm) {
            mCountProblem = 1; // default
            mNumberPicker.setValue(1);
            mLevel = 0;
            mSeekBar.setProgress(0);
        } else {
            String[] mAlarmSetting = intent.getStringExtra(AppConstants.ARG_INTENT_ALARM_TYPE_SETTING).split("/");
            mCountProblem = Integer.parseInt(mAlarmSetting[0]);
            mNumberPicker.setValue(mCountProblem);
            mLevel = Integer.parseInt(mAlarmSetting[1]);
            mSeekBar.setProgress(mLevel);
        }

        mTextNumOfMathPro.setText(getString(R.string.alarm_num_of_math_pro, mNumberPicker.getValue()));
        setFloatingText(mSeekBar.getProgress());

        initEvent();
    }

    public void initActionBar() {
        setTittle(getString(R.string.alarm_tittle_math_pro));
        setImageLeftButton(ContextCompat.getDrawable(this, R.drawable.ic_back));
        setButtonListener(this);
    }

    public void initEvent() {
        mNumberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                mCountProblem = newVal;
                mTextNumOfMathPro.setText(getString(R.string.alarm_num_of_math_pro, newVal));
            }
        });

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mLevel = progress;
                setFloatingText(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void setFloatingText(int progress) {
        if (progress == 0) {
            mFloatingTv.setText(getString(R.string.alarm_level_demo_math_easy));
        } else if (progress == 1) {
            mFloatingTv.setText(getString(R.string.alarm_level_demo_math_normal));
        } else {
            mFloatingTv.setText(getString(R.string.alarm_level_demo_math_difficult));
        }
    }

    public void sendResultMathSetting() {
        mSetting = mCountProblem + "/" + mLevel;
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
                sendResultMathSetting();
                break;
            default:
                break;
        }
    }
}
