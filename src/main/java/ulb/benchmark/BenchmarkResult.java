package ulb.benchmark;

public class BenchmarkResult {
    private final String systemName;
    private final int datasetSize;
    private final double averageTimeMs;

    public BenchmarkResult(String systemName, int datasetSize, double averageTimeMs) {
        this.systemName = systemName;
        this.datasetSize = datasetSize;
        this.averageTimeMs = averageTimeMs;
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
