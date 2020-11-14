package app.alarm.ui.detail.type;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import app.alarm.R;

public class TakePhotoAdapter extends RecyclerView.Adapter<TakePhotoAdapter.ViewHolder> {

    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<String> mData;
    private RequestOptions mOptions;

    private OnItemListener mClickListener;
    private int mPosSelected = -1;

    public void setEventDelete(OnItemListener listener) {
        mClickListener = listener;
    }

    TakePhotoAdapter(Context context, ArrayList<String> data) {
        mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;

        mOptions = new RequestOptions()
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String path = mData.get(position);
        if (path != null) {
            Glide.with(mContext).load(path).apply(mOptions).into(holder.mImage);
        }

        if (mPosSelected == position) {
            holder.mLayout.setBackground(ContextCompat.getDrawable(mContext, R.drawable.alarm_image_selected_bg));
        } else {
            holder.mLayout.setBackground(null);
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setData(ArrayList<String> data) {
        mData = data;
    }

    interface OnItemListener {
        void onItemDelete(int pos);

        void onItemClick(int pos);
    }

    public void setPosSelected(int position) {
        mPosSelected = position;

        if (mClickListener != null) {
            mClickListener.onItemClick(position);
        }

        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView mImage;
        private ImageView mImageDeleteBtn;
        private FrameLayout mLayout;

        ViewHolder(View itemView) {
            super(itemView);

            mImage = itemView.findViewById(R.id.image_add);
            mImageDeleteBtn = itemView.findViewById(R.id.delete_image);
            mLayout = itemView.findViewById(R.id.layout_image);

            mImageDeleteBtn.setOnClickListener(this);
            mImage.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int id = view.getId();
            int position = getAdapterPosition();

            if (id == R.id.delete_image) {
                if (mClickListener != null) {
                    mClickListener.onItemDelete(position);
                }

                if (mPosSelected == position) {
                    mPosSelected = -1;
                }
            } else if (id == R.id.image_add) {
                setPosSelected(position);
            }
        }
    }
}
