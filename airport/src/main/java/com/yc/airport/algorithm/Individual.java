package com.yc.airport.algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import com.yc.airport.entity.Aircraft;
import com.yc.airport.entity.AircraftClosure;
import com.yc.airport.entity.FlightInfo;
import com.yc.airport.entity.IdleTimeFlight;
import com.yc.airport.entity.MtcInfo;
import com.yc.airport.entity.Schedule;
import com.yc.airport.value.GloabValue;

public class Individual implements Cloneable {
	private static final Logger logger = LoggerFactory
			.getLogger(Individual.class);
	static int defaultGeneLength = GloabValue.flightAllNum
			+ GloabValue.mtcAllNum;
	// 基因序列
	private int[] genes = new int[defaultGeneLength];
	// 个体的 适应值
	private long fitness = -1;
	// 航班信息
	private Schedule schedule;
	private List<FlightInfo> flightInfos;
	private List<MtcInfo> mtcInfos;
	private int individualNum;
	
	public Individual() {
		schedule = GloabValue.schedule;
		flightInfos = schedule.getFlightInfos();
		mtcInfos = schedule.getMtcInfos();
	}
	public int getIndividualNum() {
		return this.individualNum;
	}
	/*
	 * isRandom true:创建一个随机的基因个体
	 * 			false:修正基因个体，修正成功 isLegal=true
	 */
	public void generateIndividual(int individualNum,boolean isRandom) {
		this.individualNum = individualNum;
		if (isRandom) {
			for (int i = 0; i < genes.length; i++) {
				genes[i] = 1;
			}
			init();
		}
		generateRandomMtcInfos(isRandom);
		generateRandomFlightInfos(isRandom);
		// 判断单个飞机航班行程是否连续，如不连续则进行修改
		
	}
	class SwapHelper{
		int val;
		int start;
		int end;
		String tail;
		int type;
		public SwapHelper(int val, int start, int end, String tail,int type) {
			super();
			this.val = val;
			this.start = start;
			this.end = end;
			this.tail = tail;
			this.type = type;
		}
	}
	private void checkIsContinuous() {
		HashMap<String, List<FlightInfo>> flightInfoMap = GloabValue.flightInfoMap;
		int head=0;
		//遍历每一架飞机
		for (int i = 0; i < GloabValue.TAILS.length; i++) {
			String tail = GloabValue.TAILS[i];
			if (flightInfoMap.get(tail) == null) {
				continue;
			}
			String endTailAirport = GloabValue.aircraftsMap.get(tail).getEndAvailableAirport();
			List<FlightInfo> flightInfoList= flightInfoMap.get(tail);
			int flightInfoLength = flightInfoList.size();
			//计算因机场关闭而取消的航班
			FlightInfo preFlightInfo = null;
			int s = 0;
			for (int j = flightInfoLength - 1; j >= 0; j--) {
				int genesNum = head + j;
				FlightInfo flightInfo = flightInfos.get(genesNum);
//				if (flightInfo.getId().equals("11166435")) {
//				logger.debug(flightInfo.getStatus()+" " + genes[genesNum]);
//				logger.debug(flightInfos.get(genesNum-1).getStatus()+" "+genes[genesNum-1]);
//			}
				int step1,step2;
				if (j == 0) {
					if (preFlightInfo == null || (genes[head] == 0 && !preFlightInfo.getDepartureAirport().equals(flightInfo.getArrivalAirport()))) {
						step1 = FindNextSameStAirportFlight(genesNum, head, flightInfoLength, flightInfo.getDepartureAirport(), GloabValue.DEPARTURE_TYPE);
						if (step1 == -1) {
							for (int k = 1; k < flightInfoLength - 1; k++) {
								FlightInfo flightInfo2 = flightInfos.get(genesNum + k);
								genes[genesNum + k] = 0;
								flightInfos.set(genesNum + k, flightInfo2);
							}
						}else 
							closeNextAirport(genesNum, step1);
					}else {
						preFlightInfo = flightInfo;
					}
				}else {
					if (genes[genesNum] == 1) {
						if (s == 0 && !flightInfo.getArrivalAirport().equals(endTailAirport)) {
							int pre = FindPreSameStAirportFlight(genesNum, head, flightInfoLength, endTailAirport, GloabValue.ARRIVIAL_TYPE);
							if (pre != -1) {
								closePreAirport(genesNum+1, pre+1);
							}
						}
						s++;
						int pre = FindPreSameStAirportFlight(genesNum, head, flightInfoLength, flightInfo.getDepartureAirport(), GloabValue.ARRIVIAL_TYPE);
						if (pre != -1) {
							FlightInfo tmpFlightInfo = flightInfos.get(genesNum - pre);
							if (!tmpFlightInfo.getArrivalAirport().equals(flightInfo.getDepartureAirport())){
								step1 = FindNextSameStAirportFlight(genesNum, head, flightInfoLength, tmpFlightInfo.getArrivalAirport(), GloabValue.DEPARTURE_TYPE);
								if (pre >= step1+1) {
									closeNextAirport(genesNum, step1);
								}else {
									closePreAirport(genesNum, pre);
								}
								preFlightInfo = flightInfos.get(genesNum - pre);
								
							}else {
								String depAirport = flightInfo.getDepartureAirport();
								String endAirport = flightInfo.getArrivalAirport();
								
								if (!depAirport.equals(flightInfos.get(genesNum - 1).getArrivalAirport())) {
									int pre1 = FindPreSameStAirportFlight(genesNum, head, flightInfoLength, depAirport, GloabValue.ARRIVIAL_TYPE);
									int pre2 = FindPreSameStAirportFlight(genesNum, head, flightInfoLength, endAirport, GloabValue.ARRIVIAL_TYPE);
									if (pre1 != -1 && pre2 != -1) {
										if (pre1 < pre2) {
											closePreAirport(genesNum, pre1);
										}else {
											closeNextAirport(genesNum-pre2+1,pre2);
										}
									}else if (pre1 != -1) {
										closePreAirport(genesNum, pre1);
									}else {
										closeNextAirport(genesNum-pre2+1,pre2);
									}
								}else {
									if (pre > 1) {
										closePreAirport(genesNum, pre);
									}
								}
								preFlightInfo = flightInfo;
							}
						}else {
								String depAirport = flightInfo.getDepartureAirport();
								String endAirport = flightInfo.getArrivalAirport();
								if (!depAirport.equals(flightInfos.get(genesNum - 1).getArrivalAirport())) {
									int pre1 = FindPreSameStAirportFlight(genesNum, head, flightInfoLength, depAirport, GloabValue.ARRIVIAL_TYPE);
									int pre2 = FindPreSameStAirportFlight(genesNum, head, flightInfoLength, endAirport, GloabValue.ARRIVIAL_TYPE);
									if (pre1 != -1 && pre2 != -1) {
										if (pre1 < pre2) {
											closePreAirport(genesNum, pre1);
										}else {
											closeNextAirport(genesNum-pre2+1,pre2);
										}
									}else if (pre1 != -1) {
										closePreAirport(genesNum, pre1);
									}else {
										closeNextAirport(genesNum-pre2+1,pre2);
									}
								}else {
									if (flightInfos.get(head).getDepartureAirport().equals(flightInfo.getDepartureAirport())) {
										flightInfos.get(head).setStatus(0);
										genes[head] = 0;
										j = 0;
									}else {
										int pre1 = FindPreSameStAirportFlight(genesNum, head, flightInfoLength, depAirport, GloabValue.ARRIVIAL_TYPE);
										int pre2 = FindPreSameStAirportFlight(genesNum, head, flightInfoLength, endAirport, GloabValue.ARRIVIAL_TYPE);
										if (pre1 != -1 && pre2 != -1) {
											if (pre1 < pre2) {
												closePreAirport(genesNum, pre1);
											}else {
												closeNextAirport(genesNum-pre2+1,pre2);
											}
										}else if (pre1 != -1) {
											closePreAirport(genesNum, pre1);
										}else {
											closeNextAirport(genesNum-pre2+1,pre2);
										}
//										int next1 = FindNextSameStAirportFlight(genesNum, head, flightInfoLength, flightInfos.get(head).getDepartureAirport(), GloabValue.DEPARTURE_TYPE);
//										int next2 = FindNextSameStAirportFlight(genesNum, head, flightInfoLength, flightInfos.get(head).getArrivalAirport(), GloabValue.DEPARTURE_TYPE);
//										if (next1 != -1 && next2 != -1) {
//											if (next1 > next2) {
//												closeNextAirport(genesNum, next2);
//											}else {
//												closeNextAirport(head, next1);
//											}
//										}else if (next1 != -1) {
//											closeNextAirport(head, next1);
//										}else {
//											closeNextAirport(genesNum, next2);
//										}
									}
								}
						}
						
					}
				}
			}
			
			head += flightInfoLength;
		}
	}
	private void generateRandomFlightInfos(boolean isRandom) {
		HashMap<String, List<FlightInfo>> flightInfoMap = GloabValue.flightInfoMap;
		int head = 0;
		//遍历每一架飞机
		for (int i = 0; i < GloabValue.TAILS.length; i++) {
			String tail = GloabValue.TAILS[i];
			if (flightInfoMap.get(tail) == null) {
				continue;
			}
			List<FlightInfo> flightInfoList= flightInfoMap.get(tail);
			int flightInfoLength = flightInfoList.size();
			//处理航班交换
			//有bug，待解决
//			if (!isRandom) {
//				for (int j = 0; j < flightInfoLength; j++) {
//					int n= head + j;
//					FlightInfo flightInfo = flightInfos.get(n);
//					int next = j;
//					if (flightInfo.isInMtc()) {
//						String startAirport = flightInfo.getDepartureAirport();
//						Aircraft aircraft = GloabValue.aircraftsMap.get(tail);
//						String endAirport ;
//						Long startTime1,startTime2;
//						Long endTime1,endTime2;
//						next++;
//						if (j != flightInfoLength -1) {
//							while(next<flightInfoLength){
//								if (!flightInfos.get(head + next).isInMtc()) {
//									next--;
//									break;
//								}
//								next++;
//							}
//							FlightInfo endFlightInfo = flightInfos.get(head + next);
//							endAirport = endFlightInfo.getArrivalAirport();
//							if (j == 0) {
//								startTime1 = aircraft.getStartAvailableTime();
//							}else {
//								startTime1 = flightInfos.get(n-1).getArrivalTime();
//							}
//							startTime2 = flightInfo.getDepartureTime();
//							endTime1 = endFlightInfo.getArrivalTime();
//							if (next != flightInfoLength) {
//								FlightInfo nextFlightInfo = flightInfos.get(head + next + 1);
//								endTime2 = nextFlightInfo.getDepartureTime();
//							}else {
//								endTime2 = aircraft.getEndAvailableTime();
//							}
//							j = next;
//						} else {
//							startTime1 = flightInfos.get(n-1).getArrivalTime();
//							startTime2 = flightInfo.getDepartureTime();
//							endAirport = aircraft.getEndAvailableAirport();
//							endTime1 = flightInfo.getArrivalTime();
//							endTime2 = aircraft.getEndAvailableTime();
//						}
//						//error
//						//getSwapFlight(startAirport,endAirport,startTime1,startTime2,endTime1,endTime2,n,head+j);
//					}
//				}
//			}
			
			//
			for (int j = 0; j < flightInfoLength; j++) {
				int genea = 1,n;
				n = head + j;
				FlightInfo flightInfo = flightInfos.get(n);
				if (!isRandom) {
					genea = flightInfo.getStatus();
					genes[n] = genea;
				}
				MeetCondition(n, genea, head, flightInfoLength);
			}
//			
			head += flightInfoLength;
		}
		if (!isRandom) {
			checkIsContinuous();
		}
		
	}
	
