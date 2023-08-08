import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

public class MultiThreadFileSearcher extends AFilePDFSearcher {
    protected ExecutorService threadPool;
    private long nComputedFiles;

    public MultiThreadFileSearcher(Path start, String word) {
        super(start, word);
    }

    @Override
    public void start() {
        nComputedFiles = 0;

        if (threadPool == null) {
            // fresh start
            instantiateThreads();
        }

        super.start();
    }

    protected void instantiateThreads() {
        threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    @Override
    protected void onFoundPDFFile(Path file, BasicFileAttributes attrs) throws RejectedExecutionException {
        threadPool.execute(() -> {
            try {
                var done = searchWordInsideFile(file, attrs);
                if (done) {
                    notifyIfFinished();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private synchronized void notifyIfFinished() {
        nComputedFiles++;

        if (isResearchFinished() && isFinished()) {
            notifyFinish();
        }
    }

    private boolean isFinished() {
        return !isPaused() && nComputedFiles == getResult().getTotalFiles();
    }

    @Override
    public void close() throws Exception {
        super.close();
        if(threadPool != null) {
            threadPool.shutdown();
            threadPool.awaitTermination(1, TimeUnit.SECONDS);
            threadPool.close();
        }
    }
}
