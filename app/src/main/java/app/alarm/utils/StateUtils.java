package app.alarm.utils;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

public class StateUtils {

    private static final String TAG = "StateUtils";

    public static boolean needToShowAsFullScreen(Context context) {
        if (!isScreenOn(context)) {
            return true;
        } else if (isKeyguardLocked(context)) {
            return true;
        }

        return false;
    }

    public static boolean isKeyguardLocked(Context context) {
        KeyguardManager localKeyguardManager = (KeyguardManager) context
                .getSystemService(Context.KEYGUARD_SERVICE);

        if ((localKeyguardManager != null) && (localKeyguardManager.isKeyguardLocked())) {
            Log.d(TAG, "isKeyguardLocked");
            return true;
        }

        return false;
    }

    public static boolean isScreenOn(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return pm.isInteractive();
    }
}
