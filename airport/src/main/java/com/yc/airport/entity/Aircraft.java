package com.yc.airport.entity;

public class Aircraft {
	private String tailNumber;
	private long startAvailableTime;
	private long endAvailableTime;
	private String startAvailableAirport;
	private String endAvailableAirport;
	public String getTailNumber() {
		return tailNumber;
	}
	public void setTailNumber(String tailNumber) {
		this.tailNumber = tailNumber;
	}
	
	public long getStartAvailableTime() {
		return startAvailableTime;
	}
	public void setStartAvailableTime(long startAvailableTime) {
		this.startAvailableTime = startAvailableTime;
	}
	public long getEndAvailableTime() {
		return endAvailableTime;
	}
	public void setEndAvailableTime(long endAvailableTime) {
		this.endAvailableTime = endAvailableTime;
	}
	public String getStartAvailableAirport() {
		return startAvailableAirport;
	}
	public void setStartAvailableAirport(String startAvailableAirport) {
		this.startAvailableAirport = startAvailableAirport;
	}
	public String getEndAvailableAirport() {
		return endAvailableAirport;
	}
	public void setEndAvailableAirport(String endAvailableAirport) {
		this.endAvailableAirport = endAvailableAirport;
	}
	public Aircraft(String tailNumber, long startAvailableTime,
			long endAvailableTime, String startAvailableAirport,
			String endAvailableAirport) {
		super();
		this.tailNumber = tailNumber;
		this.startAvailableTime = startAvailableTime;
		this.endAvailableTime = endAvailableTime;
		this.startAvailableAirport = startAvailableAirport;
		this.endAvailableAirport = endAvailableAirport;
	}
	public Aircraft() {
		super();
		// TODO Auto-generated constructor stub
	}
	@Override
	public String toString() {
		return "Aircraft [tailNumber=" + tailNumber + ", startAvailableTime="
				+ startAvailableTime + ", endAvailableTime=" + endAvailableTime
				+ ", startAvailableAirport=" + startAvailableAirport
				+ ", endAvailableAirport=" + endAvailableAirport + "]";
	}
	
}
