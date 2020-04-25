package app.alarm.utils;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.util.Log;

import app.alarm.core.data.db.entities.Alarm;

public class AlarmPlayer {

    private static final String TAG = "AlarmPlayer";

    private MediaPlayer mSoundPlayer;

    private Uri mSoundUri;

    private Uri mDefaultSoundUri;

    private static Object sVibratorLock = new Object();

    private static Object sSoundPlayerLock = new Object();

    private AudioManager mAudioManager;
    private static Vibrator mAlarmVibrator;

    private final Context mContext;

    public AlarmPlayer(Context context) {
        mContext = context;

        mDefaultSoundUri = RingtoneManager.getActualDefaultRingtoneUri(mContext,
                RingtoneManager.TYPE_ALARM);

        if (mDefaultSoundUri == null) {
            Log.e(TAG, "mDefaultSoundUri == null");
            mDefaultSoundUri = Uri.parse(Alarm.ALARM_DEFAULT_RINGTONE_URI);
        }

        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
    }

    public void setPlayResource(String uri) {
        if (uri != null) {
            String UriString = Uri.decode(uri);

            mSoundUri = Uri.parse(UriString);

            if (UriString.contains("content://media/")) { // AlarmSound: ringtone
                mSoundUri = Uri.parse(UriString);
            }

            if (mSoundUri == null) {
                mSoundUri = mDefaultSoundUri;
            }
        } else {
            mSoundUri = mDefaultSoundUri;
        }
    }

    private MediaPlayer getAlarmSoundPlayer() {
        Log.d(TAG, "getAlarmVibrator");

        if (mSoundPlayer == null) {
            synchronized (sSoundPlayerLock) {
                mSoundPlayer = new MediaPlayer();
            }
        } else {
            Log.d(TAG, "mSoundPlayer instance already exist");
        }

        return mSoundPlayer;
    }

    private Vibrator getAlarmVibrator() {
        Log.d(TAG, "getAlarmVibrator");

        if (mAlarmVibrator == null) {
            synchronized (sVibratorLock) {
                mAlarmVibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
            }
        } else {
            Log.d(TAG, "mAlarmVibrator instance already exist");
        }

        return mAlarmVibrator;
    }

    public void reset() {
        MediaPlayer mediaPlayer = getAlarmSoundPlayer();
        try {
            mediaPlayer.reset();
        } catch (Exception e) {
            Log.d(TAG, "playMusicChooseRing Exception");

            mediaPlayer.release();
            mSoundPlayer = null;
            mSoundUri = mDefaultSoundUri;
        }
    }

    public void stop() {
        stopMusic();
        stopVibrate();
    }

    public void playChooseRingTon() {
        playMusicChooseRing();
    }

    public void playService() {
        playMusicService();
        playVibration();
    }

    private void stopMusic() {
        Log.d(TAG, "stopMusic");
        if (mSoundPlayer != null) {
            try {
                Log.d(TAG, "stopMusic: mSoundPlayer.stop");
                mSoundPlayer.setVolume(0.0f, 0.0f);
                mSoundPlayer.pause();
                mSoundPlayer.stop();
                mSoundPlayer.reset();
                mSoundPlayer.release();

            } catch (Exception e) {
                Log.e(TAG, "stopSoundOnly Exception");
            }
            mSoundPlayer = null;
        }
    }

    public void stopVibrate() {
        Log.d(TAG, "stopVibrate");

        if (mAlarmVibrator != null) {
            mAlarmVibrator.cancel();
        }
        mAlarmVibrator = null;
    }

    private void playMusicService() {
        if (mSoundPlayer == null) {
            mSoundPlayer = new MediaPlayer();

            try {
                Log.d(TAG, "playMusicService  mSoundUri = " + mSoundUri);
                mSoundPlayer.setDataSource(mContext, mSoundUri);
                mSoundPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mSoundPlayer.prepare();
                mSoundPlayer.setLooping(true);
                mSoundPlayer.setVolume(1.0f, 1.0f);

                int result = mAudioManager.requestAudioFocus(mAudioFocusListener,
                        AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);

                if (result == 1) {
                    mSoundPlayer.start();
                    // For user binary debugging
                    Log.d(TAG, "MediaPlayer play : start");
                }
            } catch (Exception e) {
                Log.d(TAG, "playMusicService Exception + e = " + e.toString());

                mSoundPlayer.release();
                mSoundPlayer = null;
                mSoundUri = mDefaultSoundUri;
            }
        }
    }

    private void playMusicChooseRing() {
        MediaPlayer mediaPlayer = getAlarmSoundPlayer();
        try {
            Log.d(TAG, "playMusicChooseRing  mSoundUri = " + mSoundUri);
            mediaPlayer.setDataSource(mContext, mSoundUri);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.prepare();
            mediaPlayer.setLooping(true);
            mediaPlayer.setVolume(1.0f, 1.0f);
            mediaPlayer.start();
        } catch (Exception e) {
            Log.d(TAG, "playMusicChooseRing Exception");

            mediaPlayer.release();
            mSoundPlayer = null;
            mSoundUri = mDefaultSoundUri;
        }
    }

    private void playVibration() {
        Log.d(TAG, "playVibration");
        final AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM).build();
        long[] pattern = {0, 100, 1000, 300, 200, 100, 500, 200, 100};
        getAlarmVibrator().vibrate(pattern, 0, attributes);

        Log.d(TAG, "playVibration " + mAlarmVibrator.toString());
    }

    private final AudioManager.OnAudioFocusChangeListener mAudioFocusListener = new AudioManager.OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            Log.d(TAG, "onAudioFocusChange - focusChange : " + focusChange);

            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS: // -1
                    // Used to indicate a loss of audio focus of unknown
                    // duration.
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT: // -2
                    // Used to indicate a transient loss of audio focus.
                    // [KOR] when Alarm Alerting, phone state into RINGING -
                    // Alarm Ringtone volume become mute.
                    Log.d(TAG, "case FOCUS_LOSS(-1) or FOCUS_LOSS_TRANSIENT(-2)");

                    /*saveAlarmVolume();
                    stopSoundOnly();*/
                    break;

                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK: // -3
                    // Used to indicate a transient loss of audio focus
                    // where the loser of the audio focus can lower its output
                    // volume
                    // if it wants to continue playing (also referred to as
                    // "ducking"),
                    // as the new focus owner doesn't require others to be
                    // silent.
                    Log.d(TAG, "case FOCUS_LOSS_TRANSIENT_CAN_DUCK(-3)");
                    /*saveAlarmVolume();*/
                    break;

                case AudioManager.AUDIOFOCUS_GAIN: // 1

                default:
                    break;
            }
        }
    };
}
