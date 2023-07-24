import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

public class MultiThreadFileSearcher
        extends AFilePDFSearcher {
    private ExecutorService _threadPool;
    private final Object _fileFoundLock = new Object();
    private final ArrayList<Path> _files = new ArrayList<>();
    private String _word;

    /**
     * @param start The initial path from start
     */
    public MultiThreadFileSearcher(Path start, String word) {
        super(start);
        _word = word;
    }

    @Override
    protected void foundPDFFile(Path file) throws RejectedExecutionException {
        _threadPool.execute(() -> {
            if (_word != null) {
                synchronized (_fileFoundLock) {
                    System.out.println(file.toString());
                    _files.add(file);
                }
            }
        });
    }

    @Override
    public SearchResult search() throws IOException {
        if (_stop) {
            _files.clear();
        }
        try (var threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())) {
            _threadPool = threadPool;
            start();
        }
        return new SearchResult(_pause || _stop, _files, _totalFiles);
    }
}
