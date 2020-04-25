package app.alarm.utils;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Typeface;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;

import app.alarm.R;

public class Utils {

    public static final String SET_ON_TIME_PERFORMANCE = "SET_ON_TIME_PERFORMANCE";
    public static final int NUMBER_DAY_OF_WEEK = 7;

    public static class Song {
        public String mTab;
        public String mSongName;
        public String mUri;

        public Song(String tab, String uri, String name) {
            mTab = tab;
            mSongName = name;
            mUri = uri;
        }
    }

    public static Song getSong(String item) {
        if (item != null) {
            String[] separated = item.split("\\|");
            if (separated.length == 3) {
                return new Song(separated[0], separated[1], separated[2]);
            }
        }
        return null;
    }

    public static int convertAlarmTimeToMs(int alarmTime) {
        int hour = alarmTime / 100;
        int minute = alarmTime % 100;
        return (hour * 60 + minute) * 60;
    }

    public static String getTopActivity(Context mContext) {
        ActivityManager activityManager = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> info;
        info = activityManager.getRunningTasks(1);
        return info != null && info.size() > 0 && info.get(0) != null
                ? info.get(0).topActivity.getClassName() : "";
    }

    public static int getAmPmHour(int h) {
        int mul = 1;

        if (h < 12) {
            mul = -1;
        }

        h %= 12;

        if (h == 0) {
            h = 12;
        }

        return mul * h;
    }


    public static Typeface getFontFromOpenTheme(Context context) {
        Typeface font = null;
        String fontPath = Settings.System.getString(context.getContentResolver(),
                "theme_font_clock");

        try {
            if (fontPath != null && !TextUtils.isEmpty(fontPath)) {
                font = Typeface.createFromFile(fontPath);
            }
        } catch (Exception e) {
            Log.e("Exception", "Exception : " + e);
        }

        return font;
    }

    public static String getDayOfWeekString(Context context, int pos, int length) {
        String dayString = null;

        if (length == AppConstants.LENGTH_MEDIUM) {
            switch (pos) {
                case 0:
                    dayString = context.getResources().getString(R.string.alarm_mon);
                    break;
                case 1:
                    dayString = context.getResources().getString(R.string.alarm_tue);
                    break;
                case 2:
                    dayString = context.getResources().getString(R.string.alarm_wed);
                    break;
                case 3:
                    dayString = context.getResources().getString(R.string.alarm_thu);
                    break;
                case 4:
                    dayString = context.getResources().getString(R.string.alarm_fri);
                    break;
                case 5:
                    dayString = context.getResources().getString(R.string.alarm_sat);
                    break;
                case 6:
                    dayString = context.getResources().getString(R.string.alarm_sun);
                    break;
            }
        } else if (length == AppConstants.LENGTH_FULL) {
            switch (pos) {
                case 0:
                    dayString = context.getResources().getString(R.string.alarm_mon_full);
                    break;
                case 1:
                    dayString = context.getResources().getString(R.string.alarm_tue_full);
                    break;
                case 2:
                    dayString = context.getResources().getString(R.string.alarm_wed_full);
                    break;
                case 3:
                    dayString = context.getResources().getString(R.string.alarm_thu_full);
                    break;
                case 4:
                    dayString = context.getResources().getString(R.string.alarm_fri_full);
                    break;
                case 5:
                    dayString = context.getResources().getString(R.string.alarm_sat_full);
                    break;
                case 6:
                    dayString = context.getResources().getString(R.string.alarm_sun_full);
                    break;
            }
        }

        return dayString;
    }

    public static String getRepeatText(Context context, int repeatType) {
        String textRepeat;

        switch (repeatType) {
            case 0:
                // Need define with case not repeat
                textRepeat = "None";
                break;
            case 127:
                textRepeat = context.getResources().getString(R.string.alarm_repeat_every_day);
                break;
            default:
                StringBuilder repeatBuilder = new StringBuilder();

                for (int i = 0; i < NUMBER_DAY_OF_WEEK; i++) {
                    if (isSet(i, repeatType)) {
                        repeatBuilder.append(Utils.getDayOfWeekString(context, i, AppConstants.LENGTH_MEDIUM));
                        repeatBuilder.append("  ");
                    }
                }

                textRepeat = repeatBuilder.toString();
                break;

        }

        return textRepeat;
    }

