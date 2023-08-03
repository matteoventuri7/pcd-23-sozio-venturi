import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class VirtualThreadFileSearcher extends MultiThreadFileSearcher {

    /**
     * @param start The initial path from start
     */
    public VirtualThreadFileSearcher(Path start, String word) {
        super(start, word);
    }

    @Override
    protected void instantiateThreads() {
        _threadPool = Executors.newVirtualThreadPerTaskExecutor();
    }
}
