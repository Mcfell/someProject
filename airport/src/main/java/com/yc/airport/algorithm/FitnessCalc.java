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
        List<FlightInfo> flightInfos = schedule.getFlightInfos();
        //再次优化编码
        individual.generateIndividual(0, false);
        Individual newinIndividual=null;
		try {
			newinIndividual = (Individual) individual.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        List<FlightInfo> newFlightInfos = newinIndividual.getFlightInfos();
        List<MtcInfo> newMtcInfos = newinIndividual.getMtcInfos();
        
        int[] flightGene = newinIndividual.getFlightGene();
        int[] mtcGene = newinIndividual.getMtcGene();
		for (int i = 0; i < GloabValue.flightAllNum; i++) {
			FlightInfo flightInfo = flightInfos.get(i);
			FlightInfo newFlightInfo = newFlightInfos.get(i);
			int genea = flightGene[2*i];
			int geneb = flightGene[2*i+1];
			if (genea+geneb==0) {
				//logger.info("cancal:"+i);
				//2.取消航班代价
				fitness+=800;
				continue;
			}else {
				//1.此次航班延误时间代价
				long delay = Math.abs(newFlightInfo.getDepartureTime() - flightInfo.getDepartureTime());
				fitness+= delay;
				//4.航班交换代价
				if (genea+geneb==2) {
					fitness+=10;
				}
				//6.飞机终点机场与原计划终点机场不同所付出的代价
				boolean isArrivalAirportEqual = newFlightInfo.getArrivalAirport().equals(flightInfo.getArrivalAirport())
						&& newFlightInfo.getDepartureAirport().equals(flightInfo.getDepartureAirport());
				if (!isArrivalAirportEqual) {
					fitness+=10;
				}
			} 
			
		}
		//3.取消维护任务代价
		for (int i = 0; i < GloabValue.mtcAllNum; i++) {
			fitness+=mtcGene[i]*1000;
		}
        return fitness;
    }
}
