package Utils;

/**
 * @author ：Z
 * @date ：Created in 2021/01/22
 * @description：Main Class
 * @modified By：
 * @version: 1.1$
 */

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.*;

public class ftpDownload {
    /**
     * ftp连接
     */
    private static FTPClient ftpClient = new FTPClient();

    /**
     * ftp服务器地址
     */
    private static String hostName = XmlUtils.ReadXml("HOST");
    /**
     * ftp端口
     */
    private static int port = Integer.parseInt(XmlUtils.ReadXml("PORT"));
    /**
     * 登录名
     */
    private static String userName = XmlUtils.ReadXml("SCSLogName");
    /**
     * 登录密码
     */
    private static String password = XmlUtils.ReadXml("SCSPassWord");

    /**
     * 需要访问的远程目录
     */
    private static String remoteVisitDir = XmlUtils.ReadXml("RemoteBackupDir");
    /**
     * 需要保存的远程目录（历史）
     */
    private static String remoteHisDir = XmlUtils.ReadXml("RemoteTargetDir");
    /**
     * 需要访问的本地目录
     */
    private static String localVisitDir = XmlUtils.ReadXml("LocalBackupDir");
    /**
     * 需要保存的本地目录（历史）
     */
    private static String localTempDir = XmlUtils.ReadXml("LocalTempDir");


    /**
     * 最多重连次数
     */
    static int maxTry = 5;

    /**
     * 保持 FTP 连接
     */
    public static void touch() {
        try {
            if (!ftpClient.isConnected()) {
                connect();
            }
            ftpClient.sendNoOp();
//            LogUtils.getInstance().logInfo("Touch Successfully!");
        } catch (IOException e) {
            LogUtils.getInstance().logInfo("Can not send NoOp, keep alive failed!");
            e.printStackTrace();
        }
    }

