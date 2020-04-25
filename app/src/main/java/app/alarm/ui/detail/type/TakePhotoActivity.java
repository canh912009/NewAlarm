package app.alarm.ui.detail.type;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import app.alarm.R;
import app.alarm.core.presentation.BaseActivity;
import app.alarm.utils.AppConstants;

public class TakePhotoActivity extends BaseActivity
        implements View.OnClickListener, TakePhotoAdapter.OnItemListener {

    private static final String TAG = "TakePhotoActivity";

    private RecyclerView mRecyclerView;
    private LinearLayout mLayoutNoImage;

    private TakePhotoAdapter mAdapter;

    private ArrayList<String> mListPaths;
    private String mCurrentPhotoPath;
    private String mPathImageSelected = "";

    private boolean mIsCreateAlarm = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_photo);

        initView();
    }

    public void initView() {
        Intent intent = getIntent();
        mIsCreateAlarm = intent.getBooleanExtra(AppConstants.ARG_INTENT_TYPE_CREATE_ALARM, true);
        mPathImageSelected = intent.getStringExtra(AppConstants.ARG_INTENT_ALARM_TYPE_SETTING);

        initActionBar();
        initRecycleView();

        FloatingActionButton fab = findViewById(R.id.fab);
        ImageView mImageAddButton = findViewById(R.id.image_add_button);

        mImageAddButton.setOnClickListener(view -> dispatchTakePictureIntent());
        fab.setOnClickListener(view -> dispatchTakePictureIntent());
    }

    public void initRecycleView() {
        mLayoutNoImage = findViewById(R.id.layout_no_image);
        mRecyclerView = findViewById(R.id.list_image);
        mRecyclerView.setHasFixedSize(true);

        mListPaths = getListPath();
        mAdapter = new TakePhotoAdapter(this, mListPaths);
        mAdapter.setEventDelete(this);
        mRecyclerView.setAdapter(mAdapter);

        if (mListPaths.size() == 0) {
            setVisibleRightButton(View.GONE);
        }

        if (!mIsCreateAlarm) {
            int pos = mListPaths.indexOf(mPathImageSelected);
            if (pos >= 0) {
                mAdapter.setPosSelected(pos);
            } else {
                Log.d(TAG, "Seem image is deleted");
                setVisibleRightButton(View.GONE);
            }
        } else {
            setVisibleRightButton(View.GONE);
        }

    }

    public void initActionBar() {
        setTittle(getString(R.string.alarm_tittle_take_photo));
        setImageLeftButton(ContextCompat.getDrawable(this, R.drawable.ic_back));
        setButtonListener(this);
    }

    private ArrayList<String> getListPath() {
        ArrayList<String> listPath = new ArrayList<>();
        File file = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        if (file == null) {
            setVisibleLayout(0);
            return listPath;
        }

        if (file.isDirectory()) {
            File[] listFile = file.listFiles();
            setVisibleLayout(listFile.length);
            for (File aListFile : listFile) {
                listPath.add(aListFile.getAbsolutePath());
            }
        }

        return listPath;
    }

    public void setVisibleLayout(int sizePath) {
        if (sizePath == 0) {
            mLayoutNoImage.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        } else {
            mLayoutNoImage.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // TODO
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, AppConstants.AUTHORITY_FILE_PROVIDER, photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, AppConstants.REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        mCurrentPhotoPath = image.getAbsolutePath();

        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AppConstants.REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            if (mCurrentPhotoPath != null) {
                mListPaths.add(mCurrentPhotoPath);
                mRecyclerView.setVisibility(View.VISIBLE);
                mLayoutNoImage.setVisibility(View.GONE);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.alarm_ab_left_button:
                super.onBackPressed();
                break;
            case R.id.alarm_ab_right_button:
                Intent intent = new Intent(this, TakePhotoActivity.class);
                intent.putExtra(AppConstants.ARG_INTENT_ALARM_TYPE_SETTING, mPathImageSelected);
                setResult(RESULT_OK, intent);
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemDelete(int pos) {
        String path = mListPaths.get(pos);
        if (path != null) {
            File file = new File(path);
            if (file.isFile()) {
                if (file.delete()) {
                    mListPaths.remove(path);
                    if (mListPaths.size() == 0) {
                        mRecyclerView.setVisibility(View.GONE);
                        mLayoutNoImage.setVisibility(View.VISIBLE);
                    } else {
                        mRecyclerView.setVisibility(View.VISIBLE);
                        mLayoutNoImage.setVisibility(View.GONE);
                        mAdapter.notifyDataSetChanged();
                    }
                }
            }

            if (path.equals(mPathImageSelected)) {
                mPathImageSelected = "";
            }
        }

    }

    @Override
    public void onItemClick(int pos) {
        setVisibleRightButton(View.VISIBLE);
        mPathImageSelected = mListPaths.get(pos);
    }
}
