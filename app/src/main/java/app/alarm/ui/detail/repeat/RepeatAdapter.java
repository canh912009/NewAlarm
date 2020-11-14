package app.alarm.ui.detail.repeat;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import app.alarm.R;
import app.alarm.utils.Utils;

public class RepeatAdapter extends RecyclerView.Adapter<RepeatAdapter.ViewHolder> {
    private Context mContext;
    private List<String> mList;
    private int[] mListSelected = {0, 0, 0, 0, 0, 0, 0};

    public interface OnAllCheckedListener {
        void onAllCheckedListener(boolean isAllChecked);
    }

    private OnAllCheckedListener mListener;

    RepeatAdapter(Context context, OnAllCheckedListener listener) {
        mContext = context;
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.item_list_repeat, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        String tittle = mList.get(position);
        holder.mTittle.setText(tittle);
        if (mListSelected[position] == 1) {
            holder.mCheckBox.setChecked(true);
        } else {
            holder.mCheckBox.setChecked(false);
        }

        holder.mCheckBox.setOnClickListener(v ->
                setRepeat(holder.mCheckBox, holder.mCheckBox.isChecked(), position));

        holder.mLayout.setOnClickListener(v ->
                setRepeat(holder.mCheckBox, !holder.mCheckBox.isChecked(), position));

        if (position == 6) {
            holder.mLine.setVisibility(View.GONE);
        } else {
            holder.mLine.setVisibility(View.VISIBLE);
        }
    }

    private void setRepeat(CheckBox checkBox, boolean isCheck, int position) {
        if (isCheck) {
            checkBox.setChecked(true);
            mListSelected[position] = 1;
        } else {
            checkBox.setChecked(false);
            mListSelected[position] = 0;
        }

        mListener.onAllCheckedListener(Utils.isCheckedAll(mListSelected));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void setListItem(List<String> list) {
        mList = list;
    }

    public int[] getListItemSelected() {
        return mListSelected;
    }

    public void setListItemSelected(int[] listItemSelected) {
        mListSelected = listItemSelected;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private CheckBox mCheckBox;
        private RelativeLayout mLayout;
        private TextView mTittle;
        private View mLine;

        public ViewHolder(View itemView) {
            super(itemView);

            mCheckBox = itemView.findViewById(R.id.alarm_check_box);
            mLayout = itemView.findViewById(R.id.alarm_detail_item_list_layout);
            mTittle = itemView.findViewById(R.id.alarm_tittle);
            mLine = itemView.findViewById(R.id.line);
        }
    }
}
