import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiThreadFileSearcher
        extends AFilePDFWalker
        implements IWordSearcher {
    private ExecutorService _threadPool;
    private final Object _fileFoundLock = new Object();
    private final ArrayList<Path> _files = new ArrayList<>();

    /**
     * @param start The initial path from start
     */
    public MultiThreadFileSearcher(Path start) {
        super(start);
    }

    @Override
    protected void foundPDFFile(Path file) {
        _threadPool.execute(() -> {
            synchronized (_fileFoundLock){
                System.out.println(file.toString());
                _files.add(file);
            }
        });
    }

    @Override
    public ArrayList<Path> search(String word) throws IOException {
        _files.clear();
        try(var threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())){
            _threadPool=threadPool;
            start();
        }
        return new ArrayList<Path>(_files);
    }
}
