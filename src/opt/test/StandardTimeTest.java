package opt.test;

import java.util.*;
import java.io.FileWriter;
import java.io.IOException;

import dist.DiscreteDependencyTree;
import dist.DiscreteUniformDistribution;
import dist.Distribution;

import opt.DiscreteChangeOneNeighbor;
import opt.EvaluationFunction;
import opt.GenericHillClimbingProblem;
import opt.HillClimbingProblem;
import opt.NeighborFunction;
import opt.RandomizedHillClimbing;
import opt.SimulatedAnnealing;
import opt.example.*;
import opt.ga.CrossoverFunction;
import opt.ga.DiscreteChangeOneMutation;
import opt.ga.GenericGeneticAlgorithmProblem;
import opt.ga.GeneticAlgorithmProblem;
import opt.ga.MutationFunction;
import opt.ga.StandardGeneticAlgorithm;
import opt.ga.UniformCrossOver;
import opt.prob.GenericProbabilisticOptimizationProblem;
import opt.prob.MIMIC;
import opt.prob.ProbabilisticOptimizationProblem;
import shared.FixedTimeTrainer;

/**
 *
 * @author Timothy Ting
 * @version 1.0
 */
public class StandardTimeTest {
    /** The values to (possibly) iterate over **/
    private static Map<String, int[]> values = new HashMap<>();
    private static Map<String, double[]> doubles = new HashMap<>();

    public static void populateValues() {
        int[] N = {20,40,60,80,100};
        int[] SATemperatures = {300};
        int[] GAPopulation = {200};
        int[] GAMutations = {100};
        int[] GAMates = {100};
        int[] MIMICSamples = {300};
        int[] MIMICKeepings = {100};
        double[] times = {1000};

        values.put("N", N);
        values.put("SATemperatures", SATemperatures);
        values.put("GAPopulation", GAPopulation);
        values.put("GAMutations", GAMutations);
        values.put("GAMates", GAMates);
        values.put("MIMICSamples", MIMICSamples);
        values.put("MIMICKeepings", MIMICKeepings);
        doubles.put("times", times);
    }

    public static String generateFilename(String ef) {
        String filename = "results/" + ef + ".csv";
        return filename;
    }

