package app.alarm.ui.detail.tone;


import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import app.alarm.R;
import app.alarm.core.presentation.RadioGroupAdapter;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class SongsFragment extends AbsFragment {
    private LinearLayout mNoMusicLayout;

    private RadioGroupAdapter mAdapter;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 999;

    private Cursor mCursor;

    public Observable<Pair<ArrayList<String>, ArrayList<String>>> getSongs() {
        return Observable.fromCallable(() -> {
            Pair<ArrayList<String>, ArrayList<String>> mLists = new Pair<>(new ArrayList<>(), new ArrayList<>());
            ArrayList<String> mTitleLists = new ArrayList<>();
            ArrayList<String> mUriLists = new ArrayList<>();

            ContentResolver cr = mActivity.getContentResolver();

            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
            String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
            mCursor = cr.query(uri, null, selection, null, sortOrder);

            if (mCursor != null) {
                try {
                    int count = mCursor.getCount();
                    if (count > 0) {
                        while (mCursor.moveToNext()) {
                            String songTitle = mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                            Uri songUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mCursor.getInt(mCursor.getColumnIndex(MediaStore.Audio.Media._ID)));
                            mUriLists.add(songUri.toString());
                            mTitleLists.add(songTitle);
                        }
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

    private String getItemSelected() {
        return "1" + "|" + mUriSelected + "|" + mSongSelected;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mActivity instanceof AlarmToneActivity) {
            ((AlarmToneActivity) mActivity).setSongTabListener(this);
        }
    }

    public SongsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_songs, container, false);

        mNoMusicLayout = view.findViewById(R.id.layout_no_music);
        setVisibilityNoMusic(0); // default

        int permissionCheck = ContextCompat.checkSelfPermission(mActivity, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        } else {
            initView(view);
        }

        return view;
    }

    private void initView(View view) {
        ListView mListView = view.findViewById(R.id.list_songs);

        mCompositeDisposable.add(getSongs().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    ArrayList<String> mTitleLists = result.first;
                    ArrayList<String> mUriLists = result.second;

                    if (mTitleLists != null && mUriLists != null) {
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

                        setVisibilityNoMusic(mTitleLists.size());
                    }
                })
        );
    }

    private void setVisibilityNoMusic(int size) {
        if (size == 0) {
            if (!(mNoMusicLayout.getVisibility() == View.VISIBLE)) {
                mNoMusicLayout.setVisibility(View.VISIBLE);
            }
        } else {
            if (!(mNoMusicLayout.getVisibility() == View.GONE)) {
                mNoMusicLayout.setVisibility(View.GONE);
            }
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
