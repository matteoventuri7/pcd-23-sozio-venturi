import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VirtualThreadFileSearcher
        extends AFilePDFSearcher
        implements IWordSearcher {
    private ExecutorService _threadPool;
    private final Object _fileFoundLock = new Object();
    private final ArrayList<Path> _files = new ArrayList<>();
    private String _word;


    /**
     * @param start The initial path from start
     */
    public VirtualThreadFileSearcher(Path start) {
        super(start);
    }

    @Override
    protected void foundPDFFile(Path file) {
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
    public SearchResult search(String word) throws IOException {
        _word = word;
        _files.clear();
        try (var threadPool = Executors.newVirtualThreadPerTaskExecutor()) { // Utilizza un Executor per i virtual thread
            _threadPool = threadPool;
            start();
        }
        return new SearchResult(_pause || _stop, _files, _totalFiles);
    }
}
