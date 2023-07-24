import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

public class MultiThreadFileSearcher
        extends AFilePDFSearcher
        implements IWordSearcher {
    private ExecutorService _threadPool;
    private final Object _fileFoundLock = new Object();
    private final ArrayList<Path> _files = new ArrayList<>();
    private String _word;

    /**
     * @param start The initial path from start
     */
    public MultiThreadFileSearcher(Path start) {
        super(start);
    }

    @Override
    protected void foundPDFFile(Path file) throws RejectedExecutionException {
        _threadPool.execute(() -> {
            if(_word != null){
                synchronized (_fileFoundLock) {
                    System.out.println(file.toString());
                    _files.add(file);
                }
            }
        });
    }

    @Override
    public SearchResult search(String word) throws IOException {
        _word = word;
        _files.clear();
        try (var threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())) {
            _threadPool = threadPool;
            start();
        }
        return new SearchResult(_pause || _stop, _files, _totalFiles);
    }
}
