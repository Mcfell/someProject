package com.yc.airport.value;

import java.util.HashMap;
import java.util.List;

import com.yc.airport.entity.Aircraft;
import com.yc.airport.entity.AircraftClosure;
import com.yc.airport.entity.FlightInfo;
import com.yc.airport.entity.MtcInfo;
import com.yc.airport.entity.Schedule;

public class GloabValue {
	public static HashMap<String, Aircraft> aircrafts;
	public static List<AircraftClosure> aircraftClosures;
	public static Schedule schedule;
	public static HashMap<String, List<FlightInfo>> flightInfoMap;
	public static HashMap<String, List<MtcInfo>> mtcInfoMap;
	public static Schedule newSchdule;
	
	public static int flightAllNum;
	public static int mtcAllNum;

	public static int weightCancelMaintenance ;
	public static int weightCancelFlight ;
	public static int weightViolateBalance ;
	public static int weightViolatePositioning ;
	public static int weightFlightDelay ;
	public static int weightFlightSwap ;
	public static int maxDelayTime;
	public static int maxRunTime;
	public static int turnTime;
	
	public static String[] AIRPORTS;
	public static String[] TAILS;
	
}
