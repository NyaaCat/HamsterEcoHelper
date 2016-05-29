package cat.nyaa.HamsterEcoHelper.utils;

import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class Utils {
    public static final Random random = new Random();

    public static <T> int randomIdWithWeight(List<T> items, Function<T, Double> weightOperator) {
        if (items.size() <= 0) return -1;

        double[] weightList = new double[items.size()];
        weightList[0] = weightOperator.apply(items.get(0));
        for (int i = 1; i< items.size(); i++) {
            weightList[i] = weightList[i-1] + weightOperator.apply(items.get(i));
        }

        double rnd = random.nextDouble() * weightList[weightList.length - 1];
        for (int i = 0; i < weightList.length; i++) {
            if (weightList[i] > rnd) {
                return i;
            }
        }

        throw new RuntimeException("No item selected: Please report this BUG");
    }

    public static <T> T randomWithWeight(List<T> items, Function<T, Double> weightOperator) {
        if (items.size() <= 0) return null;
        return items.get(randomIdWithWeight(items, weightOperator));
    }

    public static int inclusiveRandomInt(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }
}
