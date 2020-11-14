package app.alarm.ui.detail.tone;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.View;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

import app.alarm.R;
import app.alarm.ui.detail.AlarmDetailActivity;
import app.alarm.utils.AlarmPlayer;
import app.alarm.utils.AppConstants;
import io.reactivex.disposables.CompositeDisposable;

public class AbsFragment extends Fragment implements View.OnClickListener, AlarmToneActivity.OnBackPressedListener, TabPackageListener {

    String mUriSelected = null;
    String mItemSelected = null;
    String mSongSelected = "default";
    Activity mActivity;
    AlarmPlayer mPlayer;

    CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        setAlarmSound();
        if (mActivity instanceof AlarmToneActivity) {
            AlarmToneActivity activity = (AlarmToneActivity) mActivity;
            activity.setButtonListener(this);
            activity.setOnBackPressedListener(this);
        }
    }

    private void setAlarmSound() {
        mPlayer = new AlarmPlayer(mActivity);
    }

    private void saveData() {
        stopRingTone();
        Intent intent = new Intent(getActivity(), AlarmDetailActivity.class);
        intent.putExtra(AppConstants.ARG_INTENT_SONG_VALUE_ALARM_DETAIL, mItemSelected);
        mActivity.setResult(Activity.RESULT_OK, intent);
        mActivity.finish();
    }

    int getPositionInit(String songInit, ArrayList<String> list) {
        if (songInit != null && list != null) {
            for (int i = 0; i < list.size(); i++) {
                if (songInit.equalsIgnoreCase(list.get(i))) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onGetItemSelected(String item) {
        mItemSelected = item;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.alarm_ab_left_button:
            case R.id.alarm_ab_right_button:
                saveData();
                break;
            default:
                break;
        }
    }

    @Override
    public void doBack() {
        saveData();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopRingTone();
    }

    @Override
    public void onDestroy() {
        if (mCompositeDisposable != null && !mCompositeDisposable.isDisposed()) {
            mCompositeDisposable.clear();
        }
        super.onDestroy();
    }

    void playRingTone() {
        if (mPlayer != null && mUriSelected != null) {
            resetRingTone();
            mPlayer.setPlayResource(mUriSelected);
            mPlayer.playChooseRingTon();
        }
    }

    void resetRingTone() {
        if (mPlayer != null) {
            mPlayer.reset();
        }
    }

    void stopRingTone() {
        if (mPlayer != null) {
            mPlayer.stop();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onTabSelected() {
        resetRingTone();
    }

    @Override
    public void onTabUnselected() {
        resetRingTone();
    }
}
