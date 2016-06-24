package com.yc.airport.entity;

public class AircraftClosure {
	private String code;
	private long startTime;
	private long endTime;
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public long getStartTime() {
		return startTime;
	}
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	public long getEndTime() {
		return endTime;
	}
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
	public AircraftClosure(String code, long startTime, long endTime) {
		super();
		this.code = code;
		this.startTime = startTime;
		this.endTime = endTime;
	}
	public AircraftClosure() {
		super();
		// TODO Auto-generated constructor stub
	}
	@Override
	public String toString() {
		return "aircraftClosure [code=" + code + ", startTime=" + startTime
				+ ", endTime=" + endTime + "]";
	}
	
}
