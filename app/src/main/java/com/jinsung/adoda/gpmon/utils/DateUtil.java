package com.jinsung.adoda.gpmon.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by 최진성 on 2015-12-27.
 */
public class DateUtil {

    public static boolean isValidDateStr(String dateStr) {
        boolean isValid = false;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = sdf.parse(dateStr);

            if (sdf.format(date).equals(dateStr))
                isValid = true;
        }
        catch (ParseException e) {
            e.printStackTrace();
            isValid = false;
        }

        return isValid;
    }

    public static String getToday() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new Date());
    }

    public static String[] getBeforeDatesStr(String date, int count) {
        String dates[] = new String[count];
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date lastDate = sdf.parse("2015-12-24");

            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(lastDate);
            cal.add(Calendar.DATE, -count);

            for (int i = 0; i < count; i++) {
                cal.add(Calendar.DATE, 1);
                dates[i] = sdf.format(cal.getTime());
            }
        }
        catch (ParseException e) {
            e.printStackTrace();
        }

        return dates;
    }
}
