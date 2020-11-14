package app.alarm.ui.detail.tone;

import android.database.Cursor;
import android.media.RingtoneManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import app.alarm.R;
import app.alarm.core.presentation.RadioGroupAdapter;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class RingTonesFragment extends AbsFragment {
    private RadioGroupAdapter mAdapter;
    private Cursor mCursor;

    public Observable<Pair<ArrayList<String>, ArrayList<String>>> getRingTones() {
        return Observable.fromCallable(() -> {
            Pair<ArrayList<String>, ArrayList<String>> mLists = new Pair<>(new ArrayList<>(), new ArrayList<>());
            ArrayList<String> mTitleLists = new ArrayList<>();
            ArrayList<String> mUriLists = new ArrayList<>();

            RingtoneManager manager = new RingtoneManager(mActivity);
            manager.setType(RingtoneManager.TYPE_ALARM);
            mCursor = manager.getCursor();

            if (mCursor != null) {
                try {
                    while (mCursor.moveToNext()) {
                        String notificationTitle = mCursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
                        String uri = mCursor.getString(RingtoneManager.URI_COLUMN_INDEX) + "/" + mCursor.getString(RingtoneManager.ID_COLUMN_INDEX);
                        mTitleLists.add(notificationTitle);
                        mUriLists.add(uri);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (mLists.first != null && mLists.second != null) {
                mLists.first.addAll(mTitleLists);
                mLists.second.addAll(mUriLists);
            }

            return mLists;
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mActivity instanceof AlarmToneActivity) {
            ((AlarmToneActivity) mActivity).setRingTonsTabListener(this);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ring_tones, container, false);

        mCompositeDisposable.add(getRingTones().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    ArrayList<String> mTitleLists = result.first;
                    ArrayList<String> mUriLists = result.second;

                    if (mTitleLists != null && mUriLists != null) {
                        ListView mListView = view.findViewById(R.id.list_ringtone);
                        mAdapter = new RadioGroupAdapter(mActivity, android.R.layout.simple_list_item_1, mTitleLists);

                        mListView.setAdapter(mAdapter);
                        mAdapter.setPosItemSelected(getPositionInit(((AlarmToneActivity) mActivity).getUriPositionInit(), mUriLists));

                        mListView.setOnItemClickListener((parent, view1, position, id) -> {
                            mAdapter.setPosItemSelected(position);
                            mUriSelected = mUriLists.get(position);
                            mSongSelected = mTitleLists.get(position);
                            playRingTone();
                            EventBus.getDefault().post(getItemSelected());
                        });
                    }
                })
        );

        return view;
    }


    private String getItemSelected() {
        return ("0" + "|" + mUriSelected + "|" + mSongSelected);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
            mCursor = null;
        }
    }

    @Override
    public void onTabSelected() {
        super.onTabSelected();
        if (mAdapter != null) {
            mAdapter.setPosItemSelected(-1);
        }
    }

    @Override
    public void onTabUnselected() {
        super.onTabUnselected();
    }

}
