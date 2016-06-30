package com.yc.airport.value;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.dom4j.Document;
import org.dom4j.Element;

import com.yc.airport.entity.Aircraft;
import com.yc.airport.entity.AircraftClosure;
import com.yc.airport.entity.MtcInfo;
import com.yc.airport.entity.Schedule;
import com.yc.airport.entity.FlightInfo;
import com.yc.airport.util.PropertiesUtil;
import com.yc.airport.util.XmlUtil;

public class DataReader {
	/*
	 * 初始化xml中的信息
	 */
	public static void ReadAllXml(String Path) {
		GloabValue.aircraftsMap = readAircrafts(Path);
		GloabValue.aircraftClosures = readAirportClosure(Path);
		Schedule schedule = readSchedule(Path);
		GloabValue.schedule = schedule;
		
		GloabValue.flightInfoMap = schedule.getPartitionFlightInfoByTail();
		GloabValue.mtcInfoMap = schedule.getPartitionMtcInfoByTail();
		
		schedule.reListFlightInfo();
		schedule.reListMtcInfo();
		
		GloabValue.aircraftClosuresMap = GenerateFlight.getAircraftClosureMap();
		GloabValue.flightAllNum = schedule.getFlightInfos().size();
		GloabValue.mtcAllNum = schedule.getMtcInfos().size();
		
		ReadAllProperties(Path);
	}
	public static void ReadAllProperties(String basePath) {
		PropertiesUtil pUtil = new PropertiesUtil();
		String path = pUtil.readPropertie("parameters");
		Document document = XmlUtil.read(new File(basePath+path));
		Element root = document.getRootElement();
		GloabValue.weightCancelFlight = Integer.parseInt(root.element("weightCancelFlight").getStringValue());
		GloabValue.weightCancelMaintenance = Integer.parseInt(root.element("weightCancelMaintenance").getStringValue());
		GloabValue.weightFlightDelay = Integer.parseInt(root.element("weightFlightDelay").getStringValue());
		GloabValue.weightFlightSwap = Integer.parseInt(root.element("weightFlightSwap").getStringValue());
		GloabValue.weightViolateBalance = Integer.parseInt(root.element("weightViolateBalance").getStringValue());
		GloabValue.weightViolatePositioning = Integer.parseInt(root.element("weightViolatePositioning").getStringValue());
		GloabValue.maxDelayTime = Integer.parseInt(root.element("maxDelayTime").getStringValue());
		GloabValue.maxRunTime = Integer.parseInt(root.element("maxRunTime").getStringValue());
		GloabValue.turnTime = Integer.parseInt(root.element("turnTime").getStringValue());
	}
	
	public static HashMap<String, Aircraft> readAircrafts(String basePath) {
		PropertiesUtil pUtil = new PropertiesUtil();
		String path = pUtil.readPropertie("aircraft");
		Document document = XmlUtil.read(new File(basePath+path));
		List root=(List) document.selectNodes("//ns1:aircraft");
		//List<Aircraft> listAircrafts = new ArrayList<Aircraft>();
		HashMap<String, Aircraft> aircraftMap = new HashMap<String, Aircraft>();
		for (Iterator iter = root.iterator(); iter.hasNext(); ) {
			Element attribute = (Element) iter.next();
			Element tailNumber=(Element) attribute.element("tailNumber");
			Element startAvailableTime=(Element) attribute.element("startAvailableTime");
			Element endAvailableTime=(Element) attribute.element("endAvailableTime");
			Element startAvailableAirport=(Element) attribute.element("startAvailableAirport");
			Element endAvailableAirport=(Element) attribute.element("endAvailableAirport");
			Aircraft aircraft = new Aircraft(tailNumber.getStringValue(),Long.valueOf(startAvailableTime.getStringValue()),
					Long.valueOf(endAvailableTime.getStringValue()),startAvailableAirport.getStringValue(),endAvailableAirport.getStringValue());
			//System.out.println(aircraft.toString());
			//listAircrafts.add(aircraft);
			aircraftMap.put(tailNumber.getStringValue(), aircraft);
		}
		return aircraftMap;
	}
	
