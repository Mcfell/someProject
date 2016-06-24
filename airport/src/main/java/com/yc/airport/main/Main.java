package com.yc.airport.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yc.airport.algorithm.Algorithm;
import com.yc.airport.algorithm.FitnessCalc;
import com.yc.airport.algorithm.Population;
import com.yc.airport.value.DataReader;
import com.yc.airport.value.GenerateFlight;
import com.yc.airport.value.GloabValue;

public class Main {

	private static final Logger logger = LoggerFactory.getLogger(Main.class);
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		logger.info("read input data start");
		DataReader.ReadAllXml("C:/Users/Administrator/Desktop/竞赛数据表格/Data/Scenario1/input");
		logger.info("read input data end");
		logger.info("生成实际Schedual数据开始");
		GenerateFlight.generateSchedule();
		logger.info("生成实际Schedual数据结束");
		logger.info("生成初始种群");
		Population myPop = new Population(50, true);

		// 不段迭代，进行进化操作。 直到找到期望的基因序列
		int generationCount = 0;
		long lastFitness = 0;
		int times = 0;
		while (true) {
			generationCount++;
			long nowFitness= myPop.getFittest().getFitness();
			System.out.println("Generation: " + generationCount + " Fittest: "+ nowFitness);
			System.out.println(myPop.getFittest().toString());
			if (Math.abs(lastFitness - nowFitness)<10) {
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
		System.out.println("Generation: " + generationCount);
		System.out.println("Final Fittest Genes:"+lastFitness);
		logger.info(myPop.getFittest().toString());
	}

}
