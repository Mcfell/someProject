package com.yc.airport.entity;

public class FlightInfo {
	private String id;
	private long departureTime;
	private long arrivalTime;
	private String departureAirport;
	private String arrivalAirport;
	private String tailNumber;
	private boolean isinMtc;
	private MtcInfo mtcInfo;
	private int status = 1;
	
	
	
	public MtcInfo getMtcInfo() {
		return mtcInfo;
	}
	public void setMtcInfo(MtcInfo mtcInfo) {
		this.mtcInfo = mtcInfo;
	}
	public boolean isInMtc() {
		return isinMtc;
	}
	public void setMtc(boolean isinMtc) {
		this.isinMtc = isinMtc;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public long getDepartureTime() {
		return departureTime;
	}
	public void setDepartureTime(long departureTime) {
		this.departureTime = departureTime;
	}
	public long getArrivalTime() {
		return arrivalTime;
	}
	public void setArrivalTime(long arrivalTime) {
		this.arrivalTime = arrivalTime;
	}
	public String getDepartureAirport() {
		return departureAirport;
	}
	public void setDepartureAirport(String departureAirport) {
		this.departureAirport = departureAirport;
	}
	public String getArrivalAirport() {
		return arrivalAirport;
	}
	public void setArrivalAirport(String arrivalAirport) {
		this.arrivalAirport = arrivalAirport;
	}
	public String getTailNumber() {
		return tailNumber;
	}
	public void setTailNumber(String tailNumber) {
		this.tailNumber = tailNumber;
	}
	
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public FlightInfo(String id, long departureTime, long arrivalTime,
			String departureAirport, String arrivalAirport, String tailNumber) {
		super();
		this.id = id;
		this.departureTime = departureTime;
		this.arrivalTime = arrivalTime;
		this.departureAirport = departureAirport;
		this.arrivalAirport = arrivalAirport;
		this.tailNumber = tailNumber;
	}
	
	public FlightInfo(String id, long departureTime, long arrivalTime,
			String departureAirport, String arrivalAirport, String tailNumber,
			int status) {
		super();
		this.id = id;
		this.departureTime = departureTime;
		this.arrivalTime = arrivalTime;
		this.departureAirport = departureAirport;
		this.arrivalAirport = arrivalAirport;
		this.tailNumber = tailNumber;
		this.status = status;
	}
	public FlightInfo() {
		super();
		// TODO Auto-generated constructor stub
	}
	@Override
	public String toString() {
		return "FlightInfo [id=" + id + ", departureTime=" + departureTime
				+ ", arrivalTime=" + arrivalTime + ", departureAirport="
				+ departureAirport + ", arrivalAirport=" + arrivalAirport
				+ ", tailNumber=" + tailNumber + ", status=" + status + "]";
	}
	
	
	
}
