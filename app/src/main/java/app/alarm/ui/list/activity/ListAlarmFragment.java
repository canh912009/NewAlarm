package app.alarm.ui.list.activity;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import app.alarm.R;
import app.alarm.core.data.db.entities.Alarm;
import app.alarm.core.receiver.AlarmHelper;
import app.alarm.ui.detail.AlarmDetailActivity;
import app.alarm.ui.list.adapter.ListAlarmAdapter;
import app.alarm.ui.list.adapter.MyDividerItemDecoration;
import app.alarm.utils.AppConstants;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class ListAlarmFragment extends Fragment implements ListAlarmAdapter.OnItemClickListener, View.OnClickListener {

    public static final String TAG = "ListAlarmFragment";

    private Activity mContext;
    private ListAlarmAdapter mAdapter;
    private LinearLayout mLinearLayout;

    CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    private AlarmViewModel mAlarmViewModel;

    public static ListAlarmFragment newInstance() {
        Bundle args = new Bundle();
        ListAlarmFragment fragment = new ListAlarmFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        EventBus.getDefault().register(this);

        mAlarmViewModel = ViewModelProviders.of(this).get(AlarmViewModel.class);
        getAlarms();
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onGetAlarmResult(Alarm item) {
        Log.i(TAG, "onGetAlarmResult:data Change item = " + item);
        if (item != null) {
            getAlarms();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.list_alarm_fragment, container, false);
        initView(rootView);
        return rootView;
    }

    private void initView(View rootView) {
        initActionBar();
        mLinearLayout = rootView.findViewById(R.id.layout_no_alarm);
        initRecycleView(rootView);
    }

    private void setVisibilityNoAlarm(int size) {
        if (size == 0) {
            if (!(mLinearLayout.getVisibility() == View.VISIBLE)) {
                mLinearLayout.setVisibility(View.VISIBLE);
            }
        } else {
            if (!(mLinearLayout.getVisibility() == View.GONE)) {
                mLinearLayout.setVisibility(View.GONE);
            }
        }
    }

    private void initActionBar() {
        if (mContext != null && mContext instanceof ListAlarmActivity) {
            ListAlarmActivity activity = (ListAlarmActivity) mContext;
            Drawable drawable = ContextCompat.getDrawable(mContext, R.drawable.ic_plus);
            if (drawable != null) {
                drawable.setColorFilter(ContextCompat.getColor(mContext, R.color.colorIconCommon), PorterDuff.Mode.SRC_ATOP);
                ((ListAlarmActivity) mContext).setImageRightButton(drawable);
            }

            activity.setVisibleLeftButton(View.GONE);
            activity.setButtonListener(this);
            activity.setTittle(mContext.getString(R.string.alarm_tittle_alarm));
        }
    }

    private void initRecycleView(View rootView) {
        RecyclerView mRecyclerView = rootView.findViewById(R.id.recycler_view);

        mAdapter = new ListAlarmAdapter(mContext);
        mAdapter.setOnItemClickListener(this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new MyDividerItemDecoration(mContext, LinearLayoutManager.VERTICAL, 16));
        mRecyclerView.setAdapter(mAdapter);
    }

    private void getAlarms() {
        mCompositeDisposable.add(mAlarmViewModel.getListAlarms()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(alarms -> {
                    setVisibilityNoAlarm(alarms.size());
                    mAdapter.setAlarms(alarms);
                }));
    }

    @Override
    public void onItemClick(Alarm alarm) {
        Log.i(TAG, "onItemClick: imageItem = " + alarm.toString());
        Intent intent = new Intent(mContext, AlarmDetailActivity.class);
        intent.putExtra(AppConstants.ARG_INTENT_TYPE_EDIT_ALARM, alarm);
        mContext.startActivity(intent);
    }

    public void onCloseClick(Alarm alarm, int position) {
        Log.i(TAG, "onCloseClick: imageItem = " + alarm.toString());
        mCompositeDisposable.add(mAlarmViewModel.deleteAlarm(alarm)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    if (result > 0) {
                        mAdapter.removeAt(position);

                        List<Alarm> list = mAdapter.getListAlarms();
                        if (list != null && list.size() == 0) {
                            setVisibilityNoAlarm(0);
                        }

                        AlarmHelper.enableNextAlert(mContext);
                    }
                }));
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onItemLongClick(Alarm alarm) {
        Log.i(TAG, "onCloseClick: imageItem = " + alarm.toString());
    }

    @Override
    public void onSwitchChange(Alarm alarm, boolean active) {
        alarm.setActive(active ? Alarm.ALARM_ACTIVE : Alarm.ALARM_INACTIVE);
        mCompositeDisposable.add(mAlarmViewModel.updateAlarmByActive(alarm.getId(), alarm.getActive())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    if (result > 0) {
                        AlarmHelper.enableNextAlert(mContext);
                    }
                }));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.alarm_ab_left_button:
                break;
            case R.id.alarm_ab_right_button:
                Intent intent = new Intent(mContext, AlarmDetailActivity.class);
                intent.putExtra(AppConstants.ARG_INTENT_TYPE_CREATE_ALARM, true);
                mContext.startActivity(intent);
                break;
            default:
                break;
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
