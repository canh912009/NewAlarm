package app.alarm.ui.alarm.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;

import app.alarm.R;
import app.alarm.core.data.db.entities.Alarm;
import app.alarm.utils.AppConstants;

public class MethodTakePhoto extends Method {

    private static final String TAG = "MethodTakePhoto";

    static {
        System.loadLibrary("opencv_java");
        System.loadLibrary("nonfree");
    }

    private Bitmap mBitmapImage;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(mContext) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    public MethodTakePhoto(Activity context, Alarm alarm) {
        super(context, alarm);
    }

    @Override
    public void initView(View rootView) {
        RequestOptions mOptions = new RequestOptions()
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL);

        LinearLayout mLinearLayout = rootView.findViewById(R.id.ln_take_a_photo);
        mLinearLayout.setVisibility(View.VISIBLE);

        TextView mDes = rootView.findViewById(R.id.text_des);
        TextView mDismissBtn = rootView.findViewById(R.id.dismiss_btn);

        ImageView mImage = rootView.findViewById(R.id.alarm_image);

        File image = new File(mPathImage);
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        mBitmapImage = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);
        if (mBitmapImage != null) {
            Glide.with(mContext).load(mPathImage).apply(mOptions).into(mImage);
            mImage.setVisibility(View.VISIBLE);
            mDes.setText(mContext.getString(R.string.take_pho_similar));
            mDismissBtn.setVisibility(View.GONE);
        } else {
            mImage.setVisibility(View.GONE);
            mDismissBtn.setVisibility(View.VISIBLE);

            mDes.setText(mContext.getString(R.string.take_pho_get_fail));
            mDismissBtn.setOnClickListener(view -> finishAlarm());
        }

        mImage.setOnClickListener(view -> dispatchTakePictureIntent());
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(mContext.getPackageManager()) != null) {
            mContext.startActivityForResult(takePictureIntent, AppConstants.REQUEST_TAKE_PHOTO);
        }
    }

    @Override
    public void create() {
        super.create();
    }

    @Override
    public void resume() {
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_10, mContext, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void pause() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");
        if (data == null) {
            return;
        }

        if (requestCode == AppConstants.REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                Bitmap imageBitmap = (Bitmap) extras.get("data");

                if (mBitmapImage != null && imageBitmap != null) {
                    mBitmapImage = Bitmap.createScaledBitmap(mBitmapImage, 100, 100, true);
                    imageBitmap = Bitmap.createScaledBitmap(imageBitmap, 100, 100, true);

                    Mat img1 = new Mat();
                    Utils.bitmapToMat(mBitmapImage, img1);

                    Mat img2 = new Mat();
                    Utils.bitmapToMat(imageBitmap, img2);

                    Imgproc.cvtColor(img1, img1, Imgproc.COLOR_RGBA2GRAY);
                    Imgproc.cvtColor(img2, img2, Imgproc.COLOR_RGBA2GRAY);

                    img1.convertTo(img1, CvType.CV_32F);
                    img2.convertTo(img2, CvType.CV_32F);

                    Mat hist1 = new Mat();
                    Mat hist2 = new Mat();

                    MatOfInt histSize = new MatOfInt(180);
                    MatOfInt channels = new MatOfInt(0);

                    ArrayList<Mat> bgr_planes1 = new ArrayList<>();
                    ArrayList<Mat> bgr_planes2 = new ArrayList<>();

                    Core.split(img1, bgr_planes1);
                    Core.split(img2, bgr_planes2);

                    MatOfFloat histRanges = new MatOfFloat(0f, 180f);
                    Imgproc.calcHist(bgr_planes1, channels, new Mat(), hist1, histSize, histRanges, false);
                    Core.normalize(hist1, hist1, 0, hist1.rows(), Core.NORM_MINMAX, -1, new Mat());

                    Imgproc.calcHist(bgr_planes2, channels, new Mat(), hist2, histSize, histRanges, false);
                    Core.normalize(hist2, hist2, 0, hist2.rows(), Core.NORM_MINMAX, -1, new Mat());

                    img1.convertTo(img1, CvType.CV_32F);
                    img2.convertTo(img2, CvType.CV_32F);

                    hist1.convertTo(hist1, CvType.CV_32F);
                    hist2.convertTo(hist2, CvType.CV_32F);

                    double compare = Imgproc.compareHist(hist1, hist2, Imgproc.CV_COMP_CHISQR);

                    Log.d(TAG, "compare: " + compare);
                    if (compare >= 0 && compare < 6000) { // 1500
                        Toast.makeText(mContext, "Images are similar", Toast.LENGTH_LONG).show();
                        finishAlarm();
                    } else
                        Toast.makeText(mContext, "Images are not similar", Toast.LENGTH_LONG).show();

                }
            }
        }
    }
}
