import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

public class MultiThreadFileSearcher extends AFilePDFSearcher {
    private ExecutorService _threadPool;

    public MultiThreadFileSearcher(Path start, String word) {
        super(start, word);
        _threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    @Override
    protected void foundPDFFile(Path file) throws RejectedExecutionException {
        _threadPool.execute(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            _serchResult.addResult(file);
        });
    }

    @Override
    public void search() throws IOException {
        _serchResult = new SearchResult();
        start();
    }

    @Override
    public void close() throws Exception {
        super.close();
        _threadPool.shutdown(); // Shutdown the thread pool gracefully
        try {
            if (!_threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                _threadPool.shutdownNow(); // Forcefully shutdown if the threads don't terminate in 5 seconds
            }
        } catch (InterruptedException e) {
            _threadPool.shutdownNow(); // Forcefully shutdown if interrupted during awaitTermination
        }
    }
}
