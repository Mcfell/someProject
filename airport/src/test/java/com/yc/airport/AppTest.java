package com.yc.airport;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.SimpleFormatter;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;









import com.sun.org.apache.bcel.internal.generic.NEW;
import com.yc.airport.algorithm.Individual;
import com.yc.airport.entity.Aircraft;
import com.yc.airport.entity.FlightInfo;
import com.yc.airport.entity.Schedule;
import com.yc.airport.util.XmlUtil;
import com.yc.airport.value.DataReader;
import com.yc.airport.value.GenerateFlight;
import com.yc.airport.value.GloabValue;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {
	/**
	 * Create the test case
	 *
	 * @param testName
	 *            name of the test case
	 */
	public AppTest(String testName) {
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(AppTest.class);
	}

	/**
	 * Rigourous Test :-)
	 * 
	 * @throws DocumentException
	 */
	public void testApp() {
		assertTrue(true);
	}

	public void testXml() throws DocumentException {
		// 方法二：设置你的DocumentFactory()的命名空间 setXPathNamespaceURIs
		Map<String, String> xmlMap = new HashMap<String, String>();
		xmlMap.put("ns3",
				"http://generated.recoverymanager.sabre.com/exportSchedule");
		xmlMap.put("ns2",
				"http://generated.recoverymanager.sabre.com/exportFlow");
		xmlMap.put("ns1",
				"http://generated.recoverymanager.sabre.com/exportAircraft");
		SAXReader reader = new SAXReader();
		Document doc = reader
				.read(new File(
						"C:/Users/Administrator/Desktop/竞赛数据表格/Data/Scenario1/input/Aircraft.xml"));

		String defaultNamespace = doc.getRootElement().getNamespaceURI();

		XPath x = doc.createXPath("//ns1:aircraft");
		x.setNamespaceURIs(xmlMap);
		List<Node> nodes = x.selectNodes(doc);
		for (Iterator iter = nodes.iterator(); iter.hasNext();) {
			Node attribute = (Node) iter.next();
			Document url = attribute.getDocument();
			Element list = url.getRootElement();
			XPath tailNumberPath = list.createXPath("//ns1:tailNumber");
			tailNumberPath.setNamespaceURIs(xmlMap);
			Node tailNode = tailNumberPath.selectSingleNode(list);
			System.out.println(tailNode.getStringValue());
			/*
			 * for ( Iterator i = list.elementIterator(); i.hasNext(); ) {
			 * 
			 * Element element = (Element) i.next(); // do something
			 * System.out.println(element.getStringValue());
			 * System.out.println("----"); }
			 */

		}
		/*
		 * Document aircrafts = XmlUtil.read(new File(
		 * "C:/Users/Administrator/Desktop/竞赛数据表格/Data/Scenario1/input/Aircraft.xml"
		 * )); System.out.println(aircrafts.getRootElement().getName());
		 * 
		 * List<?> nodes = aircrafts.getRootElement().elements("ns1:aircraft");
		 * int i=1; for (Iterator<?> it = nodes.iterator(); it.hasNext();i++) {
		 * Element elm = (Element) it.next(); // do something
		 * System.out.println(i); System.out.println(elm.getText()); }
		 * //System.out
		 * .println(XmlUtil.getElementValue("ns1:aircraft",aircrafts.
		 * getRootElement())); //System.out.println(aircrafts.getText());
		 * //System.out.println(XmlUtil.xmltoString(aircrafts));
		 * 
		 * System.out.println("end"); //
		 * System.out.println(XmlTool.xmltoString(Disconnect
		 * .getDisconnectDocument(), // "UTF-8"));
		 */
	}

	public void testXml2() throws DocumentException {
		SAXReader reader1 = new SAXReader();
		Map<String, String> map = new HashMap<String, String>();
		map.put("ns1",
				"http://generated.recoverymanager.sabre.com/exportAircraft");
		reader1.getDocumentFactory().setXPathNamespaceURIs(map);
		Document document = reader1
				.read(new File(
						"C:/Users/Administrator/Desktop/竞赛数据表格/Data/Scenario1/input/Aircraft.xml"));
		List root = (List) document.selectNodes("//ns1:aircraft");
		List<Aircraft> listAircrafts = new ArrayList<Aircraft>();
		for (Iterator iter = root.iterator(); iter.hasNext();) {
			Element attribute = (Element) iter.next();
			// System.out.println(attribute.getStringValue());
			System.out
					.println(attribute.element("tailNumber").getStringValue());
			Element tailNumber = (Element) attribute.element("tailNumber");
			Element startAvailableTime = (Element) attribute
					.element("startAvailableTime");
			Element endAvailableTime = (Element) attribute
					.element("endAvailableTime");
			Element startAvailableAirport = (Element) attribute
					.element("startAvailableAirport");
			Element endAvailableAirport = (Element) attribute
					.element("endAvailableAirport");
			Aircraft aircraft = new Aircraft(tailNumber.getStringValue(),
					Long.valueOf(startAvailableTime.getStringValue()),
					Long.valueOf(endAvailableTime.getStringValue()),
					startAvailableAirport.getStringValue(),
					endAvailableAirport.getStringValue());
			System.out.println(aircraft.toString());
			listAircrafts.add(aircraft);
		}
	}

	public void testTime() throws ParseException {
/*
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		java.util.Date UTCDate = null;
		String localTimeStr = null;

		UTCDate = format.parse("1335491700");
		format.setTimeZone(TimeZone.getTimeZone("GMT-8"));
		localTimeStr = format.format(UTCDate);

		System.out.println(localTimeStr);*/
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		System.out.println(format.format(new Date(1336590000000L)));
		System.out.println(format.format(new Date(1335490000000L)));
		System.out.println(new Date().getTime());
	}
	public void testGenerateSchedual() {
		DataReader.ReadAllXml("C:/Users/Administrator/Desktop/竞赛数据表格/Data/Scenario1/input");
		Schedule schedule = GenerateFlight.generateSchedule();
		Individual individual = new Individual();
		//individual.generateIndividual();
		individual.getFlightGene();
		individual.getMtcGene();
	}
	public void testIndividual() {
		DataReader.ReadAllXml("E:/program/Scenarios/Scn4/Input");
		Individual individual = new Individual();
		//individual.generateIndividual();
		System.out.println(GloabValue.mtcAllNum);
		System.out.println(individual.getMtcGene().length);
		//individual.generateIndividual(0, true);
		//individual.printSchedualInfo(true);
	}
	public void testCreatXml() throws Exception{
		Document document = XmlUtil.createDocument();
		Element rootElement = document.addElement("exportAircrafts");
		document.setRootElement(rootElement);
		Element flightInfoList = rootElement.addElement("flightInfoList");
		FlightInfo flightInfo = new FlightInfo("123", 12313L, 13213L, "JOE", "sam", "sdf",1);
		FlightInfo flightInfo2 = new FlightInfo("123", 12313L, 13213L, "JOE", "sam", "sdf",1);
		FlightInfo flightInfo3 = new FlightInfo("123", 12313L, 13213L, "JOE", "sam", "sdf",1);
		XmlUtil.flightInfo2Element(flightInfo, flightInfoList);
		XmlUtil.flightInfo2Element(flightInfo2, flightInfoList);
		XmlUtil.flightInfo2Element(flightInfo3, flightInfoList);
		XmlUtil.writDocumentToFile(document, "E:/Data/output.xml", "utf-8");
	}
}
