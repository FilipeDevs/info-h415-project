package ulb.benchmark;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import ulb.dao.BenchmarkDao;
import ulb.object_db_model.Player;

public class BenchmarkRunner {

    private static final int RUNS_COUNT = 6; // 1 warm-up + 5 tested runs

    public double runInsertionBenchmark(BenchmarkDao dao, Supplier<List<Player>> datasetSupplier, String systemName,
            int datasetSize) {
        System.out.println("\n==========================================");
        System.out.printf("Start of the insertion benchmark for [%s] with %d players%n", systemName, datasetSize);
        System.out.println("==========================================");

        List<Long> durations = new ArrayList<>();

        for (int i = 1; i <= RUNS_COUNT; i++) {
            try {
                List<Player> freshDataset = datasetSupplier.get();

                dao.clearDatabase();

                long startTime = System.nanoTime();
                dao.insertPlayers(freshDataset);
                long durationMs = (System.nanoTime() - startTime) / 1_000_000;

                if (i == 1) {
                    System.out.printf("Run 1 (WARM-UP - Ignored) : %d ms%n", durationMs);
                } else {
                    System.out.printf("Run %d : %d ms%n", i, durationMs);
                    durations.add(durationMs);
                }

            } catch (Exception e) {
                System.err.printf("Error during execution %d : %s%n", i, e.getMessage());
                e.printStackTrace();
            }
        }

        // Calculation of the average over the 5 executions
        double average = durations.stream().mapToLong(Long::longValue).average().orElse(0.0);
        System.out.printf("--> Average insertion time for %s (%d items) : %.2f ms%n", systemName, datasetSize,
                average);
        return average;
    }
}
