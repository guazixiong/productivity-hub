package common.util.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

/**
 * XML操作工具类，提供XML和Map之间的转换功能.
 *
 * @author: pbad
 * @date: 2023/9/7 14:52
 * @version: 1.0
 */
public class XmlUtils {

    /**
     * 将XML字符串转换为Map。
     *
     * @param xml XML字符串
     * @return 包含XML元素的Map
     */
    public static Map<String, String> xmlToMap(String xml) {
        Map<String, String> params = new HashMap<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xml)));
            Node root = doc.getElementsByTagName("xml").item(0);
            NodeList nl = root.getChildNodes();
            Node n = null;
            for (int i = 0; i < nl.getLength(); i++) {
                n = nl.item(i);
                if ("#text".equals(n.getNodeName())) {
                    continue;
                }
                params.put(n.getNodeName(), n.getChildNodes().item(0)
                        .getNodeValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return params;
    }

    /**
     * 将XML字符串转换为Map，指定根节点名称。
     *
     * @param xml  XML字符串
     * @param type 指定的根节点名称
     * @return 包含XML元素的Map
     */
    public static Map<String, String> xmlToMap(String xml, String type) {
        Map<String, String> params = new HashMap<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xml)));
            Node root = doc.getElementsByTagName(type).item(0);
            NodeList nl = root.getChildNodes();
            Node n = null;
            for (int i = 0; i < nl.getLength(); i++) {
                n = nl.item(i);
                if ("#text".equals(n.getNodeName())) {
                    continue;
                }
                params.put(n.getNodeName(), n.getChildNodes().item(0)
                        .getNodeValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return params;
    }

    /**
     * 将InputStream中的XML转换为Map。
     *
     * @param xmlInput InputStream包含的XML数据
     * @return 包含XML元素的Map
     */
    public static Map<String, String> xmlToMap(InputStream xmlInput) {
        try {
            String xml = InputStreamTOString(xmlInput, "UTF-8");
            return xmlToMap(xml);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将Map转换为XML字符串。
     *
     * @param params 包含XML元素的Map
     * @return XML字符串
     */
    public static String mapToXml(Map<String, Object> params) {
        StringBuffer sb = new StringBuffer();
        sb.append("<xml>");
        params.keySet().forEach(key -> {
            sb.append("<").append(key).append(">");
            sb.append("<![CDATA[").append(params.get(key)).append("]]>");
            sb.append("</").append(key).append(">");
        });
        sb.append("</xml>");
        return sb.toString();
    }

    /**
     * 将InputStream转换为字符串。
     *
     * @param in       InputStream对象
     * @param encoding 编码方式
     * @return 转换后的字符串
     * @throws Exception
     */
    public static String InputStreamTOString(InputStream in, String encoding) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int count = -1;
        while ((count = in.read(data, 0, 1024)) != -1) {
            outStream.write(data, 0, count);
        }
        in.close();
        outStream.flush();
        outStream.close();
        data = null;
        return new String(outStream.toByteArray(), encoding);
    }

}
