package opt.example;

import util.linalg.Vector;
import opt.EvaluationFunction;
import shared.Instance;

/**
 * A function that implements an order-3 deceptive problem
 * @author Timothy Ting
 * @version 1.0
 */
public class DeceptiveProblemEvaluationFunction implements EvaluationFunction {
    /**
     * @see opt.EvaluationFunction#value(opt.OptimizationData)
     */
    public double value(Instance d) {
        Vector data = d.getData();
        double val = 0;
        int mod = data.size() % 3;
        int size = data.size() - mod;

        for (int i = 0; i < size; i += 3) {
            if (data.get(i) == 0 && data.get(i + 1) == 0 && data.get(i + 2) == 0) {
                val += 28;
            }
            else if (data.get(i) == 0 && data.get(i + 1) == 0 && data.get(i + 2) == 1) {
                val += 26;
            }
            else if (data.get(i) == 0 && data.get(i + 1) == 1 && data.get(i + 2) == 0) {
                val += 22;
            }
            else if (data.get(i) == 1 && data.get(i + 1) == 0 && data.get(i + 2) == 0) {
                val += 14;
            }
            else if (data.get(i) == 1 && data.get(i + 1) == 1 && data.get(i + 2) == 1) {
                val += 30;
            }
        }
        return val;
    }
}