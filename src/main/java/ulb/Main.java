package ulb;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import ulb.benchmark.BenchmarkResult;
import ulb.benchmark.BenchmarkRunner;
import ulb.benchmark.DatasetGenerator;
import ulb.dao.ObjectBoxDao;
import ulb.dao.ObjectDbDao;
import ulb.dao.PostgresDao;
import ulb.object_db_model.Player;

public class Main {

    // Connection configuration for the Database systems
    private static final String PG_URL = "jdbc:postgresql://localhost:5432/pokemon_db?reRewriteBatchedInserts=true";
    private static final String PG_USER = "postgres";
    private static final String PG_PASSWORD = "postgres_password";
    private static final String OBJECTDB_PATH = "db/objectdb_benchmark.odb";
    private static final String OBJECTBOX_DIR = "db/objectbox_benchmark";

    private static final int[] DATASET_SIZES = { 1_000, 10_000, 100_000, 1_000_000 };

    private static final Gson GSON = new GsonBuilder()
            .enableComplexMapKeySerialization()
            .create();

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   BENCHMARK OODBMS vs RDBMS : Pokemon Project   ");
        System.out.println("=================================================\n");

        checkAndGenerateDatasets();

        List<BenchmarkResult> results = new ArrayList<>();
        BenchmarkRunner benchmarkRunner = new BenchmarkRunner();

        for (int size : DATASET_SIZES) {
            String jsonPath = "datasets/players_" + size + ".json";

            if (!Files.exists(Paths.get(jsonPath))) {
                System.err.printf("File %s untraceable! First, run DatasetGenerator.java%n", jsonPath);
                return;
            }

            System.out.printf("%n>>> Loading the dataset into memory (%d player)...%n", size);
            List<Player> dataset = loadDataset(jsonPath);
            System.out.printf("Dataset of %d players successfully loaded.%n", dataset.size());

            // Benchmark PostgreSQL
            try (PostgresDao pgDao = new PostgresDao(PG_URL, PG_USER, PG_PASSWORD)) {
                pgDao.init();
                //double avgTime = benchmarkRunner.runInsertionBenchmark(pgDao, () -> loadDataset(jsonPath), "PostgreSQL",
                //        size);
                //results.add(new BenchmarkResult("INSERT", "PostgreSQL", size, avgTime));
                runReadAndUpdateBenchmarks(benchmarkRunner, pgDao, dataset, "PostgreSQL", size, results);
            } catch (Exception e) {
                System.err.println("Error during PostgreSQL benchmark : " + e.getMessage());
            }

            // Benchmark ObjectDB
            try (ObjectDbDao odbDao = new ObjectDbDao(OBJECTDB_PATH)) {
                odbDao.init();
                //double avgTime = benchmarkRunner.runInsertionBenchmark(odbDao, () -> loadDataset(jsonPath), "ObjectDB",
                //        size);
                //results.add(new BenchmarkResult("INSERT", "ObjectDB", size, avgTime));
                runReadAndUpdateBenchmarks(benchmarkRunner, odbDao, dataset, "ObjectDB", size, results);
            } catch (Exception e) {
                System.err.println("Error during ObjectDB benchmark : " + e.getMessage());
            }

            // Benchmark ObjectBox
            try (ObjectBoxDao obxDao = new ObjectBoxDao(OBJECTBOX_DIR)) {
                obxDao.init();
                //double avgTime = benchmarkRunner.runInsertionBenchmark(obxDao, () -> loadDataset(jsonPath), "ObjectBox",
                //        size);
                //results.add(new BenchmarkResult("INSERT", "ObjectBox", size, avgTime));
                runReadAndUpdateBenchmarks(benchmarkRunner, obxDao, dataset, "ObjectBox", size, results);
            } catch (Exception e) {
                System.err.println("Error during ObjectBox benchmark : " + e.getMessage());
            }
        }

        printSummaryTable(results);
        exportResultsToCsv(results, "results_benchmark.csv");
    }

    private static void runReadAndUpdateBenchmarks(BenchmarkRunner benchmarkRunner, ulb.dao.BenchmarkDao dao,
            List<Player> dataset, String systemName, int datasetSize, List<BenchmarkResult> results) {
        Player targetPlayer = dataset.get(0);
        String itemName = targetPlayer.getInventory().keySet().iterator().next().getName();

        double getTime = benchmarkRunner.runGetBenchmark(dao, targetPlayer.getPlayername(), systemName, datasetSize);
        results.add(new BenchmarkResult("GET", systemName, datasetSize, getTime));

        double updateTime = benchmarkRunner.runUpdateBenchmark(dao, targetPlayer.getPlayername(), itemName, systemName,
                datasetSize);
        results.add(new BenchmarkResult("UPDATE", systemName, datasetSize, updateTime));
    }

    private static void checkAndGenerateDatasets() {
        boolean missingDataset = false;
        for (int size : DATASET_SIZES) {
            if (!Files.exists(Paths.get("datasets/players_" + size + ".json"))) {
                missingDataset = true;
                break;
            }
        }

        if (missingDataset) {
            System.out.println(">>> Datasets not found. Automatic generation in progress...\n");
            try {
                new DatasetGenerator().generate(DATASET_SIZES);
                System.out.println("\n>>> Generation completed. Starting benchmarks...\n");
            } catch (Exception e) {
                System.err.println("Error during automatic dataset generation : " + e.getMessage());
            }
        }
    }

    /**
     * Loads a dataset from a JSON file into a list of PlayerDto objects.
     */
    private static List<Player> loadDataset(String filePath) {
        try (FileReader reader = new FileReader(filePath)) {
            Type listType = new TypeToken<ArrayList<Player>>() {
            }.getType();
            return GSON.fromJson(reader, listType);
        } catch (IOException e) {
            throw new RuntimeException("Unable to read the dataset " + filePath, e);
        }
    }

    /**
     * Prints a summary table of the benchmark results to the console.
     */
    private static void printSummaryTable(List<BenchmarkResult> results) {
        System.out.println("\n=========================================================================");
        System.out.println("                       SUMMARY OF AVERAGE TIMES (MS)                       ");
        System.out.println("===========================================================================");
        System.out.printf("%-10s | %-15s | %-12s | %-20s%n", "Operation", "SGBD", "Taille (N)",
                "Average Time (ms)");
        System.out.println("---------------------------------------------------------------------------");

        for (BenchmarkResult res : results) {
            System.out.printf("%-10s | %-15s | %-12d | %-20.2f%n", res.getOperation(), res.getSystemName(),
                    res.getDatasetSize(), res.getAverageTimeMs());
        }
        System.out.println("===========================================================================\n");
    }

    /**
     * Exports the benchmark results to a CSV file.
     */
    private static void exportResultsToCsv(List<BenchmarkResult> results, String csvPath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(csvPath))) {
            writer.println("Operation,System,DatasetSize,AverageTimeMs");
            for (BenchmarkResult res : results) {
                writer.printf(Locale.US, "%s,%s,%d,%.2f%n", res.getOperation(), res.getSystemName(),
                        res.getDatasetSize(), res.getAverageTimeMs());
            }
            System.out.printf("Results exported successfully to '%s'.%n", csvPath);
        } catch (IOException e) {
            System.err.println("Error while exporting the CSV file: " + e.getMessage());
        }
    }
}
