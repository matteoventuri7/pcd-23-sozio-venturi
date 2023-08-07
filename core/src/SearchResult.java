import java.nio.file.Path;
import java.util.ArrayList;

public class SearchResult {
    private final ArrayList<Path> files = new ArrayList<>();
    private long totalFiles;
    private long elapsedTime;

    protected void addResult(Path file) { files.add(file); }

    public Path[] getFiles() { return files.toArray(new Path[0]); }

    protected void IncreaseTotalFiles() {
        totalFiles++;
    }

    public long getTotalFiles() {
        return totalFiles;
    }

    public long getTotalFoundFiles() { return files.size(); }

    public long getElapsedTime() {
        return elapsedTime;
    }

    protected void setElapsedTime(long et) { elapsedTime = et; }
}
