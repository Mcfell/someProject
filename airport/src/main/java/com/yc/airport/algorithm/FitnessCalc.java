package com.yc.airport.algorithm;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yc.airport.entity.FlightInfo;
import com.yc.airport.entity.MtcInfo;
import com.yc.airport.entity.Schedule;
import com.yc.airport.value.GloabValue;

public class FitnessCalc {
	private static final Logger logger = LoggerFactory.getLogger(FitnessCalc.class);
	/*
	 * 计算个体适应值
	 */
	public static long getFitness(Individual individual) {
        long fitness = 0;
        Schedule schedule = GloabValue.orginSchedule;
        List<FlightInfo> flightInfos = schedule.getFlightInfos();
        //再次优化编码
        individual.generateIndividual(individual.getIndividualNum(), false);
        List<FlightInfo> newFlightInfos = individual.getFlightInfos();
        List<MtcInfo> mtcInfos = individual.getMtcInfos();
        
		for (int i = 0; i < GloabValue.flightAllNum; i++) {
			FlightInfo flightInfo = flightInfos.get(i);
			FlightInfo newFlightInfo = newFlightInfos.get(i);
			
			if (newFlightInfo.getStatus() == 0) {
				//2.取消航班代价
				fitness += GloabValue.weightCancelFlight;
				continue;
			}else {
				//1.此次航班延误时间代价
				long delay = Math.abs(newFlightInfo.getDepartureTime() - flightInfo.getDepartureTime());
				fitness += delay * GloabValue.weightFlightDelay;
				//4.航班交换代价
				if (!newFlightInfo.getTailNumber().equals(flightInfo.getTailNumber())) {
					fitness += GloabValue.weightFlightSwap;
				}
				//6.飞机终点机场与原计划终点机场不同所付出的代价
//				boolean isArrivalAirportEqual = newFlightInfo.getArrivalAirport().equals(flightInfo.getArrivalAirport())
//						&& newFlightInfo.getDepartureAirport().equals(flightInfo.getDepartureAirport());
//				if (!isArrivalAirportEqual) {
//					fitness+=GloabValue.weightViolateBalance;
//				}
			} 
			
		}
		//3.取消维护任务代价
		for (MtcInfo mtcInfo : mtcInfos) {
			if (!mtcInfo.getStatus()) {
				fitness+=GloabValue.weightCancelMaintenance;
			}
			
		}
        return fitness;
    }
}
