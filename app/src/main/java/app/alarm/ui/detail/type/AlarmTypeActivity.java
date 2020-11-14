package app.alarm.ui.detail.type;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import android.view.Display;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import app.alarm.R;
import app.alarm.core.data.db.entities.Alarm;
import app.alarm.core.presentation.BaseActivity;
import app.alarm.ui.detail.AlarmDetailActivity;
import app.alarm.utils.AppConstants;

public class AlarmTypeActivity extends BaseActivity implements View.OnClickListener {
    private RelativeLayout mAlarmLayout;
    private RelativeLayout mTakePhotoLayout;
    private RelativeLayout mShakePhoneLayout;
    private RelativeLayout mMathProLayout;

    private int mAlarmType;
    private String mAlarmTypeSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_type);

        initView();
    }

    public void initView() {
        initActionBar();

        mAlarmLayout = (RelativeLayout) findViewById(R.id.alarm_layout);
        mTakePhotoLayout = (RelativeLayout) findViewById(R.id.take_photo_layout);
        mShakePhoneLayout = (RelativeLayout) findViewById(R.id.shake_phone_layout);
        mMathProLayout = (RelativeLayout) findViewById(R.id.math_pro_layout);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(100, 100);
        layoutParams.width = size.x * 5 / 11;
        layoutParams.height = layoutParams.width;
        mAlarmLayout.setLayoutParams(layoutParams);
        mTakePhotoLayout.setLayoutParams(layoutParams);
        mShakePhoneLayout.setLayoutParams(layoutParams);
        mMathProLayout.setLayoutParams(layoutParams);

        initEvent();

        Intent intent = getIntent();
        mAlarmType = intent.getIntExtra(AppConstants.ARG_INTENT_ALARM_TYPE, Alarm.ALARM_METHOD_DEFAULT);
        mAlarmTypeSetting = intent.getStringExtra(AppConstants.ARG_INTENT_ALARM_TYPE_SETTING);
        setBackgroundLayout(mAlarmType);
    }

    public void initActionBar() {
        setTittle(getString(R.string.alarm_off_method));
        setImageLeftButton(ContextCompat.getDrawable(this, R.drawable.ic_back));
        setVisibleRightButton(View.GONE);
        setButtonListener(this);
    }

    public void initEvent() {
        mAlarmLayout.setOnClickListener(this);
        mTakePhotoLayout.setOnClickListener(this);
        mShakePhoneLayout.setOnClickListener(this);
        mMathProLayout.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.alarm_layout:
                setBackgroundLayout(Alarm.ALARM_METHOD_DEFAULT);
                mAlarmType = Alarm.ALARM_METHOD_DEFAULT;
                break;
            case R.id.take_photo_layout:
                boolean isNewTakePhoto = false;
                if (mAlarmType != Alarm.ALARM_METHOD_TAKE_A_PHOTO) {
                    isNewTakePhoto = true;
                }

                intent = new Intent(this, TakePhotoActivity.class);
                intent.putExtra(AppConstants.ARG_INTENT_TYPE_CREATE_ALARM, isNewTakePhoto);
                intent.putExtra(AppConstants.ARG_INTENT_ALARM_TYPE_SETTING, mAlarmTypeSetting);
                startActivityForResult(intent, AppConstants.REQUEST_CODE_GET_TAKE_PHOTO);
                break;
            case R.id.shake_phone_layout:
                boolean isNewCreateShake = false;
                if (mAlarmType != Alarm.ALARM_METHOD_SHAKE_PHONE) {
                    isNewCreateShake = true;
                }
                intent = new Intent(this, ShakePhoneSettingActivity.class);
                intent.putExtra(AppConstants.ARG_INTENT_TYPE_CREATE_ALARM, isNewCreateShake);
                intent.putExtra(AppConstants.ARG_INTENT_ALARM_TYPE_SETTING, mAlarmTypeSetting);
                startActivityForResult(intent, AppConstants.REQUEST_CODE_GET_SHAKE_PHONE);
                break;
            case R.id.math_pro_layout:
                boolean isNewCreateMath = false;
                if (mAlarmType != Alarm.ALARM_METHOD_MATH_PROBLEMS) {
                    isNewCreateMath = true;
                }
                intent = new Intent(this, MathProblemSettingActivity.class);
                intent.putExtra(AppConstants.ARG_INTENT_TYPE_CREATE_ALARM, isNewCreateMath);
                intent.putExtra(AppConstants.ARG_INTENT_ALARM_TYPE_SETTING, mAlarmTypeSetting);
                startActivityForResult(intent, AppConstants.REQUEST_CODE_GET_MATH_PRO);
                break;
            case R.id.alarm_ab_left_button:
                sendAlarmTypeResult();
                finish();
                break;
            default:
                break;
        }
    }

    private void setBackgroundLayout(int alarmType) {
        switch (alarmType) {
            case Alarm.ALARM_METHOD_DEFAULT:
                mAlarmLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.alarm_type_layout_bg_selected));
                mTakePhotoLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.alarm_type_layout_bg));
                mShakePhoneLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.alarm_type_layout_bg));
                mMathProLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.alarm_type_layout_bg));
                break;
            case Alarm.ALARM_METHOD_TAKE_A_PHOTO:
                mAlarmLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.alarm_type_layout_bg));
                mTakePhotoLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.alarm_type_layout_bg_selected));
                mShakePhoneLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.alarm_type_layout_bg));
                mMathProLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.alarm_type_layout_bg));
                break;
            case Alarm.ALARM_METHOD_SHAKE_PHONE:
                mAlarmLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.alarm_type_layout_bg));
                mTakePhotoLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.alarm_type_layout_bg));
                mShakePhoneLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.alarm_type_layout_bg_selected));
                mMathProLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.alarm_type_layout_bg));
                break;
            case Alarm.ALARM_METHOD_MATH_PROBLEMS:
                mAlarmLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.alarm_type_layout_bg));
                mTakePhotoLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.alarm_type_layout_bg));
                mShakePhoneLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.alarm_type_layout_bg));
                mMathProLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.alarm_type_layout_bg_selected));
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case AppConstants.REQUEST_CODE_GET_SHAKE_PHONE:
                    mAlarmType = Alarm.ALARM_METHOD_SHAKE_PHONE;
                    break;
                case AppConstants.REQUEST_CODE_GET_MATH_PRO:
                    mAlarmType = Alarm.ALARM_METHOD_MATH_PROBLEMS;
                    break;
                case AppConstants.REQUEST_CODE_GET_TAKE_PHOTO:
                    mAlarmType = Alarm.ALARM_METHOD_TAKE_A_PHOTO;
                    break;
                default:
                    break;
            }
        }
        mAlarmTypeSetting = data.getStringExtra(AppConstants.ARG_INTENT_ALARM_TYPE_SETTING);
        setBackgroundLayout(mAlarmType);
    }

    public void sendAlarmTypeResult() {
        Intent intent = new Intent(this, AlarmDetailActivity.class);
        intent.putExtra(AppConstants.ARG_INTENT_ALARM_TYPE, mAlarmType);
        intent.putExtra(AppConstants.ARG_INTENT_ALARM_TYPE_SETTING, mAlarmTypeSetting);
        setResult(Activity.RESULT_OK, intent);
    }

    @Override
    public void onBackPressed() {
        sendAlarmTypeResult();
        super.onBackPressed();
    }
}
