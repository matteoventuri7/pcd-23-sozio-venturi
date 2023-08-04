import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

public class MultiThreadFileSearcher extends AFilePDFSearcher {
    protected ExecutorService _threadPool;
    private long nComputedFiles = 0;

    public MultiThreadFileSearcher(Path start, String word) {
        super(start, word);
    }

    @Override
    public void start() {
        if (_threadPool == null || _threadPool.isShutdown()) {
            nComputedFiles = 0;
            // thread pool was stopped or terminated job
            if (_threadPool != null && !_threadPool.isTerminated()) {
                // some thread is finishing own job
                try {
                    boolean allAreFinished = _threadPool.awaitTermination(5, TimeUnit.SECONDS);
                    if (!allAreFinished) {
                        throw new Exception("Some thread is still working!");
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            // all thread finished theirs job, we can re-instantiate thread pool
            instantiateThreads();
        }
        super.start();
    }

    protected void instantiateThreads() {
        _threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    @Override
    protected void onFoundPDFFile(Path file, BasicFileAttributes attrs) throws RejectedExecutionException {
        _threadPool.execute(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            boolean done = searchWordInsideFile(file, attrs);

            if (done) {
                NotifyIfFinished();
            }
        });
    }

    private synchronized void NotifyIfFinished(){
        nComputedFiles++;
        if (nComputedFiles == getResult().getTotalFiles()) {
            notifyFinish();
        }
    }

    @Override
    public void close() throws Exception {
        super.close();
        _threadPool.shutdown();
    }
}