	public static List<AircraftClosure> readAirportClosure(String basePath) {
		PropertiesUtil pUtil = new PropertiesUtil();
		String path = pUtil.readPropertie("airportClosure");
		Document document = XmlUtil.read(new File(basePath+"/AirportClosure.xml"));
		List root=(List) document.selectNodes("//ns2:flow");
		List<AircraftClosure> listAircraftClosures = new ArrayList<AircraftClosure>();
		for (Iterator iter = root.iterator(); iter.hasNext(); ) {
			Element attribute = (Element) iter.next();
			Element code=(Element) attribute.element("code");
			Element startTime=(Element) attribute.element("startTime");
			Element endTime=(Element) attribute.element("endTime");
			AircraftClosure aircraftClosure = new AircraftClosure(code.getStringValue(),Long.valueOf(startTime.getStringValue()),
					Long.valueOf(endTime.getStringValue()));
			//System.out.println(aircraftClosure.toString());
			listAircraftClosures.add(aircraftClosure);
		}
		return listAircraftClosures;
	}
	
	public static Schedule readSchedule(String basePath) {
		PropertiesUtil pUtil = new PropertiesUtil();
		String path = pUtil.readPropertie("schedule");
		Document document = XmlUtil.read(new File(basePath+"/Schedule.xml"));
		List flightInfoList=(List) document.selectNodes("//ns3:flightInfo");
		List mtcInfoList=(List) document.selectNodes("//ns3:mtcInfo");
		
		List<FlightInfo> flightInfos = new ArrayList<FlightInfo>();
		List<MtcInfo> mtcInfos = new ArrayList<MtcInfo>();
		
		Set<String> tailSet = new TreeSet<String>();
		Set<String> airportSet = new TreeSet<String>();
		
		for (Iterator iter = flightInfoList.iterator(); iter.hasNext(); ) {
			Element attribute = (Element) iter.next();
			Element id=(Element) attribute.element("id");
			Element departureTime=(Element) attribute.element("departureTime");
			Element arrivalTime=(Element) attribute.element("arrivalTime");
			Element departureAirport=(Element) attribute.element("departureAirport");
			Element arrivalAirport=(Element) attribute.element("arrivalAirport");
			Element tailNumber=(Element) attribute.element("tailNumber");
			
			FlightInfo flightInfo = new FlightInfo(id.getStringValue(),Long.valueOf(departureTime.getStringValue()),Long.valueOf(arrivalTime.getStringValue())
					,departureAirport.getStringValue(),arrivalAirport.getStringValue(),tailNumber.getStringValue());
			//System.out.println(flightInfo.toString());
			tailSet.add(flightInfo.getTailNumber());
			airportSet.add(flightInfo.getArrivalAirport());
			airportSet.add(flightInfo.getDepartureAirport());
			
			flightInfos.add(flightInfo);
		}
		for (Iterator iter = mtcInfoList.iterator(); iter.hasNext(); ) {
			Element attribute = (Element) iter.next();
			Element id=(Element) attribute.element("id");
			Element startTime=(Element) attribute.element("startTime");
			Element endTime=(Element) attribute.element("endTime");
			Element airport=(Element) attribute.element("airport");
			Element tailNumber=(Element) attribute.element("tailNumber");
			
			MtcInfo mtcInfo = new MtcInfo(id.getStringValue(),Long.valueOf(startTime.getStringValue()),
					Long.valueOf(endTime.getStringValue()),airport.getStringValue(),tailNumber.getStringValue());
			//System.out.println(mtcInfo.toString());
			mtcInfos.add(mtcInfo);
		}
		
		Schedule schedule = new Schedule(flightInfos, mtcInfos);
		String[] airportStrings = new String[airportSet.size()];
		String[] tailsStrings = new String[tailSet.size()];
		GloabValue.AIRPORTS =  airportSet.toArray(airportStrings);
		GloabValue.TAILS =  tailSet.toArray(tailsStrings);
		return schedule;
	}
	
	
}