    public static boolean isSet(int day, int repeatType) {
        return (repeatType & 1 << day) > 0;
    }

    public static String getRepeatText(Context context, int[] listRepeat) {
        StringBuilder repeatBuilder = new StringBuilder(" ");
        for (int i = 0; i < listRepeat.length; ++i) {
            if (listRepeat[i] == 1) {
                repeatBuilder.append(Utils.getDayOfWeekString(context, i, AppConstants.LENGTH_MEDIUM)).append(", ");
            }
        }

        if (repeatBuilder.length() >= 2) {
            repeatBuilder = new StringBuilder(repeatBuilder.substring(0, repeatBuilder.length() - 2));
        }

        String repeat = repeatBuilder.toString();
        if (repeat.equals(" ")) {
            repeat = context.getResources().getString(R.string.repeat_none);
        } else if (repeat.split(",").length == 7) {
            repeat = context.getResources().getString(R.string.alarm_repeat_every_day);
        }

        return repeat;
    }

    public static long getOnlyValueDate(int hour, int minutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minutes);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis();
    }

    private static String decToBinary(int dec) {
        return Integer.toBinaryString(dec);
    }

    public static int[] getListRepeat(int repeatType) {
        int[] list = {0, 0, 0, 0, 0, 0, 0};
        String binString = decToBinary(repeatType);
        StringBuilder s = new StringBuilder(binString);
        char c[] = s.reverse().toString().toCharArray();

        for (int i = 0; i < c.length; i++) {
            list[i] = Integer.parseInt(String.valueOf(c[i]));
        }

        return list;
    }

    public static String getAmPm(Calendar calendar) {
        if (calendar.get(Calendar.AM_PM) == 0) {
            return "AM";
        } else {
            return "PM";
        }
    }

    public static boolean isMode24Hour(Context context) {
        return DateFormat.is24HourFormat(context);
    }

    public static void setTimeToFormatTime(Context context, TextView textView, long time) {
        String str;

        String AM = context.getResources().getString(R.string.time_am);
        String PM = context.getResources().getString(R.string.time_pm);

        int hour = (int) time / 100;
        int minute = (int) time % 100;

        if (isMode24Hour(context)) {
            str = formatTwoDigitTime(hour) + ":" + formatTwoDigitTime(minute);
            textView.setText(str);
        } else {
            int pos;
            int length;

            if (hour >= 12) {
                if (hour == 12) {
                    str = String.format("%s:%s %s", formatTwoDigitTime(12), formatTwoDigitTime(minute), PM);
                } else {
                    str = String.format("%s:%s %s", formatTwoDigitTime(hour - 12), formatTwoDigitTime(minute), PM);
                }

                pos = str.indexOf(PM);
                length = PM.length();
            } else {
                if (hour == 0) {
                    hour = 12;
                }
                str = String.format("%s:%s %s", formatTwoDigitTime(hour), formatTwoDigitTime(minute), AM);

                pos = str.indexOf(AM);
                length = AM.length();
            }

            SpannableString spanText = new SpannableString(str);
            if (pos > 0) {
                spanText.setSpan(new AbsoluteSizeSpan((int) context.getResources().getDimension(R.dimen.alarm_item_am_pm_text_size)),
                        pos, pos + length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            textView.setText(spanText);
        }
    }

    @SuppressLint("DefaultLocale")
    public static String formatTwoDigitTime(int time) {
        // format: 1 -> 01; 10 -> 10
        return String.format("%02d", time);
    }

    public static boolean isCheckedAll(int[] list) {
        for (int aList : list) {
            if (aList == 0) {
                return false;
            }
        }

        return true;
    }
}
