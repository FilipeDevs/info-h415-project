package ulb.benchmark;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import ulb.dao.BenchmarkDao;
import ulb.object_db_model.Player;

public class BenchmarkRunner {

    private static final int RUNS_COUNT = 6; // 1 warm-up + 5 tested runs

    private static final String SEARCH_TYPE = "Dragon";

    public double runInsertionBenchmark(BenchmarkDao dao, Supplier<List<Player>> datasetSupplier, String systemName,
            int datasetSize) {
        String formattedSize = String.format(java.util.Locale.US, "%,d", datasetSize).replace(',', ' ');
        System.out.println("\n==========================================");
        System.out.printf("Start of the insertion benchmark for [%s] with %s players%n", systemName, formattedSize);
        System.out.println("==========================================");

        List<Double> durations = new ArrayList<>();

        for (int i = 1; i <= RUNS_COUNT; i++) {
            try {
                List<Player> freshDataset = datasetSupplier.get();

                dao.clearDatabase();

                long startTime = System.nanoTime();
                dao.insertPlayers(freshDataset);
                double durationMs = (System.nanoTime() - startTime) / 1_000_000.0;

                if (i == 1) {
                    System.out.printf("Run 1 (WARM-UP - Ignored) : %.2f ms%n", durationMs);
                } else {
                    System.out.printf("Run %d : %.2f ms%n", i, durationMs);
                    durations.add(durationMs);
                }

            } catch (Exception e) {
                System.err.printf("Error during execution %d : %s%n", i, e.getMessage());
                throw new RuntimeException(e);
            }
        }

        // Calculation of the average over the 5 executions
        double average = durations.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        System.out.printf("--> Average insertion time for %s (%d items) : %.2f ms%n", systemName, datasetSize,
                average);
        return average;
    }

    public double runGetBenchmark(BenchmarkDao dao, List<Player> dataset, String systemName, int datasetSize) {
        Random random = new Random(12345L);
        return runTimedBenchmark("GET", systemName, datasetSize, i -> {
            String randomPlayername = dataset.get(random.nextInt(dataset.size())).getPlayername();
            int loadedObjects = dao.getPlayerWithRelations(randomPlayername);
            if (loadedObjects <= 0) {
                throw new IllegalStateException("GET returned no related objects");
            }
        });
    }

    public double runUpdateBenchmark(BenchmarkDao dao, List<Player> dataset, String systemName, int datasetSize) {
        Random random = new Random(12345L);
        return runTimedBenchmark("UPDATE", systemName, datasetSize, i -> {
            Player target = dataset.get(random.nextInt(dataset.size()));
            String itemName = target.getInventory().keySet().iterator().next().getName();
            dao.updateInventoryAmount(target.getPlayername(), itemName, 100 + i);
        });
    }

    public double runSearchLegendaryBenchmark(BenchmarkDao dao, List<Player> dataset, String systemName,
            int datasetSize) {
        return runSearchBenchmark("SEARCH_LEG", dataset, systemName, datasetSize, dao::searchLegendaryOwners);
    }

    public double runSearchActiveTeamBenchmark(BenchmarkDao dao, List<Player> dataset, String systemName,
            int datasetSize) {
        return runSearchBenchmark("SEARCH_ACT", dataset, systemName, datasetSize,
                requester -> dao.searchActiveLegendaryOfType(requester, SEARCH_TYPE));
    }

    private double runSearchBenchmark(String operation, List<Player> dataset, String systemName, int datasetSize,
            SearchQuery query) {
        // A different requester on each run, so ObjectDB cannot answer from its query
        // result cache
        Random random = new Random(12345L);
        List<Integer> counts = new ArrayList<>();
        double average = runTimedBenchmark(operation, systemName, datasetSize, i -> {
            String requester = dataset.get(random.nextInt(dataset.size())).getPlayername();
            counts.add(query.run(requester).size());
        });
        System.out.printf("--> %s results per run for %s : %s%n", operation, systemName, counts);
        return average;
    }

    private double runTimedBenchmark(String operation, String systemName, int datasetSize, BenchmarkOperation benchmark)
            throws RuntimeException {
        String formattedSize = String.format(java.util.Locale.US, "%,d", datasetSize).replace(',', ' ');
        System.out.println("\n==========================================");
        System.out.printf("Start of the %s benchmark for [%s] with %s players%n", operation, systemName, formattedSize);
        System.out.println("==========================================");

        List<Double> durations = new ArrayList<>();
        for (int i = 1; i <= RUNS_COUNT; i++) {
            try {
                long startTime = System.nanoTime();
                benchmark.run(i);
                double durationMs = (System.nanoTime() - startTime) / 1_000_000.0;

                if (i == 1) {
                    System.out.printf("Run 1 (WARM-UP - Ignored) : %.2f ms%n", durationMs);
                } else {
                    System.out.printf("Run %d : %.2f ms%n", i, durationMs);
                    durations.add(durationMs);
                }
            } catch (Exception e) {
                System.err.printf("Error during execution %d : %s%n", i, e.getMessage());
                throw new RuntimeException(e);
            }
        }

        double average = durations.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        System.out.printf("--> Average %s time for %s (%d items) : %.2f ms%n", operation, systemName, datasetSize,
                average);
        return average;
    }

    @FunctionalInterface
    private interface BenchmarkOperation {
        void run(int iteration) throws Exception;
    }

    @FunctionalInterface
    private interface SearchQuery {
        List<String> run(String requester) throws Exception;
    }
}
