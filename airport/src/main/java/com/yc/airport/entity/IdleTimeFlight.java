package com.yc.airport.entity;

public class IdleTimeFlight {

	//停留机场
	private String stAirport;
	//空闲开始时间
	private Long stTime;
	//空闲结束时间
	private Long edTime;
	public String getStAirport() {
		return stAirport;
	}
	public void setStAirport(String stAirport) {
		this.stAirport = stAirport;
	}
	public Long getStTime() {
		return stTime;
	}
	public void setStTime(Long stTime) {
		this.stTime = stTime;
	}
	public Long getEdTime() {
		return edTime;
	}
	public void setEdTime(Long edTime) {
		this.edTime = edTime;
	}
	public IdleTimeFlight(String stAirport, Long stTime,
			Long edTime) {
		super();
		this.stAirport = stAirport;
		this.stTime = stTime;
		this.edTime = edTime;
	}
	
}
