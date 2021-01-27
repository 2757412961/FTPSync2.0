package Utils;

/**
 * @author ：Z
 * @date ：Created in 2021/01/22
 * @description：Main Class
 * @modified By：
 * @version: 1.1$
 */

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateUtils {

    public static String getYYYYMMDDHHMMSS() {
        DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        Calendar calendar = Calendar.getInstance();
        return df.format(calendar.getTime());
    }

    public static String getYYYYMMDD() {
        DateFormat df = new SimpleDateFormat("yyyyMMdd");
        Calendar calendar = Calendar.getInstance();
        return df.format(calendar.getTime());
    }

    public static Calendar getToday() {
        return Calendar.getInstance();
    }

    public static Calendar get24hBefore() {
        Calendar now = Calendar.getInstance();
        now.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY) - 24);

        return now;
    }

    public static Calendar getToday00() {
        Calendar now = Calendar.getInstance();
        now.set(Calendar.HOUR_OF_DAY, 0);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);

        return now;
    }

}
