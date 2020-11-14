package app.alarm.core.presentation;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;

import app.alarm.R;

public class RadioGroupAdapter extends ArrayAdapter<String> {
    private Context mContext;
    private LayoutInflater mInflater;

    private ArrayList<String> mList;
    private int mPosItemSelected = -1;

    public RadioGroupAdapter(@NonNull Context context, int txtViewResourceId, ArrayList<String> list) {
        super(context, txtViewResourceId, list);
        mInflater = LayoutInflater.from(context);
        mList = list;
    }

    public void setList(ArrayList<String> list) {
        mList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_list_radio_group, null);
            viewHolder = new ViewHolder();
            viewHolder.mRadioButton = (RadioButton) convertView.findViewById(R.id.alarm_radio_button);
            viewHolder.mTittle = (TextView) convertView.findViewById(R.id.alarm_tittle);
            viewHolder.mLine = (View) convertView.findViewById(R.id.line);
            viewHolder.mRadioButton.setClickable(false);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (position == mPosItemSelected) {
            viewHolder.mRadioButton.setChecked(true);
        } else {
            viewHolder.mRadioButton.setChecked(false);
        }

        if (position < mList.size()) {
            String tittle = mList.get(position);
            viewHolder.mTittle.setText(tittle);
        }

        if (position == mList.size() - 1) {
            viewHolder.mLine.setVisibility(View.GONE);
        } else {
            viewHolder.mLine.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    public void setListItem(ArrayList<String> list) {
        mList = list;
    }

    public void setPosItemSelected(int posItemSelected) {
        mPosItemSelected = posItemSelected;
        notifyDataSetChanged();
    }

    public int getPosItemSelected() {
        return mPosItemSelected;
    }

    private static class ViewHolder {
        private RadioButton mRadioButton;
        private TextView mTittle;
        private View mLine;
    }
}
