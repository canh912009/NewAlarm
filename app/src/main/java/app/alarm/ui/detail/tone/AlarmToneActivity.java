package app.alarm.ui.detail.tone;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;

import app.alarm.R;
import app.alarm.core.presentation.BaseActivity;
import app.alarm.utils.Utils;

import static app.alarm.utils.AppConstants.ARG_INTENT_TONE_VALUE_ALARM_DETAIL;

public class AlarmToneActivity extends BaseActivity implements View.OnClickListener,
        TabLayout.OnTabSelectedListener {

    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    OnBackPressedListener mOnBackPressedListener;
    public static ArrayList<Integer> sTabIndex;

    public static final int RING_TON_TAB_INDEX = 0;
    public static final int SONG_TAB_INDEX = 1;

    private TabPackageListener mRingTonsTabListener;
    private TabPackageListener mSongTabListener;
    private int mTab = 0;
    private String mUri = null;

    public void setRingTonsTabListener(TabPackageListener eventListener) {
        mRingTonsTabListener = eventListener;
    }

    public void setSongTabListener(TabPackageListener eventListener) {
        mSongTabListener = eventListener;
    }

    public void setOnBackPressedListener(OnBackPressedListener listener) {
        mOnBackPressedListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_tone);
        initData();
        initActionBar();
        initView();
    }

    public String getUriPositionInit() {
        return mUri;
    }

    private void initData() {
        String mData = getIntent().getStringExtra(ARG_INTENT_TONE_VALUE_ALARM_DETAIL);
        Utils.Song song = Utils.getSong(mData);
        if (song != null) {
            mTab = Integer.valueOf(song.mTab);
            mUri = song.mUri;
        }
    }

    private void initView() {
        createTabs();

        mViewPager = findViewById(R.id.view_pager_alarm_tone);
        final AlarmTonePagerAdapter tonePagerAdapter = new AlarmTonePagerAdapter(getSupportFragmentManager(), mTabLayout.getTabCount());
        mViewPager.setAdapter(tonePagerAdapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mViewPager.setCurrentItem(mTab);
        mTabLayout.addOnTabSelectedListener(this);
    }

    private void createTabs() {
        mTabLayout = findViewById(R.id.tab_alarm_tone);
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.ring_ton));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.song));

        sTabIndex = new ArrayList<>(Arrays.asList(RING_TON_TAB_INDEX, SONG_TAB_INDEX));
    }

    public void initActionBar() {
        setTittle(getString(R.string.alarm_text_alarm_tone_title));
        setImageLeftButton(ContextCompat.getDrawable(this, R.drawable.ic_back));
        /*setImageRightButton(getDrawable(R.drawable.ic_search));*/
        setButtonListener(this);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.alarm_ab_left_button:
            case R.id.alarm_ab_right_button:
                break;
            default:
                break;
        }
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        mViewPager.setCurrentItem(tab.getPosition());

        switch (tab.getPosition()) {
            case RING_TON_TAB_INDEX:
                if (mRingTonsTabListener != null) {
                    mRingTonsTabListener.onTabSelected();
                }
                break;
            case SONG_TAB_INDEX:
                if (mSongTabListener != null) {
                    mSongTabListener.onTabSelected();
                }
                break;
        }

    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

        switch (tab.getPosition()) {
            case RING_TON_TAB_INDEX:
                if (mRingTonsTabListener != null) {
                    mRingTonsTabListener.onTabUnselected();
                }
                break;
            case SONG_TAB_INDEX:
                if (mSongTabListener != null) {
                    mSongTabListener.onTabUnselected();
                }
                break;
        }
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public void onBackPressed() {
        mOnBackPressedListener.doBack();
        super.onBackPressed();
    }

    public interface OnBackPressedListener {
        void doBack();
    }
}
