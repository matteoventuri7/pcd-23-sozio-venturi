import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

public class MultiThreadFileSearcher
        extends AFilePDFSearcher {
    private ExecutorService _threadPool;

    /**
     * @param start The initial path from start
     */
    public MultiThreadFileSearcher(Path start, String word) {
        super(start, word);
    }

    @Override
    protected void foundPDFFile(Path file) throws RejectedExecutionException {
        _threadPool.execute(() -> {
            _serchResult.addResult(file);
        });
    }

    @Override
    public void search() throws IOException {
        _serchResult = new SearchResult();
        _threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        start();
    }

    @Override
    public void close() throws Exception {
        super.close();
        _threadPool.close();
    }
}
