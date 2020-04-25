package app.alarm.utils;

public class AppConstants {
    // 1 second = 1000 (1,000 ms)
    public static final int SECOND_MILLIS = 1000;

    public static final int LENGTH_MEDIUM = 0;
    public static final int LENGTH_FULL = 1;

    public static final String ARG_INTENT_REPEAT_ALARM_DETAIL = "ARG_INTENT_REPEAT_ALARM_DETAIL";
    public static final String ARG_INTENT_SNOOZE_VALUE_ALARM_DETAIL = "ARG_INTENT_SNOOZE_VALUE_ALARM_DETAIL";

    public static final int REQUEST_TAKE_PHOTO = 1;
    public static final String AUTHORITY_FILE_PROVIDER = "app.alarm.fileprovider";

    public static final String ARG_INTENT_TYPE_CREATE_ALARM = "ARG_INTENT_TYPE_CREATE_ALARM";
    public static final String ARG_INTENT_TYPE_EDIT_ALARM = "ARG_INTENT_TYPE_EDIT_ALARM";

    public static final String ARG_INTENT_ALARM_TYPE_SETTING = "ARG_INTENT_ALARM_TYPE_SETTING";
    public static final String ARG_INTENT_ALARM_TYPE = "ARG_INTENT_ALARM_TYPE";

    public static final String ARG_INTENT_SONG_VALUE_ALARM_DETAIL = "ARG_INTENT_SONG_VALUE_ALARM_DETAIL";
    public static final String ARG_INTENT_TONE_VALUE_ALARM_DETAIL = "ARG_INTENT_TONE_VALUE_ALARM_DETAIL";


    // start Activity result
    public static final int REQUEST_CODE_ALARM_TYPE = 0x1;
    public static final int REQUEST_CODE_REPEAT = 0x2;
    public static final int REQUEST_CODE_TONE = 0x3;
    public static final int REQUEST_CODE_SNOOZE = 0x4;

    public static final int REQUEST_CODE_GET_SHAKE_PHONE = 0x5;
    public static final int REQUEST_CODE_GET_MATH_PRO = 0x7;
    public static final int REQUEST_CODE_GET_TAKE_PHOTO = 0x11;

    public static final int REQUEST_CREATE_ALARM = 0x8;
    public static final int REQUEST_EDIT_ALARM = 0x9;
    public static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 0x10;
}