    public static boolean isFolderEmpty() {
        if (!ftpClient.isConnected()) {
            connect();
        }

        try {
            FTPFile[] files = ftpClient.listFiles(new String((remoteVisitDir).getBytes("UTF-8"), "ISO-8859-1"));
            if (files == null || files.length == 0) {
                return true;
            }

            for (FTPFile file : files) {
                if (file.isFile()) {
                    return false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    public static ArrayList<String> getTodayFiles() {
        if (!ftpClient.isConnected()) {
            connect();
        }

        ArrayList<String> names = new ArrayList<>();
        try {
            FTPFile[] files = ftpClient.listFiles(new String((remoteVisitDir).getBytes("UTF-8"), "ISO-8859-1"));
            if (files == null || files.length == 0) {
                return names;
            }

            for (FTPFile file : files) {
                // 判断是否是今天的文件
                if (file.isFile() && file.getTimestamp().after(DateUtils.getToday00())) {
                    File localFile = new File(localTempDir + file.getName());
                    // 判断本地文件是否下载完全
                    if (localFile.length() < file.getSize()) {
                        names.add(file.getName());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 打印输出
        if (!names.isEmpty()) {
            StringBuffer sb = new StringBuffer("[Download file] \n");
            for (String name : names) {
                sb.append("\t - ").append(name).append("\n");
            }
            LogUtils.getInstance().logInfo(sb.toString());
        }

        return names;
    }

    public static ArrayList<String> downloads() {
        ArrayList<String> names = new ArrayList<>();
        final ExecutorService exec = Executors.newFixedThreadPool(1);

        //开始执行耗时操作
        Callable<String> call = new Callable<String>() {
            @Override
            public String call() throws Exception {
                if (!ftpClient.isConnected()) {
                    connect();
                }

                FTPFile[] files = ftpClient.listFiles(new String((remoteVisitDir).getBytes("UTF-8"), "ISO-8859-1"));
                for (FTPFile file : files) {
                    if (file.isFile()) {
                        file.setLink(remoteVisitDir);
                        file.setName(file.getName());

                        names.add(file.getName());
                        ExcuteDownLoad(file);
                    }
                }

                return "线程执行完成.";
            }
        };

        try {
            Future<String> future = exec.submit(call);
            String obj = future.get(10 * 60 * 1000, TimeUnit.MILLISECONDS); //任务处理超时时间设为 120 秒
        } catch (TimeoutException ex) {
            ex.printStackTrace();
            LogUtils.getInstance().logException(ex, "[ftp] 下载超时");
            closeConnections();
            ftpClient = new FTPClient();
            while (!connect()) {
                LogUtils.getInstance().logInfo("[ftp] 正在尝试重连 请等待30秒！");
                try {
                    Thread.sleep(30 * 1000);//过30 秒再重连
                } catch (InterruptedException ie) {
                }
            }
        } catch (Exception e) {
            LogUtils.getInstance().logException(e, "[ftp] 下载失败");
            closeConnections();
            ftpClient = new FTPClient();
            while (!connect()) {
                LogUtils.getInstance().logInfo("[ftp] 正在尝试重连 请等待30秒！");
                try {
                    Thread.sleep(30 * 1000);//过30 秒再重连
                } catch (InterruptedException ie) {
                }
            }
        }

        exec.shutdown(); // 关闭线程池

        return names;
    }

    public static ArrayList<String> downloads(ArrayList<String> names) {
        if (names == null || names.isEmpty()) {
            return null;
        }

        if (!ftpClient.isConnected()) {
            connect();
        }

        ArrayList<String> downloadedNames = new ArrayList<>();
        try {
            FTPFile[] files = ftpClient.listFiles(new String((remoteVisitDir).getBytes("UTF-8"), "ISO-8859-1"));
            for (FTPFile file : files) {
                if (file.isFile() && names.contains(file.getName())) {
                    file.setLink(remoteVisitDir);
                    file.setName(file.getName());

                    // remove
                    LogUtils.getInstance().logInfo("[ftp] FTP file " + file.getName() + "; Size:" + file.getSize());
                    // 判断是否下载成功，成功即可复制文件
                    if (ExcuteDownLoad(file)) {
                        downloadedNames.add(file.getName());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return downloadedNames;
    }

    public static boolean mkdir(String workPath, String dir) throws IOException {
        if (ftpClient.changeWorkingDirectory(workPath + dir)) {
            return true;
        }
        ftpClient.changeWorkingDirectory(new String((workPath).getBytes("UTF-8"), "ISO-8859-1"));

        return ftpClient.makeDirectory(dir);
    }

    public static boolean mkdirs(String dirs) throws IOException {
        if (!ftpClient.isConnected()) {
            connect();
        }
        String[] dirNames = dirs.split("/");
        StringBuffer sb = new StringBuffer().append('/');

        for (String dirName : dirNames) {
            if (!mkdir(sb.toString(), dirName)) {
                return false;
            }
            sb.append(dirName).append('/');
        }

        return true;
    }

    public static boolean moveTo(String srcDir, String tarDir, String fileName) throws IOException {
        if (srcDir.length() == 0 || tarDir.length() == 0 || fileName.length() == 0) {
            return false;
        }

        if (!mkdirs(new String((tarDir).getBytes("UTF-8"), "ISO-8859-1"))) {
            return false;
        }

        return ftpClient.rename(srcDir + fileName, tarDir + fileName);
    }

    public static void transfers() {
        if (!ftpClient.isConnected()) {
            connect();
        }

        try {
            FTPFile[] files = ftpClient.listFiles(new String((remoteVisitDir).getBytes("UTF-8"), "ISO-8859-1"));
            String dataTime = DateUtils.getYYYYMMDDHHMMSS();
            for (FTPFile file : files) {
                if (file.isFile()) {
                    boolean isSuccess = moveTo(
                            remoteVisitDir,
                            remoteHisDir + dataTime + '/',
                            file.getName());
                    if (!isSuccess) {
                        LogUtils.getInstance().logException("[ftp] 转移文件 " + remoteVisitDir + file.getName() + " 失败！");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void transfers(ArrayList<String> fileNames) {
        if (!ftpClient.isConnected()) {
            connect();
        }

        try {
            for (String name : fileNames) {
                boolean isSuccess = moveTo(
                        remoteVisitDir,
                        remoteHisDir,
                        name);

                if (isSuccess) {
                    LogUtils.getInstance().logException("[ftp] 转移文件 " + remoteVisitDir + name + " 成功！");
                } else {
                    LogUtils.getInstance().logException("[ftp] 转移文件 " + remoteVisitDir + name + " 失败！");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 连接FTP服务器，这一步骤单独拥有5次最大重连次数
     *
     * @throws SocketException
     * @throws IOException
     */
    public static boolean connect() {
        if (ftpClient.isConnected()) {
            closeConnections();
        }
        int curTry = 1;
        while (curTry <= maxTry) {
            try {
                ftpClient = new FTPClient();
                // FTP Encode
                ftpClient.setControlEncoding("GBK");

                // 设置 Socket 连接超时
                ftpClient.setConnectTimeout(30 * 1000);
                // 设置终端的传输数据的 Socket 的 Sotimeout
                ftpClient.setDataTimeout(30 * 1000);
                // 设置终端的传输控制命令的 Socket 的 SoTimeout
                // ftpClient.setDefaultTimeout(30 * 1000);
                // ftpClient.setSoTimeout(30 * 1000);
                // 设置当处于传输数据过程中，按指定的时间阈值定期让传输控制命令的 Socket 发送一个无操作命令 NOOP 给服务器，
                // 让它 keep alive。每隔 15S 向控制端口发送心跳数据，保证控制端口的活性
                ftpClient.setControlKeepAliveTimeout(15);
                // 设置控制端口的响应超时
                ftpClient.setControlKeepAliveReplyTimeout(10 * 1000);

                ftpClient.connect(hostName, port);
                if (ftpClient.login(userName, password)) {
                    ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                    // ftpClient.setRemoteVerificationEnabled(false);
                    ftpClient.enterLocalPassiveMode();
                    if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                        LogUtils.getInstance().logInfo("[ftp] Connect " + hostName + " Successed");
                        return true;

                    }
                }
            } catch (SocketException e) {
                e.printStackTrace();
                LogUtils.getInstance().logException(e, "[ftp]");
            } catch (IOException e) {
                e.printStackTrace();
                LogUtils.getInstance().logException(e, "[ftp]");
            }
            closeConnections();
            // LogUtils.getInstance().logInfo("[ftp] Connect " + hostName + " Failed");
            LogUtils.getInstance().logInfo("[ftp] Connect      " + hostName + " Failed");
            LogUtils.getInstance().logException("[ftp] Connect      " + hostName + " Failed");
            curTry++;
            if (curTry <= maxTry) {
                // LogUtils.getInstance().logInfo("[ftp] Try reconnect " + hostName + " —— "
                // + curTry + "times");
                LogUtils.getInstance().logInfo("[ftp] Try reconnect " + hostName + " —— " + curTry + "times");
            }
        }
        return false;
    }

    /**
     * 断开与远程服务器的连接
     *
     * @throws IOException
     */
    public static void closeConnections() {
        try {
            if (ftpClient.isConnected()) {
                ftpClient.disconnect();
            }
        } catch (IOException e) {
            // LogUtils.getInstance().logInfo("[ftp] DisConnect Failed:" + e.getMessage());
            LogUtils.getInstance().logException("[ftp] DisConnect Failed:" + e.getMessage());
        }
    }

    /**
     * 从FTP服务器上下载文件
     *
     * @param file 特定文件名
     * @throws IOException
     */
    public static boolean ExcuteDownLoad(FTPFile file) throws IOException {
        boolean res = false;
        String filename = "";
        String ftpFilePath = "";
        try {
            if (!ftpClient.isConnected()) {
                connect();
            }
            filename = file.getName();// 对象文件名
            ftpFilePath = file.getLink();// 对象在ftp中的路径
//            DBInteraction(filename, "downloading");
            String FullURL = ftpFilePath + filename;// 完整下载路径URL // remoteDir 改为了 ftpFilePath
            long lRemoteSize = file.getSize();// 对象文件大小

            // 如果文件夹不存在则自动创建文件夹
            File localFile = new File(localTempDir + filename.replace('/', '\\'));// 创建本地文件类
            File fileParent = localFile.getParentFile();
            if (!fileParent.exists()) {
                fileParent.mkdirs();
            }

            int curTry = 1;// 尝试次数

            if (!localFile.exists()) {
                if (DataDownLoad(filename, localFile, FullURL, 0L, lRemoteSize)) {
                    return true;
                }
            }
            long localSize = localFile.length();
            // 判断本地文件大小是否大于远程文件大小
            if (localSize >= lRemoteSize) {
                // LogUtils.getInstance().logInfo("[ftp] Local file:" + filename + " is
                // already exist");
                LogUtils.getInstance().logInfo("[ftp] Local file:" + filename + " is already exist");
                return true;
            } // 断点续传或零点重下
            while (curTry <= maxTry) {
                if (DataDownLoad(filename, localFile, FullURL, localSize, lRemoteSize)) {
                    return true;
                } else {
                    connect();
                }
                curTry++;
            }
        } catch (Exception e) {
            LogUtils.getInstance().logInfo("[ftp] Download " + filename + " Failed");
            // e.printStackTrace();
            LogUtils.getInstance().logException(e, "[ftp]");
            return false;
        }
        LogUtils.getInstance().logInfo("[ftp] Download " + filename + " Failed");
        LogUtils.getInstance().logException("[ftp] Download " + filename + " Failed");
        return res;
    }

    /**
     * 具体下载步骤,支持断点续传，上传百分比汇报
     *
     * @throws IOException
     */
    public static boolean DataDownLoad(String filename, File localFile, String FullURL, long localSize, long lRemoteSize) throws IOException {
        boolean res = false;
        try {
            if (!ftpClient.isConnected()) {
                connect();
            }

            if (localSize > lRemoteSize) {
                LogUtils.getInstance().logInfo("[ftp] Download " + filename + " Size too big Error");
                LogUtils.getInstance().logException("[ftp] Download " + filename + " Size too big Error");
                return false;
            }

            if (localSize == 0) {
                // 正常下载
                LogUtils.getInstance().logInfo("开始文件" + filename + "的下载"); // 中文乱码
//                LogUtils.getInstance().logInfo("Start Download File:" + filename + " ");
                OutputStream out = new FileOutputStream(localFile);
                // InputStream in = ftpClient.retrieveFileStream(new String(FullURL.getBytes("GBK"), "ISO-8859-1"));
                InputStream in = ftpClient.retrieveFileStream(FullURL);
                if (in == null) {
                    LogUtils.getInstance().logInfo("[ftp] Can't get" + filename + " Data from target URL");
                    out.close();
                    return false;
                } else {
                    byte[] bytes = new byte[1024];
                    double step = lRemoteSize / 100;
                    long process = 0;
                    long localSize0 = 0L;
                    int c;
                    while ((c = in.read(bytes)) != -1) {
                        out.write(bytes, 0, c);
                        localSize0 += c;
                        long nowProcess = (long) (localSize0 / step);
                        if (nowProcess > process) {
                            process = nowProcess;
                            if (process % 5 == 0) {
                                LogUtils.getInstance().logInfo("[ftp] Download process :" + process + "%");
                            }
                        }
                    }
                    if (localFile.length() == lRemoteSize) {
                        LogUtils.getInstance().logInfo("[ftp] Download " + filename + " Successed");
                        res = true;
                    } else {
                        LogUtils.getInstance().logInfo("[ftp] Download " + filename + " Error1");
                        LogUtils.getInstance().logException("[ftp] Download " + filename + " Error1");
                        res = false;
                    }
                    LogUtils.getInstance().logInfo("[ftp] Start in.close!"); // keshanchu1
                    in.close();
                    LogUtils.getInstance().logInfo("[ftp] End in.close start out.close!"); // keshanchu1
                    out.close();
                    LogUtils.getInstance().logInfo("[ftp] End out.close!"); // keshanchu1
                    // 这是一个同步阻塞方法，如果调用错误，会导致程序卡住假死在这里。
                    ftpClient.completePendingCommand();
                    LogUtils.getInstance().logInfo("[ftp] Download completePendingCommand!"); // keshanchu1
                }
            } else {
                // 断点续传
                LogUtils.getInstance().logInfo("检测到未下载完的文件" + filename + "，正在开始断点续传");
                FileOutputStream out = new FileOutputStream(localFile, true);
                ftpClient.enterLocalPassiveMode();
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                // int a = ftpClient.sendCommand("REST 1");
                ftpClient.setRestartOffset(localSize);
                // InputStream in = ftpClient.retrieveFileStream(new String(FullURL.getBytes("GBK"), "ISO-8859-1"));
                InputStream in = ftpClient.retrieveFileStream(FullURL);
                if (in == null) {
                    LogUtils.getInstance().logInfo("[ftp] Can't get" + filename + " Data from target URL");
                    out.close();
                    return false;
                } else {
                    byte[] bytes = new byte[1024];
                    double step = lRemoteSize / 100;
                    long process = (long) (localSize / step);
                    int c;
                    while ((c = in.read(bytes)) != -1) {
                        out.write(bytes, 0, c);
                        localSize += c;
                        long nowProcess = (long) (localSize / step);
                        if (nowProcess > process) {
                            process = nowProcess;
//                            if (process % 20 == 0) {
                            if (process % 5 == 0) {
                                LogUtils.getInstance().logInfo("[ftp] Size :local/remote: " + localSize + "/" + lRemoteSize);
                                LogUtils.getInstance().logInfo("[ftp] Download process :" + process + "%");
                            }
                        }
                    }
                    if (localFile.length() == lRemoteSize) {
                        LogUtils.getInstance().logInfo("[ftp] Download " + filename + " Successed");
                        res = true;
                    } else {
                        LogUtils.getInstance().logInfo("[ftp] Download " + filename + " Error2");
                        LogUtils.getInstance().logException("[ftp] Download " + filename + " Error2");
                        res = false;
                    }
                    LogUtils.getInstance().logInfo("[ftp] Start in.close!"); // keshanchu1
                    in.close();
                    LogUtils.getInstance().logInfo("[ftp] End in.close start out.close!"); // keshanchu1
                    out.close();
                    LogUtils.getInstance().logInfo("[ftp] End out.close!"); // keshanchu1
                    // 这是一个同步阻塞方法，如果调用错误，会导致程序卡住假死在这里。
                    ftpClient.completePendingCommand();
                    LogUtils.getInstance().logInfo("[ftp] Download completePendingCommand!"); // keshanchu1
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // System.err.println("[ftp] " + e);
            LogUtils.getInstance().logException(e, "[ftp]");
        } finally {
            LogUtils.getInstance().logInfo("[ftp] Enter 'DataDownLoad' finally!");
        }

        return res;
    }

}
