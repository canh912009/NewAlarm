package app.alarm.ui.detail;

import android.os.Bundle;
import androidx.fragment.app.FragmentTransaction;

import app.alarm.R;
import app.alarm.core.presentation.BaseActivity;

public class AlarmDetailActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_alarm_detail);
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            attachFragment();
        }
    }

    public void attachFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.alarm_create_detail_container, AlarmDetailFragment.newInstance()).commit();
    }
}
