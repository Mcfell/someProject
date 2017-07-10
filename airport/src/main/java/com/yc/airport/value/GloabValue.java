package com.yc.airport.value;

import java.util.HashMap;
import java.util.List;

import com.yc.airport.entity.Aircraft;
import com.yc.airport.entity.AircraftClosure;
import com.yc.airport.entity.FlightInfo;
import com.yc.airport.entity.IdleTimeFlight;
import com.yc.airport.entity.MtcInfo;
import com.yc.airport.entity.Schedule;

public class GloabValue {
	//每架飞机对应的空闲时段
	public static HashMap<String, Aircraft> aircraftsMap;
	//每个机场关闭时间段
	public static HashMap<String, List<AircraftClosure>> aircraftClosuresMap;
	//机场关闭时段列表
	public static List<AircraftClosure> aircraftClosures;
	public static Schedule orginSchedule;
	public static Schedule schedule;
	//每架飞机与所跑航班的映射
	public static HashMap<String, List<FlightInfo>> flightInfoMap;
	//每架飞机与维护信息的映射
	public static HashMap<String, List<MtcInfo>> mtcInfoMap;
	//每架飞机空闲时段映射
	public static HashMap<String, List<IdleTimeFlight>> idleTimeMap;
	public static Schedule newSchdule;
	public static List<Schedule> scheduleList;
	
	public static int flightAllNum;
	public static int mtcAllNum;
	public static int popNum;

	public static int weightCancelMaintenance ;
	public static int weightCancelFlight ;
	public static int weightViolateBalance ;
	public static int weightViolatePositioning ;
	public static int weightFlightDelay ;
	public static int weightFlightSwap ;
	public static int maxDelayTime;
	public static int maxRunTime;
	public static int turnTime;
	
	public static final int DEPARTURE_TYPE = 1;
	public static final int ARRIVIAL_TYPE = 2;
	public static String[] AIRPORTS;
	public static String[] TAILS;
	
}
