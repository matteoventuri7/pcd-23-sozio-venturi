import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VirtualThreadFileSearcher
        extends AFilePDFSearcher {
    private ExecutorService _threadPool;

    /**
     * @param start The initial path from start
     */
    public VirtualThreadFileSearcher(Path start, String word) {
        super(start, word);
    }

    @Override
    protected void foundPDFFile(Path file) {
        _threadPool.execute(() -> {
            try {
                System.out.println("Sleeping...");
                Thread.sleep(3000);
                System.out.println("Wake up!!!");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            _serchResult.addResult(file);
        });
    }

    @Override
    public void search() throws IOException {
        _serchResult = new SearchResult();
        //_threadPool = Executors.newVirtualThreadPerTaskExecutor();
        start();
    }

    @Override
    public void close() throws Exception {
        super.close();
        _threadPool.close();
    }
}
