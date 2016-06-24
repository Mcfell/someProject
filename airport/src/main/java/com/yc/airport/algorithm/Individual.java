package com.yc.airport.algorithm;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.yc.airport.entity.FlightInfo;
import com.yc.airport.value.GloabValue;

public class Individual {
	static int defaultGeneLength = GloabValue.flightAllNum
			+ GloabValue.mtcAllNum;
	// 基因序列
	private int[] genes = new int[defaultGeneLength];
	// 个体的 适应值
	private long fitness = -1;
	//航班取消的基因
	private static Set<Integer> stableGenes ;
	
	public static void setStableGenes() {
		stableGenes = new HashSet<Integer>();
		List<FlightInfo> flightInfos = GloabValue.newSchdule.getFlightInfos();
		for (int i = 0; i < flightInfos.size(); i++) {
			if (!flightInfos.get(i).getStatus()) {
				stableGenes.add(i);
			}
		}
	}
	// 创建一个随机的 基因个体
	public void generateIndividual() {
		for (int i = 0; i < size(); i++) {
			if (stableGenes.contains(i)) {
				genes[i]=0;
			}else {
				byte gene = (byte) Math.round(Math.random());
				genes[i] = gene;
			}
		}
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

	public int[] getMtcGene(){
		int[] mtcGene = new int[GloabValue.mtcAllNum];
		mtcGene = Arrays.copyOfRange(genes, GloabValue.flightAllNum-1, defaultGeneLength-1);
		return mtcGene;
	}
	public int size() {
		return genes.length;
	}

	public void setGene(int i, int gene) {
		genes[i] = gene;
	}
	@Override
	public String toString() {
		return "Individual [genes=" + Arrays.toString(genes) + "]";
	}
}
