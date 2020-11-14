package app.alarm.ui.detail;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.util.Calendar;

import app.alarm.R;
import app.alarm.core.data.db.AppDatabase;
import app.alarm.core.data.db.dao.AlarmDao;
import app.alarm.core.data.db.entities.Alarm;
import app.alarm.core.receiver.AlarmHelper;
import app.alarm.ui.detail.repeat.RepeatActivity;
import app.alarm.ui.detail.snooze.SnoozeActivity;
import app.alarm.ui.detail.tone.AlarmToneActivity;
import app.alarm.ui.detail.type.AlarmTypeActivity;
import app.alarm.utils.AppConstants;
import app.alarm.utils.Utils;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class AlarmDetailFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "AlarmDetailFragment";
    private Activity mContext;
    private int mSavedHour = -1;
    private int mSavedMinute = -1;

    private TimePicker mTimePicker;

    private LinearLayout mAlarmTypeLayout;
    private TextView mAlarmTypeName;
    private ImageView mAlarmTypeImg;

    private LinearLayout mAlarmRepeatLayout;
    private TextView mAlarmRepeat;

    private LinearLayout mAlarmToneLayout;
    private TextView mAlarmToneName;

    private LinearLayout mAlarmSnoozeLayout;
    private TextView mAlarmSnooze;

    private LinearLayout mAlarmNameLayout;
    private TextView mAlarmName;

    private boolean mIsCreateAlarm = false;

    private int[] mRepeat = {0, 0, 0, 0, 0, 0, 0};

    private AlarmDao mAlarmDb;
    private Alarm mAlarmItem;

    CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    public static AlarmDetailFragment newInstance() {
        Bundle args = new Bundle();
        AlarmDetailFragment fragment = new AlarmDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        mAlarmDb = AppDatabase.getInstance(mContext).alarmDao();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_alarm_detail, container, false);
        initView(rootView);
        return rootView;
    }

    private void initView(View rootView) {
        Intent intent = mContext.getIntent();
        mIsCreateAlarm = intent.getBooleanExtra(AppConstants.ARG_INTENT_TYPE_CREATE_ALARM, false);

        initActionBar();

        mTimePicker = rootView.findViewById(R.id.alarm_time_picker);
        mTimePicker.setIs24HourView(Utils.isMode24Hour(mContext));

        mAlarmTypeLayout = rootView.findViewById(R.id.alarm_type_layout);
        mAlarmTypeName = rootView.findViewById(R.id.alarm_type_text);
        mAlarmTypeImg = rootView.findViewById(R.id.alarm_type_image);

        mAlarmRepeatLayout = rootView.findViewById(R.id.alarm_repeat_layout);
        mAlarmRepeat = rootView.findViewById(R.id.alarm_repeat_text);

        mAlarmToneLayout = rootView.findViewById(R.id.alarm_tone_layout);
        mAlarmToneName = rootView.findViewById(R.id.alarm_tone_name);

        mAlarmSnoozeLayout = rootView.findViewById(R.id.alarm_snooze_layout);
        mAlarmSnooze = rootView.findViewById(R.id.alarm_snooze);

        mAlarmNameLayout = rootView.findViewById(R.id.alarm_name_layout);
        mAlarmName = rootView.findViewById(R.id.alarm_name);

        // With create alarm: default snooze: off, repeat: none
        mAlarmItem = new Alarm();
        if (mIsCreateAlarm) {
            Calendar calendar = Calendar.getInstance();
            mAlarmItem.setAlarmTime(calendar.get(Calendar.HOUR_OF_DAY) * 100 + calendar.get(Calendar.MINUTE));
            mAlarmItem.setRepeatType(0);
            mAlarmItem.setSnoozeTime(0);
        } else {
            mAlarmItem = (Alarm) intent.getSerializableExtra(AppConstants.ARG_INTENT_TYPE_EDIT_ALARM);
        }

        updateUIView(mAlarmItem);

        initEvent();
    }

    private void initEvent() {
        mAlarmTypeLayout.setOnClickListener(this);
        mAlarmRepeatLayout.setOnClickListener(this);
        mAlarmToneLayout.setOnClickListener(this);
        mAlarmSnoozeLayout.setOnClickListener(this);
        mAlarmNameLayout.setOnClickListener(this);

        mTimePicker.setOnTimeChangedListener((timePicker, hour, minute) -> {
            mSavedHour = hour;
            mSavedMinute = minute;
            mAlarmItem.setAlarmTime(hour * 100 + minute);
        });
    }

    private void initActionBar() {
        if (mContext != null && mContext instanceof AlarmDetailActivity) {
            AlarmDetailActivity activity = (AlarmDetailActivity) mContext;
            activity.setButtonListener(this);
            if (mIsCreateAlarm) {
                activity.setTittle(mContext.getString(R.string.alarm_tittle_alarm_create));
            } else {
                activity.setTittle(mContext.getString(R.string.alarm_tittle_alarm_detail));
            }
        }
    }

    private void updateUIView(Alarm alarm) {
        int time = alarm.getAlarmTime();
        mSavedHour = time / 100;
        mSavedMinute = time % 100;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mTimePicker.setHour(mSavedHour);
            mTimePicker.setMinute(mSavedMinute);
        } else {
            mTimePicker.setCurrentHour(mSavedHour);
            mTimePicker.setCurrentMinute(mSavedMinute);
        }

        setStateAlarmType();
        if (alarm.getSnoozeTime() == 0) {
            mAlarmSnooze.setText(getString(R.string.alarm_snooze_time_off));
        } else {
            mAlarmSnooze.setText(getTextSnooze(alarm.getSnoozeTime()));
        }

        mAlarmRepeat.setText(Utils.getRepeatText(mContext, mAlarmItem.getRepeatType()));
        Utils.Song song = Utils.getSong(alarm.getUriSound());
        if (song != null) {
            mAlarmToneName.setText(song.mSongName);
        }

        if (mAlarmItem.getAlarmName() != null) {
            mAlarmName.setText(mAlarmItem.getAlarmName());
        }

        mRepeat = Utils.getListRepeat(mAlarmItem.getRepeatType());
    }

    private void showDialogSetName() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(mContext, R.style.ThemeDialog));
        alertDialogBuilder.setTitle(mContext.getResources().getString(R.string.alarm_tittle_dialog_set_name));

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dialog_set_name, null);
        final EditText edName = dialogView.findViewById(R.id.dialog_alarm_name);
        alertDialogBuilder.setView(dialogView);
        if (mAlarmItem.getAlarmName() != null) {
            edName.setText(mAlarmItem.getAlarmName());
        }

        alertDialogBuilder.setPositiveButton(mContext.getResources().getString(R.string.ok), (arg0, arg1) -> {
            String name = edName.getText().toString();
            mAlarmItem.setAlarmName(name);
            mAlarmName.setText(name);
        });

        alertDialogBuilder.setCancelable(true);
        AlertDialog mAlertDialog = alertDialogBuilder.create();
        mAlertDialog.show();

        Window window = mAlertDialog.getWindow();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();

        if (window != null) {
            DisplayMetrics displaymetrics = new DisplayMetrics();
            mContext.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            int width = displaymetrics.widthPixels;

            lp.copyFrom(window.getAttributes());
            lp.width = (int) (width * 0.85);
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

            window.setAttributes(lp);
            window.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.dialog_background));
        }
    }

    private String getTextSnooze(int valueSnoozeSelected) {
        String snoozeText;

        if (valueSnoozeSelected == 0) {
            snoozeText = getString(R.string.repeat_none);
        } else {
            snoozeText = getString(R.string.alarm_snooze_time, valueSnoozeSelected);
        }

        return snoozeText;
    }

    private synchronized void saveDetailChange() {
        Log.d(TAG, "saveDetailChange() ");
        long alertDate = Utils.getOnlyValueDate(mSavedHour, mSavedMinute);

        Alarm alarm = new Alarm(
                Alarm.ALARM_ACTIVE, mAlarmItem.getAlarmName(),
                alertDate, mAlarmItem.getAlarmTime(), mAlarmItem.getRepeatType(),
                mAlarmItem.getAlarmType(), mAlarmItem.getAlarmSettings(),
                mAlarmItem.getSnoozeTime(), mAlarmItem.getUriSound()
        );

        if (mIsCreateAlarm) {
            mCompositeDisposable.add(Observable.fromCallable(() -> mAlarmDb.insertAlarm(alarm))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(result -> {
                        if (result > 0) {
                            AlarmHelper.enableNextAlert(mContext);
                            sendAlarmTypeResult(alarm);
                        }
                    }));
        } else {
            alarm.setId(mAlarmItem.getId());

            mCompositeDisposable.add(Observable.fromCallable(() -> mAlarmDb.updateAlarm(alarm))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(result -> {
                        if (result > 0) {
                            AlarmHelper.enableNextAlert(mContext);
                            sendAlarmTypeResult(alarm);
                        }
                    }));
        }
    }

    public void sendAlarmTypeResult(Alarm alarm) {
        EventBus.getDefault().post(alarm);
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.alarm_type_layout:
                intent = new Intent(mContext, AlarmTypeActivity.class);
                intent.putExtra(AppConstants.ARG_INTENT_TYPE_CREATE_ALARM, mIsCreateAlarm);
                intent.putExtra(AppConstants.ARG_INTENT_ALARM_TYPE, mAlarmItem.getAlarmType());
                intent.putExtra(AppConstants.ARG_INTENT_ALARM_TYPE_SETTING, mAlarmItem.getAlarmSettings());
                startActivityForResult(intent, AppConstants.REQUEST_CODE_ALARM_TYPE);
                break;
            case R.id.alarm_repeat_layout:
                intent = new Intent(mContext, RepeatActivity.class);
                intent.putExtra(AppConstants.ARG_INTENT_REPEAT_ALARM_DETAIL, mRepeat);
                startActivityForResult(intent, AppConstants.REQUEST_CODE_REPEAT);
                break;
            case R.id.alarm_tone_layout:
                intent = new Intent(mContext, AlarmToneActivity.class);
                intent.putExtra(AppConstants.ARG_INTENT_TYPE_CREATE_ALARM, mIsCreateAlarm);
                intent.putExtra(AppConstants.ARG_INTENT_TONE_VALUE_ALARM_DETAIL, mAlarmItem.getUriSound());
                startActivityForResult(intent, AppConstants.REQUEST_CODE_TONE);
                break;
            case R.id.alarm_snooze_layout:
                intent = new Intent(mContext, SnoozeActivity.class);
                intent.putExtra(AppConstants.ARG_INTENT_SNOOZE_VALUE_ALARM_DETAIL, mAlarmItem.getSnoozeTime());
                startActivityForResult(intent, AppConstants.REQUEST_CODE_SNOOZE);
                break;
            case R.id.alarm_name_layout:
                showDialogSetName();
                break;
            case R.id.alarm_ab_left_button:
                mContext.finish();
                break;
            case R.id.alarm_ab_right_button:
                if ((mSavedHour != -1) && (mSavedMinute != -1)) {
                    saveDetailChange();
                    mContext.finish();
                } else {
                    Toast.makeText(mContext, mContext.getResources().getString(R.string.new_alarm_is_not_created), Toast.LENGTH_SHORT).show();
                }

                break;
            default:
                break;
        }
    }

    private void setStateAlarmType() {
        Drawable mDrawable;
        switch (mAlarmItem.getAlarmType()) {
            case Alarm.ALARM_METHOD_DEFAULT:
                mAlarmTypeName.setText(mContext.getString(R.string.alarm_type_default));
                mDrawable = ContextCompat.getDrawable(mContext, R.drawable.ic_alarm);
                mDrawable.setColorFilter(ContextCompat.getColor(mContext, R.color.colorIcAlarm), PorterDuff.Mode.SRC_ATOP);
                mAlarmTypeImg.setImageDrawable(mDrawable);
                break;
            case Alarm.ALARM_METHOD_TAKE_A_PHOTO:
                mAlarmTypeName.setText(mContext.getString(R.string.alarm_type_take_photo));
                mDrawable = ContextCompat.getDrawable(mContext, R.drawable.ic_photo_camera);
                mDrawable.setColorFilter(ContextCompat.getColor(mContext, R.color.colorIcCamera), PorterDuff.Mode.SRC_ATOP);
                mAlarmTypeImg.setImageDrawable(mDrawable);
                break;
            case Alarm.ALARM_METHOD_SHAKE_PHONE:
                mAlarmTypeName.setText(mContext.getString(R.string.alarm_type_shake_phone));
                mDrawable = ContextCompat.getDrawable(mContext, R.drawable.ic_smart_phone);
                mDrawable.setColorFilter(ContextCompat.getColor(mContext, R.color.colorIcShakePhone), PorterDuff.Mode.SRC_ATOP);
                mAlarmTypeImg.setImageDrawable(mDrawable);
                break;
            case Alarm.ALARM_METHOD_MATH_PROBLEMS:
                mAlarmTypeName.setText(mContext.getString(R.string.alarm_type_math_problems));
                mDrawable = ContextCompat.getDrawable(mContext, R.drawable.ic_calculator);
                mDrawable.setColorFilter(ContextCompat.getColor(mContext, R.color.colorIcCalculator), PorterDuff.Mode.SRC_ATOP);
                mAlarmTypeImg.setImageDrawable(mDrawable);
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (data == null) {
                return;
            }
            switch (requestCode) {
                case AppConstants.REQUEST_CODE_ALARM_TYPE:
                    int alarmType = data.getIntExtra(AppConstants.ARG_INTENT_ALARM_TYPE, Alarm.ALARM_METHOD_DEFAULT);
                    String alarmTypeSettings = data.getStringExtra(AppConstants.ARG_INTENT_ALARM_TYPE_SETTING);
                    mAlarmItem.setAlarmType(alarmType);
                    mAlarmItem.setAlarmSettings(alarmTypeSettings);
                    setStateAlarmType();
                    break;
                case AppConstants.REQUEST_CODE_REPEAT:
                    mRepeat = data.getIntArrayExtra(AppConstants.ARG_INTENT_REPEAT_ALARM_DETAIL);
                    int mRepeatType = 0;

                    for (int i = 6; i >= 0; --i) {
                        mRepeatType += mRepeat[i] * Math.pow(2, i);
                    }
                    mAlarmItem.setRepeatType(mRepeatType);
                    mAlarmRepeat.setText(Utils.getRepeatText(mContext, mRepeat));
                    break;
                case AppConstants.REQUEST_CODE_TONE:
                    String result = data.getStringExtra(AppConstants.ARG_INTENT_SONG_VALUE_ALARM_DETAIL);
                    Utils.Song song = Utils.getSong(result);
                    if (song != null) {
                        mAlarmItem.setUriSound(result);
                        mAlarmToneName.setText(song.mSongName);
                    }
                    break;
                case AppConstants.REQUEST_CODE_SNOOZE:
                    int snoozeTime = data.getIntExtra(AppConstants.ARG_INTENT_SNOOZE_VALUE_ALARM_DETAIL, 0);
                    mAlarmItem.setSnoozeTime(snoozeTime);
                    mAlarmSnooze.setText(getTextSnooze(snoozeTime));
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mTimePicker != null) {
            mTimePicker.setIs24HourView(Utils.isMode24Hour(mContext));
        }
    }

    @Override
    public void onDestroy() {
        //dispose subscriptions
        if (mCompositeDisposable != null && !mCompositeDisposable.isDisposed()) {
            mCompositeDisposable.clear();
        }
        super.onDestroy();
    }
}