	private void generateRandomMtcInfos(boolean isRandom) {
		if (isRandom) {
			for (int j = GloabValue.flightAllNum; j < genes.length; j++) {
				int genem = (int) Math.round(Math.random());
				genes[j] = genem;
				int index = j - GloabValue.flightAllNum;
				MtcInfo mtcInfo = mtcInfos.get(index);
				if (genem == 0) {
					mtcInfo.setStatus(true); //执行维护
				}else {
					mtcInfo.setStatus(false);//取消维护
				}
				mtcInfos.set(index, mtcInfo);
			}
		}else {
			for (int j = GloabValue.flightAllNum; j < genes.length; j++) {
				int index = j - GloabValue.flightAllNum;
				MtcInfo mtcInfo = mtcInfos.get(index);
				if (genes[j] == 0) {
					mtcInfo.setStatus(true); //执行维护
				}else {
					mtcInfo.setStatus(false);//取消维护
				}
				//if(mtcInfo.getId().equals("1000038")){mtcInfo.setStatus(false);}
				mtcInfos.set(index, mtcInfo);
			}
		}
	}
	private HashMap<String, List<FlightInfo>> init() {
		HashMap<String, List<FlightInfo>> flightInfoMap = GloabValue.flightInfoMap;
		GloabValue.idleTimeMap = new HashMap<String, List<IdleTimeFlight>>();
		int head=0;
		//遍历每一架飞机
		for (int i = 0; i < GloabValue.TAILS.length; i++) {
			String tail = GloabValue.TAILS[i];
			if (flightInfoMap.get(tail) == null) {
				continue;
			}
			List<FlightInfo> flightInfoList= flightInfoMap.get(tail);
			int flightInfoLength = flightInfoList.size();
			//计算因机场关闭而取消的航班
			for (int j = 0; j < flightInfoLength; j++) {
				int n;
				n = head + j;
				FlightInfo flightInfo = flightInfos.get(n);
				if (genes[n] == 0) {
					flightInfo.setStatus(0);
					continue;
				}
				if (checkIsAirportClose(flightInfo, n , head,flightInfoLength)) {
					logger.debug(flightInfo.getId()+" closed because of airport close");
					//CloseAirportAndKeepContinue(n, head,flightInfoLength);
				}
			}
			//计算每架飞机的freeTime
			List<IdleTimeFlight> idleTimeFlights = new ArrayList<IdleTimeFlight>();
			FlightInfo preFlightInfo = null;
			for (int j = 0; j < flightInfoLength; j++) {
				FlightInfo flightInfo = flightInfos.get(j);
				Aircraft aircraft = GloabValue.aircraftsMap.get(tail);
				IdleTimeFlight idleTimeFlight;
				if (j==0) {
					idleTimeFlight = new IdleTimeFlight(flightInfo.getArrivalAirport(),
							aircraft.getStartAvailableTime(), flightInfo.getArrivalTime());
				}else {
					idleTimeFlight = new IdleTimeFlight(flightInfo.getDepartureAirport(), preFlightInfo.getDepartureTime(), flightInfo.getArrivalTime());
				}
				idleTimeFlights.add(idleTimeFlight);
				if (j == flightInfos.size()-1) {
					idleTimeFlight = new IdleTimeFlight(flightInfo.getDepartureAirport(), 
							flightInfo.getDepartureTime(), aircraft.getEndAvailableTime());
					idleTimeFlights.add(idleTimeFlight);
				}
				preFlightInfo = flightInfo;
			}
			GloabValue.idleTimeMap.put(tail, idleTimeFlights);
			
			head += flightInfoLength;
		}
		return flightInfoMap;
	}
	private void getSwapFlight(String startAirport, String endAirport,
			Long startTime1, Long startTime2, Long endTime1, Long endTime2,int startGenes,int endGenes) {
		// TODO Auto-generated method stub
		FlightInfo flightInfo = flightInfos.get(startGenes);
		HashMap<String, List<FlightInfo>> flightInfoMap = GloabValue.flightInfoMap;
		String stail = flightInfo.getTailNumber();
		MtcInfo mtcInfo = flightInfo.getMtcInfo();
		if (mtcInfo == null) {
			//logger.debug("mtcInfo is null--getSwapFlight");
			return;
		}
		int head = 0;
		int minVal = Integer.MAX_VALUE;
		SwapHelper minHelper = null;
		//遍历每一架飞机
		for (int i = 0; i < GloabValue.TAILS.length; i++) {
			String tail = GloabValue.TAILS[i];
			if (flightInfoMap.get(tail) == null || tail.equals(stail)) {
				continue;
			}
			List<FlightInfo> flightInfoList= flightInfoMap.get(tail);
			int flightInfoLength = flightInfoList.size();
			//处理航班交换
			FlightInfo preFlightInfo = null;
			
			for (int j = 0; j < flightInfoLength; j++) {
				int n= head + j;
				FlightInfo fInfo = flightInfos.get(n);
				if (fInfo.getDepartureAirport().equals(flightInfo.getDepartureAirport())
						&& fInfo.getDepartureTime() > startTime1 
						&& (preFlightInfo == null || preFlightInfo.getArrivalTime() < startTime2)) {
					if (startAirport.equals(endAirport)) {
						if(fInfo.getDepartureTime()>endTime1){
							if (endGenes - startGenes + 1 < minVal) {
								minHelper = new SwapHelper(endGenes - startGenes + 1, 0, 0, tail, 1);
							}
							return;
						}
					} else {
						int next = FindNextSameStAirportFlight(n, head, flightInfoLength, endAirport, GloabValue.ARRIVIAL_TYPE);
						Long tmpEndTime;
						if (n + next == head + flightInfoLength -1) {
							tmpEndTime = GloabValue.aircraftsMap.get(tail).getEndAvailableTime();
						}else {
							tmpEndTime = flightInfos.get(n + next +1).getDepartureTime();
						}
						FlightInfo nextFlightInfo = flightInfos.get(n + next);
						if (nextFlightInfo.getArrivalAirport().equals(endAirport)
								&& nextFlightInfo.getArrivalTime() < endTime2
								&& endTime1 > tmpEndTime) {
							int val = endGenes - startGenes + next + 2;
							if (val < minVal) {
								minHelper = new SwapHelper(val, n, n + next, tail,2);
							}
						}
					}
				}
				preFlightInfo = fInfo;
			}
			
			head += flightInfoLength;
		}
		if (minHelper != null) {
			if (minHelper.type == 1) {
				for (int k = startGenes; k <= endGenes; k++) {
					FlightInfo tmpFlightInfo = flightInfos.get(k);
					tmpFlightInfo.setStatus(1);
					tmpFlightInfo.setTailNumber(minHelper.tail);
					flightInfos.set(k, tmpFlightInfo);
				}
			}else {
				for (int k = startGenes; k <= endGenes; k++) {
					FlightInfo tmpFlightInfo = flightInfos.get(k);
					tmpFlightInfo.setStatus(1);
					tmpFlightInfo.setTailNumber(minHelper.tail);
					flightInfos.set(k, tmpFlightInfo);
				}
				for (int k = minHelper.start; k <= minHelper.end; k++) {
					FlightInfo tmpFlightInfo = flightInfos.get(k);
					tmpFlightInfo.setStatus(1);
					tmpFlightInfo.setTailNumber(stail);
					flightInfos.set(k, tmpFlightInfo);
				}
			}
			logger.debug("type:"+minHelper.type);
			logger.debug("find swap:" + stail + ":"+ startGenes+"-" + endGenes);
			logger.debug(minHelper.tail+":"+minHelper.start+"-"+minHelper.end);
		}
	}
	/*
	 * 约束条件
	 */
	private void MeetCondition(int genesNum, int type, int head, int flightInfoLength) {
		FlightInfo flight = flightInfos.get(genesNum);
		boolean flag = false;
		if (type == 1) {
			if (!checkInRecoverTime(flight)) {// 判断航班时间是否在RecoverTime内
				// 修正基因，取消航班
				flag = true;
				logger.debug(flight.getId() + "航班因不在RecoverTime而取消");
			}
			if (!checkIsOverMtc(flight,genesNum,head,flightInfoLength)) {// 飞机是否在处于维护时段
				// 修正基因，取消航班
				flag = true;
			}
			if (!checkIsOverDelay(flight, genesNum)) {// 是否超过最大延迟时间
				// 修正基因，取消航班
				logger.debug(flight.getId() + "航班因延迟超出180min而取消");
				flag = true;
			}
			if (!checkIsOverTurnTime(flight, genesNum, head)) {// 两次航班间隔是否大于最小间隔时间
				flag=true;
			}
			
			if (flag) {
				genes[genesNum] = 0;
				flight.setStatus(0);
			}else {
				genes[genesNum] = 1;
				flight.setStatus(1);
			}
//			if (flight.getId().equals("11166435")) {
//				flight.setStatus(0);
//			}
		}else {// 航班取消
			genes[genesNum] = 0;
			flight.setStatus(0);
		}
		flightInfos.set(genesNum, flight);
	}
	private boolean checkInRecoverTime(FlightInfo flight) {
		String tail = flight.getTailNumber();
		Aircraft aircraft = GloabValue.aircraftsMap.get(tail);
		long arrivalTime = flight.getArrivalTime();
		long departureTime = flight.getDepartureTime();
		if (departureTime > aircraft.getStartAvailableTime()
				&& arrivalTime < aircraft.getEndAvailableTime()) {
			return true;
		} else if (departureTime < aircraft.getStartAvailableTime()) {
			long delay = aircraft.getStartAvailableTime() - departureTime;
			if (delay<GloabValue.maxDelayTime && delay/60* GloabValue.weightFlightDelay <= GloabValue.weightCancelFlight) {
				flight.setDepartureTime(aircraft.getStartAvailableTime());
				flight.setArrivalTime(arrivalTime + delay);
				logger.info(flight.getId()+"delay "+delay);;
			}else {
				flight.setStatus(0);
				return false;
			}
		} else if (arrivalTime > aircraft.getEndAvailableTime()) {
			flight.setStatus(0);
			return false;
		}
		return true;
	}


