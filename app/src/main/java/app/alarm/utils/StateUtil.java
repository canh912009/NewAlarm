package app.alarm.utils;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

public class StateUtil {

    private static final String TAG = "StateUtils";

    // check call state (except VOIP call)
    public static boolean isInCall(Context context) {
        int callState = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE))
                .getCallState();
        Log.d(TAG, "isInCallState callState = " + callState);
        boolean isCallState = callState != TelephonyManager.CALL_STATE_IDLE;
        return isCallState;
    }
}
