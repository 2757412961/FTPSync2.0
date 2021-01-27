package Utils;

/**
 * @author ：WSS
 * @date ：Created in 2019/9/10 10:41
 * @description：
 * @modified By：Jesse.Qi
 * @version: 1.1$
 */

import java.io.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class XmlUtils {
    public static String ReadXml(String node) {
        String path = "";
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder bulider = null;
            String curDiry = System.getProperty("user.dir") + "\\" + "PathSetting.xml";
//            String curDiry = System.getProperty("user.dir") + "\\" + "PathSettingSCSECData.xml";
            bulider = factory.newDocumentBuilder();
            Document document = bulider.parse(new File(curDiry));
            org.w3c.dom.Element rootElement = document.getDocumentElement();

            NodeList list = rootElement.getElementsByTagName(node);
            org.w3c.dom.Element element = (org.w3c.dom.Element) list.item(0);
            path = element.getChildNodes().item(0).getNodeValue();
        } catch (Exception e) {
            LogUtils.getInstance().logException(e);
        }
        return path;
    }
}