	/*
	 * 飞机延迟时间小于3h，否则取消
	 */
	private boolean checkIsOverDelay(FlightInfo flightInfo, int genesNum) {
		FlightInfo origonFlightInfo = GloabValue.schedule.getFlightInfos().get(genesNum);
		//logger.debug(origonFlightInfo.getId());
		if(!flightInfo.getTailNumber().equals(origonFlightInfo.getTailNumber()))
			logger.info("TailNumber not equal:"+flightInfo.getTailNumber());
		if (flightInfo.getDepartureTime() - origonFlightInfo.getDepartureTime() > GloabValue.maxDelayTime) {
			flightInfo.setStatus(0);
			return false;
		}
		return true;
	}

	/*
	 * 飞机间隔时间大于30min钟
	 */
	private boolean checkIsOverTurnTime(FlightInfo fInfo, int genesNum ,int head) {
		int tmpNum = genesNum - 1;
		if (genesNum == head) {
			//logger.debug("第一趟航班");
		}else if (tmpNum >= head && getGenesType(tmpNum) > 0 ) {
			
			FlightInfo fInfo1 = flightInfos.get(tmpNum);
			long turnTime = fInfo.getDepartureTime() - fInfo1.getArrivalTime();
			if (turnTime < GloabValue.turnTime) {
				long delay = GloabValue.turnTime - turnTime;// 延时
				if (delay <= GloabValue.maxDelayTime && delay/60 * GloabValue.weightFlightDelay <= GloabValue.weightCancelFlight) {
					fInfo.setDepartureTime(fInfo1.getArrivalTime()+ GloabValue.turnTime);// 修正
					fInfo.setArrivalTime(fInfo.getArrivalTime() + delay);// 修正
					logger.debug(fInfo.getId() + "---航班因两次起飞间隔小于30min,修正起飞时间，延迟"+ delay + "s.preFlight"+flightInfos.get(genesNum-1).getId());
					fInfo.setStatus(1);
				} else {
					fInfo.setStatus(0);
					//fInfo.setMtc(true);
					setGene(genesNum, 0);
					logger.debug(fInfo.getId() + "---因起飞延迟过长："+ delay + "s,必须取消航班 --checkIsOverTurnTime");
					return false;
				}
			}
		}else if (genesNum > head && getGenesType(tmpNum) == 0) {
			int type = 0;
			while(tmpNum >= head && tmpNum-1 >= 0 && type == 0) {
				type = getGenesType(--tmpNum);
			}
			FlightInfo fInfo1 = flightInfos.get(tmpNum);
			if (type == 1 && fInfo1.getTailNumber().equals(fInfo.getTailNumber())) {
				
				long turnTime = fInfo.getDepartureTime() - fInfo1.getArrivalTime();
				if (turnTime < GloabValue.turnTime) {
					long delay = GloabValue.turnTime - turnTime;// 延时
					if (delay <= GloabValue.maxDelayTime  && delay/60* GloabValue.weightFlightDelay  < GloabValue.weightCancelFlight) {
						fInfo.setDepartureTime(fInfo1.getArrivalTime()+ GloabValue.turnTime);// 修正
						fInfo.setArrivalTime(fInfo.getArrivalTime() + delay);// 修正
						logger.debug(fInfo.getId() + "...航班因两次起飞间隔小于30min,修正起飞时间，延迟"+ delay + "s");
						fInfo.setStatus(1);
					} else {
						//fInfo.setMtc(true);
						fInfo.setStatus(0);
						setGene(genesNum, 0);
						logger.debug(fInfo.getId() + "...因起飞延迟过长："+ delay + "s,必须取消航班");
						return false;
					}
				}
			}
		}
		return true;
	}