    public static void main(String[] args) {
        populateValues();
        String iterateOn = "N";
        int RHCIterations, SAIterations, GAIterations, MIMICIterations;

        // Fill arrays in maps so they're all the same length
        int length = values.get(iterateOn).length;

        for (Map.Entry<String, int[]> entry : values.entrySet()) {
            String key = entry.getKey();
            int[] value = entry.getValue();
            int[] array = new int[length];
            System.out.println(key);
            if (key != iterateOn) {
                Arrays.fill(array, value[0]);
                values.put(key, array);
            }
            System.out.println(Arrays.toString(values.get(key)));
        }

        if (iterateOn != "time") {
            double[] array = new double[length];
            Arrays.fill(array, doubles.get("times")[0]);
            doubles.put("times", array);
        }

        // Pull arrays from maps into specific variables
        int[] N = values.get("N");
        int[] SATemperatures = values.get("SATemperatures");
        int[] GAPopulation = values.get("GAPopulation");
        int[] GAMutations = values.get("GAMutations");
        int[] GAMates = values.get("GAMates");
        int[] MIMICSamples = values.get("MIMICSamples");
        int[] MIMICKeepings = values.get("MIMICKeepings");
        double[] times = doubles.get("times");

        String filename = generateFilename("Deceptive-compare");
        try
        {
            FileWriter writer = new FileWriter(filename, true);
            writer.append("\n---------------------------------------------\n");
            writer.append(iterateOn + ",RHC,RHC Iterations,SA,SA Iterations,GA,GA Iterations,MIMIC,MIMIC Iterations\n");

            for(int i = 0; i < N.length; i++) {
                if (iterateOn == "times") {
                    System.out.println(iterateOn + " = " + doubles.get(iterateOn)[i]);
                } else {
                    System.out.println(iterateOn + " = " + values.get(iterateOn)[i]);
                }
                int[] ranges = new int[N[i]];
                Arrays.fill(ranges, 2);
                Distribution odd = new DiscreteUniformDistribution(ranges);
                //Get evaluation function ready
                double[] ksValues = new double[N[i]];
                double[] ksVolumes = new double[N[i]];
                Random rand = new Random();
                for (int j = 0; j < ksValues.length; j++) {
                    ksValues[j] = (double) rand.nextInt((10 - 1) + 1) + 1;
                    ksVolumes[j] = (double) rand.nextInt((40 - 1) + 1) + 1;
                }

                double ksMaxVolume = 100;
                int[] ksMaxItems = new int[N[i]];
                Arrays.fill(ksMaxItems, 1);

                EvaluationFunction ef = new DeceptiveProblemEvaluationFunction();
                //EvaluationFunction ef = new ContinuousPeaksEvaluationFunction(3);
                //EvaluationFunction ef = new KnapsackEvaluationFunction(ksValues, ksVolumes, ksMaxVolume, ksMaxItems);

                NeighborFunction nf = new DiscreteChangeOneNeighbor(ranges);
                MutationFunction mf = new DiscreteChangeOneMutation(ranges);
                CrossoverFunction cf = new UniformCrossOver();
                Distribution df = new DiscreteDependencyTree(.1, ranges);
                HillClimbingProblem hcp = new GenericHillClimbingProblem(ef, odd, nf);
                GeneticAlgorithmProblem gap = new GenericGeneticAlgorithmProblem(ef, odd, mf, cf);
                ProbabilisticOptimizationProblem pop = new GenericProbabilisticOptimizationProblem(ef, odd, df);

                RandomizedHillClimbing rhc = new RandomizedHillClimbing(hcp);
                FixedTimeTrainer fit = new FixedTimeTrainer(rhc, times[i]);
                fit.train();
                RHCIterations = fit.getIterations();
                System.out.println(fit.getIterations());
                System.out.println("Randomized Hill Climbing");
                System.out.println(ef.value(rhc.getOptimal()));

                SimulatedAnnealing sa = new SimulatedAnnealing(SATemperatures[i], .95, hcp);
                fit = new FixedTimeTrainer(sa, times[i]);
                fit.train();
                SAIterations = fit.getIterations();

                System.out.println(fit.getIterations());
                System.out.println("Simulated Annealing");
                System.out.println(ef.value(sa.getOptimal()));

                StandardGeneticAlgorithm ga = new StandardGeneticAlgorithm(GAPopulation[i], GAMates[i], GAMutations[i], gap);
                fit = new FixedTimeTrainer(ga, times[i]);
                fit.train();
                GAIterations = fit.getIterations();
                System.out.println(fit.getIterations());

                System.out.println("Genetic Algorithm");
                System.out.println(ef.value(ga.getOptimal()));

                MIMIC mimic = new MIMIC(MIMICSamples[i], MIMICKeepings[i], pop);
                fit = new FixedTimeTrainer(mimic, times[i]);
                fit.train();
                MIMICIterations = fit.getIterations();

                System.out.println(fit.getIterations());
                System.out.println("MIMIC");
                System.out.println(ef.value(mimic.getOptimal()));
                if (iterateOn == "times") {
                    writer.append(doubles.get(iterateOn)[i] + "," + ef.value(rhc.getOptimal()) + "," + RHCIterations + "," + ef.value(sa.getOptimal()) + "," + SAIterations + "," + ef.value(ga.getOptimal()) + "," + GAIterations + "," + ef.value(mimic.getOptimal()) + "," + MIMICIterations + "\n");
                } else {
                    writer.append(values.get(iterateOn)[i] + "," + ef.value(rhc.getOptimal()) + "," + RHCIterations + "," + ef.value(sa.getOptimal()) + "," + SAIterations + "," + ef.value(ga.getOptimal()) + "," + GAIterations + "," + ef.value(mimic.getOptimal()) + "," + MIMICIterations + "\n");
                }
            }
            writer.append("\n\nTime(ms):,\"" + Arrays.toString(times) + "\"");
            writer.append("\nN:,\"" + Arrays.toString(N) + "\"");
            writer.append("\nSATemperatures:,\"" + Arrays.toString(SATemperatures) + "\"");
            writer.append("\nGAPopulation:,\"" + Arrays.toString(GAPopulation) + "\"");
            writer.append("\nGAMates:,\"" + Arrays.toString(GAMates) + "\"");
            writer.append("\nGAMutations:,\"" + Arrays.toString(GAMutations) + "\"");
            writer.append("\nMIMICSamples:,\"" + Arrays.toString(MIMICSamples) + "\"");
            writer.append("\nMIMICKeepings:,\"" + Arrays.toString(MIMICKeepings) + "\"");

            writer.flush();
            writer.close();
        }
        catch(IOException e)
        {
             e.printStackTrace();
        }
    }
}