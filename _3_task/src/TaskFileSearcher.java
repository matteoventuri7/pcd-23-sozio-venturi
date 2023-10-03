import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.*;

public class TaskFileSearcher extends MultiThreadFileSearcher {

    public TaskFileSearcher(Path start, String word) {
        super(start, word);
    }

    @Override
    protected void onFoundPDFFile(Path file, BasicFileAttributes attrs) throws RejectedExecutionException {
        threadPool.execute(new FutureTask<Boolean>(()->{
            try {
                var done = searchWordInsideFile(file, attrs);
                if (done) {
                    notifyIfFinished();
                }
                return done;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }));
    }
}
