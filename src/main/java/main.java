/**
 * @author ：Z
 * @date ：Created in 2021/01/22
 * @description：Main Class
 * @modified By：
 * @version: 1.1$
 */

import Utils.*;

import java.io.File;
import java.io.IOException;
import java.util.*;


public class main {

    public static void runKeepConnect() {
        // 因为 FTPClient 是单例的，所以需要时刻保持其连接状态
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        ftpDownload.touch();
                        LogUtils.getInstance().logInfo("[Touch] Touch - Sleep 5 min Stared");
                        Thread.sleep(5 * 60 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public static void runTask() throws InterruptedException {
        // 1. 5 min 轮询 FTP 服务器。如果有文件传送过来，一小时后开始下载
        ArrayList<String> preDownloadNames = ftpDownload.getTodayFiles();
        if (preDownloadNames.isEmpty()) {
            LogUtils.getInstance().logInfo("[runTask] There isn't new file for downloading!");
            return;
        }
//        LogUtils.getInstance().logInfo("[runTask] Thread Sleep for 0.5 hour, then download!");
//        Thread.sleep(30 * 60 * 1000);

        // 2. 下载文件至临时目录
        ArrayList<String> downloadNames = ftpDownload.downloads(preDownloadNames);

        // 3. 拷贝临时目录本地文件
        FileSystem.getInstance().transfers(downloadNames);

        // 4. 转移 FTP 文件
        // ftpDownload.transfers(fileNames);
    }

    public static void runTimeTask() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        runTask();
                        LogUtils.getInstance().logInfo("[runTimeTask] Polling the Server for 5 min!");
//                        Thread.sleep(5 * 60 * 1000);
                        Thread.sleep(5  * 1000);
                    } catch (InterruptedException e) {
                        LogUtils.getInstance().logInfo("[runTimeTask] RunTimeTask Failed!");
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public static void main(String[] args) throws InterruptedException {
//        runKeepConnect();
        runTimeTask();
//        runTask();
    }
}