	private boolean checkIsOverMtc(FlightInfo flight,int genesNum ,int head,int flightInfoLength){
		HashMap<String, List<MtcInfo>> mtcInfoMap = GloabValue.mtcInfoMap;
		List<MtcInfo> mtcInfos = mtcInfoMap.get(flight.getTailNumber());
		long flightInfoArrivalTime = flight.getArrivalTime();
		long flightInfoDepartureTime = flight.getDepartureTime();
		if (mtcInfos != null) {//是否在维护机场
			for (Iterator<MtcInfo> iterator = mtcInfos.iterator(); iterator.hasNext();) {//遍历
				MtcInfo mtcInfo = (MtcInfo) iterator.next();
				if (!mtcInfo.getStatus()) {
					continue;
				}
				long start = mtcInfo.getStartTime();
				long end = mtcInfo.getEndTime();
				// 飞机起飞时间在维护期间，先尝试替换飞机，后延迟起飞，否则取消航班
				if ( flight.getDepartureAirport().equals(mtcInfo.getAirport())
						&& flightInfoDepartureTime >= start
						&& flightInfoDepartureTime < end ) {
					long delay = end - flightInfoDepartureTime ;//延迟时间
					flight.setMtcInfo(mtcInfo);
					flight.setMtc(true);
					int next = FindNextSameStAirportFlight(genesNum, head, flightInfoLength, flight.getDepartureAirport(), GloabValue.DEPARTURE_TYPE);
					next = next<0?(flightInfoLength + head -genesNum):next;
					if (delay < GloabValue.maxDelayTime && delay/60* GloabValue.weightFlightDelay <= next * GloabValue.weightCancelFlight) {
						genes[genesNum] = 1;
						flight.setStatus(1);
						flight.setDepartureTime(end);
						flight.setArrivalTime(flightInfoArrivalTime+delay);
						return true;
					}else {// 取消航班
						logger.debug(flight.getId() + "航班因飞机"+flight.getTailNumber()+"起飞时段处于维护期间而取消,MtcId:"+mtcInfo.getId());
						genes[genesNum] = 0;
						flight.setStatus(0); 
						return false;
					}
				}else if (flight.getArrivalAirport().equals(mtcInfo.getAirport())
							&&flightInfoArrivalTime > start && flightInfoArrivalTime < end) 
							  {
					// 飞机飞行途中处于维护期间，先尝试替换飞机，否则取消航班
					// 取消航班
						flight.setMtcInfo(mtcInfo);
						flight.setMtc(true);
						logger.debug(flight.getId() + "航班因飞机"+flight.getTailNumber()+"在到达机场时段处于维护期间而取消,MtcId:"+mtcInfo.getId());
						genes[genesNum] = 0;
						flight.setStatus(0); 
						return false;
				}
			}
	
		}
		flight.setMtcInfo(null);
		flight.setMtc(false);
		return true;
	}
	
