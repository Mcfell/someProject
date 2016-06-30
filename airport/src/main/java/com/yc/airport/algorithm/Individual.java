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
import com.yc.airport.entity.MtcInfo;
import com.yc.airport.entity.Schedule;
import com.yc.airport.value.GloabValue;

public class Individual implements Cloneable {
	private static final Logger logger = LoggerFactory
			.getLogger(Individual.class);
	static int defaultGeneLength = GloabValue.flightAllNum * 2
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
	/*
	 * isRandom true:创建一个随机的基因个体
	 * 			false:修正基因个体，修正成功 isLegal=true
	 */
	public void generateIndividual(int individualNum,boolean isRandom) {
		this.individualNum = individualNum;
		generateRandomMtcInfos(isRandom);
		generateRandomFlightInfos(isRandom);
		// 判断单个飞机航班行程是否连续，如不连续则进行修改
		//checkIsContinuous();
		//logger.debug(individualNum+" isLegal:"+ isLegal);
	}
	private void generateRandomFlightInfos(boolean isRandom) {
		HashMap<String, List<FlightInfo>> flightInfoMap = GloabValue.flightInfoMap;
		int n = 0; 
		int head=0;
		//遍历每一架飞机
		for (int i = 0; i < GloabValue.TAILS.length; i++) {
			String tail = GloabValue.TAILS[i];
			List<FlightInfo> flightInfoList= flightInfoMap.get(tail);
			int flightInfoLength = flightInfoList.size();
			for (int j = 0; j < flightInfoLength; j++) {
				int c,genea,geneb;
				if (isRandom) {
						genea = (int) Math.round(Math.random());
						geneb = (int) Math.round(Math.random());
						genes[n] = genea;
						genes[++n] = geneb;
						c = genea + geneb;
				}else {
					 genea = genes[n];
					 geneb = genes[++n];
					 c = genea + geneb;
				}
				int geneNum = n/2;
				MeetCondition(head+j, c, head);
				n++;
				flightInfos.get(head+j).setStatus(getGenesType(head+j));
			}
			head += flightInfoLength;
		}
	}
	private void generateRandomMtcInfos(boolean isRandom) {
		if (isRandom) {
			for (int j = GloabValue.flightAllNum * 2; j < genes.length; j++) {
				int genem = (int) Math.round(Math.random());
				genes[j] = genem;
				int index = j - GloabValue.flightAllNum * 2;
				MtcInfo mtcInfo = mtcInfos.get(index);
				if (genem == 0) {
					mtcInfo.setStatus(true); //执行维护
				}else {
					mtcInfo.setStatus(false);//取消维护
				}
				mtcInfos.set(index, mtcInfo);
			}
		}else {
			for (int j = GloabValue.flightAllNum * 2; j < genes.length; j++) {
				int index = j - GloabValue.flightAllNum * 2;
				int genem = getMtcGene()[index];
				MtcInfo mtcInfo = mtcInfos.get(index);
				if (genem == 0) {
					mtcInfo.setStatus(true); //执行维护
				}else {
					mtcInfo.setStatus(false);//取消维护
				}
				mtcInfos.set(index, mtcInfo);
			}
		}
	}

