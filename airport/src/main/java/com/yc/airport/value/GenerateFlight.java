package com.yc.airport.value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.reflect.generics.tree.Tree;

import com.yc.airport.entity.Aircraft;
import com.yc.airport.entity.AircraftClosure;
import com.yc.airport.entity.FlightInfo;
import com.yc.airport.entity.MtcInfo;
import com.yc.airport.entity.Schedule;

public class GenerateFlight {

	private static final Logger logger = LoggerFactory.getLogger(GenerateFlight.class);
	//航班延误机率
	private static final double delayProbability = 0;
	//航班在机场关闭时取消的概率
	private static final double flightCancelProbabilty = 0.3;
	/*enum airport{
		SAM("SAM"), JOE("JOE"), TIJ("TIJ"), PVG("PVG"), JIM("JIM"), EZE("EZE");
		private String valString;
		private airport(String vString) {
			this.valString = vString;
		}
	}*/
	/**
	 * 生成实际的航班任务表
	 * @Description: TODO
	 * @param @return   
	 * @return Schedule  
	 * @throws
	 * @author mcfell
	 * @date 2016年6月22日
	 */
	public static Schedule generateSchedule() {
		Schedule newSchedule = new Schedule();
		newSchedule.setFlightInfos(generateRandomFlightInfos());
		newSchedule.setMtcInfos(GloabValue.schedule.getMtcInfos());
		GloabValue.newSchdule = newSchedule;
		return newSchedule;
	}
	/*
	public static List<MtcInfo> generateRandoMtcInfos() {
		List<MtcInfo> mtcInfos = new ArrayList<MtcInfo>();
		
		HashMap<String, List<MtcInfo>> mtcInfoMap = GloabValue.mtcInfoMap;
		for (int i = 0; i < GloabValue.TAILS.length; i++) {
			String tail = GloabValue.TAILS[i];
		}
		return mtcInfos;
	}
	*/
	/*
	 * 生成随机航班信息
	 */
	public static List<FlightInfo> generateRandomFlightInfos() {
		List<FlightInfo> flightInfos = new ArrayList<FlightInfo>();
		
		HashMap<String, List<FlightInfo>> flightInfoMap = GloabValue.flightInfoMap;
		int genesNum = 0;
		//遍历每一架飞机
		for (int i = 0; i < GloabValue.TAILS.length; i++) {
			String tail = GloabValue.TAILS[i];
			
			List<FlightInfo> flightInfo= flightInfoMap.get(tail);
			int flightInfoLength = flightInfo.size();
			
			Aircraft aircraft = GloabValue.aircraftsMap.get(tail);
			String startAvailableAirport = aircraft.getStartAvailableAirport();
			String endAvailableAirport = aircraft.getEndAvailableAirport();
			
			String lastArrivalAirport = startAvailableAirport;
			boolean flightChangeFlag = false;//上一次到达机场修改标识
			for (int j = 0; j < flightInfoLength; j++) {
				FlightInfo flight = flightInfo.get(j);
				String tmp;
				logger.debug("起飞机场："+lastArrivalAirport);
				/*//如果修改了上一次任务航班信息
				if (flightChangeFlag) { 
					flight.setDepartureAirport(lastArrivalAirport);//设置离开机场为上一次到达的机场
					lastArrivalAirport = flight.getArrivalAirport();
				}else{//如果机场关闭，则修改对应航班机场或者取消此次航班
					switch (isAirportClose(flight)) {
					case 1://起点机场关闭，取消航班
						flight.setStatus(0);
						break;
					case 2://到达机场关闭，分两种情况
						if (Math.random()< flightCancelProbabilty) { //取消航班
							flight.setStatus(0);
						}else {
							tmp = generateRandomAirport();
							while (lastArrivalAirport.equals(tmp) && flight.getArrivalAirport().equals(tmp)) {//随机生成到达机场,到达机场与离开机场不同
								tmp = generateRandomAirport();
							}
							flight.setArrivalAirport(tmp);
							lastArrivalAirport = tmp;
						}
						break;
					default:
						lastArrivalAirport = flight.getArrivalAirport();
						break;
					}
				}
				
				if (Math.random()<delayProbability) {
					long delayTime = generateRandomDelayTime();
					flight.setDepartureTime(delayTime+flight.getDepartureTime());//设置随机推迟时间
					logger.debug(flight.getId()+" 延迟 "+delayTime+"s"+".Now is："+flight.getDepartureTime());
				}*/
				flightInfos.add(flight);
			}
			genesNum++;
		}
		return flightInfos;
	}
	/*
	 * 判断机场是否处于关闭状态
	 * reutrn 1,起飞机场处于关闭时段；2，到达机场处于关闭时段 ；0，正常
	 */
	public static int isAirportClose(FlightInfo flightInfo) {
		HashMap<String,List<AircraftClosure>> aircraftClosureMap =	GloabValue.aircraftClosuresMap;
		int statue = 0;
		//boolean isClose = false;
		List<AircraftClosure> aircraftClosures = null;
		long flightInfoArrivalTime = flightInfo.getArrivalTime();
		long flightInfoDepartureTime = flightInfo.getDepartureTime();
		if ((aircraftClosures=aircraftClosureMap.get(flightInfo.getDepartureAirport()))!=null) {
			//起飞机场如果为关闭机场
			//判断时间是否在关闭时间段
			for (Iterator iterator = aircraftClosures.iterator(); iterator.hasNext();) {
				AircraftClosure aircraftClosure = (AircraftClosure) iterator.next();
				long start = aircraftClosure.getStartTime();
				long end = aircraftClosure.getEndTime();
				if (flightInfoArrivalTime>=start && flightInfoArrivalTime<=end) {
					statue = 1;
					logger.debug(flightInfo.getId()+" 起飞机场："+flightInfo.getDepartureAirport()+"与关闭机场"+
					aircraftClosure.getCode()+"冲突.到达时间："+flightInfoArrivalTime);
				}
				if (flightInfoDepartureTime>=start&& flightInfoDepartureTime<=end) {
					statue = 1;
					logger.debug(flightInfo.getId()+" 起飞机场："+flightInfo.getDepartureAirport()+"与关闭机场"+
							aircraftClosure.getCode()+"冲突.起飞时间："+flightInfoDepartureTime);
				}
			}
		}else if ((aircraftClosures=aircraftClosureMap.get(flightInfo.getArrivalAirport()))!=null) {
			//到达机场如果为关闭机场
			//判断时间是否在关闭时间段
			for (Iterator iterator = aircraftClosures.iterator(); iterator.hasNext();) {
				AircraftClosure aircraftClosure = (AircraftClosure) iterator.next();
				long start = aircraftClosure.getStartTime();
				long end = aircraftClosure.getEndTime();
				if (flightInfoArrivalTime>=start && flightInfoArrivalTime<=end) {
					statue = 2;
					logger.debug(flightInfo.getId()+" 到达机场："+flightInfo.getArrivalAirport()+"与关闭机场"+
							aircraftClosure.getCode()+"冲突.到达时间："+flightInfoArrivalTime);
				}
				if (flightInfoDepartureTime>=start&& flightInfoDepartureTime<=end) {
					statue = 2;
					logger.debug(flightInfo.getId()+" 到达机场："+flightInfo.getArrivalAirport()+"与关闭机场"+
							aircraftClosure.getCode()+"冲突.起飞时间："+flightInfoDepartureTime);
				}
			}
		}
		return statue;
	}
	/*
	 * 获取机场关闭的HashMap,Key为机场名
	 */
	public static HashMap<String,List<AircraftClosure>> getAircraftClosureMap() {
		List<AircraftClosure> aircraftClosures = GloabValue.aircraftClosures;
		HashMap<String,List<AircraftClosure>> aircraftClosureMap = new HashMap<String, List<AircraftClosure>>();
		for (Iterator iterator = aircraftClosures.iterator(); iterator.hasNext();) {
			AircraftClosure aircraftClosure = (AircraftClosure) iterator.next();
			List<AircraftClosure> aircraftClosures2;
			if(aircraftClosureMap.get(aircraftClosure.getCode())==null){
				aircraftClosures2 = new ArrayList<AircraftClosure>();
				aircraftClosures2.add(aircraftClosure);
			}else {
				aircraftClosures2 = aircraftClosureMap.get(aircraftClosure.getCode());
			}
			aircraftClosureMap.put(aircraftClosure.getCode(), aircraftClosures2);
		}
		return aircraftClosureMap;
	}
	/*
	 * 生成随机机场名
	 */
	public static String generateRandomAirport() {
		int sed = (int) Math.floor(Math.random()*GloabValue.AIRPORTS.length);
		logger.debug("生成随机机场"+GloabValue.AIRPORTS[sed]);
		return GloabValue.AIRPORTS[sed];
	}
	/*
	 * 生成随机延迟时间，范围由初始参数决定
	 */
	public static long generateRandomDelayTime() {
		long time = (long)Math.round(Math.random()*GloabValue.maxDelayTime);
		return time;
	}
}
