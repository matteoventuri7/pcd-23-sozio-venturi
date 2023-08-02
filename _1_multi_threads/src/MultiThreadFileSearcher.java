import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

public class MultiThreadFileSearcher extends AFilePDFSearcher {
    protected ExecutorService _threadPool;
    private long _elapsedTime;
    private long nComputedFiles = 0;
    private Object workerLock = new Object();

    public MultiThreadFileSearcher(Path start, String word) {
        super(start, word);
    }

    @Override
    public void start() {
        nComputedFiles = 0;
        _elapsedTime = 0;
        if (_threadPool == null || _threadPool.isShutdown()) {
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
                // Search the word in the PDF
                boolean wordFoundInPDF = false;
                try {
                    wordFoundInPDF = searchWordInPDF(file);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // If word found in PDF, stop the search and increment the count
                if (wordFoundInPDF) {
                    _searchResult.addResult(file);
                }

                synchronized (workerLock) {
                    nComputedFiles++;
                    if (nComputedFiles == _searchResult.getTotalFiles()) {
                        _searchResult.computationIsFinished = true;
                    }
                    _elapsedTime = super.getElapsedTime();
                }
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
