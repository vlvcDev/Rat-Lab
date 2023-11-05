import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class RatLab {

    // Constant values
    // All weights are in grams
    private final static int GOAL = 60000;

    private static final int INITIAL_MIN_WEIGHT = 200; // Minimum weight, rat females are 200-300 
    private static final int INITIAL_MAX_WEIGHT = 600; // Maximum weight, rat males are 300-600
    private static final int INITIAL_MODE_WEIGHT = 300; // Most common weight

    private final static double MUTATION_ODDS = 0.1; // Chance for rats to mutate
    private final static double MUTATE_MIN = 0.2; // Scalar for least optimal mutation
    private final static double MUTATE_MAX = 1.2; // Scalar for most optimal mutation

    private final static int LITTER_SIZE = 8; // Number of pups per pair of breed rats
    private final static int LITTERS_PER_YEAR = 7; // Number of pups per pair of breed rats
    private final static int GENERATION_LIMIT = 500; // Rat breeding cutoff
    private static int ratCount = 20;

    Random rand = new Random();

    public static void main(String[] args) throws Exception {
        System.out.println("Commence Breeding...");

        if (ratCount % 2 == 1) ratCount++; // We need an even number of rats

        // Spawn the first generation of rats
        int generation = 0;
        List<Integer> parents = populate(ratCount, INITIAL_MIN_WEIGHT, INITIAL_MAX_WEIGHT, INITIAL_MODE_WEIGHT);
        System.out.println("Initial Population Weights: " + parents);
        double ratsFitness = fitness(parents, GOAL);
        System.out.println("Initial Population Fitness: " + ratsFitness);
        System.out.println("Numer of Rats to Retain: " + ratCount);

        List<Integer> averageWeight = new ArrayList<>(ratCount);

        List<List<Integer>> selectedRats;
        List<Integer> selectedFemales;
        List<Integer> selectedMales;
        List<Integer> pups;
        
        // Engage in rat breeding until rats reach the goal weight or generation limit
        while (ratsFitness < 1 && generation < GENERATION_LIMIT) {
            selectedRats = selection(parents, ratCount);
            selectedFemales = selectedRats.get(0);
            selectedMales = selectedRats.get(1);
            pups = breed(selectedMales, selectedFemales, LITTER_SIZE);
            pups = mutate(pups, MUTATION_ODDS, MUTATE_MIN, MUTATE_MAX);
            parents = new ArrayList<>();
            parents.addAll(selectedFemales);
            parents.addAll(selectedMales);
            parents.addAll(pups);
            ratsFitness = fitness(parents, GOAL);
            System.out.printf("Generation %d Fitness = %.4f%n", generation, ratsFitness);
            averageWeight.add((int)(mean(parents)));
            generation++;
        }
        System.out.println("Average weight per generation = " + averageWeight);
        System.out.println("Number of generations = " + generation);
        System.out.println("Number of years = " + (int)generation/LITTERS_PER_YEAR);
    }

    public static List<Integer> populate(int numRats, int minWeight, int maxWeight, int modeWeight) {
        // Create a first generation of rats that starts with various weights using our weight parameters
        List<Integer> generation = new ArrayList<>();

        for (int i = 0; i < numRats; i++) {
            generation.add((int) Math.round(triangularDistribution(minWeight, maxWeight, modeWeight)));
        }
        return generation;
    }

    
    public static double fitness(List<Integer> population, int goal) {
        // Grade the rats based on desired traits. The closer to the goal weight, the better.
        // The stream method will collect the sum of all of the rats' weights
        double sum = population.stream().mapToInt(Integer::intValue).sum();
        // Return the mean of the current rats' weights / goal
        return sum / population.size() / goal;
    }
    

    public static List<List<Integer>> selection(List<Integer> population, int retained) {
        // Cull the rats who don't meet the new fitness standard until you have a desired amount of rats to retain
        List<Integer> sortedPopulation = new ArrayList<>(population);
        Collections.sort(sortedPopulation);
        int retainBySex = retained / 2;
        int ratsPerSex = sortedPopulation.size() / 2;
        List<Integer> ratFemales = sortedPopulation.subList(0, ratsPerSex);
        List<Integer> ratMales = sortedPopulation.subList(ratsPerSex, sortedPopulation.size());
        List<Integer> selectedFemales = ratFemales.subList(ratFemales.size() - retainBySex, ratFemales.size());
        List<Integer> selectedMales = ratMales.subList(ratMales.size() - retainBySex, ratMales.size());

        return Arrays.asList(selectedFemales, selectedMales);
    }

    public static List<Integer> breed(List<Integer> males, List<Integer> females, int litterSize) {
        // Breed the rats you have until you reach a desired litter size of pups
        Collections.shuffle(males);
        Collections.shuffle(females);

        List<Integer> pups = new ArrayList<>();

        for (int i = 0; i < Math.min(males.size(), females.size()); i++) {
            for (int j = 0; j < litterSize; j++) {
                int pup = ThreadLocalRandom.current().nextInt(females.get(i), males.get(i) + 1);
                pups.add(pup);
            }
        }
        return pups;
    }

    public static List<Integer> mutate(List<Integer> pups, double odds, double min, double max) {
        // Mutate the pups based on their chance to mutate and their scalars for optimal mutation
        for (int i = 0; i < pups.size(); i++) {
            int rat = pups.get(i);
            if (odds >= Math.random()) {
                pups.set(i, (int) Math.round(rat * ThreadLocalRandom.current().nextDouble(min, max)));
            }
        }
        return pups;
    }

    public static double triangularDistribution(double a, double b, double c) {
        // Generate random numbers that are distributed between 3 variables
        double F = (c - a) / (b - a);
        double rand = Math.random();
        if (rand < F) {
            return a + Math.sqrt(rand * (b - a) * (c - a));
        } else {
            return b - Math.sqrt((1 - rand) * (b - a) * (b - c));
        }
    }

    private static double mean(List<Integer> list) {
        // Find the mean of the list of integers
        return list.stream().mapToInt(Integer::intValue).average().orElse(0);
    }
    
}
