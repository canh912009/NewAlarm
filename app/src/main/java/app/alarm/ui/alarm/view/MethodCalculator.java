package app.alarm.ui.alarm.view;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Random;

import app.alarm.R;
import app.alarm.core.data.db.entities.Alarm;

public class MethodCalculator extends Method {

    private static final String TAG = "Method Calculator";

    private Activity mContext;

    private TextView mResultCaculator, mCalculation, mNumber0, mNumber1, mNumber2, mNumber3, mNumber4,
            mNumber5, mNumber6, mNumber7, mNumber8, mNumber9;
    private LinearLayout mDelete, mTick;
    private StringBuilder mStringResult;
    private LinearLayout mLinearLayout;
    private int a, b, c, numOfCaculatorDone = 0; // 3 numbers for calculation
    private int resultInput = 0, mCorrectResult;

    public MethodCalculator(Activity context, Alarm alarm) {
        super(context, alarm);
    }

    private int rand(int min, int max) {
        try {
            Random rn = new Random();
            int range = max - min + 1;
            return min + rn.nextInt(range);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private void initCalculation() {
        mStringResult.setLength(0);
        mResultCaculator.setText("???");

        Log.d(TAG, "mCount = " + mCount + ", mLevel = " + mLevel);

        switch (mLevel) {
            case 0:
                a = rand(0, 9);
                b = rand(0, 9);
                mCalculation.setText(a + " + " + b);
                mCorrectResult = a + b;
                break;
            case 1:
                a = rand(0, 9);
                b = rand(0, 9);
                c = rand(0, 9);
                mCalculation.setText(a + " + " + b + " + " + c);
                mCorrectResult = a + b + c;
                break;
            case 2:
                a = rand(1, 9);
                b = rand(1, 9);
                c = rand(0, 9);
                mCalculation.setText("(" + a + " * " + b + ")" + " + " + c);
                mCorrectResult = a * b + c;
                break;
            default:
                break;
        }

    }

    @Override
    public void initView(View rootView) {
        mLinearLayout = rootView.findViewById(R.id.ln_method_calculator);
        mLinearLayout.setVisibility(View.VISIBLE);

        mStringResult = new StringBuilder();
        mResultCaculator = rootView.findViewById(R.id.alarm_calculator_result);
        mCalculation = rootView.findViewById(R.id.alarm_calculator);
        initCalculation();

        mNumber0 = rootView.findViewById(R.id.num0);
        mNumber0.setOnClickListener(v -> {
            mStringResult.append(0);
            mResultCaculator.setText(mStringResult);
        });

        mNumber1 = rootView.findViewById(R.id.num1);
        mNumber1.setOnClickListener(v -> {
            mStringResult.append(1);
            mResultCaculator.setText(mStringResult);
        });

        mNumber2 = rootView.findViewById(R.id.num2);
        mNumber2.setOnClickListener(v -> {
            mStringResult.append(2);
            mResultCaculator.setText(mStringResult);
        });
        mNumber3 = rootView.findViewById(R.id.num3);
        mNumber3.setOnClickListener(v -> {
            mStringResult.append(3);
            mResultCaculator.setText(mStringResult);
        });

        mNumber4 = rootView.findViewById(R.id.num4);
        mNumber4.setOnClickListener(v -> {
            mStringResult.append(4);
            mResultCaculator.setText(mStringResult);
        });

        mNumber5 = rootView.findViewById(R.id.num5);
        mNumber5.setOnClickListener(v -> {
            mStringResult.append(5);
            mResultCaculator.setText(mStringResult);
        });

        mNumber6 = rootView.findViewById(R.id.num6);
        mNumber6.setOnClickListener(v -> {
            mStringResult.append(6);
            mResultCaculator.setText(mStringResult);
        });

        mNumber7 = rootView.findViewById(R.id.num7);
        mNumber7.setOnClickListener(v -> {
            mStringResult.append(7);
            mResultCaculator.setText(mStringResult);
        });

        mNumber8 = rootView.findViewById(R.id.num8);
        mNumber8.setOnClickListener(v -> {
            mStringResult.append(8);
            mResultCaculator.setText(mStringResult);
        });

        mNumber9 = rootView.findViewById(R.id.num9);
        mNumber9.setOnClickListener(v -> {
            mStringResult.append(9);
            mResultCaculator.setText(mStringResult);
        });

        mDelete = rootView.findViewById(R.id.numX);
        mDelete.setOnClickListener(v -> {
            if (mStringResult.length() > 0) {
                mStringResult.setLength(mStringResult.length() - 1);
                mResultCaculator.setText(mStringResult);
            }
        });

        mTick = rootView.findViewById(R.id.numV);
        mTick.setOnClickListener(v -> {
            try {
                resultInput = Integer.parseInt(mResultCaculator.getText().toString());
            } catch (NumberFormatException e) {
                Log.d(TAG, e.toString());
            }

            if (resultInput == mCorrectResult) {
                numOfCaculatorDone++;
                Log.e(TAG, "DONE, and count = " + numOfCaculatorDone);

                if (numOfCaculatorDone == mCount) {
                    finishAlarm();
                }

                initCalculation();
            } else {
                mStringResult.setLength(0);
                mResultCaculator.setText("Fail");
            }
        });
    }

    @Override
    public void create() {
        super.create();
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
