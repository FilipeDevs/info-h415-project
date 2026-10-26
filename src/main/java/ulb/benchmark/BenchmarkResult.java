package ulb.benchmark;

public class BenchmarkResult {
    private final String systemName;
    private final int datasetSize;
    private final double averageTimeMs;
    private final String operation;

    public BenchmarkResult(String systemName, int datasetSize, double averageTimeMs) {
        this("INSERT", systemName, datasetSize, averageTimeMs);
    }

    public BenchmarkResult(String operation, String systemName, int datasetSize, double averageTimeMs) {
        this.operation = operation;
        this.systemName = systemName;
        this.datasetSize = datasetSize;
        this.averageTimeMs = averageTimeMs;
    }

    public String getOperation() {
        return operation;
    }

    public String getSystemName() {
        return systemName;
    }

    public int getDatasetSize() {
        return datasetSize;
    }

    public double getAverageTimeMs() {
        return averageTimeMs;
    }
}
