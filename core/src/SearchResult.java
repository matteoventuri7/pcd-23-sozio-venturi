import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SearchResult {
    private final ConcurrentLinkedQueue<Path> files = new ConcurrentLinkedQueue<>();
    private long totalFiles;
    private long elapsedTime;
    private boolean searchIsFinished;
    private boolean computationIsFinished;

    protected void addResult(Path file) { files.add(file); }

    public Path[] getFiles() { return files.toArray(new Path[0]); }

    protected void IncreaseTotalFiles() {
        totalFiles++;
    }

    protected void IncreaseTotalFiles(long count) {
        totalFiles += count;
    }

    public long getTotalFiles() {
        return totalFiles;
    }

    public long getTotalFoundFiles() { return files.size(); }

    public boolean IsFinished() {
        return searchIsFinished && computationIsFinished;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    protected void setElapsedTime(long et) { elapsedTime = et;
    }

    protected void setComputationFinished() {
        computationIsFinished = true;
    }

    protected void setSearchIsFinished() {
        searchIsFinished = true;
    }
}
