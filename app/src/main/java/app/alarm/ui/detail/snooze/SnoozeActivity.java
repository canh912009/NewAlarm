package app.alarm.ui.detail.snooze;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import app.alarm.R;
import app.alarm.core.presentation.BaseActivity;
import app.alarm.core.presentation.RadioGroupAdapter;
import app.alarm.ui.detail.AlarmDetailActivity;
import app.alarm.utils.AppConstants;

public class SnoozeActivity extends BaseActivity implements View.OnClickListener {

    private RadioGroupAdapter mAdapter;
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snooze);

        initActionBar();
        initListView();
    }

    public void initActionBar() {
        setTittle(getString(R.string.alarm_snooze));
        setImageLeftButton(ContextCompat.getDrawable(this, R.drawable.ic_back));
        setButtonListener(this);
    }

    private void initListView() {
        mListView = (ListView) findViewById(R.id.alarm_list_item);

        ArrayList<String> listSnooze = getListDataSnooze();
        mAdapter = new RadioGroupAdapter(this, android.R.layout.simple_list_item_1, listSnooze);
        mAdapter.setListItem(listSnooze);
        int valueSnoozeSelected = getIntent().getIntExtra(AppConstants.ARG_INTENT_SNOOZE_VALUE_ALARM_DETAIL, 0);
        mAdapter.setPosItemSelected(getPosSnoozeSelected(valueSnoozeSelected));

        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mAdapter.setPosItemSelected(position);
            }
        });
    }

    public ArrayList<String> getListDataSnooze() {
        ArrayList<String> listSnooze = new ArrayList<>();
        listSnooze.add(getString(R.string.alarm_snooze_time_off));
        listSnooze.add(String.format(getString(R.string.alarm_snooze_time), 1));

        for (int i = 1; i < 13; i++) {
            listSnooze.add(String.format(getString(R.string.alarm_snooze_time), 5 * i));
        }

        return listSnooze;
    }

    public int getPosSnoozeSelected(int value) {
        if (value == 0 || value == 1) {
            return value;
        }

        return value / 5 + 1;
    }

    public int getValueSnoozeSelected(int pos) {
        if (pos == 0 || pos == 1) {
            return pos;
        }

        return (pos - 1) * 5;
    }

    public void sendResultSnooze() {
        int posSnoozeSelected = mAdapter.getPosItemSelected();
        Intent intent = new Intent(this, AlarmDetailActivity.class);
        intent.putExtra(AppConstants.ARG_INTENT_SNOOZE_VALUE_ALARM_DETAIL, getValueSnoozeSelected(posSnoozeSelected));
        setResult(Activity.RESULT_OK, intent);

        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.alarm_ab_left_button:
            case R.id.alarm_ab_right_button:
                sendResultSnooze();
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        sendResultSnooze();
    }
}