	private void closeNextAirport(int genesNum, int step) {
		for (int k = 0; k < step; k++) {
				FlightInfo flight = flightInfos.get(genesNum+k);
				flight.setStatus(0);
				flightInfos.set(genesNum+k, flight);
				genes[genesNum + k] = 0;
		}
	}
	private void closePreAirport(int genesNum, int step) {
		for (int k = 1; k < step; k++) {
			FlightInfo flight = flightInfos.get(genesNum - k);
			flight.setStatus(0);
			flightInfos.set(genesNum-k, flight);
			genes[genesNum - k] = 0;
		}
	}
	
	
	/*
	 * return false:没关闭，延迟后执行航班 ；true：取消航班
	 */
	private boolean checkIsAirportClose(FlightInfo flightInfo, int genesNum, int head, int flightInfosLength) {
		HashMap<String, List<AircraftClosure>> aircraftClosureMap = GloabValue.aircraftClosuresMap;
		int statue = 0;
		// boolean isClose = false;
		List<AircraftClosure> aircraftClosures = null;
		String stAirportString = flightInfo.getDepartureAirport();
		long flightInfoArrivalTime = flightInfo.getArrivalTime();
		long flightInfoDepartureTime = flightInfo.getDepartureTime();
		if ((aircraftClosures = aircraftClosureMap.get(stAirportString)) != null) {
			// 起飞机场如果为关闭机场
			// 判断时间是否在关闭时间段
			//不存在FlightSwap
			for (Iterator<AircraftClosure> iterator = aircraftClosures.iterator(); iterator.hasNext();) {
				AircraftClosure aircraftClosure = (AircraftClosure) iterator.next();
				long start = aircraftClosure.getStartTime();
				long end = aircraftClosure.getEndTime();
				if (flightInfoDepartureTime >= start && flightInfoDepartureTime < end) {
					long delay = end - flightInfoDepartureTime;
					if (delay < GloabValue.maxDelayTime) {
						if (genesNum !=head) {
							if (delay/60* GloabValue.weightFlightDelay <= GloabValue.weightCancelFlight) {
								logger.debug(flightInfo.getId() + " 因起飞机场："
										+ flightInfo.getDepartureAirport() + "关闭,"
										+ "延迟" + delay + "s起飞,起飞时间：" + end + "s");
								flightInfo.setDepartureTime(end);
								flightInfo.setArrivalTime(flightInfoArrivalTime + delay);
								flightInfo.setStatus(1);
								return false;
							}
						}else { //第一趟航班
							logger.debug(flightInfo.getId() + " 因起飞机场："
									+ flightInfo.getDepartureAirport() + "关闭,"
									+ "延迟" + delay + "s起飞,起飞时间：" + end + "s");
							flightInfo.setDepartureTime(end);
							flightInfo.setArrivalTime(flightInfoArrivalTime + delay);
							flightInfo.setStatus(1);
							return false;
						}
					} 
					flightInfo.setStatus(0);
					logger.debug(flightInfo.getId() + " 因起飞机场："
							+ flightInfo.getDepartureAirport()
							+ "关闭且无法SWAP航班,必须取消航班"+",delay"+delay);
					return true;
				}
			}
		} else if ((aircraftClosures = aircraftClosureMap.get(flightInfo.getArrivalAirport())) != null) {
			// 到达机场如果为关闭机场
			// 判断时间是否在关闭时间段
			for (Iterator iterator = aircraftClosures.iterator(); iterator.hasNext();) {
				AircraftClosure aircraftClosure = (AircraftClosure) iterator.next();
				long start = aircraftClosure.getStartTime();
				long end = aircraftClosure.getEndTime();
				if (flightInfoArrivalTime >= start && flightInfoArrivalTime < end) {
					 long delay = end - flightInfoArrivalTime;
					 if (delay < GloabValue.maxDelayTime
							) {
						logger.debug(flightInfo.getId() + " 因到达机场："
								+ flightInfo.getArrivalAirport() + "关闭" + ",延迟"
								+ delay + "s起飞,起飞时间："
								+ (flightInfoArrivalTime + delay) + "s");
						flightInfo.setDepartureTime(flightInfoDepartureTime+ delay);
						flightInfo.setArrivalTime(end);
						flightInfo.setStatus(1);
						return false;
					} else {
						flightInfo.setStatus(0);
						logger.debug(flightInfo.getId() + " 因到达机场："
								+ flightInfo.getArrivalAirport() + "关闭,取消航班"+", delay"+delay);
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private int CloseAirportAndKeepContinue(int genesNum, int head,
			int flightInfoLength) {
		FlightInfo flightInfo = flightInfos.get(genesNum);
		String depAirport = flightInfo.getDepartureAirport();
		String endAirport = flightInfo.getArrivalAirport();
		
		if (!depAirport.equals(flightInfos.get(genesNum - 1).getArrivalAirport())) {
			int pre1 = FindPreSameStAirportFlight(genesNum, head, flightInfoLength, depAirport, GloabValue.ARRIVIAL_TYPE);
			int pre2 = FindPreSameStAirportFlight(genesNum, head, flightInfoLength, endAirport, GloabValue.ARRIVIAL_TYPE);
			if (pre1 != -1 && pre2 != -1) {
				return Math.max(pre1, pre2);
			}else if (pre1 != -1) {
				return pre2;
			}else {
				return pre1;
			}
		}
		return -1;
	}
	
	private int FindNextSameStAirportFlight(int genesNum, int head,
			int flightInfoLength, String ArrivialAirport, int searchType) {
		int next = genesNum + 1;
		boolean flag = false;
		if (searchType == GloabValue.DEPARTURE_TYPE) {
			while(next < head+flightInfoLength){
				if (genes[next]==1 && flightInfos.get(next).getDepartureAirport().equals(ArrivialAirport)){ 
					flag = true;
					break;
				}
			    next++;
			}
		}else {
			while(next < head+flightInfoLength){
				if (genes[next]==1 && flightInfos.get(next).getArrivalAirport().equals(ArrivialAirport)){
					flag = true;
					break;
				}
			    next++;
			}
		}
		if (flag) {
			return next - genesNum;
		}else {
			return -1;
		}
//		if (next >= head + flightInfoLength) {
//			next = head + flightInfoLength ;
//		}
	}
	
	private int FindPreSameStAirportFlight(int genesNum, int head,
			int flightInfoLength, String departureAirport, int searchType) {
		int pre = genesNum - 1;
		boolean flag = false;
		if (searchType == GloabValue.DEPARTURE_TYPE) {
			while(pre >= head ){
				if (genes[pre] == 1 && flightInfos.get(pre).getDepartureAirport().equals(departureAirport)) {
					flag = true;	
					break;
				}
				pre--;
			}
		}else {
			while(pre >= head ){
				if (genes[pre]==1 && flightInfos.get(pre).getArrivalAirport().equals(departureAirport)) {
					flag = true;	
					break;
				}
				pre--;
			}
		}
		if (flag) {
			return genesNum - pre;
		}else {
			return -1;
		}
	}

	private int getGenesType(int i) {
		return genes[i];
	}

	public long getFitness() {
		if (fitness == -1) {
			fitness = FitnessCalc.getFitness(this);
		}
		return fitness;
	}

	public void setFitness(long fitness) {
		this.fitness = fitness;
	}

	public int getGene(int index) {
		return genes[index];
	}

	public int[] getFlightGene() {
		int[] flightGene = new int[GloabValue.flightAllNum];
		flightGene = Arrays.copyOf(genes, GloabValue.flightAllNum);
		return flightGene;
	}

	public int[] getMtcGene() {
		int[] mtcGene = new int[GloabValue.mtcAllNum];
		mtcGene = Arrays.copyOfRange(genes, GloabValue.flightAllNum,
				defaultGeneLength);
		return mtcGene;
	}

	public void printGenesInfo() {
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append("flightGenes:");
		sBuffer.append("[");
		for (int i = 0; i < GloabValue.flightAllNum; i++) {
			sBuffer.append(getGenesType(i));
			//sBuffer.append("("+i+")");
			if (i!=GloabValue.flightAllNum-1) {
				sBuffer.append("||");
			}
		}
		sBuffer.append("]");
		int[] mtcGenes = getMtcGene();
		logger.info(sBuffer.toString());
		sBuffer = new StringBuffer();
		sBuffer.append("mtcGenes:");
		sBuffer.append("[");
		for (int i = 0; i < GloabValue.mtcAllNum; i++) {
			sBuffer.append(mtcGenes[i]);
			//sBuffer.append("("+i+")");
			if (i!=GloabValue.mtcAllNum-1) {
				sBuffer.append("||");
			}
		}
		sBuffer.append("]");
		logger.info(sBuffer.toString());
	}
	public void printSchedualInfo(boolean isOrgin) {
		HashMap<String, List<FlightInfo>> scheduleMap = schedule.getPartitionFlightInfoByTail();
		for (int i = 0; i < GloabValue.TAILS.length; i++) {
			String tail = GloabValue.TAILS[i];
			if (scheduleMap.get(tail) == null) {
				continue;
			}
			List<FlightInfo> flightInfos = scheduleMap.get(tail);
			StringBuffer sBuffer = new StringBuffer();
			sBuffer.append("flightInfo:");
			sBuffer.append(tail);
			sBuffer.append("	");
			for (Iterator iterator = flightInfos.iterator(); iterator.hasNext();) {
				FlightInfo flightInfo = (FlightInfo) iterator.next();
				if (isOrgin) {
					sBuffer.append(flightInfo.getDepartureAirport());
					sBuffer.append("->");
					sBuffer.append(flightInfo.getArrivalAirport());
					sBuffer.append("("+flightInfo.getStatus()+","+flightInfo.getId()+")");
					//sBuffer.append("("+flightInfo.getStatus()+","+flightInfo.getId()+","+flightInfo.getDepartureTime()+","+flightInfo.getArrivalTime()+")");
				}else {
					if (flightInfo.getStatus()>0) {
						sBuffer.append(flightInfo.getDepartureAirport());
						sBuffer.append("->");
						sBuffer.append(flightInfo.getArrivalAirport());
						sBuffer.append("("+flightInfo.getStatus()+","+flightInfo.getId()+")");
					}
				}
				sBuffer.append("||");
			}
			logger.info(sBuffer.toString());
		}
		for (int i = 0; i < GloabValue.mtcAllNum; i++) {
			StringBuffer sBuffer = new StringBuffer();
			MtcInfo mtcInfo = mtcInfos.get(i);
			sBuffer.append("mtcInfo:");
			sBuffer.append(mtcInfo.getId());
			sBuffer.append("    "+mtcInfo.getTailNumber()+":");
			sBuffer.append(mtcInfo.getStatus());
			logger.info(sBuffer.toString());
		}
	}
	public int size() {
		return genes.length;
	}

	public void setGene(int i, int gene) {
		genes[i] = gene;
	}

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
	
	public int[] getGenes() {
		return genes;
	}
	public void setGenes(int[] genes) {
		this.genes = genes;
	}
	@Override
	public String toString() {
		return  Arrays.toString(genes);
	}
	@Override
	protected Object clone() throws CloneNotSupportedException {
		Individual individual =	(Individual)super.clone();
		List<FlightInfo> flightInfos = new ArrayList<FlightInfo>(GloabValue.flightAllNum);
		List<MtcInfo> mtcInfos = new ArrayList<MtcInfo>(GloabValue.mtcAllNum);
		individual.setGenes(genes.clone());
		flightInfos.addAll(this.flightInfos);
		mtcInfos.addAll(this.mtcInfos);
		individual.setFlightInfos(flightInfos);
		individual.setMtcInfos(mtcInfos);
		return individual;
	}
	
}
