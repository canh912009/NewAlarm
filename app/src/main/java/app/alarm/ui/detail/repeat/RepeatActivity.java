package app.alarm.ui.detail.repeat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RelativeLayout;

import java.util.Arrays;
import java.util.List;

import app.alarm.R;
import app.alarm.core.presentation.BaseActivity;
import app.alarm.utils.AppConstants;
import app.alarm.utils.Utils;

public class RepeatActivity extends BaseActivity implements View.OnClickListener, RepeatAdapter.OnAllCheckedListener {

    private RepeatAdapter mAdapter;
    private List<String> mListRepeat;

    private RelativeLayout mLayoutEveryDay;
    private CheckBox mCheckBoxEveryDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repeat);

        initActionBar();
    }

    public void initActionBar() {
        setTittle(getString(R.string.alarm_text_repeat));
        setImageLeftButton(ContextCompat.getDrawable(this, R.drawable.ic_back));
        setVisibleRightButton(View.GONE);
        setButtonListener(this);

        initData();
        initRecycleView();
    }

    public void initData() {
        mListRepeat = Arrays.asList(getResources().getStringArray(R.array.alarm_repeat));
    }

    private void initRecycleView() {
        RecyclerView mRecyclerView = findViewById(R.id.alarm_list_item);
        mLayoutEveryDay = findViewById(R.id.alarm_detail_item_list_layout);
        mCheckBoxEveryDay = findViewById(R.id.alarm_check_box);

        mAdapter = new RepeatAdapter(this, this);
        mAdapter.setListItem(mListRepeat);

        int[] list = getIntent().getIntArrayExtra(AppConstants.ARG_INTENT_REPEAT_ALARM_DETAIL);
        mAdapter.setListItemSelected(list);
        mCheckBoxEveryDay.setChecked(Utils.isCheckedAll(list));

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mLayoutEveryDay.setOnClickListener(view ->
                setRepeatEveryday(!mCheckBoxEveryDay.isChecked()));

        mCheckBoxEveryDay.setOnClickListener(view ->
                setRepeatEveryday(mCheckBoxEveryDay.isChecked()));
    }

    private void setRepeatEveryday(boolean isCheck) {
        if (isCheck) {
            mCheckBoxEveryDay.setChecked(true);
            mAdapter.setListItemSelected(new int[]{1, 1, 1, 1, 1, 1, 1});
        } else {
            mCheckBoxEveryDay.setChecked(false);
            mAdapter.setListItemSelected(new int[]{0, 0, 0, 0, 0, 0, 0});
        }

        mAdapter.notifyDataSetChanged();
    }

    public void sendResultRepeat() {
        Intent intent = new Intent(this, RepeatActivity.class);
        int[] data = mAdapter.getListItemSelected();
        intent.putExtra(AppConstants.ARG_INTENT_REPEAT_ALARM_DETAIL, data);

        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        sendResultRepeat();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.alarm_ab_left_button) {
            sendResultRepeat();
        }
    }

    @Override
    public void onAllCheckedListener(boolean isAllChecked) {
        if (mCheckBoxEveryDay != null && mCheckBoxEveryDay.isChecked() != isAllChecked) {
            mCheckBoxEveryDay.setChecked(isAllChecked);
        }
    }
}
