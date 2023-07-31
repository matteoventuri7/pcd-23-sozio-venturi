import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

public class MultiThreadFileSearcher extends AFilePDFSearcher {
    protected ExecutorService _threadPool;
    private long _elapsedTime;
    private boolean _fileSearchFinished;

    public MultiThreadFileSearcher(Path start, String word) {
        super(start, word);
    }

    @Override
    public void start() {
        if(_threadPool == null || _threadPool.isShutdown()) {
            _threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        }
        super.start();
    }

    @Override
    protected void onFoundPDFFile(Path file, BasicFileAttributes attrs) throws RejectedExecutionException {
        _threadPool.execute(() -> {
            if (_pause) {
                EnqueueFile(file, attrs);
            } else if (!_stop) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                _searchResult.addResult(file);

                _elapsedTime = super.getElapsedTime();
            }
        });
    }

    @Override
    public void close() throws Exception {
        super.close();
        _threadPool.shutdown();
    }

    @Override
    public long getElapsedTime() {
        return _elapsedTime;
    }
}
