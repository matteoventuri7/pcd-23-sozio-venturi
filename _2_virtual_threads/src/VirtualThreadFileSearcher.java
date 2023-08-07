import java.nio.file.Path;
import java.util.concurrent.Executors;

public class VirtualThreadFileSearcher extends MultiThreadFileSearcher {

    public VirtualThreadFileSearcher(Path start, String word) {
        super(start, word);
    }

    @Override
    protected void instantiateThreads() {
        _threadPool = Executors.newVirtualThreadPerTaskExecutor();
    }
}
