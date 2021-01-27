package Utils;

/**
 * @author ：WSS
 * @date ：Created in 2019/9/10 10:40
 * @description：Log Contorll
 * @modified By：Jesse.Qi
 * @version: 1.1$
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LogUtils {
    private Logger logger = Logger.getAnonymousLogger();
    private Handler handlerAllLog = null;
    private Handler handlerErrorLog = null;
    private String allLogPath = null;
    private String errorLogPath = null;
    private String lastLogDateStr = null;

    private static LogUtils logUtilsInstance = null;

    public static LogUtils getInstance() {
        if (logUtilsInstance == null) {
            logUtilsInstance = new LogUtils();
        }

        return logUtilsInstance;
    }

    private LogUtils() {
        allLogPath = XmlUtils.ReadXml("logPath");
        errorLogPath = XmlUtils.ReadXml("errlogPath");
    }

    private Logger logger() {
        try {

            //初始化
            if (logger == null) {
                logger = Logger.getLogger("ArgoLog");
                logger.setLevel(Level.ALL);

                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                lastLogDateStr = df.format(new Date());
                //根据需要创建日期目录
                File file = new File(allLogPath + "\\" + lastLogDateStr);
                if (!file.exists() || !file.isDirectory()) {
                    file.mkdirs();
                }

                file = new File(errorLogPath + "\\" + lastLogDateStr);
                if (!file.exists() || !file.isDirectory()) {
                    file.mkdirs();
                }

                String logFileName = "ArgoLog-" + lastLogDateStr + "-%g.txt";

                int limit = 1024000;
                int numLogFiles = 100;
                handlerAllLog = new FileHandler(allLogPath + "\\" + lastLogDateStr + "\\" + logFileName, limit, numLogFiles, true);
                handlerAllLog.setFormatter(new SimpleFormatter());

                handlerErrorLog = new FileHandler(errorLogPath + "\\" + lastLogDateStr + "\\" + logFileName, limit, numLogFiles, true);
                handlerErrorLog.setFormatter(new SimpleFormatter());
                handlerErrorLog.setLevel(Level.SEVERE);


                logger.addHandler(handlerAllLog);
                logger.addHandler(handlerErrorLog);
            } else {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                String nowLogDateStr = df.format(new Date());

                if (!nowLogDateStr.equals(lastLogDateStr))//不同日期的日志分开
                {
                    lastLogDateStr = nowLogDateStr;

                    //根据需要创建日期目录
                    File file = new File(allLogPath + "\\" + lastLogDateStr);
                    if (!file.exists() || !file.isDirectory()) {
                        file.mkdirs();
                    }

                    file = new File(errorLogPath + "\\" + lastLogDateStr);
                    if (!file.exists() || !file.isDirectory()) {
                        file.mkdirs();
                    }

                    String logFileName = "ArgoLog-" + lastLogDateStr + "-%g.txt";

                    int limit = 1024000;
                    int numLogFiles = 100;

                    if (handlerAllLog != null) {
                        logger.removeHandler(handlerAllLog);
                    }

                    if (handlerErrorLog != null) {
                        logger.removeHandler(handlerErrorLog);
                    }

                    handlerAllLog = new FileHandler(allLogPath + "\\" + lastLogDateStr + "\\" + logFileName, limit, numLogFiles, true);
                    handlerAllLog.setFormatter(new SimpleFormatter());

                    handlerErrorLog = new FileHandler(errorLogPath + "\\" + lastLogDateStr + "\\" + logFileName, limit, numLogFiles, true);
                    handlerErrorLog.setFormatter(new SimpleFormatter());
                    handlerErrorLog.setLevel(Level.SEVERE);


                    logger.addHandler(handlerAllLog);
                    logger.addHandler(handlerErrorLog);
                }

            }


        } catch (Exception e) {

        }

        return logger;
    }

    public void logException(Exception e) {
        String stackTraceStr = "";
        for (StackTraceElement elem : e.getStackTrace()) {
            stackTraceStr += ("\tat " + elem.toString() + "\n");
        }

        logException(e.getMessage() + "\n" + stackTraceStr);
    }

    public void logException(Exception e, String userMessage) {

        String stackTraceStr = "";
        for (StackTraceElement elem : e.getStackTrace()) {
            stackTraceStr += (elem.toString() + "\n");
        }

        logException(userMessage + "\n" + e.getMessage() + "\n" + stackTraceStr);
    }

    public void logException(String message) {
        logger().severe(message);
    }

    public void logInfo(String message) {
        logger().info(message);

        // 将log信息输出至UI界面中
        if (!message.endsWith("\n")) {
            message += "\n";
        }
    }
}
