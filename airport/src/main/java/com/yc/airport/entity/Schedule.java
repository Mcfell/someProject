package com.yc.airport.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class Schedule {
	
	private List<FlightInfo> flightInfos;
	private List<MtcInfo> mtcInfos;
	public List<FlightInfo> getFlightInfos() {
		return flightInfos;
	}
	public void setFlightInfos(List<FlightInfo> flightInfos) {
		this.flightInfos = flightInfos;
	}
	public List<MtcInfo> getMtcInfos() {
		return mtcInfos;
	}
	public void setMtcInfos(List<MtcInfo> mtcInfos) {
		this.mtcInfos = mtcInfos;
	}
	public Schedule() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Schedule(List<FlightInfo> flightInfos, List<MtcInfo> mtcInfos) {
		super();
		this.flightInfos = flightInfos;
		this.mtcInfos = mtcInfos;
	}
	/*
	 * 根据飞机Tail划分航班信息
	 */
	public HashMap<String, List<FlightInfo>> getPartitionFlightInfoByTail(){
		HashMap<String, List<FlightInfo>> rHashMap = new HashMap<String, List<FlightInfo>>();
		
		for (Iterator iterator = flightInfos.iterator(); iterator.hasNext();) {
			FlightInfo flightInfo = (FlightInfo) iterator.next();
			List<FlightInfo> list;
			String tail = flightInfo.getTailNumber();
			if (rHashMap.get(tail) == null) {
				list = new ArrayList<FlightInfo>();
				list.add(flightInfo);
			}else {
				list = rHashMap.get(tail);
				list.add(flightInfo);
			}
			rHashMap.put(tail, list);
		}
		
		return rHashMap;
	}
	/*
	 * 根据飞机Tail划分航班维护信息
	 */
	public HashMap<String, List<MtcInfo>> getPartitionMtcInfoByTail(){
		HashMap<String, List<MtcInfo>> rHashMap = new HashMap<String, List<MtcInfo>>();
		
		for (Iterator iterator = mtcInfos.iterator(); iterator.hasNext();) {
			MtcInfo mtcInfo = (MtcInfo) iterator.next();
			List<MtcInfo> list;
			if (rHashMap.get(mtcInfo.getTailNumber())==null) {
				list = new ArrayList<MtcInfo>();
				list.add(mtcInfo);
			}else {
				list = rHashMap.get(mtcInfo.getTailNumber());
				list.add(mtcInfo);
			}
			rHashMap.put(mtcInfo.getTailNumber(), list);
		}
		
		return rHashMap;
	}
	
	
}
