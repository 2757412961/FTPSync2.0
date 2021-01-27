package Utils;

/**
 * @author ：Z
 * @date ：Created in 2021/01/22
 * @description：Main Class
 * @modified By：
 * @version: 1.1$
 */

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class FileSystem {
    /**
     * 单例模式
     */
    private static FileSystem fs = null;
    /**
     * 需要访问的本地目录
     */
    private static String localVisitDir = XmlUtils.ReadXml("LocalBackupDir");
    /**
     * 需要保存的本地目录（历史）
     */
    private static String localTempDir = XmlUtils.ReadXml("LocalTempDir");


    private FileSystem() {

    }

    public static FileSystem getInstance() {
        if (fs == null) {
            synchronized (FileSystem.class) {
                if (fs == null) {
                    return new FileSystem();
                }
            }
        }

        return fs;
    }

    public boolean isFileExist(File file) {
        if (file.isFile() && file.exists()) {
            return true;
        }

        return false;
    }

    public boolean isFileExist(String filePath) {
        return isFileExist(new File(filePath));
    }

    public boolean delete(File file) {
        if (!isFileExist(file)) {
            return false;
        }

        return file.delete();
    }

    public boolean delete(String filePath) {
        return delete(new File(filePath));
    }

    public boolean mkdirs(File directory) {
        return directory.mkdirs();
    }

    public boolean mkdirs(String dirPath) {
        return mkdirs(new File(dirPath));
    }

    public boolean moveTo(File srcFile, File tarDir) {
        if (!isFileExist(srcFile)) {
            return false;
        }
        mkdirs(tarDir);
        File tarFile = new File(tarDir.getAbsolutePath() + File.separator + srcFile.getName());
        if (isFileExist(tarFile)) {
            delete(tarFile);
        }

        return srcFile.renameTo(tarFile);
    }

    public boolean moveTo(String srcPath, String tarDir) {
        return moveTo(new File(srcPath), new File(tarDir));
    }

    // 复制文件至文件
    public boolean copy(File srcFile, File destFile) {
        int byteread = 0;
        InputStream in = null;
        OutputStream out = null;

        try {
            in = new FileInputStream(srcFile);
            out = new FileOutputStream(destFile);
            byte[] buffer = new byte[1024];

            while ((byteread = in.read(buffer)) != -1) {
                out.write(buffer, 0, byteread);
            }
            return true;
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            return false;
        } finally {
            try {
                if (out != null)
                    out.close();
                if (in != null)
                    in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean copy(String srcPath, String destPath) {
        return copy(new File(srcPath), new File(destPath));
    }

    public boolean overWrite(File srcFile, File tarDir) {
        if (!isFileExist(srcFile)) {
            LogUtils.getInstance().logInfo("[local] File not exits!");
            return false;
        }

        mkdirs(tarDir);
        File tarFile = new File(tarDir.getAbsolutePath() + File.separator + srcFile.getName());

        if (tarFile.length() >= srcFile.length()) {
            LogUtils.getInstance().logInfo("[local] File " + srcFile.getAbsolutePath() + " already exits!");
            return false;
        }
        // 数据覆盖，需要先删除原有数据
        delete(tarFile);

        return copy(srcFile, tarFile);
    }

    public boolean overWrite(String srcPath, String tarDir) {
        return overWrite(new File(srcPath), new File(tarDir));
    }

    public File[] listFile(File dir) {
        if (!dir.exists()) {
            return null;
        }

        return dir.listFiles();
    }

    public File[] listFile(String dir) {
        return listFile(new File(dir));
    }

    public boolean transfers() {
        File[] files = listFile(localTempDir);
        if (files == null) {
            return false;
        }

        for (File file : files) {
            if (file.isFile()) {
                boolean b = moveTo(file.getAbsolutePath(), localVisitDir);

                if (b) {
                    LogUtils.getInstance().logInfo("[local] Move " + file.getAbsolutePath() + " to " + localVisitDir + " Successful!");
                } else {
                    LogUtils.getInstance().logInfo("[local] Move " + file.getAbsolutePath() + " to " + localVisitDir + " Failed!");
                }
            }
        }

        return true;
    }

    public boolean transfers(ArrayList<String> fileNames) {
        if (fileNames == null || fileNames.isEmpty()) {
            return false;
        }

        for (String name : fileNames) {
            File file = new File(localTempDir + name);
            boolean b = overWrite(file.getAbsolutePath(), localVisitDir);

            if (b) {
                LogUtils.getInstance().logInfo("[local] Over Write '" + file.getAbsolutePath() + "' to '" + localVisitDir + "' Successful!");
            } else {
                LogUtils.getInstance().logInfo("[local] Over Write '" + file.getAbsolutePath() + "' to '" + localVisitDir + "' Failed!");
            }
        }

        return true;
    }

}
