import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.concurrent.*;

public class TaskFileSearcher extends AFilePDFSearcher {
    private ExecutorService threadPool;
    private ArrayList<Future<Boolean>> futures = new ArrayList<>();

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
        var future = threadPool.submit(()->{
            try {
                var done = searchWordInsideFile(file, attrs);
                return done;
            } catch (InterruptedException e) {
                return false;
            }
        });

        futures.add(future);
    }

    @Override
    protected void onSearchIsFinished() {
        super.onSearchIsFinished();

        long elaboratedFilesCounter = 0;
        for (Future<Boolean> res: futures) {
            try {
                if(res.get()){
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
