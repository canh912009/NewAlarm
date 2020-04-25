package app.alarm.core.presentation;

import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import app.alarm.R;

public abstract class BaseActivity extends AppCompatActivity {

    private ImageView mRightButton;
    private ImageView mLeftButton;
    private FrameLayout mRightButtonLayout;
    private FrameLayout mLeftButtonLayout;
    private TextView mTittle;

    View.OnClickListener mListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(R.layout.activity_base);

        initViews();

        if (view != null) {
            // add the layout
            ViewGroup root = (ViewGroup) findViewById(R.id.container);
            if (root != null) {
                root.addView(view);
            }
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(R.layout.activity_base);

        initViews();

        // add the layout
        View layout = getLayoutInflater().inflate(layoutResID, null);
        ViewGroup root = (ViewGroup) findViewById(R.id.container);
        if (root != null) {
            root.addView(layout);
        }
    }

    private void initViews() {
        mRightButton = (ImageView) findViewById(R.id.alarm_ab_right_button_img);
        mLeftButton = (ImageView) findViewById(R.id.alarm_ab_left_button_img);
        mRightButtonLayout = (FrameLayout) findViewById(R.id.alarm_ab_right_button);
        mLeftButtonLayout = (FrameLayout) findViewById(R.id.alarm_ab_left_button);
        mTittle = (TextView) findViewById(R.id.alarm_ab_tittle);
    }

    public void setImageRightButton(Drawable res) {
        mRightButton.setImageDrawable(res);
    }

    public void setImageLeftButton(Drawable res) {
        mLeftButton.setImageDrawable(res);
    }

    public void setVisibleRightButton(int visible) {
        if (mRightButton.getVisibility() != visible) {
            mRightButton.setVisibility(visible);
        }
    }

    public void setVisibleLeftButton(int visible) {
        if (mLeftButton.getVisibility() != visible) {
            mLeftButton.setVisibility(visible);
        }
    }

    public void setTittle(String tittle) {
        mTittle.setText(tittle);
    }

    public void setVisibleTittle(int visible) {
        mTittle.setVisibility(visible);
    }

    public void setButtonListener(View.OnClickListener mListener) {
        this.mListener = mListener;

        mRightButtonLayout.setOnClickListener(mListener);
        mLeftButtonLayout.setOnClickListener(mListener);
    }
}
