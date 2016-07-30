package com.yc.airport.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yc.airport.algorithm.Algorithm;
import com.yc.airport.algorithm.FitnessCalc;
import com.yc.airport.algorithm.Individual;
import com.yc.airport.algorithm.Population;
import com.yc.airport.entity.FlightInfo;
import com.yc.airport.entity.MtcInfo;
import com.yc.airport.entity.Schedule;
import com.yc.airport.util.XmlUtil;
import com.yc.airport.value.DataReader;
import com.yc.airport.value.GenerateFlight;
import com.yc.airport.value.GloabValue;
/**
 * 
 * <p>Title: Main</p>
 * <p>Description: program start class</p>
 * @author mcfell
 * @date 2016年6月30日
 */
public class Main {
	private static final Logger logger = LoggerFactory.getLogger(Main.class);
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		logger.info("read input data start");
		DataReader.ReadAllXml(args[0]);
		logger.info("read input data end");
		logger.info("生成初始种群");
		GloabValue.popNum = 100;
		Population myPop = new Population(GloabValue.popNum, true);
		// 不段迭代，进行进化操作。 直到找到期望的基因序列
		Individual individualFittest = solution(myPop);
		logger.info("计算完成，航班输出路径："+args[1]+"\\Output.xml");
		XmlUtil.creatOutputXml(individualFittest.getFlightInfos(), individualFittest.getMtcInfos(), args[1]+"/Output.xml");
	}
	
	private static Individual solution(Population myPop) {
		int generationCount = 0;
		long lastFitness = 0;
		int times = 0;
		Individual individualFittest = null;
		while (true) {
			generationCount++;
			individualFittest = myPop.getFittest();
			long nowFitness= individualFittest.getFitness();
			logger.info("Generation: " + generationCount + " Fittest: "+ nowFitness);
			//individualFittest.printGenesInfo();
			if (Math.abs(lastFitness - nowFitness)<5) {
				times++;
				if (times>10) {
					break;
				}
			}else {
				times=0;
			}
			myPop = Algorithm.evolvePopulation(myPop);
			lastFitness = nowFitness;
		}
		System.out.println("Solution found!");
		//individualFittest.checkIsContinuous();
		individualFittest.printGenesInfo();
		individualFittest.printSchedualInfo(true);
		System.out.println("Generation: " + generationCount);
		System.out.println("Final Fittest Genes:"+lastFitness);
		return individualFittest;
	}

}
