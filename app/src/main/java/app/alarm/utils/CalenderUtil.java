package app.alarm.utils;

import android.text.format.DateFormat;

public class CalenderUtil {

    private static final String TAG = "CalenderUtil";

    private static final int SECOND = 1000;
    private static final int MINUTE = 60 * SECOND;
    private static final int HOUR = 60 * MINUTE;
    private static final int DAY = 24 * HOUR;


    public static final String convertMilisecondssToDetailTime(long ms) {
        return (DateFormat.format("HH:mm:ss || dd/MM/yyyy", ms).toString());
    }
}
