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
	public static HashMap<String, Aircraft> aircraftsMap;
	public static HashMap<String, List<AircraftClosure>> aircraftClosuresMap;
	public static List<AircraftClosure> aircraftClosures;
	public static Schedule orginSchedule;
	public static Schedule schedule;
	public static HashMap<String, List<FlightInfo>> flightInfoMap;
	public static HashMap<String, List<MtcInfo>> mtcInfoMap;
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
