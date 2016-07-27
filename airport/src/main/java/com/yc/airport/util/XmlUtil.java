package com.yc.airport.util;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.URL;
 
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import com.yc.airport.entity.Aircraft;
import com.yc.airport.entity.FlightInfo;
import com.yc.airport.entity.MtcInfo;
import com.yc.airport.entity.Schedule;
 
/**
 * XML工具类
 * 
 * @author lihaoguo
 * @since 2014-04-25
 */
public class XmlUtil {
    /**
     * 获取根节点
     * 
     * @param doc
     * @return
     */
    public static Element getRootElement(Document doc) {
        if (Objects.isNull(doc)) {
            return null;
        }
        return doc.getRootElement();
    }
 
    /**
     * 获取节点eleName下的文本值，若eleName不存在则返回默认值defaultValue
     * 
     * @param eleName
     * @param defaultValue
     * @return
     */
    public static String getElementValue(Element eleName, String defaultValue) {
        if (Objects.isNull(eleName)) {
            return defaultValue == null ? "" : defaultValue;
        } else {
            return eleName.getTextTrim();
        }
    }
 
    public static String getElementValue(String eleName, Element parentElement) {
        if (Objects.isNull(parentElement)) {
            return null;
        } else {
            Element element = parentElement.element(eleName);
            if (Objects.isNotNull(element)) {
                return element.getTextTrim();
            } else {
                try {
                    throw new Exception("找不到节点" + eleName);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }
    }
 
    /**
     * 获取节点eleName下的文本值
     * 
     * @param eleName
     * @return
     */
    public static String getElementValue(Element eleName) {
        return getElementValue(eleName, null);
    }
 
    public static Document read(File file) {
        return read(file, null);
    }
 
    public static Document findCDATA(Document body, String path) {
        return XmlUtil.stringToXml(XmlUtil.getElementValue(path,
                body.getRootElement()));
    }
 
    /**
     * 
     * @param file
     * @param charset
     * @return
     * @throws DocumentException
     */
    public static Document read(File file, String charset) {
        if (Objects.isNull(file)) {
            return null;
        }
        SAXReader reader = new SAXReader();
        Map<String, String> map=new HashMap<String, String>();
		map.put("ns1",
				"http://generated.recoverymanager.sabre.com/exportAircraft");
		map.put("ns2",
				"http://generated.recoverymanager.sabre.com/exportFlow");
		map.put("ns3",
				"http://generated.recoverymanager.sabre.com/exportSchedule");
		reader.getDocumentFactory().setXPathNamespaceURIs(map);
        if (Objects.isNotNull(charset)) {
            reader.setEncoding(charset);
        }
        Document document = null;
        try {
            document = reader.read(file);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return document;
    }
 
    public static Document read(URL url) {
        return read(url, null);
    }
 
    /**
     * 
     * @param url
     * @param charset
     * @return
     * @throws DocumentException
     */
    public static Document read(URL url, String charset) {
        if (Objects.isNull(url)) {
            return null;
        }
        SAXReader reader = new SAXReader();
        Map<String, String> map=new HashMap<String, String>();
		map.put("ns1",
				"http://generated.recoverymanager.sabre.com/exportAircraft");
		map.put("ns2",
				"http://generated.recoverymanager.sabre.com/exportFlow");
		map.put("ns3",
				"http://generated.recoverymanager.sabre.com/exportSchedule");
		reader.getDocumentFactory().setXPathNamespaceURIs(map);
        if (Objects.isNotNull(charset)) {
            reader.setEncoding(charset);
        }
        Document document = null;
        try {
            document = reader.read(url);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return document;
    }
 
    /**
     * 将文档树转换成字符串
     * 
     * @param doc
     * @return
     */
    public static String xmltoString(Document doc) {
        return xmltoString(doc, null);
    }
 
    /**
     * 
     * @param doc
     * @param charset
     * @return
     * @throws IOException
     */
    public static String xmltoString(Document doc, String charset) {
        if (Objects.isNull(doc)) {
            return "";
        }
        if (Objects.isNull(charset)) {
            return doc.asXML();
        }
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding(charset);
        StringWriter strWriter = new StringWriter();
        XMLWriter xmlWriter = new XMLWriter(strWriter, format);
        try {
            xmlWriter.write(doc);
            xmlWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return strWriter.toString();
    }
 
    /**
     * 持久化Document
     * @param doc
     * @param charset
     * @return
     * @throws Exception
     * @throws IOException
     */
    public static void xmltoFile(Document doc, File file, String charset)
            throws Exception {
        if (Objects.isNull(doc)) {
            throw new NullPointerException("doc cant not null");
        }
        if (Objects.isNull(charset)) {
            throw new NullPointerException("charset cant not null");
        }
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding(charset);
        FileOutputStream os = new FileOutputStream(file);
        OutputStreamWriter osw = new OutputStreamWriter(os, charset);
        XMLWriter xmlWriter = new XMLWriter(osw, format);
        try {
            xmlWriter.write(doc);
            xmlWriter.close();
            if (osw != null) {
                osw.close();
            }
 
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
 
    /**
     * 
     * @param doc
     * @param charset
     * @return
     * @throws Exception
     * @throws IOException
     */
    public static void xmltoFile(Document doc, String filePath, String charset)
            throws Exception {
        xmltoFile(doc, new File(filePath), charset);
    }
 
     
    /**
     * 
     * @param doc
     * @param filePath
     * @param charset
     * @throws Exception
     */
    public static void writDocumentToFile(Document doc, String filePath, String charset)
            throws Exception {
        xmltoFile(doc, new File(filePath), charset);
    }
     
    public static Document stringToXml(String text) {
        try {
            return DocumentHelper.parseText(text);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return null;
    }
     
    public static Document createDocument() {
        return DocumentHelper.createDocument();
    }
    public static Element flightInfo2Element(FlightInfo flightInfo,Element flightInfoListNode) {
		Element f1 = flightInfoListNode.addElement("ns3:flightInfo");
		f1.addElement("ns3:id").setText(flightInfo.getId());
		f1.addElement("ns3:departureTime").setText(String.valueOf(flightInfo.getDepartureTime()));
		f1.addElement("ns3:arrivalTime").setText(String.valueOf(flightInfo.getArrivalTime()));
		f1.addElement("ns3:departureAirport").setText(flightInfo.getDepartureAirport());
		f1.addElement("ns3:arrivalAirport").setText(flightInfo.getArrivalAirport());
		f1.addElement("ns3:tailNumber").setText(flightInfo.getTailNumber());
		if (flightInfo.getStatus()==0) {
			f1.addElement("ns3:status").setText("Cancelled");
		}else {
			f1.addElement("ns3:status").setText("Assigned");
		}
		return f1;
	}
    private static Element mtcInfo2Element(MtcInfo mtcInfo, Element mtcInfoListNode) {
    	Element f1 = mtcInfoListNode.addElement("ns3:mtcInfo");
		f1.addElement("ns3:id").setText(mtcInfo.getId());
		f1.addElement("ns3:startTime").setText(String.valueOf(mtcInfo.getStartTime()));
		f1.addElement("ns3:endTime").setText(String.valueOf(mtcInfo.getEndTime()));
		f1.addElement("ns3:airport").setText(mtcInfo.getAirport());
		f1.addElement("ns3:tailNumber").setText(mtcInfo.getTailNumber());
		if (mtcInfo.getStatus()) {
			f1.addElement("ns3:status").setText("Assigned");
		}else {
			f1.addElement("ns3:status").setText("Cancelled");
		}
		return f1;
		
	}
    public static void creatOutputXml(List<FlightInfo> flightInfos,List<MtcInfo> mtcInfos,String outputPath){
    	Document document = XmlUtil.createDocument();
		Element rootElement = document.addElement("exportAircrafts");
		rootElement.addNamespace("ns3", "http://generated.recoverymanager.sabre.com/exportSchedule");
		document.setRootElement(rootElement); 
		Element flightInfoList = rootElement.addElement("ns3:flightInfoList");
		Element mtcInfoList = rootElement.addElement("ns3:mtcInfoList");
		for (Iterator iterator = flightInfos.iterator(); iterator.hasNext();) {
			FlightInfo flightInfo = (FlightInfo) iterator.next();
			XmlUtil.flightInfo2Element(flightInfo, flightInfoList);
		}
		for (Iterator iterator = mtcInfos.iterator(); iterator.hasNext();) {
			MtcInfo mtcInfo = (MtcInfo) iterator.next();
			XmlUtil.mtcInfo2Element(mtcInfo,mtcInfoList);
		}
		try {
			XmlUtil.writDocumentToFile(document, outputPath, "utf-8");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
 
    

	public static void main(String[] args) {
    	Document aircrafts = XmlUtil.read(new File("C:/Users/Administrator/Desktop/竞赛数据表格/Data/Scenario1/input/Aircraft.xml"));
        //System.out.println(aircrafts.getRootElement().get);
    	List nodes = aircrafts.getRootElement().elements("ns1:aircraft");
    	int i=1;
    	for (Iterator it = nodes.iterator(); it.hasNext();i++) { 
    		  Element elm = (Element) it.next();
    	  	// do something
    		  System.out.println(i);
    		  System.out.println(elm.getText());
    	}
    	//System.out.println(XmlUtil.getElementValue("ns1:aircraft",aircrafts.getRootElement()));
        //System.out.println(aircrafts.getText());
    	//System.out.println(XmlUtil.xmltoString(aircrafts));
        System.out.println("end");
    	// System.out.println(XmlTool.xmltoString(Disconnect.getDisconnectDocument(),
        // "UTF-8"));
    }
}
