import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.*;

public class TaskFileSearcher extends AFilePDFSearcher {
    private ExecutorService threadPool;
    private final ArrayList<Future<Optional<Boolean>>> futures = new ArrayList<>();
    private final Object cs = new Object();

    public TaskFileSearcher(Path start, String word) {
        super(start, word);
    }

    @Override
    public void start() {
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
                var done = searchWordInsideFile(file, attrs);
                if(done.isPresent() && done.get()){
                    synchronized (cs) {
                        AddResultAndNotify(file);
                    }
                }
                return done;
            } catch (InterruptedException e) {
                return Optional.empty();
            }
        });

        futures.add(future);
    }

    @Override
    protected void onSearchIsFinished() {
        super.onSearchIsFinished();

        long elaboratedFilesCounter = 0;
        for (Future<Optional<Boolean>> res: futures) {
            try {
                var optional = res.get();
                if(optional.isPresent()){
                    elaboratedFilesCounter++;
                }
            } catch (Exception ex){
                ex.printStackTrace();
            }
        }

        if(elaboratedFilesCounter != getResult().getTotalFoundFiles()){
            // TODO handle error advise
        }

        notifyFinish();
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
