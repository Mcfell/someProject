package com.yc.airport.entity;

public class MtcInfo {
	private String id;
	private long startTime;
	private long endTime;
	private String airport;
	private String tailNumber;
	private boolean status;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
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
	public String getAirport() {
		return airport;
	}
	public void setAirport(String airport) {
		this.airport = airport;
	}
	public String getTailNumber() {
		return tailNumber;
	}
	public void setTailNumber(String tailNumber) {
		this.tailNumber = tailNumber;
	}
	
	public boolean getStatus() {
		return status;
	}
	public void setStatus(boolean status) {
		this.status = status;
	}
	public MtcInfo(String id, long startTime, long endTime, String airport,
			String tailNumber, boolean status) {
		super();
		this.id = id;
		this.startTime = startTime;
		this.endTime = endTime;
		this.airport = airport;
		this.tailNumber = tailNumber;
		this.status = status;
	}
	public MtcInfo(String id, long startTime, long endTime, String airport,
			String tailNumber) {
		super();
		this.id = id;
		this.startTime = startTime;
		this.endTime = endTime;
		this.airport = airport;
		this.tailNumber = tailNumber;
	}
	
	@Override
	public String toString() {
		return "MtcInfo [id=" + id + ", startTime=" + startTime + ", endTime="
				+ endTime + ", airport=" + airport + ", tailNumber="
				+ tailNumber + "]";
	}
	
}
