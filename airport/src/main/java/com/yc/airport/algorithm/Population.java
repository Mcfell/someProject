package com.yc.airport.algorithm;

public class Population {
	Individual[] individuals;
    /**
     * 创建种群
     * <p>Description: </p>
     * @param populationSize 种群大小
     * @param initialise 是否自动生成
     */
    public Population(int populationSize, boolean initialise) {
        individuals = new Individual[populationSize];
        // 初始化种群
        if (initialise) {
            for (int i = 0; i < size(); i++) {
                Individual newIndividual = new Individual();
                newIndividual.generateIndividual(i,true);
                saveIndividual(i, newIndividual);
            }
        }
    }
    
    public int size() {
		return individuals.length;
	}
	// Save individual
    public void saveIndividual(int index, Individual indiv) {
        individuals[index] = indiv;
    }

	public Individual getFittest() {
		Individual fittest = individuals[0];
        // Loop through individuals to find fittest
        for (int i = 0; i < size(); i++) {
            if (fittest.getFitness() >= getIndividual(i).getFitness()) {
                fittest = getIndividual(i);
            }
        }
        return fittest;
	}

	public Individual getIndividual(int i) {
		return individuals[i];
	}
}
