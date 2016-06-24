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
        Schedule schedule = GloabValue.schedule;
        Schedule newSchedule = GloabValue.newSchdule;
        
        List<FlightInfo> flightInfos = schedule.getFlightInfos();
        List<FlightInfo> newFlightInfos = newSchedule.getFlightInfos();
        List<MtcInfo> newMtcInfos = newSchedule.getMtcInfos();
        
        int[] flightGene = individual.getFlightGene();
        int[] mtcGene = individual.getMtcGene();
		for (int i = 0; i < GloabValue.flightAllNum; i++) {
			FlightInfo flightInfo = flightInfos.get(i);
			FlightInfo newFlightInfo = newFlightInfos.get(i);
			if (!newFlightInfo.getStatus()) {
				individual.setGene(i, 0);
			}
			if (flightGene[i]==0) {
				//2.取消航班代价
				fitness+=800;
				continue;
			}
			
			boolean isTailEqual = newFlightInfo.getTailNumber().equals(flightInfo.getTailNumber());
			boolean isArrivalAirportEqual = newFlightInfo.getArrivalAirport().equals(flightInfo.getArrivalAirport());
			int ArrivalK = 0;
			//1.此次航班延误时间代价
			long delay = Math.abs(newFlightInfo.getDepartureTime() - flightInfo.getDepartureTime());
			fitness+= Math.abs(delay);
			//4.航班交换代价
			if (!isTailEqual) {
				fitness+=10;
			}
			//6.飞机终点机场与原计划终点机场不同所付出的代价,有问题？
			if (isTailEqual) {
				if (!isArrivalAirportEqual) {
					ArrivalK = 1;
				}
			}else {
				if (!isArrivalAirportEqual) {
					ArrivalK = 1;
				}
			}
			fitness+=10*ArrivalK;
		}
		//3.取消维护任务代价
		for (int i = 0; i < GloabValue.mtcAllNum; i++) {
			fitness+=mtcGene[i]*1000;
		}
		
        return fitness;
    }
}
