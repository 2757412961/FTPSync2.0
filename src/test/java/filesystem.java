import Utils.FileSystem;
import org.junit.Test;

import java.io.File;
import java.nio.charset.Charset;

public class filesystem {

    @Test
    public void testMkdirs() {

        System.out.println(FileSystem.getInstance().mkdirs("F:\\EnglishPath\\03EndPoint\\FTPSync\\FTPData\\now\\sdf\\adsf"));
        ;
    }

    @Test
    public void moveFile() {

        System.out.println(
                FileSystem.getInstance().moveTo("F:\\EnglishPath\\03EndPoint\\FTPSync\\FTPData\\now\\sdf\\新建文本文档.txt",
                        "F:\\EnglishPath\\03EndPoint\\FTPSync\\FTPData\\now\\")
        );

    }

    @Test
    public void listFiles() {

        System.out.println(
                FileSystem.getInstance().listFile("F:\\EnglishPath\\03EndPoint\\FTPSync\\FTPData\\now\\")
        );
    }

    @Test
    public void transfers() {
//        FileSystem.getInstance().transfers();
    }

    @Test
    public void getSize() {
        System.out.println(new File("F:\\EnglishPath\\03EndPoint\\FTPSync2.0\\target/FTPSync-2.0-SNAPSHOT-shaded.jar").getFreeSpace());
        System.out.println(new File("F:\\EnglishPath\\03EndPoint\\FTPSync2.0\\target/FTPSync-2.0-SNAPSHOT-shaded.jar").getUsableSpace());
        System.out.println(new File("F:\\EnglishPath\\03EndPoint\\FTPSync2.0\\target/FTPSync-2.0-SNAPSHOT-shaded.jar").getTotalSpace());
        System.out.println(new File("F:\\EnglishPath\\03EndPoint\\FTPSync2.0\\target/FTPSync-2.0-SNAPSHOT-shaded.jar").length());
        System.out.println(new File("F:\\EnglishPath\\03EndPoint\\FTPSync2.0\\target/FTPSync-2.0-SNAPSHOT-shaded.jar").length() / 1024);
        System.out.println(new File("F:\\实验室\\海洋组组会\\海洋北京组工作汇报2020年12月.docx").length());
        System.out.println(new File("F:\\实验室\\海洋组组会\\海洋北京组工作汇报2020年12月.docx").length() / 1024);
        System.out.println("当前JVM的默认字符集：" + Charset.defaultCharset());
    }


}
