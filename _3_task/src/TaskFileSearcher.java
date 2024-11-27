import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.*;

public class TaskFileSearcher extends AFilePDFSearcher {
    private ExecutorService threadPool;
    private final ArrayList<Future<Optional<Boolean>>> futures = new ArrayList<>();

    public TaskFileSearcher(Path start, String word) {
        super(start, word);
    }

    @Override
    public void start() {
        futures.clear();

        if (threadPool == null) {
            // fresh start
            threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
        }

        super.start();
    }
    @Override
    protected void onFoundPDFFile(Path file, BasicFileAttributes attrs) throws RejectedExecutionException {
        Future<Optional<Boolean>> future = threadPool.submit(()->{
            try {
                CheckStartSearch();

                var done = searchWordInsideFile(file, attrs);
                if(done.isPresent() && done.get()){
                    searcherLock.lock();
                    addResultAndNotify(file);
                }
                return done;
            } catch (InterruptedException e) {
                return Optional.empty();
            } finally {
                searcherLock.unlock();
            }
        });

        futures.add(future);
    }

    @Override
    protected void onSearchIsFinished() throws InterruptedException {
        super.onSearchIsFinished();

        if(futures.size() == getResult().getTotalFiles()){
            // notify the end whether the file search end after the threads
            for(var f : futures){
                try {
                    f.get();
                } catch (Exception e) {}
            }
            notifyFinish();
        }
    }

    @Override
    public void close() throws Exception {
        super.close();
        if(threadPool != null) {
            threadPool.shutdown();
            threadPool.awaitTermination(1, TimeUnit.SECONDS);
        }
    }
}