	/*
	 * 约束条件
	 */
	private void MeetCondition(int genesNum, int type , int head) {
		FlightInfo flight = flightInfos.get(genesNum);
		boolean flag = false;
		if (type == 1) {
			if (!checkInRecoverTime(flight)) {// 判断航班时间是否在RecoverTime内
				flag = true;
			}
			if (!checkIsOverDelay(flight, genesNum)) {// 是否超过最大延迟时间
				logger.debug(flight.getId() + " 航班因延迟超出180min而取消");
				flag = true;
			}
			if (!checkIsOverTurnTime(flight, genesNum, head)) {// 两次航班间隔是否大于最小间隔时间
				flag=true;
			}
			if (!checkIsOverMtc(flight,genesNum)) {// 飞机是否在处于维护时段
				flag = true;
			}
			int statue = checkIsAirportClose(flight);// 飞机场是否关闭
			if (statue == 1) {
				flag = true;
			}
			if (flag) {
				genes[2 * genesNum] = 0;
				genes[2 * genesNum + 1] = 0;
			}else {
				flight.setStatus(1);
			}
		} else if (type == 2) {// 航班飞机Swap
			if (!checkIsInSwap(flight,genesNum)) {
				flag=true;
			}
			if (flag) {
				//logger.debug(flight.getId()+"flight swap cancal");
				genes[2 * genesNum] = 0;
				genes[2 * genesNum + 1] = 0;
				flight.setStatus(0);
			}else {
				genes[2 * genesNum] = 1;
				genes[2 * genesNum + 1] = 1;
				flight.setStatus(2);
			}
		} else {// 航班取消
			flag = true;
			genes[2 * genesNum] = 0;
			genes[2 * genesNum + 1] = 0;
			flight.setStatus(0);
			//logger.debug("flight cancal");
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
			if (delay < GloabValue.maxDelayTime && delay/60  < GloabValue.weightCancelFlight) {
				flight.setDepartureTime(aircraft.getStartAvailableTime());
				flight.setArrivalTime(arrivalTime + delay);
				logger.debug(flight.getId() + " 航班因起飞时间小于Recover Start Time,延迟"+ delay + "s起飞");
			}else {
				logger.debug(flight.getId() + " 航班因起飞时间远小于Recover Start Time,取消航班");
				flight.setStatus(0);
				return false;
			}
		} else if (arrivalTime > aircraft.getEndAvailableTime()) {
			logger.debug(flight.getId() + " 航班因到达时间大于Recover End Time,取消航班");
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
		if (flightInfo.getDepartureTime() - origonFlightInfo.getDepartureTime() > GloabValue.maxDelayTime) {
		
			flightInfo.setStatus(0);
			return false;
		}
		return true;
	}


	/*
	 * 飞机间隔时间在30min钟内
	 */
	private boolean checkIsOverTurnTime(FlightInfo fInfo, int genesNum ,int head) {
		int tmpNum = genesNum - 1;
		if (genesNum > head && getGenesType(tmpNum) > 0) {
			FlightInfo fInfo1 = flightInfos.get(tmpNum);
			long turnTime = fInfo.getDepartureTime() - fInfo1.getArrivalTime();
			if (turnTime <= GloabValue.turnTime) {
				long delay = GloabValue.turnTime - turnTime;// 延时
				if (delay < GloabValue.maxDelayTime) {
					fInfo.setDepartureTime(fInfo1.getArrivalTime()
							+ GloabValue.turnTime);// 修正
					fInfo.setArrivalTime(fInfo.getArrivalTime() + delay);// 修正
					logger.debug(fInfo.getId() + " 航班因两次起飞间隔小于30min,修正起飞时间，延迟"+ delay + "s");
					fInfo.setStatus(1);
				} else {
					fInfo.setStatus(0);
					logger.debug(fInfo.getId() + " 航班因起飞延迟过长："+ delay + "s,必须取消航班");
					return false;
				}
			}
		} else if (genesNum == head) {
		//	logger.debug("第一趟航班");
		} else if (getGenesType(tmpNum) == 0) {
			//logger.debug("genesNum:"+tmpNum);
			//logger.debug(individualNum+":" + flightInfos.get(tmpNum).getId() +"航班取消");
			int type = 0;
			while(type>0) {
				if (tmpNum==head) {
					break;
				}
				type = getGenesType(--tmpNum);
			}
			FlightInfo fInfo1 = flightInfos.get(tmpNum);
			long turnTime = fInfo.getDepartureTime() - fInfo1.getArrivalTime();
			if (turnTime < GloabValue.turnTime) {
				long delay = GloabValue.turnTime - turnTime;// 延时
				if (delay < GloabValue.maxDelayTime  && delay/60  < GloabValue.weightCancelFlight) {
					fInfo.setDepartureTime(fInfo1.getArrivalTime()+ GloabValue.turnTime);// 修正
					fInfo.setArrivalTime(fInfo.getArrivalTime() + delay);// 修正
					logger.debug(fInfo.getId() + "航班因两次起飞间隔小于30min,修正起飞时间，延迟"+ delay + "s");
					fInfo.setStatus(1);
				} else {
					fInfo.setStatus(0);
					logger.debug(fInfo.getId() + " 航班因起飞延迟过长："+ delay + "s,必须取消航班");
					return false;
				}
			}
		}
		return true;
	}

	private boolean checkIsOverMtc(FlightInfo flight,int genesNum) {
		HashMap<String, List<MtcInfo>> mtcInfoMap = GloabValue.mtcInfoMap;
		List<MtcInfo> mtcInfos = mtcInfoMap.get(flight.getTailNumber());
		long flightInfoArrivalTime = flight.getArrivalTime();
		long flightInfoDepartureTime = flight.getDepartureTime();
		if ((mtcInfos = mtcInfoMap.get(flight.getTailNumber())) != null) {//是否在维护机场
			for (Iterator<MtcInfo> iterator = mtcInfos.iterator(); iterator.hasNext();) {//遍历
				MtcInfo mtcInfo = (MtcInfo) iterator.next();
				if (!mtcInfo.getStatus()) {
					continue;
				}
				long start = mtcInfo.getStartTime();
				long end = mtcInfo.getEndTime();
				// 飞机起飞时间在维护期间，先尝试替换飞机，后延迟起飞，否则取消航班
				if (flightInfoDepartureTime >= start
						&& flightInfoDepartureTime <= end) {
					long delay = end - flightInfoDepartureTime;//延迟时间
					if (getSwapFlight()) {// 替换飞机
						// ------
						genes[2 * genesNum] = 1;
						genes[2 * genesNum + 1] = 1;
						flight.setStatus(2);
					}else if (delay<GloabValue.maxDelayTime && delay/60  < GloabValue.weightCancelFlight) {
						
						logger.debug(flight.getId() + " 航班因在起飞机场："
								+ mtcInfo.getAirport()
								+ "维护,推迟航班"+delay+"s");
						flight.setDepartureTime(end);
						flight.setArrivalTime(flightInfoArrivalTime+delay);
					}else {// 取消航班
						logger.debug(flight.getId() + " 航班因在起飞机场："
								+ mtcInfo.getAirport()
								+ "维护,取消航班");
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
	 * return 0:没关闭，延迟后执行航班 ；1：取消航班
	 */
	private int checkIsAirportClose(FlightInfo flightInfo) {
		HashMap<String, List<AircraftClosure>> aircraftClosureMap = GloabValue.aircraftClosuresMap;
		int statue = 0;
		// boolean isClose = false;
		List<AircraftClosure> aircraftClosures = null;
		long flightInfoArrivalTime = flightInfo.getArrivalTime();
		long flightInfoDepartureTime = flightInfo.getDepartureTime();
		if ((aircraftClosures = aircraftClosureMap.get(flightInfo
				.getDepartureAirport())) != null) {
			// 起飞机场如果为关闭机场
			// 判断时间是否在关闭时间段
			for (Iterator<AircraftClosure> iterator = aircraftClosures.iterator(); iterator.hasNext();) {
				AircraftClosure aircraftClosure = (AircraftClosure) iterator.next();
				long start = aircraftClosure.getStartTime();
				long end = aircraftClosure.getEndTime();

				if (flightInfoDepartureTime > start
						&& flightInfoDepartureTime <= end) {
					long delay = end - flightInfoDepartureTime;
					if (delay < GloabValue.maxDelayTime && delay/60  < GloabValue.weightCancelFlight) {
						logger.debug(flightInfo.getId() + " 因起飞机场："
								+ flightInfo.getDepartureAirport() + "关闭"
								+ "延迟" + delay + "s起飞,起飞时间：" + end + "s");
						flightInfo.setDepartureTime(end);
						flightInfo.setArrivalTime(flightInfoArrivalTime + delay);
						flightInfo.setStatus(0);
						statue = 0;
					} else {
						flightInfo.setStatus(0);
						logger.debug(flightInfo.getId() + " 因起飞机场："
								+ flightInfo.getDepartureAirport()
								+ "关闭,必须取消航班");
						statue = 1;
					}
				}
			}
		} else if ((aircraftClosures = aircraftClosureMap.get(flightInfo
				.getArrivalAirport())) != null) {
			// 到达机场如果为关闭机场
			// 判断时间是否在关闭时间段
			for (Iterator iterator = aircraftClosures.iterator(); iterator
					.hasNext();) {
				AircraftClosure aircraftClosure = (AircraftClosure) iterator
						.next();
				long start = aircraftClosure.getStartTime();
				long end = aircraftClosure.getEndTime();
				if (flightInfoArrivalTime >= start
						&& flightInfoArrivalTime < end) {
					
					long delay = end - flightInfoArrivalTime;
					if (delay < GloabValue.maxDelayTime && delay/60 < GloabValue.weightCancelFlight) {
						logger.debug(flightInfo.getId() + " 因到达机场："
								+ flightInfo.getArrivalAirport() + "关闭" + ",延迟"
								+ delay + "s起飞,起飞时间："
								+ (flightInfoArrivalTime + delay) + "s");
						flightInfo.setDepartureTime(flightInfoDepartureTime+ delay);
						flightInfo.setArrivalTime(end);
						statue = 0;
					} else {
						flightInfo.setStatus(0);
						logger.debug(flightInfo.getId() + " 因到达机场："
								+ flightInfo.getArrivalAirport() + "关闭,必须取消航班");
						statue = 1;
					}
				}
			}
		}
		return statue;
	}

	private boolean checkIsInSwap(FlightInfo flight, int genesNum) {
		return getSwapFlight();
	}


	/*
	 * 获取替换航班
	 */
	private boolean getSwapFlight() {
		// --
		if (Math.random()<swapk) {
			//logger.info("flight swap needed");
			return true;
		}
		return false;
	}

	private int getGenesType(int i) {
		return genes[2 * i] + genes[2 * i + 1];
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
		int[] flightGene = new int[GloabValue.flightAllNum * 2];
		flightGene = Arrays.copyOf(genes, GloabValue.flightAllNum * 2);
		return flightGene;
	}

	public int[] getMtcGene() {
		int[] mtcGene = new int[GloabValue.mtcAllNum];
		mtcGene = Arrays.copyOfRange(genes, GloabValue.flightAllNum * 2 - 1,
				defaultGeneLength - 1);
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
					sBuffer.append("("+flightInfo.getStatus()+","+flightInfo.getId()+","+flightInfo.getDepartureTime()+")");
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
