package com.yc.airport.algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private boolean isLegal = true;
    private static double swapk =  0.0f;
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
		}
		generateRandomMtcInfos(isRandom);
		generateRandomFlightInfos(isRandom);
		// 判断单个飞机航班行程是否连续，如不连续则进行修改
		//checkIsContinuous();
		//logger.debug(individualNum+" isLegal:"+ isLegal);
	}
	private void generateRandomFlightInfos(boolean isRandom) {
		HashMap<String, List<FlightInfo>> flightInfoMap = GloabValue.flightInfoMap;
		init(isRandom);
		int head = 0;
		//遍历每一架飞机
		for (int i = 0; i < GloabValue.TAILS.length; i++) {
			String tail = GloabValue.TAILS[i];
			List<FlightInfo> flightInfoList= flightInfoMap.get(tail);
			int flightInfoLength = flightInfoList.size();
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
			head += flightInfoLength;
		}
	}
	private HashMap<String, List<FlightInfo>> init(boolean isRandom) {
		HashMap<String, List<FlightInfo>> flightInfoMap = GloabValue.flightInfoMap;
		int head=0;
		//遍历每一架飞机
		if (isRandom) {
			for (int i = 0; i < GloabValue.TAILS.length; i++) {
				String tail = GloabValue.TAILS[i];
				List<FlightInfo> flightInfoList= flightInfoMap.get(tail);
				int flightInfoLength = flightInfoList.size();
				for (int j = 0; j < flightInfoLength; j++) {
					int c,genea,n;
					n = head + j;
					FlightInfo flightInfo = flightInfos.get(n);
					if (checkIsAirportClose(flightInfo, n , head,flightInfoLength)) {
						//	logger.debug(flight.getId() + "航班因飞机场关闭取消");
					}
				}
				head += flightInfoLength;
			}
		}
		return flightInfoMap;
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
				mtcInfos.set(index, mtcInfo);
			}
		}
	}

	private void generateIdleTime() {
		HashMap<String, List<FlightInfo>> flightInfoMap = GloabValue.flightInfoMap;
		GloabValue.idleTimeMap = new HashMap<String, List<IdleTimeFlight>>();
		for (int i = 0; i < GloabValue.TAILS.length; i++) {
			String tailNumber = GloabValue.TAILS[i];
			List<FlightInfo> flightInfos = flightInfoMap.get(tailNumber);
			List<IdleTimeFlight> idleTimeFlights = new ArrayList<IdleTimeFlight>();
			FlightInfo preFlightInfo = null;
			for (int j = 0; j < flightInfos.size(); j++) {
				FlightInfo flightInfo = flightInfos.get(j);
				Aircraft aircraft = GloabValue.aircraftsMap.get(tailNumber);
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
			GloabValue.idleTimeMap.put(tailNumber, idleTimeFlights);
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
			if (!checkIsOverMtc(flight,genesNum)) {// 飞机是否在处于维护时段
				// 修正基因，取消航班
				logger.debug(flight.getId() + "航班因飞机起飞时段处于维护期间而取消");
				flag = true;
			}
			if (checkIsAirportClose(flight, genesNum , head,flightInfoLength)) {
				// 修正基因，取消航班
			//	logger.debug(flight.getId() + "航班因飞机场关闭取消");
				flag = true;
			}
//			
//			if (!checkIsAirpotContinuous(genesNum, head, flightInfoLength)) {
//				flag = true;
//			}
			
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
		}else {// 航班取消
			flag = true;
			genes[genesNum] = 0;
			flight.setStatus(0);
		}
		flightInfos.set(genesNum, flight);
	}
	private boolean checkIsAirpotContinuous(int genesNum, int head, int flightInfosLength) {
		// TODO Auto-generated method stub
		if (genesNum == head) {
			return true;
		}else {
			FlightInfo flightInfo = flightInfos.get(genesNum);
			FlightInfo preFlightInfo = flightInfos.get(genesNum - 1);
			if (!flightInfo.getDepartureAirport().equals(preFlightInfo.getArrivalAirport())) {
				
//				logger.debug(flightInfo.getId()+" is Not continuous. preID:"+preFlightInfo.getId()
//						+"."+flightInfo.getDepartureAirport()+"/"+preFlightInfo.getArrivalAirport());
				int stepNext = FindNextSameStAirportFlight(genesNum, head,
						flightInfosLength, flightInfo.getArrivalAirport());
				int stepPre =  FindPreSameStAirportFlight(genesNum, head,
						flightInfosLength, flightInfo.getDepartureAirport());
				if (stepNext > stepPre) {
					for (int i = 0; i <= stepPre; i++) {
						genes[genesNum - i] = 0;
					}
				}else {
					for (int i = 0; i <= stepNext; i++) {
						genes[genesNum + i] = 0;
					}
				}
				return false;
			}
		}
		return true;
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
		}else if (genesNum > head && getGenesType(tmpNum) > 0) {
			FlightInfo fInfo1 = flightInfos.get(tmpNum);
			long turnTime = fInfo.getDepartureTime() - fInfo1.getArrivalTime();
			if (turnTime < GloabValue.turnTime) {
				long delay = GloabValue.turnTime - turnTime;// 延时
				if (delay <= GloabValue.maxDelayTime && delay/60 * GloabValue.weightFlightDelay <= GloabValue.weightCancelFlight) {
					fInfo.setDepartureTime(fInfo1.getArrivalTime()+ GloabValue.turnTime);// 修正
					fInfo.setArrivalTime(fInfo.getArrivalTime() + delay);// 修正
					logger.debug(fInfo.getId() + "...航班因两次起飞间隔小于30min,修正起飞时间，延迟"+ delay + "s.preFlight"+flightInfos.get(genesNum-1).getId());
					fInfo.setStatus(1);
				} else {
					fInfo.setStatus(0);
					setGene(genesNum, 0);
					logger.debug(fInfo.getId() + " 因起飞延迟过长："+ delay + "s,必须取消航班");
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
						logger.debug(fInfo.getId() + "---航班因两次起飞间隔小于30min,修正起飞时间，延迟"+ delay + "s");
						fInfo.setStatus(1);
					} else {
						fInfo.setStatus(0);
						logger.debug(fInfo.getId() + " 因起飞延迟过长："+ delay + "s,必须取消航班");
						return false;
					}
				}
			}
		}
		return true;
	}

	private boolean checkIsOverMtc(FlightInfo flight,int genesNum){
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
				if (flightInfoDepartureTime >= start && flightInfoDepartureTime <= end) {
					long delay = end - flightInfoDepartureTime;//延迟时间
					/*if (getSwapFlight()) {// 替换飞机
						genes[genesNum] = 1;
						flight.setStatus(2);
					}else */if (delay<GloabValue.maxDelayTime && delay/60* GloabValue.weightFlightDelay <= GloabValue.weightCancelFlight) {
						genes[genesNum] = 1;
						flight.setStatus(1);
						flight.setDepartureTime(end);
						flight.setArrivalTime(flightInfoArrivalTime+delay);
					}else {// 取消航班
						genes[genesNum] = 0;
						flight.setStatus(0); //--------------
						return false;
					}
				}
			}
	
		}
		return true;
	}


	/*
	 * 判断单个飞机航班行程是否连续，如不连续则进行修改
	 */
	/*private void checkIsContinuous() {
		FlightInfo lastFlightInfo = null;
		FlightInfo nextFlightInfo = null;
		
		for (int i = 0; i < flightInfos.size(); i++) {
			FlightInfo flightInfo = flightInfos.get(i);
			if (flightInfo.getStatus() == 0) {
				if (lastFlightInfo == null) {
					int statue = 0;
					while (statue == 0 && i<flightInfos.size()-1) {
						lastFlightInfo = flightInfos.get(++i);
						statue = lastFlightInfo.getStatus();
					}
					if (i==flightInfos.size()-1) {
						return;
					}
				}
				
				//飞机相同
				if (nextFlightInfo.getTailNumber().equals(lastTail)) {
					//如果连续的两趟可执行航班降落点和起飞点不同
					if (!lastFlightInfo.getArrivalAirport().equals(nextFlightInfo.getDepartureAirport())) {
						
						lastFlightInfo.setArrivalAirport(nextFlightInfo.getDepartureAirport());
						this.isLegal = false;
					}
				}else {
					lastFlightInfo = nextFlightInfo;
					lastTail = nextFlightInfo.getTailNumber();
				}
			}else {
				nextFlightInfo = flightInfos.get(i);
				//飞机相同
				if (nextFlightInfo.getTailNumber().equals(lastTail)) {
					//如果连续的两趟可执行航班降落点和起飞点不同
					if (!lastFlightInfo.getArrivalAirport().equals(
							nextFlightInfo.getDepartureAirport())) {
						this.isLegal = false;
					}
				}else {
					lastFlightInfo = nextFlightInfo;
					lastTail = nextFlightInfo.getTailNumber();
				}
			}
		}
	}*/
	
	public void checkIsContinuous() {
		HashMap<String, List<FlightInfo>> flightInfoMap = GloabValue.flightInfoMap;
		int head=0;
		//遍历每一架飞机
		for (int i = 0; i < GloabValue.TAILS.length; i++) {
			String tail = GloabValue.TAILS[i];
			List<FlightInfo> flightInfoList= flightInfoMap.get(tail);
			int flightInfoLength = flightInfoList.size();
			boolean flag = false;
			FlightInfo lastflightInfo = null;
			for (int j = flightInfoLength-1 ; j >=0; j--) {
				int index = head+j;
				FlightInfo  flightInfo = flightInfos.get(index);
				if (getGenesType(index)==0) {
					flightInfo.setStatus(0);
					if (!flag) {
						flag = true;
						lastflightInfo = flightInfo;
					}
					flightInfos.set(index, flightInfo);
				}else {
					if (flag) {
						if (flightInfo.getDepartureAirport().equals(lastflightInfo.getArrivalAirport())) {
							flightInfo.setStatus(0);
							flightInfos.set(index, flightInfo);
						}else {
							flightInfo.setArrivalAirport(lastflightInfo.getArrivalAirport());
							flag = false;
						}
					}else {
						lastflightInfo = null;
					}
				}
				if (!flightInfoList.get(j).getId().equals(flightInfo.getId())) {
					logger.warn(flightInfo.getId()+"is not equal");
				}
			}
			head += flightInfoLength;
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
							int step = FindNextSameStAirportFlight(genesNum, head,
									flightInfosLength, stAirportString);
							if (delay/60* GloabValue.weightFlightDelay <= (step+1)*GloabValue.weightCancelFlight) {
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
					 int step = FindNextSameStAirportFlight(genesNum, head,
								flightInfosLength, stAirportString);
					 if (delay/60* GloabValue.weightFlightDelay <= (step+1)*GloabValue.weightCancelFlight) {
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
			int flightInfosLength) {
		FlightInfo flightInfo = flightInfos.get(genesNum);
		int stepNext = FindNextSameStAirportFlight(genesNum, head,
				flightInfosLength, flightInfo.getArrivalAirport());
		int stepPre =  FindPreSameStAirportFlight(genesNum, head,
				flightInfosLength, flightInfo.getDepartureAirport());
		if (stepNext > stepPre) {
			for (int i = 0; i <= stepPre; i++) {
				FlightInfo flight = flightInfos.get(genesNum-i);
				flight.setStatus(0);
				genes[genesNum - i] = 0;
			}
			return stepPre;
		}else {
			for (int i = 0; i <= stepNext; i++) {
				FlightInfo flight = flightInfos.get(genesNum+i);
				flight.setStatus(0);
				genes[genesNum + i] = 0;
			}
			return stepNext;
		}
		
	}
	
	private int FindNextSameStAirportFlight(int genesNum, int head,
			int flightInfosLength, String ArrivialAirport) {
		int next = genesNum + 1;
		boolean flag = true;
		while(next < head+flightInfosLength && !flightInfos.get(next).getDepartureAirport().equals(ArrivialAirport)){
			
			next++;
		}
		return next - genesNum;
	}
	
	private int FindPreSameStAirportFlight(int genesNum, int head,
			int flightInfosLength, String departureAirport) {
		int pre = genesNum - 1;
		boolean flag = true;
		while(pre >= head ){
			if (flightInfos.get(pre).getArrivalAirport().equals(departureAirport)) {
				if (genes[pre] != 0) 
					break;
			}
			pre--;
		}
		return genesNum - pre;
	}

	private boolean checkIsInSwap(FlightInfo flight, int genesNum) {
		return getSwapFlight(flight,genesNum);
	}


	/*
	 * 获取替换航班
	 */
	private boolean getSwapFlight(FlightInfo flight,int genesNum) {
		// --
		
		return false;
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
					sBuffer.append("("+flightInfo.getStatus()+","+flightInfo.getId()+","+flightInfo.getDepartureTime()+","+flightInfo.getArrivalTime()+")");
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
