package app.alarm.ui.alarm.view;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import app.alarm.R;
import app.alarm.core.data.db.entities.Alarm;

public class MethodDefault extends Method {
    private TextView mTxtDismiss;
    private TextView mTxtSnooze;
    private RelativeLayout mLinearLayout;

    public MethodDefault(Activity context, Alarm alarm) {
        super(context, alarm);
    }

    @Override
    public void initView(View rootView) {
        mRootView = rootView;
        mTxtDismiss = (TextView) rootView.findViewById(R.id.dismiss);
        mTxtSnooze = (TextView) rootView.findViewById(R.id.snooze);
        mLinearLayout = rootView.findViewById(R.id.ln_default);
        mLinearLayout.setVisibility(View.VISIBLE);

        mTxtDismiss.setOnClickListener(view -> finishAlarm());

        mTxtSnooze.setOnClickListener(view -> snoozeAlarm());
    }

    @Override
    public void create() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }


}
