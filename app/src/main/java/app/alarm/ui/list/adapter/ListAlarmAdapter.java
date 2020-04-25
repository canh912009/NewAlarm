package app.alarm.ui.list.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import app.alarm.R;
import app.alarm.core.data.db.entities.Alarm;
import app.alarm.utils.Utils;

public class ListAlarmAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "ListAlarmAdapter";

    private List<Alarm> mAlarms = new ArrayList<>();

    private Context mContext;

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final AlarmsViewHolder viewHolder = (AlarmsViewHolder) holder;
        if (position < 0 || position >= getItemCount()) {
            Log.e(TAG, "wrong position, position=" + position);
            return;
        }

        Alarm alarm = mAlarms.get(position);
        viewHolder.bind(alarm, position);
    }

    public ListAlarmAdapter(Context context) {
        mContext = context;
    }

    public void setAlarm(Alarm alarm) {
        mAlarms.add(alarm);
        notifyDataSetChanged();
    }

    public void setAlarms(List<Alarm> alarms) {
        mAlarms = alarms;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.alarm_item, parent, false);
        return new AlarmsViewHolder(itemView);
    }

    public void removeAt(int position) {
        mAlarms.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mAlarms.size());
    }

    public List<Alarm> getListAlarms() {
        return mAlarms;
    }

    public void addItem(Alarm alarm) {
        mAlarms.add(alarm);
        notifyDataSetChanged();
    }

    public void updateItem(int position, Alarm alarm) {
        mAlarms.add(0, alarm);
        notifyItemInserted(position);
        notifyItemRangeChanged(position, mAlarms.size());
    }

    @Override
    public int getItemCount() {
        return (mAlarms != null) ? mAlarms.size() : 0;
    }

    public void clear() {
        if (mAlarms != null) {
            mAlarms.clear();
            notifyDataSetChanged();
        }
    }

    public interface OnItemClickListener {

        void onItemClick(Alarm alarm);

        void onCloseClick(Alarm alarm, int position);

        void onItemLongClick(Alarm alarm);

        void onSwitchChange(Alarm alarm, boolean value);
    }

    public class AlarmsViewHolder extends RecyclerView.ViewHolder {

        private final ImageView mTypeRepeat;
        private final TextView mName;
        private final TextView mSetTime;
        private final RelativeLayout mBackGround;
        private final ImageView mImgClose;
        private final SwitchCompat mSwitch;
        private final TextView mRepeatText;
        private final FrameLayout mFrameLayout;


        public AlarmsViewHolder(View itemView) {
            super(itemView);
            mBackGround = itemView.findViewById(R.id.backgound);
            mTypeRepeat = itemView.findViewById(R.id.img_type_repeat);
            mName = itemView.findViewById(R.id.title);
            mImgClose = itemView.findViewById(R.id.img_close);
            mSetTime = itemView.findViewById(R.id.set_time);
            mSwitch = itemView.findViewById(R.id.switch_on_off);
            mRepeatText = itemView.findViewById(R.id.repeat_day);
            mFrameLayout = itemView.findViewById(R.id.frame_close);
        }

        public void bind(final Alarm alarm, final int position) {
            mRepeatText.setText(Utils.getRepeatText(mContext, alarm.getRepeatType()));
            mName.setText(alarm.getAlarmName());
            mBackGround.setOnClickListener(view -> mOnItemClickListener.onItemClick(alarm));
            mFrameLayout.setOnClickListener(view -> mOnItemClickListener.onCloseClick(alarm, position));
            Utils.setTimeToFormatTime(mContext, mSetTime, alarm.getAlarmTime());
            setTextColorForTextTime(alarm.getActive() == 1);
            mTypeRepeat.setImageResource(getIcon(alarm.getAlarmType()).mDrawable);
            /*drawable.setColorFilter(, PorterDuff.Mode.SRC_ATOP);*/
            mTypeRepeat.setColorFilter(ContextCompat.getColor(mContext, getIcon(alarm.getAlarmType()).mColor));

            mSwitch.setChecked(alarm.getActive() == 1);

            mSwitch.setOnClickListener(v -> {
                boolean isChecked = mSwitch.isChecked();
                mSwitch.setChecked(isChecked);
                setTextColorForTextTime(isChecked);
                mOnItemClickListener.onSwitchChange(alarm, isChecked);
            });
        }

        private void setTextColorForTextTime(boolean isActive) {
            if (isActive) {
                mSetTime.setTextColor(ContextCompat.getColor(mContext, R.color.ColorTextSetTimeActive));
            } else {
                mSetTime.setTextColor(ContextCompat.getColor(mContext, R.color.ColorTextSetTimeDisAble));
            }
        }

        private MethodIcon getIcon(int method) {
            switch (method) {
                case Alarm.ALARM_METHOD_DEFAULT:
                    return new MethodIcon(R.color.colorIcAlarm, R.drawable.ic_alarm);
                case Alarm.ALARM_METHOD_MATH_PROBLEMS:
                    return new MethodIcon(R.color.colorIcCalculator, R.drawable.ic_calculator);
                case Alarm.ALARM_METHOD_SHAKE_PHONE:
                    return new MethodIcon(R.color.colorIcShakePhone, R.drawable.ic_smart_phone);
                case Alarm.ALARM_METHOD_TAKE_A_PHOTO:
                    return new MethodIcon(R.color.colorIcCamera, R.drawable.ic_photo_camera);
                default:
                    return new MethodIcon(R.color.colorIcAlarm, R.drawable.ic_alarm);
            }
        }
    }

    public static class MethodIcon {
        int mColor = -1;
        int mDrawable = -1;

        public MethodIcon(int color, int drawable) {
            mColor = color;
            mDrawable = drawable;
        }
    }
}
