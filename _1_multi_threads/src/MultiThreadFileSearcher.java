import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

public class MultiThreadFileSearcher extends AFilePDFSearcher {
    private ExecutorService _threadPool;
    private Instant startTime; // Store the start time for computing time

    public MultiThreadFileSearcher(Path start, String word) {
        super(start, word);
        _threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    @Override
    protected void onFoundPDFFile(Path file) throws RejectedExecutionException {
        _threadPool.execute(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            _searchResult.addResult(file);
        });
    }

    @Override
    protected void onSearchFilesFinished() {
        _threadPool.shutdown();
    }

    @Override
    public boolean isFinished() {
        return _threadPool.isTerminated();
    }

    @Override
    public void search() throws IOException {
        startTime = Instant.now(); // Record the start time
        start();
    }

    @Override
    public void close() throws Exception {
        super.close();
        _threadPool.shutdown();
    }

    // Method to get the computing time duration
    public Duration getComputingTime() {
        Instant endTime = Instant.now();
        return Duration.between(startTime, endTime);
    }
}
