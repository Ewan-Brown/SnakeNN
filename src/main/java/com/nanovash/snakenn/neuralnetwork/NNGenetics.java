package com.nanovash.snakenn.neuralnetwork;

import com.nanovash.snakenn.neuralnetwork.util.GenerationListener;
import lombok.Getter;

import java.io.*;
import java.util.*;

public class NNGenetics {

    private HashMap<List<Double>, Integer> population = new HashMap<>();
    private @Getter List<GenerationListener> listeners = new ArrayList<>();
    private @Getter NeuralNetwork current;
    private @Getter File storePopulation;
    private @Getter File storeBest;
    private Random rng = new Random();

    private final int populationSize = 20;
    private double mutRate = 0.2;
    public int generation = 1;

    public NNGenetics() {
        try {
            File appdata = new File(System.getenv("Appdata"), ".SnakeNN");
            if(!appdata.exists())
                appdata.mkdirs();
            storePopulation = new File(appdata, "population.txt");
            if(!storePopulation.exists()) {
                storePopulation.createNewFile();
                for (int i = 0; i < populationSize; i++) {
                    List<Double> network = new NeuralNetwork().toList();
                    population.put(network, -1);
                    if(i == 0)
                        current = new NeuralNetwork(network);
                }
            }
            else {
                BufferedReader br = new BufferedReader(new FileReader(storePopulation));
                String line;
                while ((line = br.readLine()) != null) {
                    List<Double> network = stringToList(line);
                    if(population.isEmpty())
                        current = new NeuralNetwork(network);
                    population.put(network, -1);
                }
            }
            storeBest = new File(appdata, "best.txt");
            if(!storeBest.exists())
                storeBest.createNewFile();
            BufferedReader br = new BufferedReader(new FileReader(storeBest));
            String lastLine = null;
            String line;
            while ((line = br.readLine()) != null) {
                lastLine = line;
            }
            generation = Integer.parseInt(lastLine.split(":")[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the fitness of the currently examined network
     * @param fitness score of the current network
     */
    public void updateFitnessOfCurrent(int fitness) {
        population.put(current.toList(), fitness);
        List<List<Double>> notReady = new ArrayList<>();
        for (List<Double> network : population.keySet())
            if(population.get(network) == -1)
                notReady.add(network);
        if(notReady.isEmpty())
            updateGeneration();
        else
            current = new NeuralNetwork(notReady.get(0));
    }

    /**
     * Performs a genetic algorithm on the population and updates the program files
     */
    private void updateGeneration() {
        List<Double> maxEntry = null;
        for (List<Double> entry : population.keySet())
        {
            if (maxEntry == null || population.get(entry).compareTo(population.get(maxEntry)) > 0)
            {
                maxEntry = entry;
            }
        }
        PrintWriter delete = null;
        BufferedWriter popWriter = null;
        BufferedWriter bestWriter = null;
        try {
            delete = new PrintWriter(storePopulation);
            popWriter = new BufferedWriter(new FileWriter(storePopulation, true));
            bestWriter = new BufferedWriter(new FileWriter(storeBest, true));
        } catch (IOException e) {
            e.printStackTrace();
        }
        delete.print("");
        delete.close();
        List<List<Double>> best = new ArrayList<>(population.keySet());
        Collections.sort(best, (o1, o2) -> population.get(o2) - population.get(o1));
        best.subList(best.size() / 2, best.size()).clear();
        population.clear();
        for (List<Double> network : best)
            population.put(network, -1);
        while(population.size() < 20) {
            List<Double> first = best.get(rng.nextInt(best.size()));
            List<Double> second;
            do {
                second = best.get(rng.nextInt(best.size()));
            } while(first == second);
            for (List<Double> child : breed(first, second)) {
                if(rng.nextDouble() < mutRate)
                    mutate(child);
                population.put(child, -1);
            }
        }
        try {
            for (List<Double> network : population.keySet()) {
                popWriter.write(listToString(network));
                popWriter.newLine();
            }
            popWriter.close();
            bestWriter.write(generation + ": " + listToString(maxEntry));
            bestWriter.newLine();
            bestWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        current = new NeuralNetwork(new ArrayList<>(population.keySet()).get(0));
        generation++;
        for (GenerationListener listener : listeners)
            listener.onChange(generation);
    }

    /**
     * Main function which returns 2 children of 2 parents
     * @param first first parent
     * @param second second parent
     * @return A list of 2 networks in List<Double> form
     */
    private List<List<Double>> breed(List<Double> first, List<Double> second) {
        List<List<Double>> children = new ArrayList<>();
        int x1 = rng.nextInt(first.size());
        int x2;
        do {
            x2 = rng.nextInt(first.size() + 1);
        } while(x1 >= x2);
        children.add(crossover(first, second, x1, x2));
        children.add(crossover(second, first, x1, x2));
        return children;
    }

    /**
     * The actual crossover process
     * @param first first parent
     * @param second second parent
     * @param x1 first cutpoint
     * @param x2 second cutpoint (has to be bigger than the first)
     * @return A child of both parents
     */
    private List<Double> crossover(List<Double> first, List<Double> second, int x1, int x2) {
        List<Double> child = new ArrayList<>();
        for (Double d : first.subList(0, x1))
            child.add(d);
        for (Double d : second.subList(x1, x2))
            child.add(d);
        for (Double d : first.subList(x2, first.size()))
            child.add(d);
        return child;
    }

    /**
     * Randomly modifies 2 fields in the network
     * @param network the network to mutate
     */
    private void mutate(List<Double> network) {
        for (int i = 0; i < 2; i++) {
            int index = rng.nextInt(network.size());
            network.set(index, network.get(index) * NeuralNetwork.randomValue());
        }
    }

    /**
     * Coverts a neural network in list form to string form
     * @param network the network to convert
     * @return a neural network in string form
     */
    public String listToString(List<Double> network) {
        StringBuilder builder = new StringBuilder();
        for (Double s : network)
            builder.append(s).append(s.equals(network.get(network.size() - 1)) ? "" : ",");
        return builder.toString();
    }

    /**
     * Converts a neural network in string form to list form
     * @param network the network to convert
     * @return a neural network in list form
     */
    public List<Double> stringToList(String network) {
        List<Double> net = new ArrayList<>();
        for (String s : network.split(","))
            net.add(Double.parseDouble(s));
        return net;
    }
}
