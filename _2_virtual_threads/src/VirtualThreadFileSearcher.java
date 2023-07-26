import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

public class VirtualThreadFileSearcher extends AFilePDFSearcher {
    private ExecutorService _threadPool;
    private SearchResult _searchResult;

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
            _searchResult.addResult(file);
        });
    }

    @Override
    public void search() throws IOException {
        _searchResult = new SearchResult();
        _threadPool = new ForkJoinPool(); // Use ForkJoinPool for virtual threads on JDK < 18
        start();
    }

    @Override
    public void close() throws Exception {
        super.close();
        _threadPool.shutdown();
    }
}
