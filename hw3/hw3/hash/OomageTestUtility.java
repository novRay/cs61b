package hw3.hash;

import java.util.List;

public class OomageTestUtility {
    public static boolean haveNiceHashCodeSpread(List<Oomage> oomages, int M) {
        /*
         * Write a utility function that returns true if the given oomages
         * have hashCodes that would distribute them fairly evenly across
         * M buckets. To do this, convert each oomage's hashcode in the
         * same way as in the visualizer, i.e. (& 0x7FFFFFFF) % M.
         * and ensure that no bucket has fewer than N / 50
         * Oomages and no bucket has more than N / 2.5 Oomages.
         */
        int[] bucket = new int[M];
        for (Oomage oomage : oomages) {
            int value = (oomage.hashCode() & 0x7FFFFFFF) % M;   // ensure all values are positive
            bucket[value] += 1;
        }
        for (int i = 0; i < M; i++) {
            int N = oomages.size();
            if (bucket[i] > N / 2.5 || bucket[i] < N / 50) {
                return false;
            }
        }
        return true;
    }
}
